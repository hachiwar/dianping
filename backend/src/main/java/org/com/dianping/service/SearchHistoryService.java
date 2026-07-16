package org.com.dianping.service;

import jakarta.transaction.Transactional;
import org.com.dianping.entity.SearchHistory;
import org.com.dianping.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchHistoryService {
    private final SearchHistoryRepository repository;

    public SearchHistoryService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    public SearchHistory saveSearch(Long userId, String keyword) {
        SearchHistory history = new SearchHistory(userId, keyword);
        return repository.save(history);
    }

    public List<SearchHistory> getHistory(Long userId) {
        return repository.findByUserIdOrderByCreatedTimeDesc(userId);
    }
    @Transactional
    public void deleteHistory(Long userId, Long historyId) {
        repository.deleteByUserIdAndId(userId, historyId);
    }
    @Transactional
    public void clearAll(Long userId) {
        repository.deleteAllByUserId(userId);
    }
}