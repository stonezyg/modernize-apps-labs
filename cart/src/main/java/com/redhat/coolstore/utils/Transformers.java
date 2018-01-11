package com.redhat.coolstore.utils;

import com.redhat.coolstore.model.*;
import com.redhat.coolstore.model.impl.ProductImpl;
import com.redhat.coolstore.model.impl.ShoppingCartImpl;
import com.redhat.coolstore.model.impl.ShoppingCartItemImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Transformers {

    public static JsonObject shoppingCartToJson(ShoppingCart cart) {
        JsonArray cartItems = new JsonArray();
        cart.getShoppingCartItemList().forEach(item -> cartItems.add(new JsonObject()
            .put("product",productToJson(item.getProduct()))
            .put("quantity",item.getQuantity())
        ));

        return new JsonObject()
            .put("cartId",cart.getCartId())
            .put("orderValue", cart.getCartTotal())
            .put("retailPrice", cart.getCartItemTotal())
            .put("discount", cart.getCartItemPromoSavings())
            .put("shippingFee", cart.getShippingTotal())
            .put("shippingDiscount", cart.getShippingPromoSavings())
            .put("items",cartItems);
    }


    public static ShoppingCart jsonToShoppingCart(JsonObject json) {
        ShoppingCart sc = new ShoppingCartImpl();
        List<ShoppingCartItem> sciList = new ArrayList<>();
        json.getJsonArray("items").forEach(item -> {
            JsonObject itemJson  = (JsonObject) item;
            ShoppingCartItem sci = new ShoppingCartItemImpl();
            sci.setQuantity(itemJson.getInteger("quantity"));
            sci.setProduct(jsonToProduct(itemJson.getJsonObject("product")));
            sciList.add(sci);
        });
        sc.setCartId(json.getString("cartId"));
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

    private static JsonObject productToJson(Product product) {
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
