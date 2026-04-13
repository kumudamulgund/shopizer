package com.salesmanager.core.model.wishlist;

import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;

import javax.persistence.*;

@Entity
@Table(name = "WISHLIST_ITEM")
public class WishlistItem extends SalesManagerEntity<Long, WishlistItem> implements Auditable {

    private static final long serialVersionUID = 1L;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "WISHLIST_ITEM_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "WISHLIST_ITEM_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WISHLIST_ID", nullable = false)
    private Wishlist wishlist;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    @Column(name = "SKU", nullable = true)
    private String sku;

    @Override
    public AuditSection getAuditSection() { return auditSection; }
    @Override
    public void setAuditSection(AuditSection audit) { this.auditSection = audit; }
    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    public Wishlist getWishlist() { return wishlist; }
    public void setWishlist(Wishlist wishlist) { this.wishlist = wishlist; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
}
