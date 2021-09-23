package com.example.projekt1.activities.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projekt1.R;
import com.example.projekt1.dialog.PluginListElementDialog;
import com.example.projekt1.models.plugins.Plugin;
import com.example.projekt1.models.plugins.PluginToDo;
import com.example.projekt1.models.plugins.pluginData.ToDo;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginToDoFragment extends PluginBaseFragment implements PluginListElementDialog.DialogCloseListener {

    ItemTouchHelper itemTouchHelper;
    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;
    //List for tasks
    private ArrayList<ToDo> taskList;

    @Override
    public void initializePlugin() {

        // cast plugin to TodoPlugin
        PluginToDo actualPlugin = (PluginToDo) plugin;
        this.taskList = this.plugin.getPluginData();

        // initialize RecyclerView and Adapter
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        tasksAdapter = new ToDoAdapter(this.taskList, getActivity(), actualPlugin);
        tasksRecyclerView.setAdapter(tasksAdapter);
        tasksAdapter.setTasks(taskList);

        // attach itemTouchHelper to Recyclerview
        itemTouchHelper = new ItemTouchHelper(new ToDoRecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        //fab = + Button
        fab = view.findViewById(R.id.fabToDo);

        //if + Button is pressed call AddNewTask
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment addNewToDoDialog = new PluginListElementDialog(actualPlugin);
                addNewToDoDialog.show(getActivity().getSupportFragmentManager(), "Add ToDo-Task");
            }
        });

        // update pluginDataList from Firebase when changed
        this.pluginRefFirebase.orderByChild("id").equalTo(actualPlugin.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot pluginDataList = null;
                for (DataSnapshot pluginDs : snapshot.getChildren()) {
                    pluginDataList = pluginDs.hasChild("pluginData") ? pluginDs.child("pluginData") : null;
                }

                ArrayList<ToDo> tempArrayList = new ArrayList<>();

                if (pluginDataList != null) {
                    for (DataSnapshot toDoDs : pluginDataList.getChildren()) {
                        ToDo toDo = toDoDs.getValue(ToDo.class);
                        tempArrayList.add(toDo);
                    }
                }
                taskList = tempArrayList;
                tasksAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public PluginToDo setNewPlugin(String key) {
        return new PluginToDo(key, "Plugin für ToDo-Liste", pluginType, chatId, new ArrayList<ToDo>()) {
            @Override
            public void doPluginStuff() {

            }
        };
    }

    @Override
    protected Plugin castToSpecifiedPlugin(DataSnapshot pluginData, DataSnapshot pluginSpecificData) {
        ArrayList<ToDo> tasks = new ArrayList<>();
        PluginToDo tempPlugin = pluginData.getValue(PluginToDo.class);

        if(pluginSpecificData != null){
            for( DataSnapshot todoDs : pluginSpecificData.getChildren()){
                ToDo toDo = todoDs.getValue(ToDo.class);
                tasks.add(toDo);
            }
        }

        return new PluginToDo(tempPlugin.getId(), tempPlugin.getBeschreibung(), pluginType, tempPlugin.getChatRef(), tasks);
    }

    @Override
    protected void setPluginLayout() {
        this.layout = R.layout.fragment_todo_plugin;
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}


// Recycler-Adapter for ToDoFragment
class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private ArrayList<ToDo> todoList;
    private FragmentActivity activity;
    private PluginToDo pluginToDo;
    // firebase
    private FirebaseDatabase root = FirebaseDatabase.getInstance();
    private DatabaseReference pluginRefFirebase = root.getReference("Plugin");

    //constructor passing database and activity
    public ToDoAdapter(ArrayList<ToDo> todoList, FragmentActivity activity, PluginToDo pluginToDo) {
        this.activity = activity;
        this.todoList = todoList;
        this.pluginToDo = pluginToDo;
    }

    //RecyclerView calls onCreateViewHolder
    //onCreateViewHolder creates ItemView
    @NonNull
    @Override
    public ToDoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater takes the XML file as input and builds the view objets from it
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_todo, parent, false);
        return new ToDoAdapter.ViewHolder(itemView);
    }

    //ItemView calls onBindViewHolder
    //onBindViewHolder reads Item from list and fills Item View
    @Override
    public void onBindViewHolder(@NonNull final ToDoAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // todo
        final ToDo item = todoList.get(position);
        //assign text and checked to ToDoModel
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));

        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //if status changed, the updateStatus function from the DatabaseHandler class will be called
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    item.setStatus(1);
                    pluginToDo.updateToDoTask(item);
                    pluginRefFirebase.child(pluginToDo.getId()).setValue(pluginToDo);
                } else {
                    item.setStatus(0);
                    pluginToDo.updateToDoTask(item);
                    pluginRefFirebase.child(pluginToDo.getId()).setValue(pluginToDo);
                }
            }
        });
    }

    //helperfunction to convert integer into boolean
    private boolean toBoolean(int n) {
        return n != 0;
    }

    //returns Listsize
    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(ArrayList<ToDo> todoList) {
        this.todoList = todoList;
        //notify RecyclerView
        notifyDataSetChanged();
    }

    //Delete function
    public void deleteItem(int position) {
        ToDo item = todoList.get(position);
        todoList.remove(position);
        notifyItemRemoved(position);
        pluginToDo.setToDo(todoList);
        // firebase delete
        pluginRefFirebase.child(pluginToDo.getId()).setValue(pluginToDo);
    }

    //Edit function
    public void editItem(int position) {
        ToDo item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("id", item.getId());
        bundle.putString("task", item.getTask());
        BottomSheetDialogFragment addNewToDoDialog = new PluginListElementDialog(pluginToDo);
        addNewToDoDialog.setArguments(bundle);
        addNewToDoDialog.show(activity.getSupportFragmentManager(), "Task hinzufügen");
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
        }
    }
}

// ItemTouchHelper for RecyclerView

class ToDoRecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private ToDoAdapter adapter;

    public ToDoRecyclerItemTouchHelper(ToDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Löschen");
            builder.setMessage("Wollen Sie den Inhalt wirklich löschen?");
            builder.setPositiveButton("Bestätigen",
                    new DialogInterface.OnClickListener() {
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
        } else {
            adapter.editItem(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon;
        ColorDrawable background;

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        if (dX > 0) {
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_edit);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.colorPrimaryDark));
        } else {
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_delete);
            background = new ColorDrawable(Color.RED);
        }

        assert icon != null;
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}