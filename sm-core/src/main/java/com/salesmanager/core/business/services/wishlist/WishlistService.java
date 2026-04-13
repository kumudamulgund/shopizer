package com.salesmanager.core.business.services.wishlist;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.wishlist.Wishlist;

import java.util.Optional;

public interface WishlistService {

    Wishlist getOrCreate(Long customerId, MerchantStore store) throws ServiceException;

    Wishlist addItem(Wishlist wishlist, Long productId, String sku) throws ServiceException;

    void removeItem(Wishlist wishlist, Long productId) throws ServiceException;

    Optional<Wishlist> getByCustomer(Long customerId, MerchantStore store) throws ServiceException;
}
