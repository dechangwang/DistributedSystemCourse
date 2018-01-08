package cn.edu.wang.DataAnalysis;

public enum MobileOperator
{
    Telecom,// 中国电信
    Mobile, // 中国移动
    Unicom,// 中国联通
    Other;


    public static MobileOperator FromInteger(int x) {
        switch(x) {
            case 1:
                return Telecom;
            case 2:
                return Mobile;
            case 3:
                return Unicom;
            default:
                return Other;
        }
    }

    public static int ToInteger(MobileOperator x) {
        switch(x) {
            case Telecom:
                return 1;
            case Mobile:
                return 2;
            case Unicom:
                return 3;
            default:
                return 4;
        }
    }
}

