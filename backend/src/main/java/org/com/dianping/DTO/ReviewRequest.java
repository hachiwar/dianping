package org.com.dianping.DTO;

public record ReviewRequest (
    Long userID,
    Long merchantID,
    Double rating,
    String comment,
    Long parentID,
    String createTime
){
    
}