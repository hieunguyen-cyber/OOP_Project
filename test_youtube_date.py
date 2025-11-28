#!/usr/bin/env python3
"""Quick test to verify YouTube Vietnamese date parsing"""

import json
import re

# Simulate what the Java code will do
def test_vietnamese_date_pattern():
    # This is what we found in the YouTube JSON
    test_date = "thg 12, 2024"
    
    # Java regex pattern
    pattern = r'thg\s+(\d+),?\s+(\d{4})'
    match = re.search(pattern, test_date)
    
    if match:
        month_num = int(match.group(1))
        year = int(match.group(2))
        
        months = ["", "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                 "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
        
        month_name = months[month_num]
        result = f"{month_name} {year} 1"
        
        print(f"✓ Vietnamese date parsing works!")
        print(f"  Input: {test_date}")
        print(f"  Extracted: month={month_num}, year={year}")
        print(f"  Converted: {result}")
        print(f"  Will parse as: {month_name} 1, {year}")
        return True
    else:
        print(f"✗ Could not parse: {test_date}")
        return False

# Test the full JSON pattern
def test_json_pattern():
    # Simulate YouTube JSON with Vietnamese date
    test_json = '''
    {
        "dateText": {
            "simpleText": "thg 12, 2024"
        }
    }
    '''
    
    # Java regex to find this
    pattern = r'"(?:dateText|publishedTimeText|uploadDate)"\s*:\s*\{[^}]*"simpleText"\s*:\s*"([^"]*thg[^"]+)"'
    match = re.search(pattern, test_json)
    
    if match:
        date_str = match.group(1)
        print(f"✓ JSON pattern matching works!")
        print(f"  Found in JSON: {date_str}")
        return True
    else:
        print(f"✗ JSON pattern failed")
        return False

if __name__ == "__main__":
    print("Testing YouTube Vietnamese Date Parsing")
    print("=" * 50)
    
    test1 = test_vietnamese_date_pattern()
    print()
    test2 = test_json_pattern()
    
    print()
    if test1 and test2:
        print("✓✓ All patterns work correctly!")
    else:
        print("✗ Some patterns failed")
