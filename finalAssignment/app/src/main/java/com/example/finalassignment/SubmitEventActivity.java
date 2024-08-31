package com.example.finalassignment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Calendar;

public class SubmitEventActivity
        extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private FusedLocationProviderClient fusedLocationClient;
    String[] catastrophicEvents = {"Fire", "Flood",
            "Earthquake", "Tornado", "Hurricane",
            "Lightning", "Winter Storm"};
    ImageView uploadImage;
    Button submitButton;
    EditText eventDescription, eventName;
    String imageURL, location;
    Spinner spinner;
    Uri uri;
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_event);

        uploadImage = findViewById(R.id.uploadImage);
        eventName = findViewById(R.id.editTextEventName);
        eventDescription = findViewById(R.id.editTextDescription);
        submitButton = findViewById(R.id.buttonSubmitEvent);

        fab = findViewById(R.id.fab);

        spinner = findViewById(R.id.typeSpinner);
        spinner.setOnItemSelectedListener(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        ArrayAdapter<String> ad = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                catastrophicEvents);

        ad.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(ad);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            uploadImage.setImageURI(uri);
                        } else {
                            Toast.makeText(SubmitEventActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubmitEventActivity.this, UserHomepageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void saveData() {
        if (uri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Android Images")
                    .child(uri.getLastPathSegment());
            AlertDialog.Builder builder = new AlertDialog.Builder(SubmitEventActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress_layout);
            AlertDialog dialog = builder.create();
            dialog.show();
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete()) ;
                    Uri urlImage = uriTask.getResult();
                    imageURL = urlImage.toString();
                    SubmitEvent();
                    dialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void SubmitEvent() {

        String title = eventName.getText().toString();
        String desc = eventDescription.getText().toString();
        String type = spinner.getSelectedItem().toString();
        String timestamp = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        String state = "Submitted";
        Log.d("SubmitEventActivity","Location retrieved: " +location);
        EventsClass eventsClass = new EventsClass(title, desc, type, imageURL, location, timestamp, state);

        FirebaseDatabase.getInstance().getReference("Events").child(timestamp)
                .setValue(eventsClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SubmitEventActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SubmitEventActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location loc) {
                        Log.d("getLocationBeforeIf", "Location retrieved: " + location);
                        if (loc != null) {
                            Log.d("Location!=null", "Location retrieved: " + location);

                            double latitude = loc.getLatitude();
                            double longitude = loc.getLongitude();

                            location = latitude + ", " + longitude;
                        } else {
                            location = "Unknown";
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        location = "Unknown";
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

}