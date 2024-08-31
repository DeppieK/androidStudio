package com.example.finalassignment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        PieChart pieChart = findViewById(R.id.pieChart);

        fab = findViewById(R.id.fab);

        databaseReference = FirebaseDatabase.getInstance().getReference("Events");

        List<EventsClass> dataList = new ArrayList<>();

        findCountofAcceptedEvents(new CountCallback() {
            @Override
            public void onCountReady(int[] array) {
                Log.d("StatisticsActivity", "Array contents after finding count: " + Arrays.toString(array));

                ArrayList<PieEntry> events = new ArrayList<>();
                String[] eventTypes = {"Fire", "Flood", "Earthquake", "Tornado", "Hurricane", "Lightning", "Winter Storm"};

                for (int i = 0; i < array.length; i++) {
                    if (array[i] > 0) {
                        events.add(new PieEntry(array[i], eventTypes[i]));
                    }
                }

                PieDataSet pieDataSet = new PieDataSet(events, "events");
                pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                pieDataSet.setValueTextColor(Color.BLACK);
                pieDataSet.setValueTextSize((16f));

                PieData pieData = new PieData(pieDataSet);

                pieChart.setData(pieData);
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText("Events");
                pieChart.animate();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getEmail() != null && user.getEmail().contains("@sm.com")) {
                    Intent intent = new Intent(StatisticsActivity.this, EmployeeHomepageActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(StatisticsActivity.this, UserHomepageActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });

    }

    public void findCountofAcceptedEvents(final CountCallback callback) {
        int[] array = new int[7]; // Initialize array

        databaseReference.orderByChild("state").equalTo("Accepted").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                Log.d("StatisticsActivity", "inside the function " + Arrays.toString(array));
                callback.onCountReady(array);
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle cancellation
            }
        });
    }

    // Callback interface
    public interface CountCallback {
        void onCountReady(int[] array);
    }
}