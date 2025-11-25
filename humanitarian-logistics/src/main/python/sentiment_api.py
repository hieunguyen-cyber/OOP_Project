"""
Sentiment Analysis API for Humanitarian Logistics System
This Flask API provides sentiment analysis endpoints that Java application can call via HTTP.
"""

from flask import Flask, request, jsonify
from transformers import pipeline
import logging

app = Flask(__name__)
app.config['JSON_SORT_KEYS'] = False

# Initialize the sentiment analysis pipeline
# Can be changed to other models like "roberta-large-mnli", "distilroberta-finetuned-financial-sentiments"
try:
    classifier = pipeline("sentiment-analysis", 
                         model="distilbert-base-uncased-finetuned-sst-2-english")
    logging.info("Sentiment analysis model loaded successfully")
except Exception as e:
    logging.error(f"Error loading sentiment model: {e}")
    classifier = None

@app.route('/analyze', methods=['POST'])
def analyze_sentiment():
    """
    Analyze sentiment of provided text.
    
    Request JSON:
    {
        "text": "The humanitarian aid was well distributed"
    }
    
    Response JSON:
    {
        "sentiment": "POSITIVE",
        "confidence": 0.9987
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
        
        # Perform sentiment analysis
        result = classifier(text[:512])  # Truncate to 512 tokens (model limit)
        
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
            "raw_label": label
        })
    
    except Exception as e:
        logging.error(f"Error in sentiment analysis: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/analyze_batch', methods=['POST'])
def analyze_batch():
    """
    Analyze sentiment for multiple texts.
    
    Request JSON:
    {
        "texts": ["text1", "text2", "text3"]
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
                result = classifier(text[:512])
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
        
        return jsonify({"results": results})
    
    except Exception as e:
        logging.error(f"Error in batch sentiment analysis: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/models', methods=['GET'])
def get_available_models():
    """
    Returns information about available sentiment analysis models.
    """
    return jsonify({
        "current_model": "distilbert-base-uncased-finetuned-sst-2-english",
        "available_models": [
            "distilbert-base-uncased-finetuned-sst-2-english",
            "roberta-large-mnli",
            "bert-base-uncased-finetuned-sst-2-english",
            "xlm-roberta-large-xnli"
        ],
        "description": "Change model by updating the pipeline initialization in analyze_sentiment()"
    })

@app.route('/health', methods=['GET'])
def health_check():
    """
    Health check endpoint.
    """
    return jsonify({
        "status": "healthy",
        "model_loaded": classifier is not None
    })

@app.errorhandler(404)
def not_found(error):
    return jsonify({"error": "Endpoint not found"}), 404

@app.errorhandler(500)
def server_error(error):
    return jsonify({"error": "Internal server error"}), 500

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    print("Starting Sentiment Analysis API on http://localhost:5000")
    print("Endpoints:")
    print("  POST /analyze - Analyze single text")
    print("  POST /analyze_batch - Analyze multiple texts")
    print("  GET /models - List available models")
    print("  GET /health - Health check")
    app.run(debug=False, port=5001, host='0.0.0.0')
