package org.com.dianping.controller;

import org.com.dianping.entity.PackageGroup;
import org.com.dianping.repository.PackageGroupRepository;
import org.com.dianping.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PackageController {

    private final PackageGroupRepository packageGroupRepository;
    private final PackageService packageService;

    @Autowired
    public PackageController(PackageGroupRepository packageGroupRepository, PackageService packageService) {
        this.packageGroupRepository = packageGroupRepository;
        this.packageService = packageService;
    }

    // 获取指定商户的团购套餐列表
    @GetMapping("/packages/business/{businessId}")
    public List<PackageGroup> getPackagesByBusinessId(@PathVariable("businessId") Long businessId) {
        return packageGroupRepository.findByMerchantId(businessId);
    }

    // 获取指定套餐的详细信息
    @GetMapping("/packages/{packageId}")
    public ResponseEntity<PackageGroup> getPackageDetail(@PathVariable("packageId") Long packageId) {
        Optional<PackageGroup> packageGroup = packageService.findById(packageId);
        return packageGroup.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
