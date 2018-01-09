package com.redhat.coolstore.utils;

import com.redhat.coolstore.MainVerticle;
import com.redhat.coolstore.model.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
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
    Vertx vertx;
    int port;

    @Before
    public void setup(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject().put("http.port", port)
                .put("catalog.service.port",8081)
                .put("catalog.service.hostname","localhost")
                .put("catalog.service.timeout",2000)
            );
        vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());

    }

    @Test
    public void testShoppingCartEndpoints(TestContext context) {

        final Async async = context.async();
        WebClient client = WebClient.create(vertx);
        CompletableFuture<Void> future1 = new CompletableFuture<>();
        CompletableFuture<Void> future2 =  new CompletableFuture<>();

        //Add products
        client.post(port,"localhost","/services/cart/" + SHOPPING_CART_ID + "/329299/2")
            .timeout(5000)
            .send(handler -> {
                assertThat(handler.succeeded()).as("Adding product to shopping cart");
                future1.complete(null);
        });

        //Add products
        client.post(port,"localhost","/services/cart/" + SHOPPING_CART_ID + "/329199/5")
            .timeout(5000)
            .send(handler -> {
                assertThat(handler.succeeded()).as("Adding product to shopping cart");
                future2.complete(null);
            });


        CompletableFuture.allOf(future1,future2).thenAccept( f -> {
            vertx.createHttpClient().getNow(port, "localhost", "/services/cart/" + SHOPPING_CART_ID, response -> {
                response.handler(body -> {
                    try {
                        assertThat(body).as("Body can't be null").isNotNull();
                        assertThat(body.toJsonObject()).as("Body must be a JSON String").isNotNull();
                        ShoppingCart cart = Transformers.jsonToShoppingCart(body.toJsonObject());
                        assertThat(cart).as("Json must be able to transform into a shopping cart").isNotNull();
                        assertThat(cart.getShoppingCartItemList().size()).as("The shopping cart has two entries").isEqualTo(2);
                        assertThat(cart.getCartTotal()).as("The Cart Value should be 112.48").isEqualTo(112.48);
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
