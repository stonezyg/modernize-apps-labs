package com.redhat.coolstore.service.impl;

import com.redhat.coolstore.model.*;
import com.redhat.coolstore.service.CartService;
import com.redhat.coolstore.utils.Transformers;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;


import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class CartServiceImpl implements CartService {

    /**
     * This is the HashMap that holds the shopping cart. This should be replace with a replicated cache like Infinispan etc
     */
    private final static HashMap<String,ShoppingCart> carts = new HashMap<String,ShoppingCart>();

    /**
     * We need the vertx instance to call external services etc
     */
    private Vertx vertx;

    public CartServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void getCart(String cartId, Handler<AsyncResult<JsonObject>> resultHandler) {
        ShoppingCart cart = getCart(cartId);
        resultHandler.handle(Future.succeededFuture(Transformers.shoppingCartToJson(cart)));
    }

    @Override
    public void checkout(String cartId, Handler<AsyncResult<JsonObject>> resultHandler) {
        System.out.println("Checking out cart");
        //TODO
    }

    /**
     * addItems will add a number of product items to the Shopping cart. If the product is already in the shopping cart
     * it updates the quantity, if not it will call the Catalog service and retrieve data about the product.
     * @param cartId
     * @param itemId
     * @param quantity
     * @param resultHandler
     */
    @Override
    public void addItems(String cartId, String itemId, int quantity, Handler<AsyncResult<JsonObject>> resultHandler) {
        ShoppingCart cart = getCart(cartId);
        final AtomicReference<Boolean> productAlreadyInCart = new AtomicReference<>();
        productAlreadyInCart.set(false);
        cart.getShoppingCartItemList().forEach( item -> {
            if (item.getProduct().getItemId().equals(itemId)) {
                item.setQuantity(item.getQuantity() + quantity);
                resultHandler.handle(Future.succeededFuture(Transformers.shoppingCartToJson(cart)));
                productAlreadyInCart.set(true);
            }
        });

        if(!productAlreadyInCart.get()) {
            ShoppingCartItem newItem = new ShoppingCartItemImpl();
            newItem.setQuantity(quantity);
            Future<Product> future = getProduct(itemId);
            future.setHandler( ar -> {
                if(ar.succeeded()) {
                    newItem.setProduct(ar.result());
                    cart.addShoppingCartItem(newItem);
                    resultHandler.handle(Future.succeededFuture(Transformers.shoppingCartToJson(cart)));
                } else {
                    resultHandler.handle(Future.failedFuture(ar.cause()));
                }
            });


        }

    }

    /**
     * This method will remove items from the shopping cart. If the number of items to remove is equal or more than
     * the number in the shopping cart it will remove the cartItem from the list. Otherwise it will lower the amount in
     * the shopping cart with the quantity supplied.
     * @param cartId
     * @param itemId
     * @param quantity Number of items to remove
     * @param resultHandler
     */
    @Override
    public void removeItems(String cartId, String itemId, int quantity, Handler<AsyncResult<JsonObject>> resultHandler) {
        ShoppingCart cart = getCart(cartId);

        //If all quantity with the same Id should be removed then remove it from the list completely. The is the normal use-case
        cart.getShoppingCartItemList().removeIf(i -> i.getProduct().getItemId().equals(itemId) && i.getQuantity()<=quantity);

        //If not all quantities should be removed we need to update the list
        cart.getShoppingCartItemList().forEach(i ->  {
            if(i.getProduct().getItemId().equals(itemId))
                i.setQuantity(i.getQuantity()-quantity);
            }
        );

        resultHandler.handle(Future.succeededFuture(Transformers.shoppingCartToJson(cart)));
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


}
