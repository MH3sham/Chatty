package app.com.example.vip.chattyapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton imageButtonSend;
    EditText mMessageEditText;
    ScrollView scrollView;
    TextView mMsgTextView;
    DatabaseReference databaseReferenceUsers;
    String currentGroupName, currentUserID, currentUserName, currentDate , currentTime;

    private RecyclerView mChatRecyclerView, mMedia;
    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager, mMediaLayoutManager;
    ArrayList<MessageObject> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("users");

        intializeVars();

        getUserInfo(); //this method gets username

        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessageIntoDatabase(); //this method saves msgs into database
                mMessageEditText.setText(null);
            }
        });

        displayMessages();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void displayMessages() {
        //retrieve all previous msgs on start of the chat activity
        DatabaseReference databaseReferenceMessage = FirebaseDatabase.getInstance().getReference().child("groups").child(currentGroupName);
        databaseReferenceMessage.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {

                    String messageId = "", name = "", message = "", date = "", time = "";
                    if (dataSnapshot.getKey() != null) {
                        messageId = dataSnapshot.getKey();
                    }
                    if (dataSnapshot.child("name").getValue() != null) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if (dataSnapshot.child("message").getValue() != null) {
                        message = dataSnapshot.child("message").getValue().toString();
                    }
                    if (dataSnapshot.child("date").getValue() != null) {
                        date = dataSnapshot.child("date").getValue().toString();
                    }
                    if (dataSnapshot.child("time").getValue() != null) {
                        time = dataSnapshot.child("time").getValue().toString();
                    }

                    //MessageObject messageObject = new MessageObject(messageId, name, message, date, time);
                    //messageList.add(messageObject);
                    mChatLayoutManager.scrollToPosition(messageList.size() - 1); //to scroll directly to the latest msg
                    mChatAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveMessageIntoDatabase() {
        //get message
        String message = mMessageEditText.getText().toString();
        if (TextUtils.isEmpty(message)){
            //Display a toast
        }
        else {
            //get current date
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = dateFormat.format(calendarDate.getTime());
            //get current time
            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = timeFormat.format(calendarTime.getTime());


            //save to database
            String messageKey = FirebaseDatabase.getInstance().getReference().child("groups").child(currentGroupName).push().getKey();
            DatabaseReference databaseReferenceMessage = FirebaseDatabase.getInstance().getReference().child("groups").child(currentGroupName).child(messageKey);
            HashMap hashMap = new HashMap();
            hashMap.put("name", currentUserName);
            hashMap.put("message" , message);
            hashMap.put("date", currentDate);
            hashMap.put("time", currentTime);

            databaseReferenceMessage.updateChildren(hashMap);
        }
    }

    private void getUserInfo() {
        databaseReferenceUsers.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("WrongConstant")
    private void intializeVars() {
        mToolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        imageButtonSend = findViewById(R.id.sendMessage);
        mMessageEditText = findViewById(R.id.messageEditText);
        messageList = new ArrayList<>();
        mChatRecyclerView = findViewById(R.id.messageList);
        mChatRecyclerView.setNestedScrollingEnabled(false);
        mChatRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        ((LinearLayoutManager) mChatLayoutManager).setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(messageList);
        mChatRecyclerView.setAdapter(mChatAdapter);
    }
}
