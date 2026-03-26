package com.phishguard.dto;

public class LeaderboardDTO {

    private Long userId;
    private String userName;
    private String email;
    private int totalScore;

    public LeaderboardDTO() {
    }

    public LeaderboardDTO(Long userId, String userName, String email, int totalScore) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.totalScore = totalScore;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}