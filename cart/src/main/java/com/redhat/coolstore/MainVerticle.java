package com.redhat.coolstore;

import com.redhat.coolstore.model.*;
import com.redhat.coolstore.model.impl.ShoppingCartImpl;
import com.redhat.coolstore.model.impl.ShoppingCartItemImpl;
import com.redhat.coolstore.utils.Generator;
import com.redhat.coolstore.utils.Transformers;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MainVerticle extends AbstractVerticle {

    /**
     * This is the HashMap that holds the shopping cart. This should be replace with a replicated cache like Infinispan etc
     */
    private final static Map<String,ShoppingCart> carts = new ConcurrentHashMap<>();

    static {
        carts.put("99999", Generator.generateShoppingCart());
    }


    @Override
    public void start() {


        Router router = Router.router(vertx);
        router.get("/").handler(rc-> rc.response().end("Hello from Cart Service"));
        router.get("/services/cart/:cartId").handler(this::getCart);
        router.post("/services/cart/:cartId/:itemId/:quantity").handler(this::addToCart);
        router.post("/services/cart/checkout/:cartId").handler(this::checkout);
        router.delete("/services/cart/:cartId/:itemId/:quantity").handler(this::removeFromCart);


        setupConfiguration().setHandler(handler -> {
            if(handler.succeeded()) {
                Integer serverPort = config().getInteger("http.port", 8080);
                System.out.println("Starting the HTTP Server on port " + serverPort);
                vertx.createHttpServer().requestHandler(router::accept).listen(serverPort);
            } else {
                System.out.println("Failed to Start HTTP Server with reason: " + handler.cause());
            }
        });

    }

    private void getCart(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");
        ShoppingCart cart = getCart(cartId);
        sendCart(cart,rc);

    }

    private void addToCart(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");
        String itemId = rc.pathParam("itemId");
        int quantity = Integer.parseInt(rc.pathParam("quantity"));

        ShoppingCart cart = getCart(cartId);

        boolean productAlreadyInCart = cart.getShoppingCartItemList().stream()
            .anyMatch(i -> i.getProduct().getItemId().equals(itemId));


        if(productAlreadyInCart) {
            cart.getShoppingCartItemList().forEach(item -> {
                if (item.getProduct().getItemId().equals(itemId)) {
                    item.setQuantity(item.getQuantity() + quantity);
                    sendCart(cart,rc);
                }
            });
        } else {
            ShoppingCartItem newItem = new ShoppingCartItemImpl();
            newItem.setQuantity(quantity);
            Future<Product> future = getProduct(itemId);
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    newItem.setProduct(ar.result());
                    cart.addShoppingCartItem(newItem);
                    sendCart(cart,rc);
                } else {
                    sendError(rc);
                }
            });
        }


    }

    private void removeFromCart(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");
        String itemId = rc.pathParam("itemId");
        int quantity = Integer.parseInt(rc.pathParam("quantity"));


        ShoppingCart cart = getCart(cartId);

        //If all quantity with the same Id should be removed then remove it from the list completely. The is the normal use-case
        cart.getShoppingCartItemList().removeIf(i -> i.getProduct().getItemId().equals(itemId) && i.getQuantity()<=quantity);

        //If not all quantities should be removed we need to update the list
        cart.getShoppingCartItemList().forEach(i ->  {
                if(i.getProduct().getItemId().equals(itemId))
                    i.setQuantity(i.getQuantity()-quantity);
            }
        );

        sendCart(cart,rc);

    }

    private void checkout(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");
        //TODO send the shopping cart to order
        getCart(cartId).clear();
    }

    private static void sendCart(ShoppingCart cart, RoutingContext rc) {
        rc.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(Transformers.shoppingCartToJson(cart).encodePrettily());
    }


    private static void sendError(RoutingContext rc) {
        rc.response().setStatusCode(500).end();
    }

    private Future<Product> getProduct(String itemId) {
        WebClient client = WebClient.create(vertx);
        //Context context = Vertx.currentContext();
        Future<Product> future = Future.future();
        Integer port = config().getInteger("catalog.service.port", 8080);
        String hostname = config().getString("catalog.service.hostname", "localhost");
        Integer timeout = config().getInteger("catalog.service.timeout", 0);
        client.get(port, hostname,"/services/product/"+itemId)
            .timeout(timeout)
            .send(handler -> {
                if(handler.succeeded()) {
                    future.complete(Transformers.jsonToProduct(handler.result().body().toJsonObject()));
                } else {
                    future.fail(new RuntimeException("Failed to get Product from the catalog service"));
                }


            });
        return future;
    }


    private static ShoppingCart getCart(String cartId) {
        if(carts.containsKey(cartId)) {
            return carts.get(cartId);
        } else {
            ShoppingCart cart = new ShoppingCartImpl();
            carts.put(cartId,cart);
            return cart;
        }

    }

    private Future<Void> setupConfiguration() {
        Future<Void> future = Future.future();
        ConfigStoreOptions defaultFileStore = new ConfigStoreOptions()
            .setType("file")
            .setConfig(new JsonObject().put("path", "config-default.json"));
        ConfigRetrieverOptions options = new ConfigRetrieverOptions();
        options.addStore(defaultFileStore);
        String profilesStr = System.getProperty("vertx.profiles.active");
        if(profilesStr!=null && profilesStr.length()>0) {
            Arrays.stream(profilesStr.split(",")).forEach(s -> options.addStore(new ConfigStoreOptions()
            .setType("file")
            .setConfig(new JsonObject().put("path", "config-" + s + ".json"))));
        }
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig((AsyncResult<JsonObject> ar) -> {
            if (ar.succeeded()) {
                JsonObject result = ar.result();
                result.fieldNames().forEach(s -> config().put(s, result.getValue(s)));
                future.complete();
            } else {
                future.fail("Failed to read configuration");
            }

        });
        return future;
    }

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<AsyncResult<String>> q = new ArrayBlockingQueue<>(1);
        Vertx.vertx().deployVerticle(new MainVerticle(), q::offer);
        AsyncResult<String> result = q.take();
        if (result.failed()) {
            throw new RuntimeException(result.cause());
        }
    }


}


