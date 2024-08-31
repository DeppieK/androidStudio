package com.example.finalassignment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    //database reference
    DatabaseReference databaseReference;
    DatabaseReference usersReference; // Add this reference

    //declaring the elements
    TextView detailDesc, detailTitle, detailType, detailLoc, detailTime, hiddenState;
    Button buttonAccept, buttonDecline;
    ImageView detailImage;

    //declaring the corresponding string variables for the elements
    String title,desc, type, loc, time,state;
    String key = "";
    String imageUrl = "";
    private static final String TAG = "detailActivity";
    static final int NOTIFICATION_ID = 1;
    static final String CHANNEL_ID = "LocationNotification";

    //declaring the map fragment and the gMap
    FrameLayout map;
    GoogleMap gMap;

    //onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        usersReference = FirebaseDatabase.getInstance().getReference("users"); // Initialize reference to "users" table

        //getting the xml elements using their id
        map = findViewById(R.id.map);
        detailDesc = findViewById(R.id.detailDesc);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailType = findViewById(R.id.detailType);
        detailTime = findViewById(R.id.detailTime);
        buttonAccept = findViewById(R.id.buttonAccept);
        buttonDecline = findViewById(R.id.buttonDecline);
        hiddenState = findViewById(R.id.state);

        //TBD
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailDesc.setText(bundle.getString("Description"));
            detailTitle.setText(bundle.getString("Title"));
            detailTime.setText(bundle.getString("Time"));
            detailType.setText(bundle.getString("Type"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }
        //getting the database instance using the db path ("Events")
        databaseReference = FirebaseDatabase.getInstance().getReference("Events").child(key);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    EventsClass eventsClass = snapshot.getValue(EventsClass.class);
                    if (eventsClass != null) {
                        //so the value of the location and the timestamp does not change
                        //when we update the db record
                        loc = eventsClass.getLocation();
                        time = eventsClass.getTimestamp();
                    }
                    if (!eventsClass.getState().equals("Submitted")){ //if the state of the event is not submitted
                        //remove the button for Accept/Decline, instead show the state
                        buttonAccept.setVisibility(View.GONE);
                        buttonDecline.setVisibility(View.GONE);
                        hiddenState.setVisibility(View.VISIBLE);
                        hiddenState.setText(eventsClass.getState());

                    }
                }
                //TBD
                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                supportMapFragment.getMapAsync(DetailActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //impl of gMap
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (loc != null) {
            String[] parts = loc.split(",");
            if (parts.length >= 2) { //check if loc contains latitude and longitude
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);

                LatLng location = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title("Marker")); //label of the marker
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15)); // Initialize it with the specific location and zoomed in
            } else {
                //handle the case where loc does not contain latitude and longitude
                Toast.makeText(this, "Location data is not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            //handle the case where loc is null
            Toast.makeText(this, "Location data is not available", Toast.LENGTH_SHORT).show();
        }
    }

    //on click function for Accept button
    public void Accept(View view) {
        // Get the values of the elements
        title = detailTitle.getText().toString().trim();
        desc = detailDesc.getText().toString().trim();
        type = detailType.getText().toString().trim();
        state = "Accepted"; // Set state as Accepted

        EventsClass eventsClass = new EventsClass(title, desc, type, imageUrl, loc, time, state); // Update the record
        // Set stateDate with the current timestamp
        eventsClass.setStateDate(DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));

        updateRecord(eventsClass);
    }

    private void updateRecord(EventsClass eventsClass) {
        databaseReference.setValue(eventsClass)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            fetchUserLocations();
                            Toast.makeText(DetailActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserLocations() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    fetchUserLocation(userSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserLocation(DataSnapshot userSnapshot) {
        UsersClass user = userSnapshot.getValue(UsersClass.class);
        String userEmail = "";
        if (user != null) {
            DatabaseReference userEmailRef = userSnapshot.getRef().child("email");
            userEmailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userEmail = dataSnapshot.getValue(String.class);
                        if (userEmail != null) {
                            getUserIdFromEmail(userEmail, new UserIdCallback() {
                                @Override
                                public void onUserIdReceived(String userId) {
                                    if (userId != null) {
                                        DatabaseReference userLocationRef = userSnapshot.getRef().child("location");
                                        userLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    String userLocation = dataSnapshot.getValue(String.class);
                                                    handleUserLocation(user, userLocation, userId);
                                                } else {
                                                    Log.d(TAG, "User location does not exist");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(DetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "User ID is null");
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "User email is null");
                        }
                    } else {
                        Log.d(TAG, "User email does not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void handleUserLocation(UsersClass user, String userLocation, String userId) {
        if (userLocation != null) {
            String[] userLocationParts = userLocation.split(",");
            double userLatitude = Double.parseDouble(userLocationParts[0]);
            double userLongitude = Double.parseDouble(userLocationParts[1]);

            String[] locParts = loc.split(",");
            double eventLatitude = Double.parseDouble(locParts[0]);
            double eventLongitude = Double.parseDouble(locParts[1]);

            double distance = calculateDistance(userLatitude, userLongitude, eventLatitude, eventLongitude);
            if (distance < 20) {
                sendNotification("There has been a serious event near you!!",userId);
            }
        } else {
            Log.d(TAG, "User location is null");
        }
    }

    private void sendNotification(String message, String userId) {
        Log.d(TAG, "Sending notification to user: " + userId);

        Intent notificationIntent = new Intent(this, DetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Notification")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent);

        // Check if the notification channel exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    // Create the notification channel if it doesn't exist
                    CharSequence name = "Location Updates";
                    String description = "Service is running in the background";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    channel = new NotificationChannel(CHANNEL_ID, name, importance);
                    channel.setDescription(description);
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }

        // Build the notification
        Notification notification = builder.build();

        // Notify
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            int notificationId = userId.hashCode(); // Generate unique notification ID
            Log.d(TAG, "Notification ID for user " + userId + ": " + notificationId);
            notificationManager.notify(notificationId, notification); // Use userId.hashCode() as notification ID
        }
    }



    // Method to calculate distance between two geographical coordinates using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }

    //on click function for Accept button
    public void Decline(View view){

        title = detailTitle.getText().toString().trim();
        desc = detailDesc.getText().toString().trim();
        title = detailTitle.getText().toString().trim();
        type = detailType.getText().toString().trim();
        state = "Declined"; //set state as Declined

        EventsClass eventsClass = new EventsClass(title, desc, type, imageUrl, loc, time, state);
        databaseReference.setValue(eventsClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(DetailActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DetailActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    public static void getUserIdFromEmail(String email, UserIdCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods().size() > 0) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                Log.d(TAG, "User ID for email " + email + ": " + userId); // Add this log
                                callback.onUserIdReceived(userId);
                            } else {
                                Log.d(TAG, "Current user is null for email " + email); // Add this log
                                callback.onUserIdReceived(null);
                            }
                        } else {
                            Log.d(TAG, "No sign-in methods found for email " + email); // Add this log
                            callback.onUserIdReceived(null);
                        }
                    } else {
                        Log.d(TAG, "Failed to fetch sign-in methods for email " + email); // Add this log
                        callback.onUserIdReceived(null);
                    }
                });
    }


    // Define a callback interface to handle the result asynchronously
    public interface UserIdCallback {
        void onUserIdReceived(String userId);
    }
}