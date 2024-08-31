package com.example.finalassignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<EventsClass> dataList;
    MyAdapter adapter;
    SearchView searchView;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);


        //initializing recycler view and search bar
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        fab = findViewById(R.id.fab);

        //TBD
        GridLayoutManager gridLayoutManager = new GridLayoutManager(EventListActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        //loading bar
        AlertDialog.Builder builder = new AlertDialog.Builder(EventListActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        //db
        databaseReference = FirebaseDatabase.getInstance().getReference("Events");

        //the list that we are gonna display in the recycler view
        dataList = new ArrayList<>();
        //initializing adapter
        adapter = new MyAdapter(EventListActivity.this, dataList, databaseReference);

        recyclerView.setAdapter(adapter);
        dialog.show();

        //toolbar menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            //TBD
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear(); //clear list
                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                    EventsClass eventsClass = itemSnapshot.getValue(EventsClass.class);
                    eventsClass.setKey(itemSnapshot.getKey());
                    dataList.add(eventsClass);//add snapshot to the list
                }
                adapter.sortDataByState();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });
        //TBD
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventListActivity.this, EmployeeHomepageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    } //end of OnCreate

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //implement the actions of each menu item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        String caseId = findCase(id);// find which item was selected
        int[] intArray = new int[7];
        String[] stringArray = new String[0];

        if (!dataList.isEmpty()) {
            stringArray = new String[dataList.size()];
        }
        //case for All
        if (caseId != null && caseId.contains("All")) {
            adapter.filterByType(caseId.replace("All", ""), databaseReference);
        } //case for Count
        else if (caseId != null && caseId.contains("Count")) {
            adapter.findCountofEachType(intArray);
        } //case for Reset
        else if (caseId != null && caseId.contains("Reset")){
            adapter.resetRecyclerView(databaseReference);
        } //case for Location
        else if (caseId != null && caseId.contains("Location")){
            adapter.bubbleSortLocationArray(caseId.replace("Location", ""),stringArray);
        }
        return true;
    }
    //implementation of find action
    public String findCase(int id) {
        String resourceName = getResources().getResourceName(id);

        if (resourceName.contains("All")) {
            return findSpecificCase(resourceName) + "All";
        } else if (resourceName.contains("Location")) {
            return findSpecificCase(resourceName) + "Location";
        } else if (resourceName.contains("Count")) {
            return "Count";
        } else if (resourceName.contains("Reset")) {
            return "Reset";
        }
        return null;
    }

    //implementation of find type
    public String findSpecificCase(String idString) {
        if (idString.contains("Fire")) {
            return "Fire";
        } else if (idString.contains("Flood")) {
            return "Flood";
        } else if (idString.contains("Earthquake")) {
            return "Earthquake";
        } else if (idString.contains("Tornado")) {
            return "Tornado";
        } else if (idString.contains("Hurricane")) {
            return "Hurricane";
        } else if (idString.contains("Lightning")) {
            return "Lightning";
        } else if (idString.contains("WinterStorm")) {
            return "Winter Storm";
        }
        return null;
    }


    //implementation of search
    public void searchList(String text){
        ArrayList<EventsClass> searchList = new ArrayList<>();
        for (EventsClass eventsClass : dataList){
            //search with title
            if (eventsClass.getDataTitle().toLowerCase().contains(text.toLowerCase())){
                searchList.add(eventsClass);
            }//search with type
            else if (eventsClass.getDataType().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(eventsClass);
            }//search with state
            else if (eventsClass.getState().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(eventsClass);
            }//search with timestamp
            else if (eventsClass.getTimestamp().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(eventsClass);
            }
        }
        adapter.searchDataList(searchList); //call adapter function
    }
}
