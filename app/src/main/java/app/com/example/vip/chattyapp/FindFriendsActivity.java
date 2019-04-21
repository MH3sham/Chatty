package app.com.example.vip.chattyapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FindFriendsActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mRecyclerAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<User> mUserList , mSearchedUserList;
    ArrayList<String> userNames;
    Toolbar toolbar;
    DatabaseReference referenceRoot;

    FirebaseAuth mAuth;
    String currentUserId;

    ImageButton searchBtn;
    TextView findFriendsTV;
    AutoCompleteTextView searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();
        referenceRoot = FirebaseDatabase.getInstance().getReference();

        userNames = new ArrayList<>();
        mSearchedUserList = new ArrayList<>();

        intializeVars();
        getUsersFromDatabase();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findFriendsTV.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.INVISIBLE);
                searchEditText.setVisibility(View.VISIBLE);
            }
        });

       searchFunction();
    }

    private void searchFunction() {
        /*
        we need to search for user by his username so first we got all usernames in list named userNames then we gonna set setOnItemClickListener
        on the list that created from the autocompleteText so if we clicked on the username we will get his object from the main list named mUserList
        and add this object to a new ArrayList<User> named mSearchedUserList and we do that in a new arrayList bCOz we can't modify the mainList while
        we still iterating on it.. after that we just assign this new list to our adapter to update the recycler view with the searched name ONLY.
         */

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this ,android.R.layout.simple_list_item_1, userNames);
        searchEditText.setAdapter(arrayAdapter);
        searchEditText.setThreshold(1); // this make the results shown after writing the first letter
        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(FindFriendsActivity.this, searchEditText.getText() + " selected", Toast.LENGTH_LONG).show();

                for (User mSearchedUser : mUserList){
                    if (mSearchedUser.getName().contentEquals(searchEditText.getText())){
                        mSearchedUserList.clear();
                        mSearchedUserList.add(mSearchedUser);
                        mRecyclerAdapter = new FindFriendsListAdapter(mSearchedUserList , FindFriendsActivity.this);
                        mRecyclerView.setAdapter(mRecyclerAdapter);
                        mRecyclerAdapter.notifyDataSetChanged();
                    }
                }

            }
        });
    }

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


    public void updateUserState(String state){
        HashMap hashMap = new HashMap();
        hashMap.put("state" , state);
        referenceRoot.child("users").child(currentUserId).child("userState").updateChildren(hashMap);
    }


    private void getUsersFromDatabase() {
        referenceRoot.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserList.clear();
                userNames.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot mDataSnapshotUser : dataSnapshot.getChildren()){
                        String userid="", name="", status="", imageUrl="";
                        if (mDataSnapshotUser.hasChild("uid"))
                            userid = mDataSnapshotUser.child("uid").getValue().toString();
                        if (mDataSnapshotUser.hasChild("name"))
                            name = mDataSnapshotUser.child("name").getValue().toString();
                        if (mDataSnapshotUser.hasChild("status"))
                            status = mDataSnapshotUser.child("status").getValue().toString();
                        if (mDataSnapshotUser.hasChild("image"))
                            imageUrl = mDataSnapshotUser.child("image").getValue().toString();

                        User user = new User(userid, name, status, imageUrl);
                        mUserList.add(user);
                        userNames.add(name);
                        mRecyclerAdapter.notifyDataSetChanged();

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void intializeVars() {
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar = findViewById(R.id.findfriends_chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.bar_search_findfriends, null);
        searchBtn = findViewById(R.id.search_button);
        searchEditText = findViewById(R.id.search_edittext);
        findFriendsTV = findViewById(R.id.findfriends_textview);
        actionBar.setCustomView(actionBarView);


        mUserList = new ArrayList<>();
        mRecyclerView= findViewById(R.id.find_friends_recView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerAdapter = new FindFriendsListAdapter(mUserList , FindFriendsActivity.this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }
}
