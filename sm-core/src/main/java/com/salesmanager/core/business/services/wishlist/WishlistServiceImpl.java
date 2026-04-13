package com.salesmanager.core.business.services.wishlist;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.wishlist.WishlistRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.wishlist.Wishlist;
import com.salesmanager.core.model.wishlist.WishlistItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

@Service("wishlistService")
public class WishlistServiceImpl extends SalesManagerEntityServiceImpl<Long, Wishlist>
        implements WishlistService {

    private final WishlistRepository wishlistRepository;

    @Inject
    public WishlistServiceImpl(WishlistRepository wishlistRepository) {
        super(wishlistRepository);
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    @Transactional
    public Wishlist getOrCreate(Long customerId, MerchantStore store) throws ServiceException {
        return wishlistRepository.findByCustomerIdAndMerchantStoreId(customerId, store.getId())
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setCustomerId(customerId);
                    wishlist.setMerchantStore(store);
                    return wishlistRepository.save(wishlist);
                });
    }

    @Override
    @Transactional
    public Wishlist addItem(Wishlist wishlist, Long productId, String sku) throws ServiceException {
        boolean alreadyExists = wishlist.getLineItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
        if (!alreadyExists) {
            WishlistItem item = new WishlistItem();
            item.setProductId(productId);
            item.setSku(sku);
            item.setWishlist(wishlist);
            wishlist.getLineItems().add(item);
            wishlistRepository.save(wishlist);
        }
        return wishlist;
    }

    @Override
    @Transactional
    public void removeItem(Wishlist wishlist, Long productId) throws ServiceException {
        wishlist.getLineItems().removeIf(item -> item.getProductId().equals(productId));
        wishlistRepository.save(wishlist);
    }

    @Override
    public Optional<Wishlist> getByCustomer(Long customerId, MerchantStore store) throws ServiceException {
        return wishlistRepository.findByCustomerIdAndMerchantStoreId(customerId, store.getId());
    }
}
