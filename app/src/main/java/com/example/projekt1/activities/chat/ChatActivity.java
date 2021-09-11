package com.example.projekt1.activities.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.ArraySet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.projekt1.R;
import com.example.projekt1.activities.plugins.PluginNotizenFragment;
import com.example.projekt1.activities.plugins.PluginToDoFragment;
import com.example.projekt1.dialog.AddUserToChatDialog;
import com.example.projekt1.models.Chat;
import com.example.projekt1.models.Message;
import com.example.projekt1.models.Session;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity implements AddUserToChatDialog.UserDialogListener, NavigationView.OnNavigationItemSelectedListener {
    RecyclerView recyclerView;
    ImageButton sendMessageButton;
    NavigationView navigationView;
    DrawerLayout drawer;
    EditText enteredText;
    ImageButton drawerToggleButton;
    ConstraintLayout fragmentContainer;

    // recylcer adapter
    ChatMessages chatMessages;

    // current Chat
    Chat chat;

    // List to hold firebase-messages
    ArrayList<Message> chat_messages = new ArrayList<Message>();

    // Setup Firebase-Database
    FirebaseDatabase root =  FirebaseDatabase.getInstance();
    // Get Message-Table-Reference from FireDB
    DatabaseReference messageref = root.getReference("Message");
    DatabaseReference chatref = root.getReference("Chat");

    // Session for current-user
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // get Session
        session = new Session(getApplicationContext());

        // get chat passed as value to activity and extract messages
        chat = getIntent().getParcelableExtra("CHAT");
        // get user to current chat
        ArrayList<String> userList = (ArrayList<String>) getIntent().getSerializableExtra("users");
        chat.addUsers(userList);

        // init adapter for recycler-view
        chatMessages = new ChatMessages(chat_messages, session.getId());

        // get data from Firebase when changed and update chat with new messageList
        messageref.orderByChild("chatId").equalTo(chat.getId()).addChildEventListener(new ChatActivity.ChildListener());

        // init Recycler-View with chatMessages
        recyclerView = findViewById(R.id.chat_activity_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        recyclerView.setAdapter(chatMessages);

        // init sendMessageButton and editText
        sendMessageButton = findViewById(R.id.sendMessageButton);
        enteredText = findViewById(R.id.enterMessageET);

        // set sendMessageButton onClickListener
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // generate unique ID
                String key =  messageref.push().getKey();
                // add Username to message
                String message =  session.getUserName() + "\n\n" + enteredText.getText().toString();
                // throw assertion-error if null
                assert key != null;
                // save message to firebase
                messageref.child(key).setValue(new Message(key, message, session.getId(), chat.getId()));
                // reset message-input
                enteredText.setText("");
            }
        });


        // init drawer - toggle
        drawer = findViewById(R.id.drawer_layout);
        drawerToggleButton = findViewById(R.id.chatSideBarButton);

        drawerToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( drawer.isDrawerOpen(Gravity.LEFT) ){
                    drawer.closeDrawer(Gravity.LEFT);
                }else{
                    drawer.openDrawer(Gravity.LEFT);
                }
            }
        });

        // init navigationView and child-elements
        navigationView = findViewById(R.id.nav_view);
        fragmentContainer = findViewById(R.id.chat_activity_fragment_container);
        navigationView.setNavigationItemSelectedListener(this);

        // init addUser-Button
        //addUser = findViewById(R.id.add_users_button_chat);
        /*addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddUserToChatDialog addUserToChatDialog = new AddUserToChatDialog();
                addUserToChatDialog.show(getSupportFragmentManager(), "Add User to Chat - Dialog");
            }
        });*/
    }

    @Override
    public void applyData(ArraySet<String> users) {
        this.chat.addUsers(users);
        chatref.child(this.chat.getId()).setValue(this.chat);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        Fragment fragment;
        // to pass and identify chat in plugin
        Bundle bundle = new Bundle();
        bundle.putString("chatRef", this.chat.getId());
        switch (item.getItemId()){
            case R.id.plugin_open_notiz:
                // set fragment and attach data
                fragment = new PluginNotizenFragment();
                fragment.setArguments(bundle);
                // show fragmentContainer
                this.fragmentContainer.setTranslationZ(10.00f);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.chat_activity_fragment_container, fragment).commit();
                break;
            case R.id.plugin_open_todo:
                // set fragment and attach data
                fragment = new PluginToDoFragment();
                fragment.setArguments(bundle);
                // show fragmentContainer
                this.fragmentContainer.setTranslationZ(10.00f);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.chat_activity_fragment_container, fragment).commit();
                break;
            case R.id.add_users_button_chat:
                AddUserToChatDialog addUserToChatDialog = new AddUserToChatDialog();
                addUserToChatDialog.show(getSupportFragmentManager(), "Add User to Chat - Dialog");
                break;
            case R.id.close_fragment:
                // remove all Fragments
                for (Fragment frag : getSupportFragmentManager().getFragments()) {
                    getSupportFragmentManager().beginTransaction().remove(frag).commit();
                }
                // hide fragmentContainer
                this.fragmentContainer.setTranslationZ(-10.00f);
                // remove current fragment selection
                navigationView.getCheckedItem().setChecked(false);
                break;
            default: break;
        }
        return true;
    }

    private class ChildListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
            chat_messages.add(dataSnapshot.getValue(Message.class));
            chatMessages.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
        }

        @Override
        public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
        }

        @Override
        public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
        }

        @Override
        public void onCancelled(@NonNull @NotNull DatabaseError error) {
        }
    }
}