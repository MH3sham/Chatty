package app.com.example.vip.chattyapp;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    Button updateSettingsProfile;
    EditText usernameEt, statusEt;
    CircleImageView profileImage;
    DatabaseReference databaseReferenceRoot;
    StorageReference profileImagesRef;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        databaseReferenceRoot = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        profileImagesRef = FirebaseStorage.getInstance().getReference().child("images"); //to save images
        intializeVars();

        getPermission();

        usernameEt.setVisibility(View.INVISIBLE); //this will be visible only if the user is new but if we got his username then we dont want him to update it.


        updateSettingsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(); //method to let user add username and update status.
            }
        });


        retrieveUserData(); //method that retrieves user's data (username , status, profileImage)

        //add image profile from gallary
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGallary = new Intent();
                intentGallary.setAction(Intent.ACTION_GET_CONTENT);
                intentGallary.setType("image/*");
                startActivityForResult(intentGallary, 1);
            }
        });


    }

/*
    @Override
    protected void onResume() {
        super.onResume();
        updateUserState("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserState("offline");

    }
*/

    public void updateUserState(String state){
        HashMap hashMap = new HashMap();
        hashMap.put("state" , state);
        databaseReferenceRoot.child("users").child(currentUserId).child("userState").updateChildren(hashMap);
    }


    private void updateSettings() {
        final String username = usernameEt.getText().toString();
        String status = statusEt.getText().toString();

        if (username.isEmpty()) {
            usernameEt.setError("Please enter your username");
            usernameEt.requestFocus();
            return;
        }
        if (status.isEmpty()) {
            status = "Hi, i'm using chatty!"; //default status
        }

        HashMap mHashMapProfile = new HashMap();
        mHashMapProfile.put("uid" , FirebaseAuth.getInstance().getUid());
        mHashMapProfile.put("name" , username);
        mHashMapProfile.put("status", status);

        databaseReferenceRoot.child("users").child(FirebaseAuth.getInstance().getUid()).updateChildren(mHashMapProfile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendUserToMainActivity();
                            Toast.makeText(SettingsActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String msgError = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + msgError, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void retrieveUserData() {
        databaseReferenceRoot.child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")){
                            String username = dataSnapshot.child("name").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();
                            usernameEt.setText(username);
                            statusEt.setText(status);
                            //Picasso.get().load(image).into(profileImage);
                            Glide.with(getApplicationContext()).load(Uri.parse(image)).into(profileImage);

                        }
                        else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")){
                            String username = dataSnapshot.child("name").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();
                            usernameEt.setText(username);
                            statusEt.setText(status);
                        }
                        else {
                            usernameEt.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    //profile image get > crop > save to storage > save to database > get from database and preview
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1 && data != null){
            //Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        //get cropped image and save it into database
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();

                //save to database storage
                final StorageReference filePath = profileImagesRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //get download link to save it in database with users info
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();  //download image link
                                //save to real-time database
                                databaseReferenceRoot.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }
    }

    private void intializeVars() {
        updateSettingsProfile = findViewById(R.id.btn_update_profile);
        usernameEt = findViewById(R.id.set_username);
        statusEt = findViewById(R.id.set_profile_status);
        profileImage = findViewById(R.id.profile_image);
        toolbar = findViewById(R.id.settings_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},1);

        }
    }
}
