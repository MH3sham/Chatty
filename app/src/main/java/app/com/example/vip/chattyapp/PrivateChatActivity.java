package app.com.example.vip.chattyapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PrivateChatActivity extends AppCompatActivity {
    Toolbar mToolbar;
    ImageButton imageButtonSend;
    EditText mMessageEditText;
    ScrollView scrollView;
    TextView mMsgTextView;
    DatabaseReference rootRef;
    String currentGroupName, currentUserID, currentUserName, currentDate , currentTime;
    String visted_userid, visted_username, visted_status, visted_profileImageUrl, currentUserid, current_State;
    FirebaseAuth mAuth;

    ImageView visitedProfileImage , visitedStateImage;
    TextView visitedUsernameTV , visitedStatusTV;
    String messageKey;

    private RecyclerView mChatRecyclerView, mMedia;
    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager, mMediaLayoutManager;
    ArrayList<MessageObject> messageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getUid();

        visted_userid = getIntent().getExtras().get("id").toString();
        visted_username = getIntent().getExtras().get("username").toString();
        visted_status = getIntent().getExtras().get("status").toString();
        visted_profileImageUrl = getIntent().getExtras().get("imageprofile").toString();


        intializeVars();

        getVisitedUserState(); //online or offline
        visitedUsernameTV.setText(visted_username);
        visitedStatusTV.setText(visted_status);
        Glide.with(PrivateChatActivity.this).load(Uri.parse(visted_profileImageUrl)).placeholder(R.drawable.profile_image).into(visitedProfileImage);

        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


    }

    private void getVisitedUserState() {

        rootRef.child("users").child(visted_userid).child("userState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String state = "";
                    if(dataSnapshot.hasChild("state")){
                        state = dataSnapshot.child("state").getValue().toString();

                        if (state.equals("online")){
                            visitedStateImage.setVisibility(View.VISIBLE);
                        }
                        else {
                            visitedStateImage.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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


    // this method updates current user's state so that others can see that he's online or offline
    public void updateUserState(String state){
        HashMap hashMap = new HashMap();
        hashMap.put("state" , state);
        rootRef.child("users").child(currentUserID).child("userState").updateChildren(hashMap);
    }


    //retriveing all messages
    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("messages").child(currentUserID).child(visted_userid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //MessageObject messageObject = dataSnapshot.getValue(MessageObject.class);
                //messageList.add(messageObject);
                //mChatAdapter.notifyDataSetChanged();

                String fromid = "", message = "", type = "", date="" , time="";
                if (dataSnapshot.hasChild("from"))
                    fromid = dataSnapshot.child("from").getValue().toString();
                if (dataSnapshot.hasChild("message"))
                    message = dataSnapshot.child("message").getValue().toString();
                if (dataSnapshot.hasChild("type"))
                    type = dataSnapshot.child("type").getValue().toString();
                if (dataSnapshot.hasChild("date"))
                    date = dataSnapshot.child("date").getValue().toString();
                if (dataSnapshot.hasChild("time"))
                    time = dataSnapshot.child("time").getValue().toString();


                MessageObject messageObject = new MessageObject(fromid, message, type, date, time);
                messageList.add(messageObject);
                mChatAdapter.notifyDataSetChanged();
                mChatRecyclerView.scrollToPosition(messageList.size() - 1);
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

    /*
    SendMessage() Explanation:
   in this send message method we save the msg in 2 places:
   1- currentUser path :::  logged in userId > receiverId > then we put the msg along with the senderId (fromId) which is loggedIn user
   2- receiverUser Path ::  receiverId > logged in userId > then we put the msg along with the senderId (fromId) which is loggedIn user

   -so the msg is saved with 2 paths BUT both have the same msg key and same fromId key which is the id of the sender.
   -on the other hand on the receiver device he's the sender on his acc so msg he send will get the fromId of his as he's the sender in this case.
   -so when we gonna retrive it we gonna check in the adapter class if the id is = to the loggedin user so will get the msg in
   style of sender, otherwise will be style of receiver.

    */
    private void sendMessage() {
        final String messageText = mMessageEditText.getText().toString();
        if (!TextUtils.isEmpty(messageText)){
            messageKey = rootRef.child("messages").child(currentUserID).child(visted_userid).push().getKey();

            final String saveCurrentDate, saveCurrentTime;
            Calendar calendar = Calendar.getInstance();
            //get date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = dateFormat.format(calendar.getTime());

            //get time
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = timeFormat.format(calendar.getTime());

            //add message to sender id
            DatabaseReference senderRef = rootRef.child("messages").child(currentUserID).child(visted_userid).child(messageKey);
            final HashMap hashMapMessageSender = new HashMap();
            hashMapMessageSender.put("message" , messageText);
            hashMapMessageSender.put("type" , "text");
            hashMapMessageSender.put("from" , currentUserID);
            hashMapMessageSender.put("date" , saveCurrentDate);
            hashMapMessageSender.put("time" , saveCurrentTime);

            senderRef.updateChildren(hashMapMessageSender).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    //add message to Receiver id
                    DatabaseReference receiverRef = rootRef.child("messages").child(visted_userid).child(currentUserID).child(messageKey);
                    HashMap hashMapMessagereceiver = new HashMap();
                    hashMapMessagereceiver.put("message" , messageText);
                    hashMapMessagereceiver.put("type" , "text");
                    hashMapMessagereceiver.put("from" , currentUserID);
                    hashMapMessagereceiver.put("date" , saveCurrentDate);
                    hashMapMessagereceiver.put("time" , saveCurrentTime);

                    receiverRef.updateChildren(hashMapMessagereceiver).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            //Toast.makeText(PrivateChatActivity.this , "Message Sent" , Toast.LENGTH_SHORT).show();
                            mMessageEditText.setText(null);
                        }
                    });
                }
            });
        }
    }




    @SuppressLint("WrongConstant")
    private void intializeVars() {
        //Toolbar Stuff
        mToolbar = findViewById(R.id.privateChat_chat_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_tool_bar, null);
        visitedProfileImage = findViewById(R.id.toolbar_profile_image);
        visitedUsernameTV = findViewById(R.id.toolbar_username);
        visitedStatusTV = findViewById(R.id.toolbar_status);
        visitedStateImage = findViewById(R.id.user_online_privateChatimage);
        actionBar.setCustomView(actionBarView);


        imageButtonSend = findViewById(R.id.sendMessage_privateChat);
        mMessageEditText = findViewById(R.id.messageEditText_privateChat);
        messageList = new ArrayList<>();
        mChatRecyclerView = findViewById(R.id.messageList_privateChat);
        mChatRecyclerView.setNestedScrollingEnabled(false);
        mChatRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        ((LinearLayoutManager) mChatLayoutManager).setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(messageList);
        mChatRecyclerView.setAdapter(mChatAdapter);
    }
}
