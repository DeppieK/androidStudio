package com.example.finalassignment;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    EditText email, password;
    FirebaseAuth auth;
    FirebaseUser user;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private String currentLocation;

    // LocationManager and LocationListener
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.editTextText);
        password = findViewById(R.id.editTextTextPassword);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        getLocation(); // Call getLocation() method here
    }

    public void signin(View view) {
        if (!email.getText().toString().isEmpty() &&
                !password.getText().toString().isEmpty()) {
            auth.signInWithEmailAndPassword(email.getText().toString(),
                    password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String userId = currentUser.getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

                            // Update user's email
                            userRef.child("email").setValue(currentUser.getEmail());

                            // Update user's current location
                            userRef.child("location").setValue(currentLocation);

                            if (email.getText().toString().contains("@sm.com")) {
                                Intent intent = new Intent(MainActivity.this, EmployeeHomepageActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(MainActivity.this, UserHomepageActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } else {
                        showMessage("Error", task.getException().getLocalizedMessage());
                    }
                }
            });
        }
    }

    private void getLocation() {
        // Initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Initialize location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Do something with the updated location (e.g., store it in the database)
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // Convert latitude and longitude to a string
                currentLocation = latitude + "," + longitude;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Request location updates
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Method to handle location permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, request location updates
                requestLocationUpdates();
            } else {
                // Location permission denied, show a toast or handle it appropriately
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void signupForm(View view) {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}