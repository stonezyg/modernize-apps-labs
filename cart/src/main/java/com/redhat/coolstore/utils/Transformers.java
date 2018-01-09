package com.redhat.coolstore.utils;

import com.redhat.coolstore.model.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Transformers {

//    private static final String[] RANDOM_NAMES = {"Sven Karlsson","Johan Andersson","Karl Svensson","Anders Johansson","Stefan Olson","Martin Ericsson"};
//    private static final String[] RANDOM_EMAILS = {"sven@gmail.com","johan@gmail.com","karl@gmail.com","anders@gmail.com","stefan@gmail.com","martin@gmail.com"};

    public static JsonObject shoppingCartToJson(ShoppingCart cart) {
        JsonArray cartItems = new JsonArray();
        cart.getShoppingCartItemList().forEach(item -> {
            cartItems.add(new JsonObject()
                .put("product",productToJson(item.getProduct()))
                .put("promoSavings",item.getPromoSavings())
                .put("quantity",item.getQuantity())
            );
        });

//        int randomNameAndEmailIndex = ThreadLocalRandom.current().nextInt(RANDOM_NAMES.length);

        JsonObject jsonObject = new JsonObject()
            .put("orderValue", new Double(cart.getCartTotal()))
//            .put("customerName",RANDOM_NAMES[randomNameAndEmailIndex])
//            .put("customerEmail",RANDOM_EMAILS[randomNameAndEmailIndex])
            .put("retailPrice", cart.getCartItemTotal())
            .put("discount", new Double(cart.getCartItemPromoSavings()))
            .put("shippingFee", new Double(cart.getShippingTotal()))
            .put("shippingDiscount", new Double(cart.getShippingPromoSavings()))
            .put("items",cartItems);

        return jsonObject;
    }


    public static ShoppingCart jsonToShoppingCart(JsonObject json) {
        ShoppingCart sc = new ShoppingCartImpl();
        List<ShoppingCartItem> sciList = new ArrayList<>();
        json.getJsonArray("items").forEach(item -> {
            JsonObject itemJson  = (JsonObject) item;
            ShoppingCartItem sci = new ShoppingCartItemImpl();
            sci.setQuantity(itemJson.getInteger("quantity"));
            sci.setProduct(jsonToProduct(itemJson.getJsonObject("product")));
            sci.setPromoSavings(itemJson.getDouble("promoSavings"));
            sciList.add(sci);
        });
        sc.setShoppingCartItemList(sciList);
        sc.setShippingPromoSavings(json.getDouble("shippingDiscount"));
        sc.setShippingTotal(json.getDouble("shippingFee"));
        return sc;

    }

    public static Product jsonToProduct(JsonObject json) {
        Product product = new ProductImpl();
        product.setItemId(json.getString("itemId"));
        product.setPrice(json.getDouble("price"));
        product.setName(json.getString("name"));
        product.setDesc(json.getString("desc"));
        product.setLocation(json.getString("location"));
        product.setLink(json.getString("link"));
        return product;
    }

    public static JsonObject productToJson(Product product) {
        JsonObject json = new JsonObject();
        json.put("itemId",product.getItemId());
        json.put("price",product.getPrice());
        json.put("name",product.getName());
        json.put("desc",product.getDesc());
        json.put("location",product.getLocation());
        json.put("link",product.getLink());

        return json;
    }

}
