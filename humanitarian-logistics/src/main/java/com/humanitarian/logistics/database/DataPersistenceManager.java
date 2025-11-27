package com.humanitarian.logistics.database;

import com.humanitarian.logistics.model.*;
import java.io.*;
import java.util.*;

/**
 * Manages persistent storage of posts and disaster types.
 * Saves/loads data to/from local cache files.
 */
public class DataPersistenceManager {
    private static final String DATA_DIR = "data";
    private static final String POSTS_FILE = DATA_DIR + "/posts.dat";
    private static final String DISASTERS_FILE = DATA_DIR + "/disasters.dat";

    public DataPersistenceManager() {
        // Create data directory if it doesn't exist
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Save posts to persistent storage
     */
    public void savePosts(List<Post> posts) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(POSTS_FILE))) {
            oos.writeObject(new ArrayList<>(posts));
            System.out.println("✓ Posts saved: " + posts.size() + " items");
        } catch (IOException e) {
            System.err.println("Error saving posts: " + e.getMessage());
        }
    }

    /**
     * Load posts from persistent storage
     */
    @SuppressWarnings("unchecked")
    public List<Post> loadPosts() {
        File file = new File(POSTS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(POSTS_FILE))) {
            List<Post> posts = (List<Post>) ois.readObject();
            System.out.println("✓ Posts loaded: " + posts.size() + " items");
            return posts;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Save disaster types to persistent storage
     */
    public void saveDisasters(DisasterManager manager) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DISASTERS_FILE))) {
            // Save all non-default disaster types
            Set<String> defaultDisasters = new HashSet<>(Arrays.asList(
                "yagi", "matmo", "bualo", "koto", "fung-wong"
            ));
            
            Map<String, DisasterType> customDisasters = new HashMap<>();
            for (String name : manager.getAllDisasterNames()) {
                if (!defaultDisasters.contains(name.toLowerCase())) {
                    customDisasters.put(name, manager.getDisasterType(name));
                }
            }
            
            oos.writeObject(customDisasters);
            System.out.println("✓ Disaster types saved: " + customDisasters.size() + " custom types");
        } catch (IOException e) {
            System.err.println("Error saving disasters: " + e.getMessage());
        }
    }

    /**
     * Load custom disaster types from persistent storage
     */
    @SuppressWarnings("unchecked")
    public void loadDisasters(DisasterManager manager) {
        File file = new File(DISASTERS_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DISASTERS_FILE))) {
            Map<String, DisasterType> customDisasters = (Map<String, DisasterType>) ois.readObject();
            for (DisasterType disaster : customDisasters.values()) {
                manager.addDisasterType(disaster);
            }
            System.out.println("✓ Custom disaster types loaded: " + customDisasters.size() + " types");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading disasters: " + e.getMessage());
        }
    }

    /**
     * Clear all persistent data
     */
    public void clearAllData() {
        File postsFile = new File(POSTS_FILE);
        File disastersFile = new File(DISASTERS_FILE);
        
        if (postsFile.exists()) {
            postsFile.delete();
        }
        if (disastersFile.exists()) {
            disastersFile.delete();
        }
        
        System.out.println("✓ All persistent data cleared");
    }

    /**
     * Check if saved data exists
     */
    public boolean hasSavedData() {
        return new File(POSTS_FILE).exists();
    }

    /**
     * Get data directory path
     */
    public String getDataDirectory() {
        return DATA_DIR;
    }
}
