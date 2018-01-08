package cn.edu.wang.DataAnalysis;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import static java.time.temporal.ChronoUnit.SECONDS;

public class TimeFragments {
    public final HashMap<TimeFragment, Duration> Frags = new HashMap<TimeFragment, Duration>()
    {{
        put( new TimeFragment("时间段 1", LocalTime.of(0,0), LocalTime.of(3, 0)), null);
        put( new TimeFragment("时间段 2", LocalTime.of(3,0), LocalTime.of(6, 0)), null);
        put( new TimeFragment("时间段 3", LocalTime.of(6,0), LocalTime.of(9, 0)), null);
        put( new TimeFragment("时间段 4", LocalTime.of(9,0), LocalTime.of(12, 0)), null);
        put( new TimeFragment("时间段 5", LocalTime.of(12,0), LocalTime.of(15, 0)), null);
        put( new TimeFragment("时间段 6", LocalTime.of(15,0), LocalTime.of(18, 0)), null);
        put( new TimeFragment("时间段 7", LocalTime.of(18,0), LocalTime.of(21, 0)), null);
        put( new TimeFragment("时间段 8", LocalTime.of(21,0), LocalTime.of(0,0)), null);
    }};

    //八个时间段总时间, 分钟
    public Long TotalDurationTime()
    {
        Duration sumTime = Duration.of(0, SECONDS);
        Frags.forEach((time, duration)->sumTime.plus(duration));
        return sumTime.toMinutes();
    }
}
