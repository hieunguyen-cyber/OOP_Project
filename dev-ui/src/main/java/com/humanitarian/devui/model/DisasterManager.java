package com.humanitarian.devui.model;

import java.util.*;
import java.util.stream.Collectors;

public class DisasterManager {
    private final Map<String, DisasterType> disasterTypes;
    private static DisasterManager instance;

    private DisasterManager() {
        this.disasterTypes = new HashMap<>();
        initializeDefaultDisasters();
    }

    public static DisasterManager getInstance() {
        if (instance == null) {
            instance = new DisasterManager();
        }
        return instance;
    }

    private void initializeDefaultDisasters() {

        DisasterType yagi = new DisasterType("yagi");
        yagi.addAlias("#yagi");
        yagi.addAlias("yagi");
        addDisasterType(yagi);

        DisasterType matmo = new DisasterType("matmo");
        matmo.addAlias("#matmo");
        matmo.addAlias("matmo");
        addDisasterType(matmo);

        DisasterType bualo = new DisasterType("bualo");
        bualo.addAlias("#bualo");
        bualo.addAlias("bualo");
        addDisasterType(bualo);

        DisasterType koto = new DisasterType("koto");
        koto.addAlias("#koto");
        koto.addAlias("koto");
        addDisasterType(koto);

        DisasterType fungwong = new DisasterType("FUNG-WONG");
        fungwong.addAlias("#FUNG-WONG");
        fungwong.addAlias("FUNG-WONG");
        fungwong.addAlias("#fung-wong");
        fungwong.addAlias("fung-wong");
        addDisasterType(fungwong);
    }

    public void addDisasterType(DisasterType disasterType) {
        if (disasterType != null) {
            disasterTypes.put(disasterType.getName(), disasterType);
        }
    }

    public DisasterType getDisasterType(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return disasterTypes.get(DisasterType.normalize(name));
    }

    public DisasterType findDisasterType(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        
        String normalized = DisasterType.normalize(keyword);
        
        if (disasterTypes.containsKey(normalized)) {
            return disasterTypes.get(normalized);
        }
        
        for (DisasterType disaster : disasterTypes.values()) {
            if (disaster.matches(keyword)) {
                return disaster;
            }
        }
        
        return null;
    }

    public DisasterType findDisasterTypeForPost(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        String contentLower = content.toLowerCase();
        
        for (DisasterType disaster : disasterTypes.values()) {

            if (contentLower.contains(disaster.getName().toLowerCase())) {
                return disaster;
            }
            
            for (String alias : disaster.getAliases()) {
                if (contentLower.contains(alias.toLowerCase())) {
                    return disaster;
                }
            }
        }
        
        return null;
    }

    public List<String> getAllDisasterNames() {
        return disasterTypes.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }

    public Collection<DisasterType> getAllDisasterTypes() {
        return new ArrayList<>(disasterTypes.values());
    }

    public boolean isKeywordForDisaster(String keyword, String disasterName) {
        DisasterType disaster = getDisasterType(disasterName);
        if (disaster == null) {
            return false;
        }
        return disaster.matches(keyword);
    }

    public DisasterType getOrCreateDisasterType(String name) {
        DisasterType existing = getDisasterType(name);
        if (existing != null) {
            return existing;
        }
        
        DisasterType newDisaster = new DisasterType(name);
        addDisasterType(newDisaster);
        return newDisaster;
    }

    public int getDisasterTypeCount() {
        return disasterTypes.size();
    }

    public void removeDisasterType(String name) {
        if (name != null) {
            disasterTypes.remove(DisasterType.normalize(name));
        }
    }
}
