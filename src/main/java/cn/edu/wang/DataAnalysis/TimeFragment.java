package cn.edu.wang.DataAnalysis;


import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;

import static java.time.temporal.ChronoUnit.SECONDS;

public class TimeFragment {
    public  LocalTime StartTime;
    public LocalTime EndTime;
    public String Name;
    public TimeFragment(LocalTime start, LocalTime end)
    {
        this("", start, end);
    }
    public TimeFragment(String name, LocalTime start, LocalTime end)
    {
        Name = name;
        StartTime = start;
        EndTime = end;

        //第二天，加24小时
        if(EndTime.isBefore(StartTime))
            EndTime.plusHours(24);
    }

    public boolean IsInFragment(TimeFragment frag)
    {
        return frag.StartTime.isBefore(this.EndTime);
    }

    public static LocalTime Earlier(LocalTime left, LocalTime right)
    {
        return left.isBefore(right) ? left : right;
    }

    public static LocalTime Later(LocalTime left, LocalTime right)
    {
        return left.isAfter(right) ? left : right;
    }

    public Duration DuringTimeInThisFragment(TimeFragment period)
    {
        if(!IsInFragment(period)) return Duration.ZERO;

        LocalTime start = Later(StartTime, period.StartTime);
        LocalTime end = Earlier(EndTime, period.EndTime);

        return Duration.of(end.minusHours(start.getHour()).minusMinutes(start.getMinute()).minusSeconds(start.getSecond()).toSecondOfDay(), SECONDS);
    }
}
