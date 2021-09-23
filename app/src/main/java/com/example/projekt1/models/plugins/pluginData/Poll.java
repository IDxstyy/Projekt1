package com.example.projekt1.models.plugins.pluginData;

public class Poll {
    private String id;
    private boolean multiOption;
    private String title;
    private ArrayList<PollOption> pollOptions;


    public Poll(String id, boolean multiOption,  String title, ArrayList<PollOption> pollOptions) {
        this.id = id;
        this.multiOption = multiOption;
        this.title = title;
        this.pollOptions = pollOptions;


    }

    public Poll () {

    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public boolean isMultiOption() {
        return multiOption;
    }

    public void setMultiOption(boolean multiOption) {
        this.multiOption = multiOption;
    }

    public ArrayList<PollOption> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(ArrayList<PollOption> pollOptions) {
        this.pollOptions = pollOptions;
    }

    public void addPollOption(PollOption n){ this.pollOptions.add(n); }

}
