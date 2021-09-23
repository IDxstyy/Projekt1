package com.example.projekt1.models.plugins;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projekt1.R;
import com.example.projekt1.models.plugins.pluginData.Poll;

import java.util.ArrayList;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.ViewHolder> {

    private Activity context;
    private ArrayList<String> pollOptionsNameList;
    private ArrayList<Boolean> isChecked;

    public PollAdapter(Activity context, ArrayList<String> pollOptionsNameList, ArrayList<Boolean> isChecked){
        this.context = context;
        this.pollOptionsNameList = pollOptionsNameList;
        this.isChecked = isChecked;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poll_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PollAdapter.ViewHolder holder, int position) {
        //Daten aus Pool an Recyclerview binden
        holder.eT_PollOption.setText(pollOptionsNameList.get(position).toString());
        holder.checkBox_Poll.setChecked(isChecked.get(position));
    }

    @Override
    public int getItemCount() {
        return pollOptionsNameList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        EditText eT_PollOption;
        CheckBox checkBox_Poll;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            eT_PollOption = itemView.findViewById(R.id.eT_PollOption);
            checkBox_Poll = itemView.findViewById(R.id.checkBox_Poll);
        }
    }
}
