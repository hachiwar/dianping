package org.com.dianping.repository;

import org.com.dianping.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserIdOrderByCreatedTimeDesc(Long userId);
    void deleteByUserIdAndId(Long userId, Long id);
    void deleteAllByUserId(Long userId);
}