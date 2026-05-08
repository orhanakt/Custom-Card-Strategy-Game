package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Changing date format for showing high score dates better
public final class DateUtil 
{
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateUtil() 
    {
    	
    }

    public static String nowString() 
    {
        return format(LocalDateTime.now());
    }

    public static String format(LocalDateTime dt) 
    {
        if (dt == null) return "";
        return FMT.format(dt);
    }
}
