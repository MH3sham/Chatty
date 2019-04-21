package app.com.example.vip.chattyapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView usernameTV, statusTV;
    ImageView pImage;
    Button sendMessageReqBtn , declineMessageBtn;

    String visted_userid, username, status, profileImage, currentUserid, current_State;
    FirebaseAuth mAuth;
    DatabaseReference databaseReferenceChatReq, refContactsCurrentUser,refContactsVisitedUser, databaseReferenceNotifications , rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getUid();

        visted_userid = getIntent().getExtras().get("id").toString();
        username = getIntent().getExtras().get("username").toString();
        status = getIntent().getExtras().get("status").toString();
        profileImage = getIntent().getExtras().get("imageprofile").toString();


        rootRef = FirebaseDatabase.getInstance().getReference();
        databaseReferenceChatReq = FirebaseDatabase.getInstance().getReference("chat_requests");
        refContactsCurrentUser = FirebaseDatabase.getInstance().getReference("users").child(currentUserid).child("contacts");
        refContactsVisitedUser = FirebaseDatabase.getInstance().getReference("users").child(visted_userid).child("contacts");
        databaseReferenceNotifications = FirebaseDatabase.getInstance().getReference("notifications");



        //Toast.makeText(ProfileActivity.this, userid, Toast.LENGTH_SHORT).show();

        intializeVars();
        getVisitedUserInfo(visted_userid, username, status, profileImage);

        current_State = "new";

        manageChatRequest();



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
        rootRef.child("users").child(currentUserid).child("userState").updateChildren(hashMap);
    }

    private void manageChatRequest() {
        //first we need to check if user already sent/received a request to/from the visited user OR this visited user is a friend of this current user.
        databaseReferenceChatReq.child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(visted_userid)){
                    String request_type = dataSnapshot.child(visted_userid).child("request_type").getValue().toString();
                    if (request_type.equals("sent")){
                        current_State = "request_sent";
                        sendMessageReqBtn.setText("Cancel Chat Request");
                    }

                    //check if current user received request then the sender profile will have 2 buttons i can press on accept or decline his chat.
                    if (request_type.equals("received")){
                        current_State = "request_received"; //line 127 u can accept
                        sendMessageReqBtn.setText("Accept Chat Request");
                        declineMessageBtn.setText("Decline Chat Request"); // u can decline
                        declineMessageBtn.setVisibility(View.VISIBLE);
                        declineMessageBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelChatRequest();
                            }
                        });
                    }
                }
                else {
                    // he's a friend
                    //user doesn't have chat requests for this visited user but he might has him as a contact so if he has, he can remove him.
                    refContactsCurrentUser.child(currentUserid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(visted_userid)){
                                current_State = "friends";
                                sendMessageReqBtn.setText("Remove Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //if user haven't sent a request then he'll be able to send one
        if (!currentUserid.equals(visted_userid)){
            sendMessageReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageReqBtn.setEnabled(false); //u can only send one request.
                    //first case if this user is new to u and u want to send him a chat request
                    if (current_State.equals("new")){
                        sendChatRequest();
                    }
                    //if u already send a user a chat request but u want to cancel it.
                    if (current_State.equals("request_sent")){
                        cancelChatRequest();
                    }
                    //if u received a chat request
                    if (current_State.equals("request_received")){
                        acceptChatRequest();
                    }
                    //if u want to remove a contact
                    if (current_State.equals("friends")){
                        removeFriendContact();
                    }

                }
            });
        }else {
            //if it's my profile so u cant chat with urself xD
            sendMessageReqBtn.setVisibility(View.INVISIBLE);
            sendMessageReqBtn.setEnabled(false);
        }
    }

    private void removeFriendContact() {
        //delete all values from contacts .. so it's all clear and can send request again
        refContactsCurrentUser.child(currentUserid).child(visted_userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    refContactsVisitedUser.child(visted_userid).child(currentUserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sendMessageReqBtn.setEnabled(true);
                            current_State = "new";
                            sendMessageReqBtn.setText("Send Chat Request");

                            declineMessageBtn.setEnabled(false);
                            declineMessageBtn.setVisibility(View.INVISIBLE);

                            //ChatsFragment.getContacts();
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest() {
        //add request sender as a contact for the current id and also add the current user as a contact for the sender. (they both gonna be contacts at each others)
        refContactsCurrentUser.child(currentUserid).child(visted_userid).setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    refContactsVisitedUser.child(visted_userid).child(currentUserid).setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //now we saved as contacts so delete chat request from database
                                databaseReferenceChatReq.child(currentUserid).child(visted_userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            databaseReferenceChatReq.child(visted_userid).child(currentUserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    sendMessageReqBtn.setEnabled(true);
                                                    current_State = "friends";
                                                    sendMessageReqBtn.setText("Remove Contact");

                                                    declineMessageBtn.setEnabled(false);
                                                    declineMessageBtn.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }


    private void sendChatRequest() {
        //go to chat req > current user > visited User > add request sent
        databaseReferenceChatReq.child(currentUserid).child(visted_userid).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //go to chat req > visited user > current User > add request received
                    databaseReferenceChatReq.child(visted_userid).child(currentUserid).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            //add notification id to the user id who the request send to in the notification node.
                            HashMap notificationRequestMap = new HashMap();
                            notificationRequestMap.put("from" , currentUserid);
                            notificationRequestMap.put("type", "request");
                            databaseReferenceNotifications.child(visted_userid).push().setValue(notificationRequestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendMessageReqBtn.setEnabled(true);
                                        sendMessageReqBtn.setText("Cancel Chat Request");
                                        current_State = "request_sent";
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    private void cancelChatRequest() {
        //delete all values .. so it's all clear and can send request again
        databaseReferenceChatReq.child(currentUserid).child(visted_userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    databaseReferenceChatReq.child(visted_userid).child(currentUserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sendMessageReqBtn.setEnabled(true);
                            current_State = "new";
                            sendMessageReqBtn.setText("Send Chat Request");

                            declineMessageBtn.setEnabled(false);
                            declineMessageBtn.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

    }


    private void getVisitedUserInfo(String userid, String username, String status, String profileImage) {
        usernameTV.setText(username);
        statusTV.setText(status);
        Glide.with(ProfileActivity.this).load(Uri.parse(profileImage)).placeholder(R.drawable.profile_image).into(pImage);
    }

    private void intializeVars() {
        usernameTV = findViewById(R.id.username_visit);
        statusTV = findViewById(R.id.status_visit);
        pImage = findViewById(R.id.profile_image_visit);
        sendMessageReqBtn = findViewById(R.id.btn_send_message_visit);
        declineMessageBtn = findViewById(R.id.btn_decline_message_visit);
    }


}
