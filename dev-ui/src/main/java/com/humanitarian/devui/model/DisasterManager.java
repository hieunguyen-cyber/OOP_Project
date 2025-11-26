package com.humanitarian.devui.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all disaster types for the system.
 * - Maintains a database of known disaster types
 * - Maps keywords and hashtags to disaster types
 * - Allows adding new disaster types
 */
public class DisasterManager {
    private final Map<String, DisasterType> disasterTypes;
    private static DisasterManager instance;

    private DisasterManager() {
        this.disasterTypes = new HashMap<>();
        initializeDefaultDisasters();
    }

    /**
     * Get singleton instance
     */
    public static DisasterManager getInstance() {
        if (instance == null) {
            instance = new DisasterManager();
        }
        return instance;
    }

    /**
     * Initialize with default disaster types
     */
    private void initializeDefaultDisasters() {
        // Yagi (Typhoon)
        DisasterType yagi = new DisasterType("yagi");
        yagi.addAlias("#yagi");
        yagi.addAlias("yagi");
        yagi.addAlias("typhoon");
        yagi.addAlias("#typhoon");
        addDisasterType(yagi);

        // Matmo (Typhoon)
        DisasterType matmo = new DisasterType("matmo");
        matmo.addAlias("#matmo");
        matmo.addAlias("matmo");
        addDisasterType(matmo);

        // Flood
        DisasterType flood = new DisasterType("flood");
        flood.addAlias("#bualoi");
        flood.addAlias("bualoi");
        flood.addAlias("flood");
        flood.addAlias("#flood");
        addDisasterType(flood);

        // Disaster/General
        DisasterType disaster = new DisasterType("disaster");
        disaster.addAlias("disaster");
        disaster.addAlias("#disaster");
        addDisasterType(disaster);

        // Aid/Relief
        DisasterType aid = new DisasterType("aid");
        aid.addAlias("aid");
        aid.addAlias("#aid");
        aid.addAlias("relief");
        aid.addAlias("#relief");
        addDisasterType(aid);
    }

    /**
     * Add a new disaster type
     */
    public void addDisasterType(DisasterType disasterType) {
        if (disasterType != null) {
            disasterTypes.put(disasterType.getName(), disasterType);
        }
    }

    /**
     * Get disaster type by name
     */
    public DisasterType getDisasterType(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return disasterTypes.get(DisasterType.normalize(name));
    }

    /**
     * Find matching disaster type for a keyword
     */
    public DisasterType findDisasterType(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        
        String normalized = DisasterType.normalize(keyword);
        
        // Check if keyword exactly matches a disaster name
        if (disasterTypes.containsKey(normalized)) {
            return disasterTypes.get(normalized);
        }
        
        // Check all disaster types for matching aliases
        for (DisasterType disaster : disasterTypes.values()) {
            if (disaster.matches(keyword)) {
                return disaster;
            }
        }
        
        return null;
    }

    /**
     * Find disaster type from post content (text)
     */
    public DisasterType findDisasterTypeForPost(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        String contentLower = content.toLowerCase();
        
        // Search for any disaster type keywords/aliases in content
        for (DisasterType disaster : disasterTypes.values()) {
            // Check main name
            if (contentLower.contains(disaster.getName().toLowerCase())) {
                return disaster;
            }
            
            // Check aliases
            for (String alias : disaster.getAliases()) {
                if (contentLower.contains(alias.toLowerCase())) {
                    return disaster;
                }
            }
        }
        
        return null;
    }

    /**
     * Get all disaster type names
     */
    public List<String> getAllDisasterNames() {
        return disasterTypes.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Get all disaster types
     */
    public Collection<DisasterType> getAllDisasterTypes() {
        return new ArrayList<>(disasterTypes.values());
    }

    /**
     * Check if a keyword belongs to a disaster type
     */
    public boolean isKeywordForDisaster(String keyword, String disasterName) {
        DisasterType disaster = getDisasterType(disasterName);
        if (disaster == null) {
            return false;
        }
        return disaster.matches(keyword);
    }

    /**
     * Get or create disaster type
     */
    public DisasterType getOrCreateDisasterType(String name) {
        DisasterType existing = getDisasterType(name);
        if (existing != null) {
            return existing;
        }
        
        DisasterType newDisaster = new DisasterType(name);
        addDisasterType(newDisaster);
        return newDisaster;
    }

    /**
     * Get disaster type count
     */
    public int getDisasterTypeCount() {
        return disasterTypes.size();
    }

    /**
     * Remove a disaster type by name
     */
    public void removeDisasterType(String name) {
        if (name != null) {
            disasterTypes.remove(DisasterType.normalize(name));
        }
    }
}
