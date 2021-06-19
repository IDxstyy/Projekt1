package com.example.projekt1.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Chat implements Parcelable {
    private String id;
    private String titel = "default";
    private ArrayList<String> userList = new ArrayList<String>();

    public Chat(){}

    public Chat(String id, String titel) {
        this.id = id;
        this.titel = titel;
    }

    public Chat(String id, String titel, ArrayList<String> uList) {
        this.id = id;
        this.titel = titel;
        this.userList = uList;
    }

    // Parcelable.Creator
    protected Chat(Parcel in) {
        id = in.readString();
        titel = in.readString();
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public String getId(){return this.id;}
    
    public String getTitel(){return this.titel;}

    public ArrayList<String> getUsers(){return this.userList;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(titel);
    }

    public void addUser(String user) {
        if(this.userList.stream().noneMatch(value -> value.equals(user))) this.userList.add(user);
    }
}
