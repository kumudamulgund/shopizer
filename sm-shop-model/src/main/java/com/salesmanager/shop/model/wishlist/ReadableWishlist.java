package com.salesmanager.shop.model.wishlist;

import com.salesmanager.shop.model.entity.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReadableWishlist extends Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ReadableWishlistItem> items = new ArrayList<>();

    public List<ReadableWishlistItem> getItems() { return items; }
    public void setItems(List<ReadableWishlistItem> items) { this.items = items; }
}
