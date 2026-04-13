package com.salesmanager.shop.store.facade.wishlist;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.wishlist.WishlistService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.wishlist.Wishlist;
import com.salesmanager.shop.mapper.catalog.ReadableMinimalProductMapper;
import com.salesmanager.shop.model.catalog.product.ReadableMinimalProduct;
import com.salesmanager.shop.model.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.wishlist.ReadableWishlistItem;
import com.salesmanager.shop.model.wishlist.WishlistItem;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.controller.wishlist.facade.WishlistFacade;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("wishlistFacade")
public class WishlistFacadeImpl implements WishlistFacade {

    @Inject
    private WishlistService wishlistService;

    @Inject
    private ProductService productService;

    @Inject
    private ReadableMinimalProductMapper readableMinimalProductMapper;

    @Override
    public ReadableWishlist getWishlist(Customer customer, MerchantStore store, Language language) {
        try {
            Optional<Wishlist> wishlist = wishlistService.getByCustomer(customer.getId(), store);
            ReadableWishlist readableWishlist = new ReadableWishlist();
            if (!wishlist.isPresent()) {
                return readableWishlist;
            }
            readableWishlist.setId(wishlist.get().getId());
            List<ReadableWishlistItem> items = new ArrayList<>();
            for (com.salesmanager.core.model.wishlist.WishlistItem item : wishlist.get().getLineItems()) {
                Product product = productService.getById(item.getProductId());
                if (product == null) continue;
                ReadableMinimalProduct readableProduct = readableMinimalProductMapper.convert(product, store, language);
                ReadableWishlistItem readableItem = new ReadableWishlistItem();
                readableItem.setWishlistItemId(item.getId());
                readableItem.setDescription(readableProduct.getDescription());
                readableItem.setProductPrice(readableProduct.getProductPrice());
                readableItem.setFinalPrice(readableProduct.getFinalPrice());
                readableItem.setImage(readableProduct.getImage());
                readableItem.setId(readableProduct.getId());
                readableItem.setSku(readableProduct.getSku());
                items.add(readableItem);
            }
            readableWishlist.setItems(items);
            return readableWishlist;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public void addToWishlist(WishlistItem item, Customer customer, MerchantStore store) {
        try {
            Product product = productService.getById(item.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product with id [" + item.getProductId() + "] not found");
            }
            Wishlist wishlist = wishlistService.getOrCreate(customer.getId(), store);
            wishlistService.addItem(wishlist, item.getProductId(), item.getSku());
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public void removeFromWishlist(Long productId, Customer customer, MerchantStore store) {
        try {
            Optional<Wishlist> wishlist = wishlistService.getByCustomer(customer.getId(), store);
            if (!wishlist.isPresent()) {
                throw new ResourceNotFoundException("Wishlist not found for customer [" + customer.getId() + "]");
            }
            wishlistService.removeItem(wishlist.get(), productId);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
