package com.example.finalassignment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<EventsClass> dataList;
    private DatabaseReference databaseReference;

    public MyAdapter(Context context, List<EventsClass> dataList, DatabaseReference databaseReference) {
        this.context = context;
        this.dataList = dataList;
        this.databaseReference = databaseReference;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(dataList.get(position).getDataImage()).into(holder.recImage);
        holder.recTitle.setText(dataList.get(position).getDataTitle());
        holder.recType.setText(dataList.get(position).getDataType());
        holder.recState.setText(dataList.get(position).getState());
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("Image", dataList.get(holder.getAdapterPosition()).getDataImage());
                intent.putExtra("Type", dataList.get(holder.getAdapterPosition()).getDataType());
                intent.putExtra("Title", dataList.get(holder.getAdapterPosition()).getDataTitle());
                intent.putExtra("Description", dataList.get(holder.getAdapterPosition()).getDataDesc());
                intent.putExtra("Location", dataList.get(holder.getAdapterPosition()).getLocation());
                intent.putExtra("Time", dataList.get(holder.getAdapterPosition()).getTimestamp());
                intent.putExtra("Key",dataList.get(holder.getAdapterPosition()).getKey());
                context.startActivity(intent);
            }
        });
        EventsClass dataItem = dataList.get(position);
        holder.bindData(dataItem);

    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public void searchDataList(ArrayList<EventsClass> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
    public void resetRecyclerView(DatabaseReference databaseReference) {
        List<EventsClass> newDataList = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventsClass data = snapshot.getValue(EventsClass.class);
                    if (data != null) {
                        newDataList.add(data);
                    }
                }
                // Clear existing data
                dataList.clear();
                // Add new data to the list
                dataList.addAll(newDataList);
                // Notify adapter about the changes
                notifyDataSetChanged();
                sortDataByState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    public void sortDataByState() {
        Collections.sort(dataList, new Comparator<EventsClass>() {
            @Override
            public int compare(EventsClass o1, EventsClass o2) {

                if (o1.getState().equals("Submitted") && !o2.getState().equals("Submitted")) {
                    return -1;
                } else if (o1.getState().equals("Accepted") && o2.getState().equals("Declined")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        notifyDataSetChanged();
    }
    public void filterByType(String type, DatabaseReference databaseReference) {
        databaseReference.orderByChild("dataType").equalTo(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<EventsClass> filteredDataList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventsClass data = snapshot.getValue(EventsClass.class);
                    if (data != null) {
                        filteredDataList.add(data);
                    }
                }
                dataList.clear();
                dataList.addAll(filteredDataList);
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    public void findCountofEachType(int[] array) {
        databaseReference.orderByChild("dataType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each record in the database
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventsClass data = snapshot.getValue(EventsClass.class);
                    if (data != null) {
                        String eventType = data.getDataType();
                        if ("Fire".equals(eventType)) {
                            array[0]++;
                        } else if ("Flood".equals(eventType)) {
                            array[1]++;
                        } else if ("Earthquake".equals(eventType)) {
                            array[2]++;
                        } else if ("Tornado".equals(eventType)) {
                            array[3]++;
                        } else if ("Hurricane".equals(eventType)) {
                            array[4]++;
                        } else if ("Lightning".equals(eventType)) {
                            array[5]++;
                        } else if ("Winter Storm".equals(eventType)) {
                            array[6]++;
                        }
                    }
                }
                onDataCounted(array);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void bubbleSortLocationArray(String type, String[] array) {
        databaseReference.orderByChild("dataType").equalTo(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<EventsClass> dataList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventsClass data = snapshot.getValue(EventsClass.class);
                    if (data != null) {
                        dataList.add(data);
                    }
                }

                // Bubble sort implementation
                int n = dataList.size();
                for (int i = 0; i < n - 1; i++) {
                    for (int j = 0; j < n - i - 1; j++) {
                        EventsClass data1 = dataList.get(j);
                        EventsClass data2 = dataList.get(j + 1);

                        // Parse latitude and longitude separately
                        String[] location1 = data1.getLocation().split(", ");
                        String[] location2 = data2.getLocation().split(", ");

                        // Check if location1 and location2 have enough elements
                        if (location1.length >= 2 && location2.length >= 2) {
                            // Compare latitude first
                            double lat1 = Double.parseDouble(location1[0]);
                            double lat2 = Double.parseDouble(location2[0]);
                            if (lat1 > lat2 || (lat1 == lat2 && Double.parseDouble(location1[1]) > Double.parseDouble(location2[1]))) {
                                // swap data1 and data2
                                dataList.set(j, data2);
                                dataList.set(j + 1, data1);
                            }
                        }
                    }
                }

                // After sorting, update the original array with sorted locations if it has enough length
                int arrayLength = Math.min(n, array.length);
                for (int i = 0; i < arrayLength; i++) {
                    array[i] = dataList.get(i).getLocation();
                }
                onDataSorted(dataList.subList(0, arrayLength)); // Pass only the relevant sublist for further processing
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }



    private void onDataSorted(List<EventsClass> sortedList) {
        // Clear the original dataList
        dataList.clear();
        // Add the sorted data to the original dataList
        dataList.addAll(sortedList);

        // Notify the adapter about the changes
        notifyDataSetChanged();
    }



    private void onDataCounted(int[] countArray) {
        // Ensure countArray is not empty before processing
        if (countArray.length == 0) {
            // Handle the case where countArray is empty
            return;
        }

        List<EventsClass> filteredList = new ArrayList<>();
        int[] maxArray;
        int totalCount = 0;
        do {
            maxArray = findMax(countArray); // Find the maximum value and its index
            int maxCount = maxArray[0];
            int maxIndex = maxArray[1];

            if (maxCount == 0) {
                break; // If maxCount is 0, exit the loop
            }
            totalCount += maxCount; // Add maxCount to the total count

            // Find the type corresponding to the max index
            String type = findCaseType(maxIndex);

            // Add records of the max type to the filteredList
            for (EventsClass data : dataList) {
                if (data.getDataType().equals(type)) {
                    filteredList.add(data);
                }
            }
            // Update countArray and remove the max count from it
            countArray[maxIndex] = 0;

        } while (true);
        // Update the RecyclerView with the filtered list
        dataList.clear(); // Clear the original list
        dataList.addAll(filteredList); // Add filtered records to the original list
        notifyDataSetChanged(); // Notify the adapter about the changes
    }


    public int[] findMax(int[] array) {
        int[] maxArray = new int[2];
        maxArray[0] = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxArray[0]) {
                maxArray[0] = array[i];
                maxArray[1] = i;
            }
        }
        return maxArray;
    }
    public String findCaseType (int index){
        String type = "";
        if (index == 0){
            type = "Fire";
        }
        else if (index == 1){
            type = "Flood";
        }
        else if (index == 2){
            type = "Earthquake";
        }
        else if (index == 3){
            type = "Tornado";
        }
        else if (index == 4){
            type = "Hurricane";
        }
        else if (index == 5){
            type = "Lightning";
        }
        else if (index == 6){
            type = "Winter Storm";
        }
        return type;
    }

}
class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recTitle, recType, recState;
    CardView recCard;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recType = itemView.findViewById(R.id.recType);
        recTitle = itemView.findViewById(R.id.recTitle);
        recState = itemView.findViewById(R.id.recState);
    }
    public void bindData(EventsClass eventsClass) {
        if (eventsClass != null) {
            String state = eventsClass.getState();

            recState.setText(state);

            if (state.equals("Accepted")) {
                recState.setTextColor(Color.GREEN);
            } else if (state.equals("Declined")) {
                recState.setTextColor(Color.RED);
            }
        }
    }
}