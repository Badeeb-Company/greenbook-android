package com.badeeb.greenbook.models;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class WorkingDay {

    private String name;
    private String openedAt;
    private String closedAt;
    private String state;

    public WorkingDay() {
        this.name = "";
        this.openedAt = "";
        this.closedAt = "";
        this.state = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
