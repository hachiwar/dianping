package org.com.dianping.config;

import java.math.BigDecimal;
import java.util.List;
import org.com.dianping.entity.Merchant;
import org.com.dianping.entity.PackageGroup;
import org.com.dianping.repository.MerchantRepository;
import org.com.dianping.repository.PackageGroupRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private final MerchantRepository merchants;
    private final PackageGroupRepository packages;

    public DemoDataInitializer(MerchantRepository merchants, PackageGroupRepository packages) {
        this.merchants = merchants;
        this.packages = packages;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (merchants.count() != 0) return;

        Merchant hotpot = merchant("蜀香火锅", "shuxianghuoguo", "火锅", 4.8f, 108f,
                "北京市朝阳区示范路 18 号", "010-88880001", "10:00—23:00",
                "川味锅底与现切食材", "/images/dish/dish(1).png", 116.401, 39.915);
        Merchant barbecue = merchant("炭火烧肉", "tanhuoshaorou", "烧烤", 4.6f, 86f,
                "北京市朝阳区示范路 32 号", "010-88880002", "11:00—22:30",
                "炭火烤肉与多人套餐", "/images/dish/dish(11).png", 116.408, 39.909);
        Merchant tea = merchant("喜茶体验店", "xichatiyandian", "奶茶", 4.7f, 28f,
                "北京市朝阳区示范路 66 号", "010-88880032", "09:30—22:00",
                "现制茶饮与当季水果茶", "/images/dish/dish(21).png", 116.395, 39.904);
        hotpot = merchants.save(hotpot);
        barbecue = merchants.save(barbecue);
        tea = merchants.save(tea);

        packages.saveAll(List.of(
                packageOf(hotpot.getId(), "双人火锅套餐", "锅底、荤素拼盘和饮品", "128.00", "168.00", "/packages/1.jpg"),
                packageOf(barbecue.getId(), "招牌烧肉双人餐", "六款烧肉、主食和饮品", "99.00", "139.00", "/packages/2.jpg"),
                packageOf(tea.getId(), "双杯茶饮任选", "门店在售中杯茶饮任选两杯", "29.90", "39.00", "/packages/3.jpg"),
                packageOf(hotpot.getId(), "四人欢聚套餐", "锅底、精品肉类、蔬菜拼盘和小吃", "238.00", "318.00", "/packages/4.jpg")));
    }

    private Merchant merchant(String name, String pinyin, String category, float rating, float avgPrice,
                              String address, String telephone, String hours, String description,
                              String cover, double longitude, double latitude) {
        Merchant value = new Merchant();
        value.setMerchantName(name); value.setMerchantNamePinyin(pinyin); value.setCategory(category);
        value.setRating(rating); value.setAvgPrice(avgPrice); value.setAddress(address);
        value.setTelephone(telephone); value.setBusinessHours(hours); value.setDescription(description);
        value.setCoverUrl(cover); value.setPhotoUrls(List.of(cover));
        value.setLongitude(longitude); value.setLatitude(latitude);
        return value;
    }

    private PackageGroup packageOf(Long merchantId, String title, String description,
                                   String price, String originalPrice, String imageUrl) {
        PackageGroup value = new PackageGroup();
        value.setMerchantId(merchantId); value.setTitle(title); value.setDescription(description);
        value.setPrice(new BigDecimal(price)); value.setOriginalPrice(new BigDecimal(originalPrice));
        value.setSales(0); value.setStock(100); value.setImageUrl(imageUrl);
        return value;
    }
}
