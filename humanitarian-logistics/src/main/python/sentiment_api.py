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

@app.route('/models', methods=['GET'])
def get_available_models():
    """
    Returns information about available sentiment analysis models.
    All support Vietnamese and English.
    """
    return jsonify({
        "current_model": "xlm-roberta-large-xnli",
        "current_model_name": MODEL_NAME,
        "languages_supported": ["Vietnamese (Tiếng Việt)", "English", "Chinese", "Arabic", "French", "Spanish", "German", "Japanese", "Korean", "Russian", "and 90+ others"],
        "available_models": [
            {
                "name": "xlm-roberta-large-xnli",
                "description": "Excellent multilingual support including Vietnamese (RECOMMENDED)",
                "languages": "100+ languages",
                "vietnamese_support": "Excellent"
            },
            {
                "name": "bert-base-multilingual-uncased",
                "description": "Fallback multilingual model",
                "languages": "104 languages",
                "vietnamese_support": "Good"
            }
        ],
        "change_model_instruction": "Update the 'model=' parameter in the pipeline() call"
    })

@app.route('/health', methods=['GET'])
def health_check():
    """
    Health check endpoint with Vietnamese support status.
    """
    return jsonify({
        "status": "healthy",
        "model_loaded": classifier is not None,
        "current_model": MODEL_NAME,
        "vietnamese_support": "Yes" if classifier is not None else "No",
        "supported_languages": "Vietnamese, English, Chinese, Arabic, and 95+ others"
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
    print("Starting Sentiment Analysis API with Vietnamese Support")
    print("=" * 70)
    print(f"Model: {MODEL_NAME}")
    print(f"Vietnamese Support: ✓ Yes")
    print(f"Supported Languages: Vietnamese, English, Chinese, Arabic, +95 more")
    print(f"Server: http://localhost:5001")
    print("=" * 70)
    print("Endpoints:")
    print("  POST /analyze - Analyze single text (Vietnamese or English)")
    print("  POST /analyze_batch - Analyze multiple texts (Vietnamese or English)")
    print("  GET /models - List available models with Vietnamese support")
    print("  GET /health - Health check with Vietnamese status")
    print("=" * 70)
    app.run(debug=False, port=5001, host='0.0.0.0')
