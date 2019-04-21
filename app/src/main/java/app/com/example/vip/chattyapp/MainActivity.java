package app.com.example.vip.chattyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    ViewPager mViewPager;
    TabLayout mTabLayout;
    TabsFragmentsAccessorAdapter mTabsFragmentsAccessorAdapter;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference databaseReferenceRoot;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chatty");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = mAuth.getUid();

        databaseReferenceRoot = FirebaseDatabase.getInstance().getReference();

        //create xml design of ViewPager and Tablayout >> define ur fragments in Adapter >> Initialize all like below.
        mViewPager = findViewById(R.id.main_tabs_pager);
        mTabsFragmentsAccessorAdapter = new TabsFragmentsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsFragmentsAccessorAdapter);
        mTabLayout = findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfNoInternetDisplayDialogElseLoadMyData();
        if (currentUser == null){
            //to check if the user already login or not
            sendUserToLoginActivity();
        }
        else
        {
            //if there's a user logged in we need to make sure that he has a name.
            verifyUsername();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            updateUserCurrentState("online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUser != null) {
            updateUserCurrentState("offline");
        }
    }



    private void verifyUsername() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReferenceRoot.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()){
                    //Toast.makeText(MainActivity.this , "Welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createNewChatGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Enter group name");
        final EditText groupNameET = new EditText(MainActivity.this);
        groupNameET.setHint("e.g Friends Chat");
        builder.setView(groupNameET);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String groupName = groupNameET.getText().toString();
                if (groupName.isEmpty()){
                    Toast.makeText(MainActivity.this , "Please add group name", Toast.LENGTH_SHORT).show();
                }
                else {
                    databaseReferenceRoot.child("groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(MainActivity.this ,  groupName + "group created successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this , LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this , SettingsActivity.class);
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        //finish();
    }

    private void sendUserToFindFriendsActivity() {
        Intent findIntent = new Intent(MainActivity.this , FindFriendsActivity.class);
        startActivity(findIntent);
    }


    //this method will update the user state as online or offline and that happens when user start the app >> online OR when he stops it or crashed >> offline
    //this method will be static so we can use it in another class
    public void updateUserCurrentState(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calendar = Calendar.getInstance();
        //get date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = dateFormat.format(calendar.getTime());

        //get time
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = timeFormat.format(calendar.getTime());

        HashMap hashMap = new HashMap();
        hashMap.put("time" , saveCurrentTime);
        hashMap.put("date" , saveCurrentDate);
        hashMap.put("state" , state);

        //save to database
        databaseReferenceRoot.child("users").child(currentUserId).child("userState").updateChildren(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.logout){
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        //if (item.getItemId() == R.id.createGroupChat){
            //createNewChatGroup();
        //}
        if (item.getItemId() == R.id.settings){
            Intent settingsIntent = new Intent(MainActivity.this , SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if (item.getItemId() == R.id.find_friends){
            sendUserToFindFriendsActivity();
        }
        return true;
    }




    private void checkIfNoInternetDisplayDialogElseLoadMyData() {
        //method that check if no internet then display AlertDialg says "No internet connection!" else run the thread and get my data.
        ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null){
            new AlertDialog.Builder(MainActivity.this, R.style.myDialog)
                    .setTitle(getResources().getString(R.string.app_name))
                    .setMessage(getResources().getString(R.string.internet_error))
                    .setIcon(R.drawable.ic_launcher)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //at this point i am trying to exit the app
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            System.exit(0);
                        }
                    }).show();
        }
    }



}
