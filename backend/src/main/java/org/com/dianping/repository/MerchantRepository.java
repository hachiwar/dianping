package org.com.dianping.repository;

import org.com.dianping.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MerchantRepository extends JpaRepository<Merchant, Long>, JpaSpecificationExecutor<Merchant> {

    @Query("SELECT m FROM Merchant m WHERE " +
            "(:keyword IS NULL OR LOWER(m.merchantName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.merchantNamePinyin) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.merchantNamePinyin) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))) " +
            "AND (:minRating IS NULL OR m.rating >= :minRating) " +
            "AND (:minPrice IS NULL OR m.avgPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR m.avgPrice <= :maxPrice)")
    List<Merchant> searchMerchantsWithPinyin(
            @Param("keyword") String keyword,
            @Param("minRating") Float minRating,
            @Param("minPrice") Float minPrice,
            @Param("maxPrice") Float maxPrice);

    @Query("SELECT m FROM Merchant m WHERE LOWER(m.merchantName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.merchantNamePinyin) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.merchantNamePinyin) LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', '')))")
    List<Merchant> findByKeyword(@Param("keyword") String keyword);

}