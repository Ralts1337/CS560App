package com.zhanghan.activityrecognizor;

public class RowOfData {
    private String time;
    private String frequency;

    public RowOfData() {
    }

    public RowOfData(String time, String frequency) {

        this.time = time;
        this.frequency = frequency;
    }

    public void setData(String time, String frequency) {
        this.time = time;
        this.frequency = frequency;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getTime() {
        return time;
    }

    public String getFrequency() {
        return frequency;
    }
}
