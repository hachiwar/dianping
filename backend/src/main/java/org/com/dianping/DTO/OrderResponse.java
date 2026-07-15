package org.com.dianping.DTO;

import org.com.dianping.entity.Order;
import org.com.dianping.entity.PackageGroup;
import java.math.BigDecimal;

public record OrderResponse(
        Long id,    // 改为id而不是orderId
        String couponCode,
        String orderNo,
        String packageTitle,
        String businessName,
        BigDecimal finalPrice
) {
    public OrderResponse(Order order, PackageGroup pkg) {
        this(order.getId(), 
             order.getVoucherCode(), 
             "ORDER_" + order.getId(),
             pkg.getTitle(), 
             order.getBusinessName(),
             order.getFinalPrice());
    }
}
