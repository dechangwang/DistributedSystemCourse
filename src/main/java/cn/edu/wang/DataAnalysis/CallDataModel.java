package cn.edu.wang.DataAnalysis;


public class CallDataModel {
    public String day_id;

    public String getDay_id() {
        return day_id;
    }

    public void setDay_id(String day_id) {
        this.day_id = day_id;
    }

    public String getCalling_nbr() {
        return calling_nbr;
    }

    public void setCalling_nbr(String calling_nbr) {
        this.calling_nbr = calling_nbr;
    }

    public String getCalled_nbr() {
        return called_nbr;
    }

    public void setCalled_nbr(String called_nbr) {
        this.called_nbr = called_nbr;
    }

    public MobileOperator getCalling_optr() {
        return calling_optr;
    }

    public void setCalling_optr(MobileOperator calling_optr) {
        this.calling_optr = calling_optr;
    }

    public MobileOperator getCalled_optr() {
        return called_optr;
    }

    public void setCalled_optr(MobileOperator called_optr) {
        this.called_optr = called_optr;
    }

    public String getCalling_city() {
        return calling_city;
    }

    public void setCalling_city(String calling_city) {
        this.calling_city = calling_city;
    }

    public String getCalled_city() {
        return called_city;
    }

    public void setCalled_city(String called_city) {
        this.called_city = called_city;
    }

    public String getCalling_roam_city() {
        return calling_roam_city;
    }

    public void setCalling_roam_city(String calling_roam_city) {
        this.calling_roam_city = calling_roam_city;
    }

    public String getCalled_roam_city() {
        return called_roam_city;
    }

    public void setCalled_roam_city(String called_roam_city) {
        this.called_roam_city = called_roam_city;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getRaw_dur() {
        return raw_dur;
    }

    public void setRaw_dur(String raw_dur) {
        this.raw_dur = raw_dur;
    }

    public CallType getCall_type() {
        return call_type;
    }

    public void setCall_type(CallType call_type) {
        this.call_type = call_type;
    }

    public String getCalling_cell() {
        return calling_cell;
    }

    public void setCalling_cell(String calling_cell) {
        this.calling_cell = calling_cell;
    }

    public String calling_nbr;
    public String called_nbr;
    public MobileOperator calling_optr;
    public MobileOperator called_optr;
    public String calling_city;
    public String called_city;
    public String calling_roam_city;
    public String called_roam_city;
    public String start_time;
    public String end_time;
    public String raw_dur;
    public CallType call_type;
    public String calling_cell;
}
