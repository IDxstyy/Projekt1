package com.example.projekt1.models.plugins.pluginData;

import java.util.ArrayList;

public class PollOption {
    String id;
    String optionTitle;
    ArrayList<String> userRef;
    private ArrayList<Boolean> pollOptionCheckboxList;
    private boolean isItChecked;

    public ArrayList<Boolean> getPollOptionCheckboxList() {
        return pollOptionCheckboxList;
    }

    public void setPollOptionCheckboxList(ArrayList<Boolean> pollOptionCheckboxList) {
        this.pollOptionCheckboxList = pollOptionCheckboxList;
    }

    public boolean isChecked() {
        return isItChecked;
    }

    public void setChecked(boolean checked) {
        isItChecked = checked;
    }

    public String getId() {
        return id;
    }

    public PollOption () {

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOptionTitle() {
        return optionTitle;
    }

    public void setOptionTitle(String optionTitle) {
        this.optionTitle = optionTitle;
    }

    public ArrayList<String> getUserRef() {
        return userRef;
    }

    public void setUserRef(ArrayList<String> userRef) {
        this.userRef = userRef;
    }
}
