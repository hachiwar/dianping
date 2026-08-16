package org.com.dianping.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @NotNull(message = "packageId 不能为空") Long packageId,
        @NotNull(message = "businessId 不能为空") Long businessId,
        @Size(max = 6, message = "邀请码长度不能超过 6 位") String invitationCode,
        @NotBlank(message = "idempotencyKey 不能为空")
        @Size(max = 64, message = "idempotencyKey 长度不能超过 64 位") String idempotencyKey) {
}
