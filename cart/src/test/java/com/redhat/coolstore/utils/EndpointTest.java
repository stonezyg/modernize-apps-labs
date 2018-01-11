package com.redhat.coolstore.utils;

import com.redhat.coolstore.CartServiceVerticle;
import com.redhat.coolstore.PromoServiceVerticle;
import com.redhat.coolstore.ShippingServiceVerticle;
import com.redhat.coolstore.model.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("WeakerAccess")
@RunWith(VertxUnitRunner.class)
public class EndpointTest {

    private static final String SHOPPING_CART_ID = "00001";
    private static final String DUMMY_SHOPPING_CART_ID = "99999";

    private Vertx vertx;
    private int port;

    @Before
    public void setup(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        ServerSocket serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
        serverSocket.close();
        vertx.deployVerticle(CartServiceVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("http.port",port)), context.asyncAssertSuccess());
        vertx.deployVerticle(PromoServiceVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("http.port",port)), context.asyncAssertSuccess());
        vertx.deployVerticle(ShippingServiceVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("http.port",port)), context.asyncAssertSuccess());
    }

    @Test
    public void testShoppingCartEndpoints(TestContext context) {
        System.out.println("TEST GETTING SHOPPING CART");
        vertx.createHttpClient().getNow(port, "localhost", "/services/cart/" + DUMMY_SHOPPING_CART_ID, response -> response.handler(body -> {
                assertThat(body).as("Body can't be null").isNotNull();
                assertThat(body.toJsonObject()).as("Body must be a JSON String").isNotNull();
                ShoppingCart cart = Transformers.jsonToShoppingCart(body.toJsonObject());
                assertThat(cart).as("The shopping Cart item cannot be null").isNotNull();
                assertThat(cart.getShoppingCartItemList().size()).as("The shopping cart should  has at least one entry").isGreaterThan(0);
                assertThat(cart.getCartTotal()).as("The Cart Value should be greater than ZERO").isGreaterThan(0.0);

        }));

    }

    @Test
    @Ignore
    public void testAddProduct() {
        WebClient client = WebClient.create(vertx);

        //Add products
        client.post(port,"localhost","/services/cart/" + SHOPPING_CART_ID + "/329299/2")
            .timeout(2000)
            .send(handler -> {
                assertThat(handler.succeeded()).as("Adding product to shopping cart").isTrue();
                assertThat(handler.result().statusCode()).as("Status code is 200").isEqualTo(200);
            });

    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
