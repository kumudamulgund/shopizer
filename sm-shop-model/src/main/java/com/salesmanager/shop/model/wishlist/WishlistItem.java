package com.salesmanager.shop.model.wishlist;

import com.salesmanager.shop.model.entity.Entity;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class WishlistItem extends Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long productId;

    private String sku;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
}
