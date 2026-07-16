package org.com.dianping.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "review")

public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="userID",nullable = false, unique = false)
    private Long userID;

    @Column(name="merchantID",nullable = false, unique = false)
    private Long merchantID;

    @Column(name="rating",nullable = false, unique = false)
    private Double rating;

    @Column(name="comment",nullable = false, unique = false)
    private String comment;

    @Column(name="parentID",nullable = true, unique = false)
    private Long parentID;

    @Column(name="createTime",nullable = false, unique = false)
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserID() {
        return userID;
    }
    public void setUserID(Long userID) {
        this.userID = userID;
    }
    public Long getMerchantID() {
        return merchantID;
    }
    public void setMerchantID(Long merchantID) {
        this.merchantID = merchantID;
    }
    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public Long getParentID() {
        return parentID;
    }
    public void setParentID(Long parentID) {
        this.parentID = parentID;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
}
