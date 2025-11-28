package com.humanitarian.devui.database;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.ui.Model;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Random;

/**
 * Utility class to load predefined database content.
 * Provides fixed, immutable reference data with diverse dates and relief types.
 * - YAGI: September 2024
 * - MATMO: June-July 2025
 * - FLOOD: November-December 2024
 * - DISASTER: January-March 2025
 */
public class DatabaseLoader {
    
    private static final Random random = new Random();
    
    public static void loadOurDatabase(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        model.getPosts().clear();
        loadComprehensiveData(model);
    }
    
    private static LocalDateTime randomDateInMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int dayOfMonth = random.nextInt(yearMonth.lengthOfMonth()) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    }
    
    private static void loadComprehensiveData(Model model) {
        int postCounter = 1;
        
        // ===== YAGI (September 2024) - 8 posts =====
        DisasterType yagi = DisasterManager.getInstance().findDisasterType("yagi");
        
        model.addPost(createPost(postCounter++, "Emergency alert: Typhoon Yagi approaching. Evacuation centers opening immediately. #yagi", 
            randomDateInMonth(2024, 9), "Disaster Management", "yagi", yagi, 
            Sentiment.SentimentType.NEGATIVE, 0.9, ReliefItem.Category.SHELTER, "Evacuation alert"));
        
        model.addPost(createPost(postCounter++, "Medical teams deployed for #yagi response. Hospitals prepared for mass casualty operations. Critical supplies in place.", 
            randomDateInMonth(2024, 9), "Health Ministry", "yagi", yagi,
            Sentiment.SentimentType.POSITIVE, 0.85, ReliefItem.Category.MEDICAL, "Medical preparedness"));
        
        model.addPost(createPost(postCounter++, "Shelter distribution for #yagi evacuees. 500+ families safely relocated to relief centers with safety measures.", 
            randomDateInMonth(2024, 9), "Red Cross", "yagi", yagi,
            Sentiment.SentimentType.POSITIVE, 0.88, ReliefItem.Category.SHELTER, "Relief shelters"));
        
        model.addPost(createPost(postCounter++, "Food distribution program launched for #yagi affected areas. Daily meals guaranteed for survivors.", 
            randomDateInMonth(2024, 9), "Food Program", "yagi", yagi,
            Sentiment.SentimentType.POSITIVE, 0.85, ReliefItem.Category.FOOD, "Food distribution"));
        
        model.addPost(createPost(postCounter++, "Transport assistance for #yagi survivors. Free shuttles connecting to hospitals and relief centers.", 
            randomDateInMonth(2024, 9), "Transport Ministry", "yagi", yagi,
            Sentiment.SentimentType.POSITIVE, 0.82, ReliefItem.Category.TRANSPORTATION, "Transport service"));
        
        model.addPost(createPost(postCounter++, "Cash assistance now available for #yagi disaster. Visit banks with ID for withdrawal of emergency funds.", 
            randomDateInMonth(2024, 9), "Finance Ministry", "yagi", yagi,
            Sentiment.SentimentType.POSITIVE, 0.85, ReliefItem.Category.CASH, "Cash transfer"));
        
        model.addPost(createPost(postCounter++, "Recovery phase: Livelihood support and job training programs for #yagi survivors begin next month.", 
            randomDateInMonth(2024, 10), "Development", "yagi", yagi,
            Sentiment.SentimentType.POSITIVE, 0.81, ReliefItem.Category.CASH, "Livelihood support"));
        
        model.addPost(createPost(postCounter++, "Infrastructure assessment complete for #yagi affected zones. Reconstruction planning underway.", 
            randomDateInMonth(2024, 10), "Infrastructure", "yagi", yagi,
            Sentiment.SentimentType.POSITIVE, 0.80, ReliefItem.Category.TRANSPORTATION, "Infrastructure"));
        
        // ===== FLOOD (November-December 2024) - 9 posts =====
        DisasterType flood = DisasterManager.getInstance().findDisasterType("flood");
        
        model.addPost(createPost(postCounter++, "Heavy rainfall causes severe flooding in lowland areas. Evacuation orders issued for #flood zones. #urgent", 
            randomDateInMonth(2024, 11), "Disaster Management", "flood", flood,
            Sentiment.SentimentType.NEGATIVE, 0.9, ReliefItem.Category.SHELTER, "Evacuation alert"));
        
        model.addPost(createPost(postCounter++, "Evacuation successful for #flood zones. 4000+ people safely relocated to higher grounds with belongings.", 
            randomDateInMonth(2024, 11), "Emergency Services", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.87, ReliefItem.Category.SHELTER, "Evacuation"));
        
        model.addPost(createPost(postCounter++, "Emergency food distribution in #flood villages. Priority given to children, elderly, and vulnerable groups.", 
            randomDateInMonth(2024, 11), "Food Aid", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.89, ReliefItem.Category.FOOD, "Food packets"));
        
        model.addPost(createPost(postCounter++, "Mobile health units deployed in #flood areas. Free medical services and disease prevention education.", 
            randomDateInMonth(2024, 11), "Health Department", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.85, ReliefItem.Category.MEDICAL, "Medical support"));
        
        model.addPost(createPost(postCounter++, "Temporary shelters erected in #flood relief camps. 2000 capacity centers with sanitation facilities.", 
            randomDateInMonth(2024, 11), "Shelter Program", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.86, ReliefItem.Category.SHELTER, "Relief centers"));
        
        model.addPost(createPost(postCounter++, "Cash support for #flood affected families. 6000 families receiving emergency financial assistance.", 
            randomDateInMonth(2024, 11), "Finance", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.83, ReliefItem.Category.CASH, "Cash aid"));
        
        model.addPost(createPost(postCounter++, "Supply routes established for #flood disaster. Transport assistance ensuring relief reaches remote villages.", 
            randomDateInMonth(2024, 12), "Logistics", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.84, ReliefItem.Category.TRANSPORTATION, "Supply delivery"));
        
        model.addPost(createPost(postCounter++, "Water and sanitation programs in #flood zones. Clean water distribution and hygiene education ongoing.", 
            randomDateInMonth(2024, 12), "WASH Program", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.82, ReliefItem.Category.MEDICAL, "WASH services"));
        
        model.addPost(createPost(postCounter++, "Infrastructure restoration in #flood areas. Roads and bridges being repaired for access.", 
            randomDateInMonth(2024, 12), "Public Works", "flood", flood,
            Sentiment.SentimentType.POSITIVE, 0.80, ReliefItem.Category.TRANSPORTATION, "Infrastructure"));
        
        // ===== MATMO (June-July 2025) - 9 posts =====
        DisasterType matmo = DisasterManager.getInstance().findDisasterType("matmo");
        
        model.addPost(createPost(postCounter++, "Water contamination emergency reported in #matmo region. Health department issues public alert. #health", 
            randomDateInMonth(2025, 6), "Health Department", "matmo", matmo,
            Sentiment.SentimentType.NEGATIVE, 0.88, ReliefItem.Category.MEDICAL, "Health emergency"));
        
        model.addPost(createPost(postCounter++, "Food distribution centers established in #matmo region. Serving 3000+ people daily with nutrition support.", 
            randomDateInMonth(2025, 6), "Food Supply", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.90, ReliefItem.Category.FOOD, "Food distribution"));
        
        model.addPost(createPost(postCounter++, "Medical clinic expanded for #matmo survivors. Free vaccination drive and health screening now available.", 
            randomDateInMonth(2025, 6), "Health Services", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.87, ReliefItem.Category.MEDICAL, "Medical services"));
        
        model.addPost(createPost(postCounter++, "Shelter construction for #matmo families. 1200+ temporary shelters completed and occupied.", 
            randomDateInMonth(2025, 6), "Housing Program", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.88, ReliefItem.Category.SHELTER, "Shelter facilities"));
        
        model.addPost(createPost(postCounter++, "Cash disbursement for #matmo disaster. 8000 families receiving financial assistance this month.", 
            randomDateInMonth(2025, 6), "Finance Ministry", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.85, ReliefItem.Category.CASH, "Cash transfer"));
        
        model.addPost(createPost(postCounter++, "Transport network restored for #matmo region. Free shuttles connecting to medical and market centers.", 
            randomDateInMonth(2025, 6), "Transport", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.83, ReliefItem.Category.TRANSPORTATION, "Transport"));
        
        model.addPost(createPost(postCounter++, "Vocational training programs launching for #matmo survivors. Courses in agriculture and handicrafts.", 
            randomDateInMonth(2025, 7), "Training Center", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.81, ReliefItem.Category.CASH, "Skill training"));
        
        model.addPost(createPost(postCounter++, "Livelihood restoration projects for #matmo communities. Employment opportunities created.", 
            randomDateInMonth(2025, 7), "Development", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.82, ReliefItem.Category.CASH, "Employment"));
        
        model.addPost(createPost(postCounter++, "Community recovery assessment for #matmo region. Long-term support planning in progress.", 
            randomDateInMonth(2025, 7), "Recovery", "matmo", matmo,
            Sentiment.SentimentType.POSITIVE, 0.80, ReliefItem.Category.CASH, "Recovery plan"));
        
        // ===== DISASTER Multi-sector (January-March 2025) - 5 posts =====
        DisasterType disaster = DisasterManager.getInstance().findDisasterType("disaster");
        
        model.addPost(createPost(postCounter++, "Multi-sector disaster response coordination meeting. Medical, food, shelter and transport aligned. #coordination", 
            randomDateInMonth(2025, 1), "Coordinator", "disaster", disaster,
            Sentiment.SentimentType.POSITIVE, 0.84, ReliefItem.Category.MEDICAL, "Multi-sector"));
        
        model.addPost(createPost(postCounter++, "Comprehensive relief operation underway. All essential services provided simultaneously to affected communities.", 
            randomDateInMonth(2025, 2), "Program Manager", "disaster", disaster,
            Sentiment.SentimentType.POSITIVE, 0.88, ReliefItem.Category.FOOD, "Relief ops"));
        
        model.addPost(createPost(postCounter++, "Recovery and reconstruction phase begins. Community building initiatives for affected regions.", 
            randomDateInMonth(2025, 2), "Recovery Officer", "disaster", disaster,
            Sentiment.SentimentType.POSITIVE, 0.82, ReliefItem.Category.CASH, "Reconstruction"));
        
        model.addPost(createPost(postCounter++, "Long-term support programs for disaster survivors. Sustainability and resilience focus.", 
            randomDateInMonth(2025, 3), "LTRC Manager", "disaster", disaster,
            Sentiment.SentimentType.POSITIVE, 0.81, ReliefItem.Category.SHELTER, "Long-term"));
        
        model.addPost(createPost(postCounter++, "Community resilience building for disaster-prone areas. Preparedness training and warning systems.", 
            randomDateInMonth(2025, 3), "Resilience Coordinator", "disaster", disaster,
            Sentiment.SentimentType.POSITIVE, 0.83, ReliefItem.Category.CASH, "Preparedness"));
        
        System.out.println("\n✓ DATABASE LOADED SUCCESSFULLY");
        System.out.println("  YAGI (Sep 2024): 8 posts");
        System.out.println("  FLOOD (Nov-Dec 2024): 9 posts");
        System.out.println("  MATMO (Jun-Jul 2025): 9 posts");
        System.out.println("  DISASTER (Jan-Mar 2025): 5 posts");
        System.out.println("  TOTAL: 31 posts with diverse dates & relief types");
    }
    
    private static YouTubePost createPost(int id, String content, LocalDateTime date,
                                          String author, String keyword, DisasterType disaster,
                                          Sentiment.SentimentType sentiment, double confidence,
                                          ReliefItem.Category category, String description) {
        YouTubePost post = new YouTubePost(
            "db_" + id,
            content,
            date,
            author,
            "page_" + id
        );
        if (disaster != null) post.setDisasterType(disaster);
        post.setDisasterKeyword(keyword);
        post.setSentiment(new Sentiment(sentiment, confidence, content));
        post.setReliefItem(new ReliefItem(category, description, 4));
        return post;
    }
}
