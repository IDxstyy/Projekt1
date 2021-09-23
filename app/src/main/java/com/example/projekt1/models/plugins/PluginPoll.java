package com.example.projekt1.models.plugins;

import com.example.projekt1.models.plugins.pluginData.Poll;
import com.example.projekt1.models.plugins.pluginData.PollOption;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PluginPoll extends Plugin<Poll> {

    Poll poll;

    public PluginPoll (String id, String beschreibung, String chatRef, String typ) {
        super(id, beschreibung, chatRef, "pluginPoll");
    }

    public PluginPoll(String id, String beschreibung, String typ, String chatRef, ArrayList<Poll> polls){
        super(id, beschreibung, typ, chatRef, polls);
    }

    public PluginPoll () {
        super();
    }

    @Override
    public void doPluginStuff() {

    }

    public void updatePollText (String id, String text ){
        this.pluginData = this.pluginData.stream().map(val -> {
            if(id.equals(val.getId())) val.setTitle(text);
            return val;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    public void updatePollCheckBox (String id, Boolean isChecked ){

    }


    public void addPoll(Poll p){ this.pluginData.add(p);}

    public void setPolls(ArrayList<Poll> newPolls){
        this.pluginData = newPolls;
    }
    public void setPollOptions(ArrayList<PollOption> pollOptions) {
        poll.setPollOptions(pollOptions);
    }

}
