import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Quick test of YouTube date parsing logic
 */
public class TestYouTubeDate {
    
    public static LocalDateTime parseYouTubeDateString(String dateStr) {
        try {
            // Remove common prefixes
            dateStr = dateStr.replaceAll("(Uploaded on|Streamed|Started streaming|Published|Premiere)\\s*", "");
            dateStr = dateStr.trim();
            
            // Handle Vietnamese month abbreviation "thg" (tháng)
            // E.g., "thg 12, 2024" -> "Dec 12, 2024"
            if (dateStr.toLowerCase().contains("thg")) {
                String[] viMonths = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                
                Pattern pattern = Pattern.compile("thg\\s+(\\d+),?\\s+(\\d{4})");
                Matcher matcher = pattern.matcher(dateStr);
                
                if (matcher.find()) {
                    int month = Integer.parseInt(matcher.group(1));
                    int year = Integer.parseInt(matcher.group(2));
                    
                    if (month >= 1 && month <= 12) {
                        String englishDate = viMonths[month] + " 1, " + year; // Day 1 of the month
                        System.out.println("  🌍 Converted Vietnamese date: " + dateStr + " → " + englishDate);
                        dateStr = englishDate;
                    }
                }
            }
            
            // Try various date formats
            String[] formats = {
                "MMM dd, yyyy",           // Jan 15, 2023
                "MMM d, yyyy",            // Jan 5, 2023
                "MMMM dd, yyyy",          // January 15, 2023
                "MMMM d, yyyy",           // January 5, 2023
                "yyyy-MM-dd",             // 2023-01-15 (ISO format)
                "MMM dd yyyy",            // Jan 15 2023 (no comma)
                "MMM d yyyy",             // Jan 5 2023 (no comma)
                "MMM dd",                 // Jan 15 (no year)
                "MMMM d"                  // January 15 (no year)
            };
            
            for (String format : formats) {
                try {
                    DateTimeFormatter formatter = 
                        DateTimeFormatter.ofPattern(format);
                    
                    // If format has no year, add current year
                    String dateToparse = dateStr;
                    if (!format.contains("yyyy")) {
                        dateToparse = dateStr + ", " + java.time.Year.now().getValue();
                    }
                    
                    java.time.LocalDate date = java.time.LocalDate.parse(dateToparse, formatter);
                    return date.atStartOfDay();
                } catch (Exception e) {
                    // Try next format
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Testing YouTube Vietnamese Date Parsing");
        System.out.println("=".repeat(50));
        
        // Test cases
        String[] testDates = {
            "thg 12, 2024",
            "Dec 12, 2024",
            "January 15, 2023",
            "Jan 5, 2023",
            "2023-01-15"
        };
        
        for (String testDate : testDates) {
            System.out.println();
            System.out.println("Testing: " + testDate);
            LocalDateTime result = parseYouTubeDateString(testDate);
            
            if (result != null) {
                System.out.println("✓ Parsed successfully → " + result);
            } else {
                System.out.println("✗ Failed to parse");
            }
        }
        
        System.out.println();
        System.out.println("=".repeat(50));
        System.out.println("✓ All date parsing tests completed");
    }
}
