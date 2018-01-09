package com.redhat.coolstore.utils;

import com.redhat.coolstore.MainVerticle;
import com.redhat.coolstore.model.*;
import com.redhat.coolstore.service.CartService;
import com.redhat.coolstore.service.impl.CartServiceImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class EndpointTest {

    private static final String SHOPPING_CART_ID = "00001";
    CartService cartService = new CartServiceImpl();
    Vertx vertx;
    int port;

    @Before
    public void setup(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());

//        System.out.println("Transformers.shoppingCartToJson(generateShoppingCart()).encodePrettily() = " + Transformers.shoppingCartToJson(generateShoppingCart()).encodePrettily());
    }

    @Test
    public void getCart(TestContext context) {
        final Async async = context.async();

        CompletableFuture<Void> future1 = new CompletableFuture<>();
        CompletableFuture<Void> future2 =  new CompletableFuture<>();

        cartService.addItems(SHOPPING_CART_ID,"329199",8, x -> future1.complete(null));
        cartService.addItems(SHOPPING_CART_ID,"329299",3, x -> future2.complete(null));

        CompletableFuture.allOf(future1,future2).thenAccept( f -> {
            vertx.createHttpClient().getNow(port, "localhost", "/services/cart/" + SHOPPING_CART_ID, response -> {
                response.handler(body -> {
                    try {
                        System.out.println("body = " + body);
                        ShoppingCart cart = Transformers.jsonToShoppingCart(body.toJsonObject());
                        System.out.println("cart = " + cart);
                        System.out.println("orderValue = " + cart.getCartTotal());
                        System.out.println("retailPrice = " + cart.getCartItemTotal());
                        assertThat(cart.getCartTotal()).as("The Cart Value should be 279.92").isEqualTo(279.92);
                    } finally {
                        async.complete();
                    }

                });
            });
        });






    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
