package com.redhat.coolstore.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface CartService {

    /**
     * The address on which the service is published.
     */
    String ADDRESS = "service.cart";

    /**
     * The address on which the successful action are sent.
     */
    String EVENT_ADDRESS = "cart";

    void getCart(String cartId,Handler<AsyncResult<JsonObject>> resultHandler);

    void checkout(String cartId,Handler<AsyncResult<JsonObject>> resultHandler);

    void addItems(String cartId, String itemId, int quantity, Handler<AsyncResult<JsonObject>> resultHandler);

    void removeItems(String cartId, String itemId, int quantity, Handler<AsyncResult<JsonObject>> resultHandler);


}
