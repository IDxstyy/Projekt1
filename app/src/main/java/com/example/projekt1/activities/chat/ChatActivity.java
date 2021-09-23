package com.example.projekt1.activities.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.ArraySet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.projekt1.R;
import com.example.projekt1.activities.plugins.PluginNotizenFragment;
import com.example.projekt1.activities.plugins.PluginPollFragment;
import com.example.projekt1.activities.plugins.PluginToDoFragment;
import com.example.projekt1.dialog.AddUserDialogTypeOne;
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

import java.text.DateFormat;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements AddUserDialogTypeOne.UserDialogListener, NavigationView.OnNavigationItemSelectedListener {
    RecyclerView recyclerView;
    ImageButton sendMessageButton;
    NavigationView navigationView;
    DrawerLayout drawer;
    EditText enteredText;
    ImageButton drawerToggleButton;
    ConstraintLayout fragmentContainer;
    TextView userIdTextView;


    // recylcer adapter
    ChatMessages chatMessages;

    // current Chat
    Chat chat;

    // List to hold firebase-messages
    ArrayList<Message> chat_messages = new ArrayList<Message>();

    // Setup Firebase-Database
    FirebaseDatabase root =  FirebaseDatabase.getInstance();
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
        getSupportActionBar().setTitle(chat.getTitel());
        // get user to current chat
        ArrayList<String> userList = (ArrayList<String>) getIntent().getSerializableExtra("users");
        chat.addUsers(userList);

        // init adapter for recycler-view
        chatMessages = new ChatMessages(chat_messages, session.getId());

        // get data from Firebase when changed and update chat with new message/messageList
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
                // add username and date to message
                String date = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
                String message = session.getUserName() + "\n\n" + date + "\n\n" + enteredText.getText().toString();
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

        // set username to drawer
        userIdTextView = navigationView.getHeaderView(0).findViewById(R.id.user_id_txtview2);
        userIdTextView.setText(session.getUserName());

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
        bundle.putString("chatId", this.chat.getId());
        switch (item.getItemId()){
            case R.id.plugin_open_notiz:
                // set fragment and attach data
                fragment = new PluginNotizenFragment();
                // set pluginType for firebase check
                bundle.putString("pluginType", "pluginNotizen");
                fragment.setArguments(bundle);
                // show fragmentContainer
                this.fragmentContainer.setTranslationZ(10.00f);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.chat_activity_fragment_container, fragment).commit();
                break;
            case R.id.plugin_open_todo:
                // set fragment and attach data
                fragment = new PluginToDoFragment();
                // set pluginType for firebase check
                bundle.putString("pluginType", "pluginToDo");
                fragment.setArguments(bundle);
                // show fragmentContainer
                this.fragmentContainer.setTranslationZ(10.00f);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.chat_activity_fragment_container, fragment).commit();
                break;
            case R.id.plugin_open_poll:

                // set fragment and attach data
                fragment = new PluginPollFragment();
                // set pluginType for firebase check
                bundle.putString("pluginType", "pluginPoll");
                fragment.setArguments(bundle);
                // show fragmentContainer
                this.fragmentContainer.setTranslationZ(10.00f);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.chat_activity_fragment_container, fragment).commit();
                break;
            case R.id.add_users_button_chat:
                AddUserDialogTypeOne addUserDialogTypeOne = new AddUserDialogTypeOne();
                addUserDialogTypeOne.show(getSupportFragmentManager(), "Add User");
                break;
            case R.id.close_fragment:
                // remove all Fragments
                for (Fragment frag : getSupportFragmentManager().getFragments()) {
                    getSupportFragmentManager().beginTransaction().remove(frag).commit();
                }
                // hide fragmentContainer
                this.fragmentContainer.setTranslationZ(-10.00f);
                // remove current fragment selection
                if(navigationView.getCheckedItem() != null) navigationView.getCheckedItem().setChecked(false);
                break;
            default: break;
        }
        drawer.closeDrawer(Gravity.LEFT);
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