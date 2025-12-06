package com.humanitarian.devui.analysis;

import com.humanitarian.devui.model.Post;
import java.util.List;
import java.util.Map;

public interface AnalysisModule {
    
    Map<String, Object> analyze(List<Post> posts);

    String getModuleName();

    String getDescription();
}
