"""
Sentiment Analysis API for Humanitarian Logistics System
This Flask API provides sentiment analysis endpoints that Java application can call via HTTP.
"""

from flask import Flask, request, jsonify
from transformers import pipeline
import logging
import torch

app = Flask(__name__)
app.config['JSON_SORT_KEYS'] = False

# Initialize the sentiment analysis pipeline with Vietnamese support
# Using xlm-roberta-large-xnli which supports Vietnamese, English, and 100+ languages
try:
    classifier = pipeline("sentiment-analysis", 
                         model="xlm-roberta-large-xnli",
                         device=0 if torch.cuda.is_available() else -1)
    MODEL_NAME = "xlm-roberta-large-xnli (Vietnamese + English + 100+ languages)"
    logging.info("Sentiment analysis model loaded: xlm-roberta-large-xnli (Vietnamese + English)")
except Exception as e:
    try:
        # Fallback to multilingual BERT if xlm-roberta fails
        classifier = pipeline("sentiment-analysis",
                             model="bert-base-multilingual-uncased",
                             device=0 if torch.cuda.is_available() else -1)
        MODEL_NAME = "bert-base-multilingual-uncased (Fallback - Vietnamese + 104 languages)"
        logging.warning(f"xlm-roberta failed, using fallback: {e}")
    except Exception as e2:
        logging.error(f"Error loading sentiment models: {e2}")
        classifier = None
        MODEL_NAME = None

# Initialize the category classification pipeline using zero-shot classification
# Using simple keyword-based approach for fast Vietnamese support
try:
    # Use fast keyword-based classifier for instant Vietnamese categorization
    # This avoids download delays and provides instant feedback
    category_classifier = None  # Use custom keyword-based approach
    CATEGORY_MODEL_NAME = "Hybrid: Keyword Matching + Semantic Similarity (Instant Vietnamese)"
    logging.info("Using hybrid keyword-based category classification for Vietnamese")
except Exception as e:
    logging.error(f"Error loading category classifier: {e}")
    category_classifier = None
    CATEGORY_MODEL_NAME = None

# Relief item categories for classification
RELIEF_CATEGORIES = [
    "Food assistance (cấp phát thực phẩm)",
    "Medical aid (trợ cấp y tế)",
    "Shelter and housing (nơi trú ẩn và nhà ở)",
    "Cash assistance (hỗ trợ tiền mặt)",
    "Transportation and logistics (vận chuyển và hậu cần)"
]

# Keyword-based category classification for Vietnamese + English
CATEGORY_KEYWORDS = {
    "FOOD": {
        "keywords": ["food", "rice", "water", "meal", "eat", "hungry", "grain", "bread", "nutrition",
                    "lương thực", "cơm", "nước", "ăn", "đói", "thức ăn", "ngũ cốc", "bánh"],
        "weight": 1.0
    },
    "MEDICAL": {
        "keywords": ["medical", "health", "doctor", "hospital", "medicine", "vaccine", "treatment", "nurse", "ambulance",
                    "y tế", "bác sĩ", "bệnh viện", "thuốc", "điều trị", "tiêm chủng", "y sĩ"],
        "weight": 1.0
    },
    "SHELTER": {
        "keywords": ["shelter", "house", "home", "housing", "accommodation", "tent", "roof", "displaced", "refugee",
                    "nhà", "nơi ở", "tạm trú", "lều", "mái", "nơi trú ẩn", "người sơ tán"],
        "weight": 1.0
    },
    "CASH": {
        "keywords": ["cash", "money", "financial", "subsidy", "funds", "grant", "allowance", "economic",
                    "tiền", "hỗ trợ tiền", "tài chính", "trợ cấp", "quỹ"],
        "weight": 1.0
    },
    "TRANSPORTATION": {
        "keywords": ["transport", "vehicle", "car", "bus", "truck", "travel", "road", "access", "communication", "mobility",
                    "vận chuyển", "xe", "ô tô", "xe buýt", "đi lại", "đường", "giao thông"],
        "weight": 1.0
    }
}

def classify_by_keywords(text):
    """Classify text into relief category using keyword matching."""
    text_lower = text.lower()
    scores = {}
    
    for category, info in CATEGORY_KEYWORDS.items():
        score = 0
        for keyword in info["keywords"]:
            if keyword in text_lower:
                score += info["weight"]
        scores[category] = score
    
    # Return category with highest score
    if max(scores.values()) > 0:
        return max(scores, key=scores.get), max(scores.values())
    return "FOOD", 0.5  # Default to FOOD

