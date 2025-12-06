package com.humanitarian.logistics.preprocessor;

import com.humanitarian.logistics.model.Post;
import com.humanitarian.logistics.model.ReliefItem;
import java.util.*;
import java.util.regex.Pattern;

public class ReliefItemClassifier {
    private final Map<ReliefItem.Category, List<Pattern>> categoryPatterns;
    private final TextPreprocessor textPreprocessor;

    public ReliefItemClassifier() {
        this.textPreprocessor = new BasicTextPreprocessor();
        this.categoryPatterns = new HashMap<>();
        initializeCategoryPatterns();
    }

    private void initializeCategoryPatterns() {

        List<Pattern> cashPatterns = Arrays.asList(
                Pattern.compile(".*\\b(cash|money|financial aid|economic support)\\b.*", Pattern.CASE_INSENSITIVE),
                Pattern.compile(".*\\b(subsidy|funds|grants|allowance)\\b.*", Pattern.CASE_INSENSITIVE)
        );
        categoryPatterns.put(ReliefItem.Category.CASH, cashPatterns);

        List<Pattern> medicalPatterns = Arrays.asList(
                Pattern.compile(".*\\b(medical|healthcare|hospital|doctor|medicine|doctor|ambulance)\\b.*", Pattern.CASE_INSENSITIVE),
                Pattern.compile(".*\\b(treatment|therapy|vaccine|health|nursing)\\b.*", Pattern.CASE_INSENSITIVE)
        );
        categoryPatterns.put(ReliefItem.Category.MEDICAL, medicalPatterns);

        List<Pattern> shelterPatterns = Arrays.asList(
                Pattern.compile(".*\\b(shelter|housing|house|home|accommodation|roof)\\b.*", Pattern.CASE_INSENSITIVE),
                Pattern.compile(".*\\b(tent|temporary|refugee|displaced)\\b.*", Pattern.CASE_INSENSITIVE)
        );
        categoryPatterns.put(ReliefItem.Category.SHELTER, shelterPatterns);

        List<Pattern> foodPatterns = Arrays.asList(
                Pattern.compile(".*\\b(food|meal|rice|water|drinking|bread|grain)\\b.*", Pattern.CASE_INSENSITIVE),
                Pattern.compile(".*\\b(nutrition|hungry|starving|eat|provisions)\\b.*", Pattern.CASE_INSENSITIVE)
        );
        categoryPatterns.put(ReliefItem.Category.FOOD, foodPatterns);

        List<Pattern> transportPatterns = Arrays.asList(
                Pattern.compile(".*\\b(transportation|vehicle|car|bus|truck|transport)\\b.*", Pattern.CASE_INSENSITIVE),
                Pattern.compile(".*\\b(mobility|road|travel|communication|access)\\b.*", Pattern.CASE_INSENSITIVE)
        );
        categoryPatterns.put(ReliefItem.Category.TRANSPORTATION, transportPatterns);
    }

    public ReliefItem.Category classifyText(String text) {
        String preprocessed = textPreprocessor.preprocess(text);

        for (Map.Entry<ReliefItem.Category, List<Pattern>> entry : categoryPatterns.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(preprocessed).matches()) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    public void classifyPost(Post post) {
        if (post.getReliefItem() == null) {
            ReliefItem.Category category = classifyText(post.getContent());
            if (category != null) {
                post.setReliefItem(new ReliefItem(category, "Auto-classified", 3));
            }
        }
    }

    public List<Pattern> getPatterns(ReliefItem.Category category) {
        return categoryPatterns.getOrDefault(category, Collections.emptyList());
    }

    public void addPattern(ReliefItem.Category category, Pattern pattern) {
        categoryPatterns.computeIfAbsent(category, k -> new ArrayList<>()).add(pattern);
    }
}
