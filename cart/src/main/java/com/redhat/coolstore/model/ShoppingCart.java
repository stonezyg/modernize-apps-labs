package com.redhat.coolstore.model;

import java.util.List;

public interface ShoppingCart {
    List<ShoppingCartItem> getShoppingCartItemList();

    void setShoppingCartItemList(List<ShoppingCartItem> shoppingCartItemList);

    void resetShoppingCartItemList();

    void addShoppingCartItem(ShoppingCartItem sci);

    boolean removeShoppingCartItem(ShoppingCartItem sci);

    double getCartItemTotal();

    double getShippingTotal();

    void setShippingTotal(double shippingTotal);

    double getCartTotal();

    double getCartItemPromoSavings();

    double getShippingPromoSavings();

    void setShippingPromoSavings(double shippingPromoSavings);

    void clear();



}
