package me.yogurt0811.linhtien.realms;

import java.util.List;
import java.util.Map;

public record Realm(
        String id,
        String displayName,
        double expToNext,
        Map<String, Double> stats,
        List<RealmPhase> phases
) {}