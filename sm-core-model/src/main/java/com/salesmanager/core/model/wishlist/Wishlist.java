package com.salesmanager.core.model.wishlist;

import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;
import com.salesmanager.core.model.merchant.MerchantStore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "WISHLIST", indexes = {
        @Index(name = "WISHLIST_CUSTOMER_IDX", columnList = "CUSTOMER_ID")
})
public class Wishlist extends SalesManagerEntity<Long, Wishlist> implements Auditable {

    private static final long serialVersionUID = 1L;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "WISHLIST_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "WISHLIST_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "wishlist")
    private Set<WishlistItem> lineItems = new HashSet<>();

    @Override
    public AuditSection getAuditSection() { return auditSection; }
    @Override
    public void setAuditSection(AuditSection audit) { this.auditSection = audit; }
    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public MerchantStore getMerchantStore() { return merchantStore; }
    public void setMerchantStore(MerchantStore merchantStore) { this.merchantStore = merchantStore; }
    public Set<WishlistItem> getLineItems() { return lineItems; }
    public void setLineItems(Set<WishlistItem> lineItems) { this.lineItems = lineItems; }
}
