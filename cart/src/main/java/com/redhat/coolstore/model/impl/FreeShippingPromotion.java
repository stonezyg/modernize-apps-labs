package com.redhat.coolstore.model.impl;

import com.redhat.coolstore.model.Promotion;
import com.redhat.coolstore.model.ShoppingCart;

public class FreeShippingPromotion implements Promotion {

    private double promotion;

    private double minValue;


    public FreeShippingPromotion(double minValue) {
        this.minValue = minValue;
    }

    public FreeShippingPromotion() {
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    @Override
    public double getPromotion() {
        return promotion;
    }

    @Override
    public void setPromotion(double promotion) {
        this.promotion=promotion;
    }

    @Override
    public boolean isCriteriaMet(ShoppingCart cart) {
        if(cart.getCartTotal()>minValue) {
            promotion = cart.getShippingTotal();
            return true;
        }
        return false;
    }
}