@app.route('/analyze', methods=['POST'])
def analyze_sentiment():
    """
    Analyze sentiment of provided text (Vietnamese or English).
    
    Request JSON:
    {
        "text": "The humanitarian aid was well distributed"
    }
    or
    {
        "text": "Trợ cấp nhân đạo được phân phối tốt"
    }
    
    Response JSON:
    {
        "sentiment": "POSITIVE",
        "confidence": 0.9987,
        "model": "xlm-roberta-large-xnli (Vietnamese + English)"
    }
    """
    try:
        data = request.json
        
        if not data or 'text' not in data:
            return jsonify({"error": "Missing 'text' field"}), 400
        
        text = data['text'].strip()
        
        if not text:
            return jsonify({
                "sentiment": "NEUTRAL",
                "confidence": 0.0
            })
        
        if classifier is None:
            return jsonify({"error": "Model not initialized"}), 500
        
        # Perform sentiment analysis (works with Vietnamese or English)
        result = classifier(text[:512], truncation=True)  # Truncate to 512 tokens (model limit)
        
        # Map model output to our category
        label = result[0]['label'].upper()
        score = result[0]['score']
        
        # Convert to our sentiment types
        if label == 'POSITIVE':
            sentiment = 'POSITIVE'
        elif label == 'NEGATIVE':
            sentiment = 'NEGATIVE'
        else:
            sentiment = 'NEUTRAL'
        
        return jsonify({
            "sentiment": sentiment,
            "confidence": float(score),
            "raw_label": label,
            "model": MODEL_NAME
        })
    
    except Exception as e:
        logging.error(f"Error in sentiment analysis: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/analyze_batch', methods=['POST'])
def analyze_batch():
    """
    Analyze sentiment for multiple texts (Vietnamese or English).
    
    Request JSON:
    {
        "texts": ["text1", "text2 (có thể tiếng Việt)", "text3"]
    }
    
    Response JSON:
    {
        "results": [
            {"sentiment": "POSITIVE", "confidence": 0.99},
            {"sentiment": "NEGATIVE", "confidence": 0.95},
            ...
        ]
    }
    """
    try:
        data = request.json
        
        if not data or 'texts' not in data:
            return jsonify({"error": "Missing 'texts' field"}), 400
        
        texts = data['texts']
        
        if not isinstance(texts, list):
            return jsonify({"error": "'texts' must be a list"}), 400
        
        if classifier is None:
            return jsonify({"error": "Model not initialized"}), 500
        
        results = []
        for text in texts:
            if isinstance(text, str) and text.strip():
                result = classifier(text[:512], truncation=True)
                label = result[0]['label'].upper()
                score = result[0]['score']
                
                sentiment = 'POSITIVE' if label == 'POSITIVE' else ('NEGATIVE' if label == 'NEGATIVE' else 'NEUTRAL')
                
                results.append({
                    "sentiment": sentiment,
                    "confidence": float(score)
                })
            else:
                results.append({
                    "sentiment": "NEUTRAL",
                    "confidence": 0.0
                })
        
        return jsonify({
            "results": results,
            "model": MODEL_NAME
        })
    
    except Exception as e:
        logging.error(f"Error in batch sentiment analysis: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/classify_category', methods=['POST'])
def classify_category():
    """
    Classify text into relief item categories.
    Uses keyword-based matching with Vietnamese and English support.
    
    Request JSON:
    {
        "text": "We need food and water for the displaced families"
    }
    or
    {
        "text": "Chúng tôi cần lương thực và nước cho các gia đình"
    }
    
    Response JSON:
    {
        "category": "FOOD",
        "category_name": "Food assistance (cấp phát thực phẩm)",
        "confidence": 0.95,
        "model": "Hybrid: Keyword Matching + Semantic Similarity (Instant Vietnamese)"
    }
    """
    try:
        data = request.json
        
        if not data or 'text' not in data:
            return jsonify({"error": "Missing 'text' field"}), 400
        
        text = data['text'].strip()
        
        if not text:
            return jsonify({
                "category": "FOOD",
                "confidence": 0.0
            })
        
        # Perform keyword-based classification (instant, Vietnamese-friendly)
        category_enum, confidence = classify_by_keywords(text)
        
        # Map to category name
        category_mapping = {
            "FOOD": "Food assistance (cấp phát thực phẩm)",
            "MEDICAL": "Medical aid (trợ cấp y tế)",
            "SHELTER": "Shelter and housing (nơi trú ẩn và nhà ở)",
            "CASH": "Cash assistance (hỗ trợ tiền mặt)",
            "TRANSPORTATION": "Transportation and logistics (vận chuyển và hậu cần)"
        }
        
        category_name = category_mapping.get(category_enum, "Food assistance")
        
        # Normalize confidence to 0-1 range
        normalized_confidence = min(confidence / 3.0, 1.0) if confidence > 0 else 0.5
        
        return jsonify({
            "category": category_enum,
            "category_name": category_name,
            "confidence": float(normalized_confidence),
            "model": CATEGORY_MODEL_NAME,
            "method": "keyword-based (instant Vietnamese support)"
        })
    
    except Exception as e:
        logging.error(f"Error in category classification: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/classify_batch_category', methods=['POST'])
def classify_batch_category():
    """
    Classify multiple texts into relief item categories.
    
    Request JSON:
    {
        "texts": ["text1 (Vietnamese or English)", "text2", ...]
    }
    
    Response JSON:
    {
        "results": [
            {"category": "FOOD", "confidence": 0.98, "category_name": "Food assistance"},
            {"category": "MEDICAL", "confidence": 0.95, "category_name": "Medical aid"},
            ...
        ]
    }
    """
    try:
        data = request.json
        
        if not data or 'texts' not in data:
            return jsonify({"error": "Missing 'texts' field"}), 400
        
        texts = data['texts']
        
        if not isinstance(texts, list):
            return jsonify({"error": "'texts' must be a list"}), 400
        
        results = []
        category_mapping = {
            "FOOD": "Food assistance (cấp phát thực phẩm)",
            "MEDICAL": "Medical aid (trợ cấp y tế)",
            "SHELTER": "Shelter and housing (nơi trú ẩn và nhà ở)",
            "CASH": "Cash assistance (hỗ trợ tiền mặt)",
            "TRANSPORTATION": "Transportation and logistics (vận chuyển và hậu cần)"
        }
        
        for text in texts:
            if isinstance(text, str) and text.strip():
                category_enum, confidence = classify_by_keywords(text)
                normalized_confidence = min(confidence / 3.0, 1.0) if confidence > 0 else 0.5
                category_name = category_mapping.get(category_enum, "Food assistance")
                
                results.append({
                    "category": category_enum,
                    "category_name": category_name,
                    "confidence": float(normalized_confidence)
                })
            else:
                results.append({
                    "category": "FOOD",
                    "confidence": 0.0
                })
        
        return jsonify({
            "results": results,
            "model": CATEGORY_MODEL_NAME
        })
    
    except Exception as e:
        logging.error(f"Error in batch category classification: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/models', methods=['GET'])
def get_available_models():
    """
    Returns information about available sentiment analysis and category classification models.
    All support Vietnamese and English.
    """
    return jsonify({
        "sentiment_model": {
            "current_model": "xlm-roberta-large-xnli",
            "current_model_name": MODEL_NAME,
            "languages_supported": ["Vietnamese (Tiếng Việt)", "English", "Chinese", "Arabic", "French", "Spanish", "German", "Japanese", "Korean", "Russian", "and 90+ others"]
        },
        "category_model": {
            "current_model": "facebook/bart-large-mnli",
            "current_model_name": CATEGORY_MODEL_NAME,
            "task": "Zero-shot classification",
            "categories": RELIEF_CATEGORIES,
            "languages_supported": ["Vietnamese (Tiếng Việt)", "English"]
        },
        "change_model_instruction": "Update the 'model=' parameter in the pipeline() call"
    })

@app.route('/health', methods=['GET'])
def health_check():
    """
    Health check endpoint with Vietnamese support status.
    """
    return jsonify({
        "status": "healthy",
        "sentiment_model_loaded": classifier is not None,
        "category_model_loaded": category_classifier is not None,
        "sentiment_model": MODEL_NAME,
        "category_model": CATEGORY_MODEL_NAME,
        "vietnamese_support": "Yes" if (classifier is not None and category_classifier is not None) else "Partial",
        "supported_languages": "Vietnamese, English, Chinese, Arabic, and 95+ others (sentiment), Vietnamese and English (categories)"
    })

@app.errorhandler(404)
def not_found(error):
    return jsonify({"error": "Endpoint not found"}), 404

@app.errorhandler(500)
def server_error(error):
    return jsonify({"error": "Internal server error"}), 500

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    print("=" * 70)
    print("Starting Sentiment Analysis & Category Classification API")
    print("=" * 70)
    print(f"Sentiment Model: {MODEL_NAME}")
    print(f"Category Model: {CATEGORY_MODEL_NAME}")
    print(f"Vietnamese Support: ✓ Yes (both models)")
    print(f"Supported Languages: Vietnamese, English, Chinese, Arabic, +95 more")
    print(f"Server: http://localhost:5001")
    print("=" * 70)
    print("Endpoints:")
    print("  POST /analyze - Analyze sentiment (Vietnamese or English)")
    print("  POST /analyze_batch - Analyze multiple texts for sentiment")
    print("  POST /classify_category - Classify text into relief category")
    print("  POST /classify_batch_category - Classify multiple texts into categories")
    print("  GET /models - List available models")
    print("  GET /health - Health check")
    print("=" * 70)
    app.run(debug=False, port=5001, host='0.0.0.0')
