package com.campus.lostfound.service;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.MatchResult;

import java.util.*;

public class MatchingEngine {

    public List<MatchResult> findMatches(Item targetItem, List<Item> candidates) {
        List<MatchResult> results = new ArrayList<>();
        if (targetItem == null || candidates == null) return results;

        String oppositeType = "LOST".equalsIgnoreCase(targetItem.getItemType()) ? "FOUND" : "LOST";

        for (Item candidate : candidates) {
            // Must be opposite type and not resolved
            if (!oppositeType.equalsIgnoreCase(candidate.getItemType()) || "RESOLVED".equalsIgnoreCase(candidate.getStatus())) {
                continue;
            }

            int score = 0;
            List<String> reasons = new ArrayList<>();

            // 1. Category equality (40 points)
            if (targetItem.getCategoryId() == candidate.getCategoryId() ||
               (targetItem.getCategoryName() != null && targetItem.getCategoryName().equalsIgnoreCase(candidate.getCategoryName()))) {
                score += 40;
                reasons.add("Matching Category (" + candidate.getCategoryName() + ")");
            }

            // 2. Location equality (20 points)
            if (targetItem.getLocationId() == candidate.getLocationId() ||
               (targetItem.getBuildingName() != null && targetItem.getBuildingName().equalsIgnoreCase(candidate.getBuildingName()))) {
                score += 20;
                reasons.add("Matching Location (" + candidate.getBuildingName() + ")");
            }

            // 3. Date proximity (up to 30 points)
            if (targetItem.getIncidentDate() != null && candidate.getIncidentDate() != null) {
                long diffMillis = Math.abs(targetItem.getIncidentDate().getTime() - candidate.getIncidentDate().getTime());
                long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                if (diffDays <= 2) {
                    score += 30;
                    reasons.add("Incident dates within " + diffDays + " days");
                } else if (diffDays <= 5) {
                    score += 20;
                    reasons.add("Incident dates within 5 days");
                } else if (diffDays <= 10) {
                    score += 10;
                }
            }

            // 4. Keyword token overlap (10 points)
            Set<String> targetTokens = extractTokens(targetItem.getTitle() + " " + targetItem.getDescription());
            Set<String> candidateTokens = extractTokens(candidate.getTitle() + " " + candidate.getDescription());

            int sharedTokens = 0;
            for (String token : candidateTokens) {
                if (targetTokens.contains(token)) {
                    sharedTokens++;
                }
            }

            if (sharedTokens >= 2) {
                score += 10;
                reasons.add("Shared descriptive keywords");
            }

            if (score >= 40) {
                results.add(new MatchResult(candidate, Math.min(score, 100), reasons));
            }
        }

        results.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return results;
    }

    private Set<String> extractTokens(String text) {
        Set<String> set = new HashSet<>();
        if (text == null) return set;
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        for (String w : words) {
            if (w.length() >= 3) {
                set.add(w);
            }
        }
        return set;
    }
}
