package com.example.projekt1.activities.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projekt1.R;
import com.example.projekt1.activities.chat.ChatActivity;
import com.example.projekt1.activities.login.LoginActivity;
import com.example.projekt1.models.Chat;
import com.example.projekt1.models.Message;
import com.example.projekt1.models.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ChatMessages extends RecyclerView.Adapter<ChatMessages.ViewHolder> {
    // to identify Viewtype
    public static final int VIEWTYPE_MYMESSAGE = 0;
    public static final int VIEWTYPE_OTHERMESSAGE = 1;

    ArrayList<Message> data;
    Activity context;

    public ChatMessages(){
        this.data = this.getMessages();
    }

    public ChatMessages(ArrayList<Message> data){
        this.data = data;
    }

    @NonNull
    @Override
    public ChatMessages.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());;
        if(viewType == VIEWTYPE_MYMESSAGE){
            view = layoutInflater.inflate(R.layout.message_send_layout, parent, false);
            System.out.println("Inflate send.");
        }
        else {
            view = layoutInflater.inflate(R.layout.message_received_layout, parent, false);
            System.out.println("Inflate received.");
        }

        ViewHolder viewHolder = new ViewHolder(view);;

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position).getUserId() == LoginActivity.currentUser.getId()) return VIEWTYPE_MYMESSAGE;
        else return VIEWTYPE_OTHERMESSAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessages.ViewHolder holder, final int position) {
        System.out.println(data.get(position).getContent());
        if(data.get(position).getUserId() == LoginActivity.currentUser.getId()) holder.sendMessage.setText(data.get(position).getContent());
        else holder.receiveMessage.setText(data.get(position).getContent());;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public ArrayList<Message> getMessages(){
        return new ArrayList<Message>(){};
    }

    public void setMessages(ArrayList<Message> newData){
        this.data = newData;
        for( Message m : data) System.out.println(m.getContent() + m.getUserId());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView sendMessage;
        TextView receiveMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMessage = itemView.findViewById(R.id.textView_chat_send);
            receiveMessage = itemView.findViewById(R.id.textView_chat_received);
        }
    }
}
