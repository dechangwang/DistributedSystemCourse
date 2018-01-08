package cn.edu.wang.DataAnalysis;

public enum CallType
{
    City,// 市话
    LongDistance, // 长途
    Roaming; // 漫游


    public static CallType FromInteger(int x) {
        switch(x) {
            case 1:
                return City;
            case 2:
                return LongDistance;
            case 3:
                return Roaming;
            default:
                return null;
        }
    }
    public static int ToInteger(CallType x) {
        switch(x) {
            case City:
                return 1;
            case LongDistance:
                return 2;
            case Roaming:
                return 3;
            default:
                return 4;
        }
    }
}
