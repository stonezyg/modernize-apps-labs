package com.redhat.coolstore.model.impl;

import com.redhat.coolstore.model.Promotion;
import com.redhat.coolstore.model.ShoppingCart;

import java.util.List;
import java.util.stream.Collectors;

public class SingleProductPromotion implements Promotion {

    private double promotion;
    private String itemId;

    public SingleProductPromotion(double promotion, String prodId) {
        this.promotion = promotion;
        this.itemId = prodId;
    }

    public SingleProductPromotion() {
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public double getPromotion() {
        return promotion;
    }

    @Override
    public void setPromotion(double promotion) {
        this.promotion = promotion;
    }

    @Override
    public boolean isCriteriaMet(ShoppingCart cart) {
        List<String> itemIdsInCart = cart.getShoppingCartItemList().stream().map(sc -> sc.getProduct().getItemId()).collect(Collectors.toList());
        return itemIdsInCart.contains(itemId);
    }
}
