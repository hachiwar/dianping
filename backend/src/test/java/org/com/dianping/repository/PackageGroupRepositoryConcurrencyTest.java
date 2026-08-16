package org.com.dianping.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.concurrent.*;
import org.com.dianping.entity.PackageGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
class PackageGroupRepositoryConcurrencyTest {
    @Autowired PackageGroupRepository packages;
    @Autowired TransactionTemplate transactions;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void onlyOneConcurrentPurchaseCanConsumeTheLastStock() throws Exception {
        PackageGroup saved = transactions.execute(status -> packages.saveAndFlush(newPackage()));
        Long id = saved.getId(); Long version = saved.getVersion();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Future<Integer> first = pool.submit(() -> updateAfter(start, id, version));
        Future<Integer> second = pool.submit(() -> updateAfter(start, id, version));
        start.countDown();
        assertEquals(1, first.get() + second.get());
        assertEquals(0, transactions.execute(status -> packages.findById(id).orElseThrow().getStock()).intValue());
        assertEquals(Long.valueOf(version + 1), transactions.<Long>execute(status -> packages.findById(id).orElseThrow().getVersion()));
        pool.shutdownNow();
    }

    private int updateAfter(CountDownLatch start, Long id, Long version) throws InterruptedException {
        start.await();
        return transactions.execute(status -> packages.decrementStockAndIncrementSales(id, version));
    }
    private PackageGroup newPackage() {
        PackageGroup value = new PackageGroup(); value.setTitle("test"); value.setDescription("test"); value.setPrice(new BigDecimal("10.00")); value.setSales(0); value.setMerchantId(1L); value.setStock(1); return value;
    }
}
