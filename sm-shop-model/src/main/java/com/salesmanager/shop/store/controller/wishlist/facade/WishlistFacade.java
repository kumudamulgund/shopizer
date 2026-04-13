package com.salesmanager.shop.store.controller.wishlist.facade;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.wishlist.WishlistItem;

public interface WishlistFacade {

    ReadableWishlist getWishlist(Customer customer, MerchantStore store, Language language);

    void addToWishlist(WishlistItem item, Customer customer, MerchantStore store);

    void removeFromWishlist(Long productId, Customer customer, MerchantStore store);
}
