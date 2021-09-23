package com.example.projekt1.models.plugins.pluginData;

public class ToDo {
    //id = row in Database
    //status 0 = not checked
    //Status 1 = checked
    private int status;
    private String id;

    //String task is the actual name of the task
    //each task has id and status
    private String task;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
