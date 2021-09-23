package com.example.projekt1.activities.plugins;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projekt1.R;
import com.example.projekt1.dialog.ConfirmDialog;
import com.example.projekt1.dialog.PluginListElementDialog;
import com.example.projekt1.models.plugins.pluginData.Notiz;
import com.example.projekt1.models.plugins.pluginData.PollOption;
import com.example.projekt1.models.plugins.Plugin;
import com.example.projekt1.models.plugins.PluginPoll;
import com.example.projekt1.models.plugins.pluginData.Poll;
import com.example.projekt1.models.plugins.pluginData.ToDo;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class PluginPollFragment extends PluginBaseFragment implements PluginListElementDialog.DialogCloseListener {

    ItemTouchHelper itemTouchHelper;
    private RecyclerView pollRecyclerView;
    private PollAdapter pollAdapter;
    private RecyclerView pollOptionsRecyclerView;
    private OptionsAdapter optionsAdapter;
    private CheckBox checkBoxPoll;
    private TextView etPollOption;
    private TextView pollTitle;
    private EditText pollOptionInput;
    private Button pollSubmitButton;
    private ImageButton ibAddPoll;
    private FloatingActionButton addPollFab;



    //List for Poll
    private ArrayList<Poll> pollList;
    private ArrayList<PollOption> optionsList;



    String zwischenspeicher;



    @Override
    public void initializePlugin() {


        //cast plugin to PollPlugin
        PluginPoll actualPlugin = (PluginPoll) plugin;
        this.pollList = this.plugin.getPluginData();


        // init viewElements

        addPollFab = view.findViewById(R.id.addPollFab);






        addPollFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialogFragment addNewToDoDialog = new PluginListElementDialog(actualPlugin);
                addNewToDoDialog.show(getActivity().getSupportFragmentManager(), "Poll hinzufügen");
            }
        });


        //Initialize RecyclerView and Adapter
        pollRecyclerView = view.findViewById(R.id.pollRecyclerView);
        pollRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        pollAdapter = new PollAdapter(this.pollList, getActivity(), actualPlugin);
        pollRecyclerView.setAdapter(pollAdapter);

        //pollOptionsRecyclerView = view.findViewById(R.id.pollOptionRecyclerView);
        //pollOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        //pollOptionsRecyclerView.setAdapter(optionsAdapter);

        //Attach TouchHelper to Recyclerview


        this.pluginRefFirebase.orderByChild("id").equalTo(actualPlugin.getId()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot pluginDataList = null;
                for (DataSnapshot pluginDs : snapshot.getChildren()) {
                    pluginDataList = pluginDs.hasChild("pluginData") ? pluginDs.child("pluginData") : null;
                }

                ArrayList<Poll> tempArrayList = new ArrayList<>();

                if (pluginDataList != null) {
                    for (DataSnapshot pollDs : pluginDataList.getChildren()) {
                        Poll poll = pollDs.getValue(Poll.class);
                        DataSnapshot pollOptionsDs = pollDs.child("pollOptions");
                        if(pollDs != null && pollOptionsDs != null ) {
                            ArrayList<PollOption> options = new ArrayList<>();
                            for (DataSnapshot pollOptionDs : pollOptionsDs.getChildren()) {
                                PollOption pollOption = pollOptionDs.getValue(PollOption.class);
                                options.add(pollOption);
                            }
                            poll.setPollOptions(options);
                        }
                        tempArrayList.add(poll);
                    }
                }
                pollList = tempArrayList;
                pollAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //attach itemTouchHelper to RecyclerView
        //itemTouchHelper = new ItemTouchHelper(new PollRecyclerItemTouchHelper(pollAdapter));
        //itemTouchHelper.attachToRecyclerView(pollRecyclerView);

       /* pollSubmitButton = view.findViewById(R.id.pollSubmitButton);

        pollSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Du hast abgestimmt", Toast.LENGTH_SHORT).show();
            }
        }); */



    }




    @Override
    public Plugin setNewPlugin(String key) {

        return new PluginPoll(key, "Mit diesem Plugin lassen sich Abstimmungen durchführen", this.pluginType, this.chatId, new ArrayList<Poll>()) {
            @Override
            public void doPluginStuff() {

            }
        };
    }

    @Override
    protected PluginPoll castToSpecifiedPlugin(DataSnapshot pluginData, DataSnapshot pluginSpecificData) {
        ArrayList<Poll> polls = new ArrayList<>();
        PluginPoll tempPluginPoll = pluginData.getValue(PluginPoll.class);

        if(pluginSpecificData != null) {
            for( DataSnapshot pollDs : pluginSpecificData.getChildren()){
                Poll poll = pollDs.getValue(Poll.class);
                DataSnapshot pollOptionsDs = pollDs.child("pollOptions");
                if(pollDs != null && pollOptionsDs != null ) {
                    ArrayList<PollOption> options = new ArrayList<>();
                    for (DataSnapshot pollOptionDs : pollOptionsDs.getChildren()) {
                        PollOption pollOption = pollOptionDs.getValue(PollOption.class);
                        options.add(pollOption);
                    }
                    poll.setPollOptions(options);
                }
                polls.add(poll);
            }
        }
        return new PluginPoll(tempPluginPoll.getId(), tempPluginPoll.getBeschreibung(), tempPluginPoll.getTyp(), tempPluginPoll.getChatRef(), polls);
    }

    @Override
    protected void setPluginLayout() {
        this.layout = R.layout.fragment_poll_plugin;
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        pollAdapter.setPolls(this.pollList);
        pollAdapter.notifyDataSetChanged();
    }


    class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollViewHolder> implements ConfirmDialog.ConfirmDialogListener{

        private ArrayList<Poll> pollList;
        private FragmentActivity activity;
        private PluginPoll pluginPoll;




        //firebase
        private FirebaseDatabase root = FirebaseDatabase.getInstance();
        private DatabaseReference pluginRefFirebase = root.getReference("plugin");

        //constructor passing database and activity
        public PollAdapter(ArrayList<Poll> pollList, FragmentActivity activity, PluginPoll pluginPoll) {
            this.pollList = pollList;
            this.activity = activity;
            this.pluginPoll = pluginPoll;
        }

        @NonNull
        @Override
        public PollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.poll_recylleritem, parent, false);
            return new PollAdapter.PollViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PollViewHolder holder, @SuppressLint("RecyclerView") int position) {
            final Poll item = pollList.get(position);



            // submit poll
            holder.subPollBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(activity.getApplicationContext(), "Du hast abgestimmt", Toast.LENGTH_SHORT).show();
                }
            });

            holder.pollTitle.setText(item.getTitle());

            // set nested RecyclerView
            holder.optionsRecyclerView.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext()));
            OptionsAdapter optionsAdapter = new OptionsAdapter(this.pollList.get(position).getPollOptions(), activity, pluginPoll);
            holder.optionsRecyclerView.setAdapter(optionsAdapter);

            itemTouchHelper = new ItemTouchHelper(new PollRecyclerItemTouchHelper(optionsAdapter));
            itemTouchHelper.attachToRecyclerView(holder.optionsRecyclerView);



            // addPoll
            holder.addPollOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PollOption pollOption = new PollOption();
                    pollOption.setOptionTitle(holder.pollOptionEditText.getText().toString());



                    Container.getInstance().btnRefZws = holder.subPollBtn;

                    Container.getInstance().zwischenspeicher = holder.pollOptionEditText.getText().toString(); /*
                    Den Inhalt der Abstimmungsoption in den Zwischenspeicher speichern
                    */
                    holder.pollOptionEditText.setText(""); //Eingegebenen Text entfernen

                    //OptionsAdapter.OptionViewHolder optionViewHolder = new OptionsAdapter.OptionViewHolder(holder);
                    //optionViewHolder.tVPollOption.setText(holder.pollOptionEditText.getText().toString());
                    pollList.get(position).addPollOption(pollOption);
                    pluginPoll.setPolls(pollList);
                    pluginRefFirebase.child(pluginPoll.getId()).setValue(pluginPoll);
                    optionsAdapter.notifyDataSetChanged();
                }
            });

            holder.subPollBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.deletePollBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Hier fehler

                    openDialog();
                    if(Container.getInstance().deleteValue){
                        ConfirmDialog.ConfirmDialogListener cDL = new ConfirmDialog.ConfirmDialogListener() {
                            @Override
                            public void applyData(boolean result) {
                                deletePoll(position);
                            }
                        };

                    }

                }
            });


        }
        public void openDialog(){
            //PollDialog pollDialog = new PollDialog();
            //pollDialog.show(activity.getSupportFragmentManager(), "Delete Poll");
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.show(activity.getSupportFragmentManager(), "Delete Poll");
        }
        @Override
        public void applyData(boolean result) {

        }
        public void deletePoll(int position) {
            Poll item = pollList.get(position);
            pollList.remove(position);
            notifyItemRemoved(position);
            pluginPoll.setPolls(pollList);

            // firebase delete
            pluginRefFirebase.child(pluginPoll.getId()).setValue(pluginPoll);
        /*
        Nochmal diesen Teil checken
         */
        }

        @Override
        public int getItemCount() {
            return this.pollList.size();
        }

        public void setPolls(ArrayList<Poll> pollList) {
            this.pollList = pollList;
        }




        public class PollViewHolder extends RecyclerView.ViewHolder {
            TextView pollTitle;
            EditText pollOptionEditText;
            ImageButton addPollOption;
            RecyclerView optionsRecyclerView;
            Button subPollBtn;
            ImageButton deletePollBtn;


            PollViewHolder(View view) {
                super(view);
                pollTitle = view.findViewById(R.id.tVTitleOfPoll);
                pollOptionEditText = view.findViewById(R.id.pollOptionInput);
                addPollOption = view.findViewById(R.id.iBaddPollOptions);
                subPollBtn = view.findViewById(R.id.pollSubmitButton);
                optionsRecyclerView = view.findViewById(R.id.pollOptionRecyclerView);
                deletePollBtn = view.findViewById(R.id.deletePollBtn);

            }
        }


    }

    class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionViewHolder> {

        private ArrayList<PollOption> pollOptionList;
        private FragmentActivity activity;
        private PluginPoll pluginPoll;



        //firebase
        private FirebaseDatabase root = FirebaseDatabase.getInstance();
        private DatabaseReference pluginRefFirebase = root.getReference("plugin");

        //constructor passing database and activity
        public OptionsAdapter(ArrayList<PollOption> pollOptionList, FragmentActivity activity, PluginPoll pluginPoll) {
            this.pollOptionList = pollOptionList;
            this.activity = activity;
            this.pluginPoll = pluginPoll;
        }



        @NonNull
        @Override
        public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.poll_list, parent, false);
            return new OptionsAdapter.OptionViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
            final PollOption item = pollOptionList.get(position);



            if (!Container.getInstance().zwischenspeicher.isEmpty()) {
                holder.tVPollOption.setText(Container.getInstance().zwischenspeicher);
            }

            final PollOption pollItem = pollOptionList.get(position);
            holder.pollCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    /* Logik für nur einmal checken*/


                }
            });
            if (item.isChecked()) {
                holder.pollCheckBox.setChecked(true);
                //Einf. von Methode zum updaten des Texts / Status der CheckBox
                pluginRefFirebase.child(pluginPoll.getId()).setValue(pluginPoll);
                if(Container.getInstance().btnRefZws.getVisibility() == View.INVISIBLE) {
                    Button submitBtn = Container.getInstance().getBtnRefZws();
                    submitBtn.setVisibility(View.VISIBLE);
                }
            }
        }
        public Context getContext() {
            return activity;
        }

        public void deleteItem(int position) {
            PollOption item = pollOptionList.get(position);
            pollOptionList.remove(position);
            notifyItemRemoved(position);
            pluginPoll.setPollOptions(pollOptionList);
            // firebase delete
            pluginRefFirebase.child(pluginPoll.getId()).setValue(pluginPoll);
        }

        public void editItem(int position) {
            PollOption item = pollOptionList.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("id", item.getId());
            bundle.putString("option", item.getOptionTitle());
            BottomSheetDialogFragment addNewPollOptionDialog = new PluginListElementDialog(pluginPoll);
            addNewPollOptionDialog.setArguments(bundle);
            addNewPollOptionDialog.show(activity.getSupportFragmentManager(), "Poll Option bearbeiten");
        }

        @Override
        public int getItemCount() {
            return this.pollOptionList.size();
        }

        public void setPolls(ArrayList<PollOption> pollOptionList) {
            this.pollOptionList = pollOptionList;
        }

        public class OptionViewHolder extends RecyclerView.ViewHolder {
            TextView tVPollOption;
            CheckBox pollCheckBox;


            OptionViewHolder(View view) {
                super(view);
                tVPollOption = view.findViewById(R.id.tVPollOption);
                pollCheckBox = view.findViewById(R.id.checkBoxPoll);
            }

        }



    }


    class PollRecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback{

        OptionsAdapter adapter;

        public PollRecyclerItemTouchHelper(OptionsAdapter adapter) {
            super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
                builder.setTitle("Löschen");
                builder.setMessage("Wirklich löschen?");
                builder.setPositiveButton("Ja, löschen!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.deleteItem(position);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                adapter.editItem(position);
            }
        }
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }


    }
}




class Container{
    String zwischenspeicher;

    Button btnRefZws;

    boolean deleteValue;


    public String getZwischenspeicher() {return zwischenspeicher;}
    public void setZwischenspeicher(String zwischenspeicher) {this.zwischenspeicher = zwischenspeicher;}
    public Button getBtnRefZws() {return btnRefZws;}
    public void setBtnRefZws(Button btnRefZws) {this.btnRefZws = btnRefZws;}

    private static Container instance = null;
    private void Container(){}
    public static Container getInstance(){
        if(instance==null){
            instance = new Container();
        }
        return instance;
    }
}


