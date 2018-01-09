package com.redhat.coolstore;

import com.redhat.coolstore.service.CartService;
import com.redhat.coolstore.service.impl.CartServiceImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    private CartService cartService;

    @Override
    public void start() {
        this.cartService = new CartServiceImpl(vertx);
        Router router = Router.router(vertx);
        router.get("/").handler(rc-> rc.response().end("Hello from Cart Service"));
        router.get("/services/cart/:cartId").handler(this::getCart);
        router.post("/services/cart/:cartId/:itemId/:quantity").handler(this::addToCart);
        router.post("/services/cart/checkout/:cartId").handler(this::checkout);
        router.delete("/services/cart/:cartId/:itemId/:quantity").handler(this::removeFromCart);
        router.route().failureHandler(this::handleFailure);


        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port",8080));

    }

    private void removeFromCart(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");
        String itemId = rc.pathParam("itemId");
        int quantity = Integer.parseInt(rc.pathParam("quantity"));


        cartService.removeItems(cartId,itemId,quantity,reply -> rc.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(reply.result().encodePrettily()));

    }

    private void checkout(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");

        cartService.checkout(cartId,reply -> rc.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(reply.result().encodePrettily()));
    }

    private void getCart(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");

        JsonObject request = new JsonObject();
        request.put("cartId",cartId);

        cartService.getCart(cartId,reply -> rc.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(reply.result().encodePrettily()));


    }

    private void addToCart(RoutingContext rc) {
        String cartId = rc.pathParam("cartId");
        String itemId = rc.pathParam("itemId");
        int quantity = Integer.parseInt(rc.pathParam("quantity"));

        cartService.addItems(cartId,itemId,quantity,reply -> {
            if(reply.succeeded()) {
                rc.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(reply.result().encodePrettily());
            }});

    }

    private void handleFailure(RoutingContext ctx) {
        Throwable exception = ctx.failure();

        final JsonObject error = new JsonObject()
            .put("timestamp", System.nanoTime())
            .put("status", 500)
            .put("error", HttpResponseStatus.valueOf(500).reasonPhrase())
            .put("path", ctx.normalisedPath())
            .put("exception", exception.getClass().getName());

        if(exception.getMessage() != null) {
            error.put("message", exception.getMessage());
        }

        ctx.response().setStatusCode(500);
        ctx.response().end(error.encode());


    }


}


