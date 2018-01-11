package com.redhat.coolstore.model;

public interface Promotion {

    double getPromotion();

    void setPromotion(double promotion);

    boolean isCriteriaMet(ShoppingCart cart);
}
