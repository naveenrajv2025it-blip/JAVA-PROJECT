package com.campus.lostfound.model;

import java.util.List;

public class MatchResult {
    private Item item;
    private int score;
    private List<String> reasons;

    public MatchResult() {}

    public MatchResult(Item item, int score, List<String> reasons) {
        this.item = item;
        this.score = score;
        this.reasons = reasons;
    }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}
