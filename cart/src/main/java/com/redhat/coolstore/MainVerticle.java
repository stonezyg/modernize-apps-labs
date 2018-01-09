package com.redhat.coolstore;

import com.redhat.coolstore.model.*;
import com.redhat.coolstore.utils.Transformers;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainVerticle extends AbstractVerticle {

    /**
     * This is the HashMap that holds the shopping cart. This should be replace with a replicated cache like Infinispan etc
     */
    private final static Map<String,ShoppingCart> carts = new ConcurrentHashMap<>();



    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/").handler(rc-> rc.response().end("Hello from Cart Service"));
        router.get("/services/cart/:cartId").handler(this::getCart);
        router.post("/services/cart/:cartId/:itemId/:quantity").handler(this::addToCart);
        router.post("/services/cart/checkout/:cartId").handler(this::checkout);
        router.delete("/services/cart/:cartId/:itemId/:quantity").handler(this::removeFromCart);

        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port",8080));

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
                    sendError(500,rc);
                }
            });
        }



//        cartService.addItems(cartId,itemId,quantity,reply -> {
//            if(reply.succeeded()) {
//                rc.response()
//                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
//                    .end(reply.result().encodePrettily());
//            }});

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


    private static void sendError(int statusCode, RoutingContext rc) {
        rc.response().setStatusCode(statusCode).end();
    }

    private Future<Product> getProduct(String itemId) {
        WebClient client = WebClient.create(vertx);
        Context context = Vertx.currentContext();
        Future<Product> future = Future.future();
        Integer port = context.config().getInteger("catalog.service.port", 8080);
        String hostname = context.config().getString("catalog.service.hostname", "localhost");
        Integer timeout = context.config().getInteger("catalog.service.timeout", 0);
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

}


