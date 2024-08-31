package com.example.finalassignment;

import static com.example.finalassignment.MainActivity.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserHomepageActivity extends AppCompatActivity {

    String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_homepage);



        // Request location permissions
        requestLocationPermissions();


    }
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted, start the service
            startLocationForegroundService();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permissions granted, start the service
                startLocationForegroundService();
            } else {
                // Location permissions denied, handle accordingly
                // You may show a message to the user or disable location-related functionality
            }
        }
    }

    private void startLocationForegroundService() {
        Intent serviceIntent = new Intent(this, LocationForegroundService.class);
        startService(serviceIntent);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            findUserByEmailAndRetrieveLocation(currentUser.getEmail());
        }
    }
    private void findUserByEmailAndRetrieveLocation(String email) {
        // Assuming you have a database structure where user locations are stored based on their email addresses
        FirebaseDatabase.getInstance().getReference("UserLocations")
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Assuming there's only one location for each user
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // Assuming each user location has "latitude" and "longitude" fields
                                double latitude = snapshot.child("latitude").getValue(Double.class);
                                double longitude = snapshot.child("longitude").getValue(Double.class);
                                location = latitude + ", " + longitude;
                            }
                        } else {
                            location = "Unknown";
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        location = "Unknown";
                    }
                });
    }

    public void createEvent(View view){
        Intent intent = new Intent(UserHomepageActivity.this, SubmitEventActivity.class);
        startActivity(intent);
        finish();
    }
    public void viewStatistics(View view){
        Intent intent = new Intent(UserHomepageActivity.this, StatisticsActivity.class);
        startActivity(intent);
        finish();
    }
}