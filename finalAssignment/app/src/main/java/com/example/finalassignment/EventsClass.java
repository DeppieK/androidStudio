package com.example.finalassignment;

public class EventsClass {
    private String dataTitle;
    private String dataDesc;
    private String dataType;
    private String dataImage;
    private String location;
    private String timestamp;
    private String state;
    private String stateDate;
    private String key;

    //getter setter for key
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

//constructors
    public EventsClass(){

    }
    public EventsClass(String dataTitle, String dataDesc, String dataType, String dataImage, String location, String timestamp, String state) {
        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.dataType = dataType;
        this.dataImage = dataImage;
        this.location = location;
        this.timestamp = timestamp;
        this.state = state;
    }

    //getters
    public String getDataTitle() {
        return dataTitle;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public String getDataType() {
        return dataType;
    }

    public String getDataImage() {
        return dataImage;
    }

    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getState() {
        return state;
    }

    public String getStateDate() {
        return stateDate;
    }

    //setters
    public void setStateDate(String stateDate) {
        this.stateDate = stateDate;
    }

    public void setState(String state) {
        this.state = state;
    }
}
