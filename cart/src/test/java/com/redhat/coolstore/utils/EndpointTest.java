package com.redhat.coolstore.utils;

import com.redhat.coolstore.MainVerticle;
import com.redhat.coolstore.model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class EndpointTest {

    private static final String SHOPPING_CART_ID = "00001";
    private Vertx vertx;
    private int port = 8082;

    @Before
    public void setup(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @Test
    public void testShoppingCartEndpoints(TestContext context) throws InterruptedException,TimeoutException,ExecutionException {

        final Async async = context.async();
        WebClient client = WebClient.create(vertx);
        CompletableFuture<Void> future1 = new CompletableFuture<>();
        CompletableFuture<Void> future2 =  new CompletableFuture<>();

        //Add products
        client.post(port,"localhost","/services/cart/" + SHOPPING_CART_ID + "/329299/2")
            .timeout(2000)
            .send(handler -> {
                assertThat(handler.succeeded()).as("Adding product to shopping cart").isTrue();
                assertThat(handler.result().statusCode()).as("Status code is 200").isEqualTo(200);
                future1.complete(null);
        });

        //Add products
        client.post(port,"localhost","/services/cart/" + SHOPPING_CART_ID + "/329199/5")
            .timeout(2000)
            .send(handler -> {
                assertThat(handler.succeeded()).as("Adding product to shopping cart").isTrue();
                assertThat(handler.result().statusCode()).as("Status code is 200").isEqualTo(200);
                future2.complete(null);
            });


        CompletableFuture.allOf(future1,future2).thenAccept( f -> vertx.createHttpClient().getNow(port, "localhost", "/services/cart/" + SHOPPING_CART_ID, response -> response.handler(body -> {
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

        }))).get(5, TimeUnit.SECONDS);





    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
