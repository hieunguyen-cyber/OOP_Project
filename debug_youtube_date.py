#!/usr/bin/env python3
"""Debug YouTube date extraction"""

import requests
import re
import json
from datetime import datetime

def extract_ytInitialData(html):
    """Extract ytInitialData from HTML"""
    match = re.search(r'var ytInitialData = ({.*?});', html)
    if match:
        return json.loads(match.group(1))
    return None

def find_keys_with_date_info(obj, target_keys=['dateText', 'publishedTimeText', 'uploadDate'], depth=0, max_depth=3):
    """Recursively search for keys with date info"""
    results = []
    
    if depth > max_depth:
        return results
    
    if isinstance(obj, dict):
        for key, value in obj.items():
            if key in target_keys:
                results.append({
                    'key': key,
                    'value': value,
                    'depth': depth,
                    'path': key
                })
            results.extend(find_keys_with_date_info(value, target_keys, depth+1, max_depth))
    elif isinstance(obj, list):
        for item in obj:
            results.extend(find_keys_with_date_info(item, target_keys, depth+1, max_depth))
    
    return results

def search_for_date_strings(obj, max_depth=2):
    """Search for readable date strings in JSON"""
    dates = []
    
    def search_recursive(o, depth=0):
        if depth > max_depth:
            return
        
        if isinstance(o, dict):
            for key, value in o.items():
                if isinstance(value, str):
                    # Check if it looks like a date
                    if re.search(r'[A-Za-z]+ \d{1,2}, \d{4}', value):
                        dates.append({
                            'key': key,
                            'value': value,
                            'depth': depth
                        })
                else:
                    search_recursive(value, depth+1)
        elif isinstance(o, list):
            for item in o:
                search_recursive(item, depth+1)
    
    search_recursive(obj)
    return dates

# Test with YouTube video
video_url = "https://www.youtube.com/watch?v=f6B8blI0klg"
print(f"Fetching {video_url}...")

headers = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
}

try:
    response = requests.get(video_url, headers=headers, timeout=10)
    response.raise_for_status()
    
    print(f"✓ Got response ({len(response.text)} chars)")
    
    # Extract ytInitialData
    data = extract_ytInitialData(response.text)
    if data:
        print(f"✓ Extracted ytInitialData ({len(json.dumps(data))} chars)")
        
        # Search for date keys
        date_keys = find_keys_with_date_info(data)
        print(f"\n📅 Found {len(date_keys)} date-related keys:")
        for item in date_keys[:10]:  # First 10
            print(f"  {item['key']}: {item['value']}")
        
        # Search for date strings
        date_strings = search_for_date_strings(data, max_depth=3)
        print(f"\n📝 Found {len(date_strings)} potential date strings:")
        for item in date_strings[:10]:
            print(f"  {item['key']}: {item['value']}")
        
        # Also search the raw JSON string for patterns
        json_str = json.dumps(data)
        date_pattern = re.findall(r'[A-Za-z]+ \d{1,2}, \d{4}', json_str)
        print(f"\n📋 Date patterns in raw JSON ({len(set(date_pattern))} unique):")
        for date in list(set(date_pattern))[:5]:
            print(f"  {date}")
    else:
        print("✗ Could not extract ytInitialData")
        
except Exception as e:
    print(f"✗ Error: {e}")
