package org.com.dianping.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private LocalDateTime createdTime;

    public SearchHistory() {}

    public SearchHistory(Long userId, String keyword) {
        this.userId = userId;
        this.keyword = keyword;
        this.createdTime = LocalDateTime.now();
    }
    // 添加全参构造函数（JPA需要）
    public SearchHistory(Long id, Long userId, String keyword, LocalDateTime createdTime) {
        this.id = id;
        this.userId = userId;
        this.keyword = keyword;
        this.createdTime = createdTime;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}