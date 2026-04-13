package com.salesmanager.shop.model.wishlist;

import com.salesmanager.shop.model.catalog.product.ReadableMinimalProduct;

import java.io.Serializable;

public class ReadableWishlistItem extends ReadableMinimalProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long wishlistItemId;

    public Long getWishlistItemId() { return wishlistItemId; }
    public void setWishlistItemId(Long wishlistItemId) { this.wishlistItemId = wishlistItemId; }
}
