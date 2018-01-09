package com.redhat.coolstore.utils;

import com.redhat.coolstore.model.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@RunWith(VertxUnitRunner.class)
public class TransformersTest {

    private static final String SC_JSON = "{\n" +
        "  \"orderValue\" : 465.0,\n" +
        "  \"retailPrice\" : 410.0,\n" +
        "  \"discount\" : -35.0,\n" +
        "  \"shippingFee\" : 100.0,\n" +
        "  \"shippingDiscount\" : -10.0,\n" +
        "  \"items\" : [ {\n" +
        "    \"product\" : {\n" +
        "      \"itemId\" : \"00001\",\n" +
        "      \"price\" : 100.0,\n" +
        "      \"name\" : \"Fake product\",\n" +
        "      \"desc\" : \"This is a fake product and is only used for testing\",\n" +
        "      \"location\" : \"Stockholm/Sweden\",\n" +
        "      \"link\" : \"http://fakeproduct.me\"\n" +
        "    },\n" +
        "    \"promoSavings\" : -10.0,\n" +
        "    \"quantity\" : 2\n" +
        "  }, {\n" +
        "    \"product\" : {\n" +
        "      \"itemId\" : \"00002\",\n" +
        "      \"price\" : 70.0,\n" +
        "      \"name\" : \"Fake product 2\",\n" +
        "      \"desc\" : \"This is a fake product and is only used for testing\",\n" +
        "      \"location\" : \"Stockholm/Sweden\",\n" +
        "      \"link\" : \"http://fakeproduct.me\"\n" +
        "    },\n" +
        "    \"promoSavings\" : -5.0,\n" +
        "    \"quantity\" : 3\n" +
        "  } ]\n" +
        "}";

    @Before
    public void setup() {
//        System.out.println("Transformers.shoppingCartToJson(generateShoppingCart()).encodePrettily() = " + Transformers.shoppingCartToJson(generateShoppingCart()).encodePrettily());
    }

    @Test
    public void shoppingCartToJson() {
        ShoppingCart originalCart = generateShoppingCart();
        JsonObject scJson = Transformers.shoppingCartToJson(originalCart);
        assertThat(scJson).as("Make sure that transformed Json isn't Null").isNotNull();
        assertThat(scJson.encode()).as("Make sure that Json String is not Empty").isNotEmpty();
        assertThat(scJson.encodePrettily()).isEqualTo(SC_JSON);

        ShoppingCart newCart = Transformers.jsonToShoppingCart(scJson);
        assertThat(newCart).as("Make sure that transformed ShoppingCart isn't Null").isNotNull();
        assertThat(newCart.getShoppingCartItemList()).as("Make sure that transformated ShoppingCart has Items").isNotEmpty();
        assertThat(originalCart).as("Compare Original Shopping Cart with Shopping Cart after double transformation").isEqualTo(newCart);



    }

    @Test
    public void jsonToShoppingCart() {
        ShoppingCart shoppingCart = Transformers.jsonToShoppingCart(new JsonObject(SC_JSON));
        assertThat(shoppingCart).isNotNull();
        assertThat(shoppingCart.getCartItemTotal()).isEqualTo(410.0);
        assertThat(shoppingCart.getShippingTotal()).isEqualTo(100.0);
        assertThat(shoppingCart.getShippingPromoSavings()).isEqualTo(-10.0);
        assertThat(shoppingCart.getCartItemPromoSavings()).isEqualTo(-35.0);
        assertThat(shoppingCart.getCartTotal()).isEqualTo(465.0);
        assertThat(shoppingCart.getShoppingCartItemList()).isNotEmpty();
        assertThat(shoppingCart.getShoppingCartItemList()).extracting("product.itemId","promoSavings","quantity")
            .contains(tuple("00001",-10.0,2))
            .contains(tuple("00002",-5.0,3));
    }

    private static ShoppingCart generateShoppingCart() {
        ShoppingCart sc = new ShoppingCartImpl();
        sc.setShippingTotal(100.0);
        sc.setShippingPromoSavings(-10.0);

        ShoppingCartItem sci1 = new ShoppingCartItemImpl();
        sci1.setQuantity(2);
        sci1.setPromoSavings(-10.0);
        Product prod1 = new ProductImpl();
        prod1.setItemId("00001");
        prod1.setName("Fake product");
        prod1.setPrice(100);
        prod1.setLink("http://fakeproduct.me");
        prod1.setDesc("This is a fake product and is only used for testing");
        prod1.setLocation("Stockholm/Sweden");
        sci1.setProduct(prod1);
        sc.addShoppingCartItem(sci1);

        ShoppingCartItem sci2 = new ShoppingCartItemImpl();
        sci2.setQuantity(3);
        sci2.setPromoSavings(-5.0);
        Product prod2 = new ProductImpl();
        prod2.setItemId("00002");
        prod2.setName("Fake product 2");
        prod2.setPrice(70);
        prod2.setLink("http://fakeproduct.me");
        prod2.setDesc("This is a fake product and is only used for testing");
        prod2.setLocation("Stockholm/Sweden");
        sci2.setProduct(prod2);
        sc.addShoppingCartItem(sci2);

        return sc;

    }
}