package com.redhat.coolstore.model.impl;

import com.redhat.coolstore.model.Promotion;
import com.redhat.coolstore.model.ShoppingCart;

import java.util.List;
import java.util.stream.Collectors;

public class ProductCombinationPromotion implements Promotion {


    /**
     * List of product id's for the promotion
     */
    private List<String> combination;
    private double promotion;

    public ProductCombinationPromotion() {
    }

    public ProductCombinationPromotion(List<String> combination, double promotion) {
        this.combination = combination;
        this.promotion = promotion;
    }

    public List<String> getCombination() {
        return combination;
    }

    public void setCombination(List<String> combination) {
        this.combination = combination;
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
        List<String> itemIdInCart = cart.getShoppingCartItemList().stream().map(sc -> sc.getProduct().getItemId()).collect(Collectors.toList());
        return itemIdInCart.containsAll(combination);
    }
}
