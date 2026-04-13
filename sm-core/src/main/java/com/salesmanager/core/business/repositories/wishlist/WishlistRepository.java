package com.salesmanager.core.business.repositories.wishlist;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.wishlist.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("select w from Wishlist w left join fetch w.lineItems wl join fetch w.merchantStore wm where w.customerId = ?1 and wm.id = ?2")
    Optional<Wishlist> findByCustomerIdAndMerchantStoreId(Long customerId, Integer merchantStoreId);

    @Query("select w from Wishlist w left join fetch w.lineItems wl join fetch w.merchantStore wm where w.customerId = ?1")
    Optional<Wishlist> findByCustomerId(Long customerId);
}
