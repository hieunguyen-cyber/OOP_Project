package com.humanitarian.devui.crawler;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.sentiment.EnhancedSentimentAnalyzer;
import com.humanitarian.devui.sentiment.SentimentAnalyzer;
import com.humanitarian.devui.sentiment.PythonCategoryClassifier;
import java.time.LocalDateTime;
import java.util.*;

public class MockDataCrawler implements DataCrawler {
    private final boolean initialized;
    private final Random random;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final PythonCategoryClassifier categoryClassifier;

    public MockDataCrawler() {
        this.initialized = true;
        this.random = new Random();
        this.sentimentAnalyzer = new EnhancedSentimentAnalyzer();
        this.sentimentAnalyzer.initialize();
        this.categoryClassifier = new PythonCategoryClassifier();
    }

    @Override
    public List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit) {
        List<Post> posts = new ArrayList<>();

        Map<ReliefItem.Category, String[]> categoryContents = new HashMap<>();
        
        categoryContents.put(ReliefItem.Category.CASH, new String[]{

            "💰 Cash assistance program launched today! Eligible families can register immediately.",
            "Families waiting for cash support. Registration process began this morning at 5 different centers.",
            "Initial cash distribution encountered system issues. Payment processing delayed by 2 days.",
            "⚠️ Many families complaining about long queues at registration centers. No clear timeline given.",
            "Cash program halted temporarily due to verification database problems. Frustrated families.",
            "Administrative delays blocking cash disbursement. Families report still no clear schedule.",
            "Budget constraints limiting cash amounts per family. Only 500k per household approved.",
            "Registration requirements complex and unclear. Families confused about eligibility.",
            "Cash delivery points insufficient. Only 3 centers in entire district of 50,000.",
            "Corruption allegations: Some officials allegedly skimming from relief funds.",

            "Good news: Cash disbursement began smoothly. Hundreds of families received support.",
            "Update: 500 families received emergency cash assistance in first week.",
            "Financial aid distribution ongoing at 8 new distribution centers this week.",
            "Community appreciates cash support. Families using assistance to buy essentials.",
            "Payment processing accelerating. Now handling 200+ families daily.",
            "Additional registration sites opened. Waiting times reduced from 4 hours to 45 minutes.",
            "Audit completed: No major irregularities found. Officials implementing improvements.",
            "Families receiving cash beginning small businesses. Positive economic impact.",
            "Second round of cash distribution planning to begin next month.",
            "Community leaders report satisfaction with cash program.",

            "Cash assistance proving effective. Families report improved ability to meet needs.",
            "Success story: Family with 5 children now able to rent safe house.",
            "Economic activity increasing. Local markets busier with cash-supported families.",
            "Cash program now distributed to 2000+ families. Community morale improving.",
            "Third round of payments completed successfully. 100% recipient satisfaction.",
            "Children returning to school thanks to cash assistance for uniforms.",
            "Healthcare access improved as families afford medicines and clinics.",
            "Entrepreneurs using grants to restart businesses and hire workers.",
            "Independence growing: Less dependent on aid, relying on commerce.",
            "Long-term impact visible: Housing improvements and business development."
        });

        categoryContents.put(ReliefItem.Category.MEDICAL, new String[]{

            "🚑 First confirmed cases reported in affected area. Healthcare workers on alert.",
            "Medical crisis alert: Only 1 doctor available for 10,000 residents.",
            "Hospitals overwhelmed with patients. Not enough beds for seriously ill.",
            "Medicine shortages critical: Antibiotics, painkillers completely out of stock.",
            "Patients dying from preventable diseases due to lack of basic medicine.",
            "❌ Healthcare system collapsing: Patients turned away from overflowing clinics.",
            "Disease outbreak spreading. Vaccination campaign delayed due to supply shortage.",
            "Medical staff exhausted: Working 18-hour shifts with minimal equipment.",
            "Patients queuing for hours. Some die waiting for treatment.",

            "✅ Mobile clinic visited 3 villages yesterday. Treated 150+ patients successfully.",
            "Great news: Vaccine shipment arrived! Healthcare staff vaccinating today.",
            "Medical support improving. Healthcare workers more dedicated now.",
            "100 patients treated at medical station today. Good care provided.",
            "Health services improving. Communities have better access to medicines.",
            "Medical supplies delivered but in limited quantities. Some needs unmet.",
            "Healthcare challenges: Staff shortages in remote regions still.",
            "Health situation stabilizing: Patient wait times reduced significantly.",
            "Vaccination program reaching remote villages successfully.",

            "🏥 Health metrics improving dramatically. Disease cases declining.",
            "Medical breakthroughs: New treatment protocols reducing mortality rates.",
            "❌ Medical services failing: People dying from treatable illnesses.",
            "Healthcare access improved: Families no longer fear medical emergencies.",
            "Doctor-patient ratio normalized. Now 1 doctor per 2,500 residents.",
            "Maternal and child health program: 99% of pregnant women receiving care.",
            "Disease eradication program 80% complete in district.",
            "Healthcare workers celebrated: Communities grateful for their dedication.",
            "Medical supply chain now reliable. No more critical shortages.",
            "Health education program: Communities understanding prevention better."
        });

        categoryContents.put(ReliefItem.Category.SHELTER, new String[]{

            "🏚️ Thousands of families homeless after disaster. Sleeping under trees.",
            "Urgent: Shelter crisis deepening. Winter approaching, families in danger.",
            "Rain last night: Many families got wet. Children crying from cold.",
            "Makeshift camps overcrowded and unsanitary. Diseases spreading rapidly.",
            "⚠️ Shelter shortage critical: 5,000 families with nowhere to sleep.",
            "Dangerous conditions: Families in temporary structures with no walls.",
            "Tent supplies insufficient: Only 1,000 tents for 5,000 homeless families.",
            "Families moving to schools and temples. Private spaces invaded.",
            "Winter will be deadly: Families unprepared for cold season.",

            "🏠 Temporary shelters constructed in 5 new locations. Good progress!",
            "Shelter setup completed. Families have safe places now.",
            "Families moving into temporary housing. Grateful for the support.",
            "Shelter quality acceptable. Provides adequate protection.",
            "Home reconstruction started. Communities see hope.",
            "Shelter construction slower than expected. Weather delays.",
            "Housing shortage persists. Many families still in inadequate structures.",
            "Construction pace accelerating. 500 shelters completed this month.",
            "Families training in building skills. Starting own constructions.",

            "🏘️ Shelter conditions improving. Repairs completed before rainy season.",
            "Permanent housing program halfway done. 2,000 houses rebuilt.",
            "❌ Housing crisis: Families still in dangerous conditions.",
            "Communities rebuilding. New structures stronger and safer.",
            "Schools reopening: Children back in classrooms, not shelters.",
            "Overcrowding resolved: Families have private spaces again.",
            "Home ownership returning: Families feeling stability.",
            "Construction quality improving: Houses built to new standards.",
            "Environmental impact minimal: Building using sustainable materials.",
            "Community spirit: Neighbors helping each other build homes."
        });

        categoryContents.put(ReliefItem.Category.FOOD, new String[]{

            "🚨 Food crisis: Markets destroyed. No food available in affected area.",
            "Hunger spreading fast. Children showing signs of malnutrition.",
            "Food prices skyrocketing: Normal families cannot afford meals.",
            "❌ Starvation threat: Families eating leaves and tree bark.",
            "Food shortage critical: Only wild fruits available.",
            "Rations insufficient: 1 cup rice per family per day.",
            "Children crying from hunger. Mothers rationing own food to feed kids.",
            "Famine conditions developing. Aid organizations overwhelmed.",
            "Deaths reported: Some elderly died from malnutrition.",

            "🍚 Food distribution completed successfully today. Everyone got supplies.",
            "Great: Abundance of food at distribution points. No shortages.",
            "Food quality good. Families satisfied with supplies.",
            "Vegetables and rice in good supply. Nutritious meals possible.",
            "Food distribution smooth. Community morale improving.",
            "Food shipments arriving regularly now. Supply chain established.",
            "Variety improving: Rice, vegetables, fish, oil distributed.",
            "Farmers restarting production: Crops growing in fields.",
            "Markets reopening: Some food available for purchase.",

            "Food shortage worsening: Ration cuts necessary.",
            "🌾 Food security improving: Harvest season approaching.",
            "Agricultural production 70% of normal levels now.",
            "Food prices stabilizing. Families can afford meals again.",
            "Community gardens productive: Families growing own vegetables.",
            "Fish farming program successful: Protein source improved.",
            "School feeding program: Children eating 2 meals daily.",
            "Food self-sufficiency target 60% of needs now.",
            "Commercial trade resuming: Markets thriving again.",
            "Nutrition indicators improving: Children gaining weight."
        });

        categoryContents.put(ReliefItem.Category.TRANSPORTATION, new String[]{

            "🚨 Roads destroyed: Communities completely isolated.",
            "Evacuation impossible: Heavy equipment blocked by debris.",
            "❌ Transport system collapsed: No vehicles available.",
            "Families stranded in affected areas. No way to escape.",
            "Medical emergencies: Pregnant women cannot reach hospitals.",
            "Supply trucks cannot reach remote villages.",
            "Fuel shortage: Remaining vehicles unable to operate.",
            "Bridges destroyed: 5 communities cut off completely.",
            "Walking 30km to reach nearest medical clinic.",

            "🚗 Fleet of vehicles assembled. Evacuation operations ready.",
            "Transportation system working smoothly. People reaching destinations efficiently.",
            "Vehicles available for medical and supply transport. Operations effective.",
            "Transport coordination excellent. Supply routes established.",
            "Community mobility improved significantly. People can access services.",
            "Main road cleared: Vehicles now reaching district center.",
            "Emergency routes established: Alternative paths available.",
            "Temporary bridges built: Crossing now possible.",
            "Vehicle maintenance network established.",

            "Limited vehicles still. Some areas not yet served.",
            "🚕 Fuel shortages impacting operations. Routes suspended.",
            "Transport system functioning normally. Regular schedules.",
            "Trade resumed: Commercial vehicles carrying goods daily.",
            "Public transportation restarted: Buses running schedules.",
            "Travel time reduced: Journey to regional city now 3 hours.",
            "Economic activity: Goods moving between communities.",
            "Vehicle fleet expanded: Private operators resuming service.",
            "Employment: Transport sector hiring drivers and mechanics.",
            "Connectivity restored: Communities no longer isolated."
        });

        String[] authors = {
            "Relief_Coordinator_1", "Community_Leader", "Affected_Resident",
            "Volunteer_Team", "Health_Worker", "NGO_Manager", "Local_Official",
            "Social_Worker", "Logistics_Staff", "Field_Officer",
            "Emergency_Responder", "Aid_Worker", "Humanitarian_Staff"
        };

        LocalDateTime baseTime = LocalDateTime.now().minusDays(90);
        int postIndex = 0;

        for (ReliefItem.Category category : categoryContents.keySet()) {
            String[] contents = categoryContents.get(category);
            int postsPerCategory = Math.max(limit / 5, 16);

            for (int i = 0; i < postsPerCategory && postIndex < limit; i++) {

                int dayOffset = random.nextInt(90);
                int hour = 6 + random.nextInt(16);
                int minute = random.nextInt(60);
                
                LocalDateTime postTime = baseTime.plusDays(dayOffset)
                    .plusHours(hour)
                    .plusMinutes(minute);

                double dayProgress = dayOffset / 90.0;
                double contentProgress = dayProgress * (contents.length - 1);
                int contentIndex = Math.min((int) contentProgress, contents.length - 1);
                
                String content = contents[contentIndex];

                ReliefItem reliefItem = new ReliefItem(
                    category,
                    "Relief: " + category.getDisplayName(),
                    random.nextInt(4) + 2
                );

                YouTubePost post = new YouTubePost(
                    "POST_MOCK_" + category.name() + "_" + System.currentTimeMillis() + "_" + postIndex,
                    content,
                    postTime,
                    authors[postIndex % authors.length],
                    "CHANNEL_" + (postIndex % 3 == 0 ? "OFFICIAL" : "COMMUNITY")
                );

                Sentiment analyzedSentiment = sentimentAnalyzer.analyzeSentiment(content);
                post.setSentiment(analyzedSentiment);
                post.setReliefItem(reliefItem);
                post.setLikes(random.nextInt(800) + 30);
                
                List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
                if (!disasterNames.isEmpty()) {
                    String randomDisaster = disasterNames.get(random.nextInt(disasterNames.size()));
                    DisasterType disasterType = DisasterManager.getInstance().findDisasterType(randomDisaster);
                    if (disasterType != null) {
                        post.setDisasterType(disasterType);
                    }
                }

                addMockComments(post, category, contentIndex);

                posts.add(post);
                postIndex++;
            }
        }

        return posts;
    }

    private void addMockComments(YouTubePost post, ReliefItem.Category category, int contentIndex) {
        Map<ReliefItem.Category, String[]> commentsByCategory = new HashMap<>();

        commentsByCategory.put(ReliefItem.Category.CASH, new String[]{
            "Finally getting some help! This will make a real difference.",
            "The process was quick and fair. Very grateful.",
            "When will the second round of payments happen?",
            "Many families still haven't received anything. Please hurry!",
            "The amount is too small. How are we supposed to survive?"
        });

        commentsByCategory.put(ReliefItem.Category.MEDICAL, new String[]{
            "The doctors were so caring and professional. Thank you!",
            "Treatment was excellent. Much better now!",
            "We waited hours but got good care eventually.",
            "Not enough medicine for everyone. My child still sick.",
            "Critical medicines missing! People are dying needlessly."
        });

        commentsByCategory.put(ReliefItem.Category.SHELTER, new String[]{
            "Shelter is safe and clean. Really helpful.",
            "The temporary housing is good quality. We feel protected.",
            "Still waiting for our shelter assignment. Hope it's soon.",
            "The shelters are starting to show problems. Roof leaks reported.",
            "Living conditions are unbearable. We need permanent solutions!"
        });

        commentsByCategory.put(ReliefItem.Category.FOOD, new String[]{
            "Good quality food received. Families are eating well now.",
            "Great supply of fresh vegetables this time!",
            "Some items were okay but we need more variety.",
            "Food quantity is decreasing. Rations being cut.",
            "Children are malnourished. Food aid is critical!"
        });

        commentsByCategory.put(ReliefItem.Category.TRANSPORTATION, new String[]{
            "Transport services are reliable and well-organized!",
            "Got to the hospital quickly thanks to transport support.",
            "Waiting times are getting longer. Need more vehicles.",
            "Many people can't get transport. Some areas have no service.",
            "Transport system has failed. Supplies not reaching us!"
        });

        String[] commentAuthors = {"User_A", "User_B", "User_C", "Resident_X", "Community_Member", "Local_Voice"};

        int commentCount = random.nextInt(3) + 1;
        String[] categoryComments = commentsByCategory.get(category);

        for (int i = 0; i < commentCount; i++) {
            String commentText = categoryComments[random.nextInt(categoryComments.length)];

            Comment comment = new Comment(
                "CMT_MOCK_" + System.currentTimeMillis() + "_" + i,
                post.getPostId(),
                commentText,
                LocalDateTime.now().minusHours(random.nextInt(48)),
                commentAuthors[i % commentAuthors.length]
            );

            Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(commentText);
            comment.setSentiment(sentiment);
            
            ReliefItem.Category classifiedCategory = categoryClassifier.classifyText(commentText);
            if (classifiedCategory != null) {
                comment.setReliefItem(new ReliefItem(classifiedCategory, "ML-classified (facebook/bart-large-mnli)", 3));
            } else {

                comment.setReliefItem(new ReliefItem(category, "From parent post category (API fallback)", 2));
            }
            
            post.addComment(comment);
        }
    }

    @Override
    public String getCrawlerName() {
        return "MockDataCrawler";
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void shutdown() {
        System.out.println("MockDataCrawler shutdown");
    }
}
