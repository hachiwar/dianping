package org.com.dianping.controller;

import org.com.dianping.entity.Merchant;
import org.com.dianping.service.MerchantService;
import org.com.dianping.service.MerchantLocationService;
import org.com.dianping.DTO.NearbyMerchantResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing merchant-related operations.
 */
@RestController
@RequestMapping("/api/businesses")
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantLocationService locationService;

    /**
     * Constructs a new {@link MerchantController}.
     *
     * @param merchantService the merchant service
     */
    public MerchantController(MerchantService merchantService, MerchantLocationService locationService) {
        this.merchantService = merchantService;
        this.locationService = locationService;
    }

    /**
     * Retrieves a merchant by its ID.
     *
     * @param id the ID of the merchant
     * @return the merchant details or a 404 response if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable Long id) {
        return merchantService.getMerchantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a list of merchants based on optional filters.
     *
     * @param keyword    the search keyword
     * @param rating     the minimum rating
     * @param priceRange the price range
     * @param avgPrice   the average price
     * @param sort       the sorting criteria
     * @return a list of merchants matching the filters
     */
    @GetMapping
    public List<Merchant> getBusinesses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Float rating,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Float avgPrice,
            @RequestParam(defaultValue = "default") String sort) {
        return merchantService.getMerchants(keyword, rating, priceRange, avgPrice, sort);
    }

    /**
     * Searches merchants using a keyword with pinyin support.
     *
     * @param keyword the search keyword
     * @return a list of merchants matching the keyword
     */
    @GetMapping("/search")
    public List<Merchant> searchMerchants(@RequestParam String keyword) {
        return merchantService.searchMerchantsWithPinyin(keyword);
    }

    @GetMapping("/nearby")
    public List<NearbyMerchantResponse> nearby(@RequestParam String category, @RequestParam double longitude, @RequestParam double latitude, @RequestParam(defaultValue = "3000") double radiusMeters, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return locationService.nearby(category, longitude, latitude, radiusMeters, page, size);
    }
}
