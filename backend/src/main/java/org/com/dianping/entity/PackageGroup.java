package org.com.dianping.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "packages")
public class PackageGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String description;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal price;
    @Column(precision = 19, scale = 2) private BigDecimal originalPrice;
    @Column(nullable = false) private Integer sales;
    @Column(nullable = false) private Long merchantId;
    @Column(nullable = false, columnDefinition = "integer default 100") private Integer stock = 100;
    @Version private Long version;
    private String imageUrl;
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal v) { price = v; }
    public BigDecimal getOriginalPrice() { return originalPrice; } public void setOriginalPrice(BigDecimal v) { originalPrice = v; }
    public Integer getSales() { return sales; } public void setSales(Integer v) { sales = v; }
    public Long getMerchantId() { return merchantId; } public void setMerchantId(Long v) { merchantId = v; }
    public Integer getStock() { return stock; } public void setStock(Integer v) { stock = v; }
    public Long getVersion() { return version; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String v) { imageUrl = v; }
}
