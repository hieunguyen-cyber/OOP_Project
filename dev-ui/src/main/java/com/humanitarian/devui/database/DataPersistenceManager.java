package com.humanitarian.devui.database;

import com.humanitarian.devui.model.*;
import java.io.*;
import java.util.*;

public class DataPersistenceManager {
    private String postsFile;
    private String disastersFile;
    
    private String getDataDir() {

        String currentDir = new File(".").getAbsolutePath();
        File dataDir;
        
        if (currentDir.endsWith("dev-ui")) {
            dataDir = new File("data");
        } else {

            dataDir = new File("dev-ui/data");
        }
        
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir.getAbsolutePath();
    }

    public DataPersistenceManager() {
        String dataDir = getDataDir();
        this.postsFile = dataDir + "/posts.dat";
        this.disastersFile = dataDir + "/disasters.dat";

    }

    public void savePosts(List<Post> posts) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(postsFile))) {
            oos.writeObject(new ArrayList<>(posts));
            System.out.println("✓ Posts saved: " + posts.size() + " items");
        } catch (IOException e) {
            System.err.println("Error saving posts: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Post> loadPosts() {
        File file = new File(postsFile);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(postsFile))) {
            List<Post> posts = (List<Post>) ois.readObject();
            System.out.println("✓ Posts loaded: " + posts.size() + " items");
            return posts;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveDisasters(DisasterManager manager) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(disastersFile))) {

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

    @SuppressWarnings("unchecked")
    public void loadDisasters(DisasterManager manager) {
        File file = new File(disastersFile);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(disastersFile))) {
            Map<String, DisasterType> customDisasters = (Map<String, DisasterType>) ois.readObject();
            for (DisasterType disaster : customDisasters.values()) {
                manager.addDisasterType(disaster);
            }
            System.out.println("✓ Custom disaster types loaded: " + customDisasters.size() + " types");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading disasters: " + e.getMessage());
        }
    }

    public void clearAllData() {
        File postsFileObj = new File(postsFile);
        File disastersFileObj = new File(disastersFile);
        
        if (postsFileObj.exists()) {
            postsFileObj.delete();
        }
        if (disastersFileObj.exists()) {
            disastersFileObj.delete();
        }
        
        System.out.println("✓ All persistent data cleared");
    }

    public boolean hasSavedData() {
        return new File(postsFile).exists();
    }

    public String getDataDirectory() {
        return getDataDir();
    }
}
