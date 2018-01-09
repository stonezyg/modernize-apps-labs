package com.redhat.coolstore.model;

import java.io.Serializable;
import java.util.Objects;

public class ShoppingCartItemImpl implements Serializable, ShoppingCartItem {

	private static final long serialVersionUID = 6964558044240061049L;

	private int quantity;
	private double promoSavings;
	private Product product;

	public ShoppingCartItemImpl() {

	}


	@Override
    public Product getProduct() {
		return product;
	}

	@Override
    public void setProduct(Product product) {
		this.product = product;
	}

	@Override
    public int getQuantity() {
		return quantity;
	}

	@Override
    public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
    public double getPromoSavings() {
		return promoSavings;
	}

	@Override
    public void setPromoSavings(double promoSavings) {
		this.promoSavings = promoSavings;
	}

	@Override
	public String toString() {
		return "ShoppingCartItem [quantity=" + quantity
				+ ", promoSavings=" + promoSavings + ", product=" + product
				+ "]";
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCartItemImpl that = (ShoppingCartItemImpl) o;
        return quantity == that.quantity &&
            Double.compare(that.promoSavings, promoSavings) == 0 &&
            Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {

        return Objects.hash(quantity, promoSavings, product);
    }
}