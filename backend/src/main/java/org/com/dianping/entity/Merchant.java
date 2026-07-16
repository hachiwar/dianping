package org.com.dianping.entity;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "merchant")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "merchant_name_pinyin", nullable = false)
    private String merchantNamePinyin; // 商家名称拼音

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "avgPrice", nullable = false)
    private Float avgPrice;

    @Column(name = "telephone", nullable = false)
    private String telephone;

    @Column(name = "business_hours", nullable = false)
    private String businessHours; // 营业时间

    @Column(name = "description", nullable = false,length = 1000)
    private String description; // 商家特点

    @Column(name = "cover_url", nullable = false)
    private String coverUrl; // 封面照片

    @Column(name = "photo_urls", nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> photoUrls;  // 商家详情页支持多张图片
    // Getters and Setters
    private Double longitude;
    private Double latitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantNamePinyin() {
        return merchantNamePinyin;
    }

    public void setMerchantNamePinyin(String merchantNamePinyin) {
        this.merchantNamePinyin = merchantNamePinyin;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Float getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Float avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
}

@Converter
class StringListConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> list) {
        return list != null ? String.join(";", list) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        return joined != null ? Arrays.asList(joined.split(";")) : List.of();
    }
}
