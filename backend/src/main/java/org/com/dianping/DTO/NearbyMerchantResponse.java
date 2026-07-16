package org.com.dianping.DTO;
import org.com.dianping.entity.Merchant;
public record NearbyMerchantResponse(Merchant merchant, double distanceMeters) { }
