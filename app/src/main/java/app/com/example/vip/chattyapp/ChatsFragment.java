package app.com.example.vip.chattyapp;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    View chatsView;
    RecyclerView recyclerViewChats;
    RecyclerView.Adapter mUserAdapter;
    ArrayList<User> mChatsList;
    DatabaseReference contactsRef , userRef;
    FirebaseAuth mAuth;
    String currentUserid;


    public ChatsFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getUid();

        Log.e("Token is ", FirebaseInstanceId.getInstance().getToken());

        chatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        //contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        intializeVars();
        //getContacts();


        return chatsView;
    }


    @Override
    public void onPause() {
        super.onPause();
        //mChatsList.clear();
        //mUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mChatsList.clear();
        mUserAdapter.notifyDataSetChanged();
        getContacts();
    }


    private void getContacts() {
        userRef.child(currentUserid).child("contacts").child(currentUserid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateChatList(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //mChatsList.clear();
                //mUserAdapter.notifyDataSetChanged();
                //updateChatList(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*
        //go first get all current user contacts from contacts ref > get all info about every id from user ref.
        contactsRef.child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot contactId : dataSnapshot.getChildren()){
                        if (contactId.getKey() != null){
                            userRef.child(contactId.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    mChatsList.clear();
                                    if (dataSnapshot.exists()) {
                                        String userid = "", name = "", status = "", imageUrl = "", state="";
                                        if (dataSnapshot.hasChild("uid"))
                                            userid = dataSnapshot.child("uid").getValue().toString();
                                        if (dataSnapshot.hasChild("name"))
                                            name = dataSnapshot.child("name").getValue().toString();
                                        if (dataSnapshot.hasChild("status"))
                                            status = dataSnapshot.child("status").getValue().toString();
                                        if (dataSnapshot.hasChild("image"))
                                            imageUrl = dataSnapshot.child("image").getValue().toString();
                                        if (dataSnapshot.hasChild("userState"))
                                            state = dataSnapshot.child("userState").child("state").getValue().toString();

                                        User user = new User(userid, name, status, imageUrl, state);
                                        mChatsList.add(user);
                                        mUserAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        */
    }


    private void updateChatList(DataSnapshot dataSnapshot){

        if (dataSnapshot.exists()){
            userRef.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //mChatsList.clear();
                    if (dataSnapshot.exists()) {
                        String userid = "", name = "", status = "", imageUrl = "", state="";
                        if (dataSnapshot.hasChild("uid"))
                            userid = dataSnapshot.child("uid").getValue().toString();
                        if (dataSnapshot.hasChild("name"))
                            name = dataSnapshot.child("name").getValue().toString();
                        if (dataSnapshot.hasChild("status"))
                            status = dataSnapshot.child("status").getValue().toString();
                        if (dataSnapshot.hasChild("image"))
                            imageUrl = dataSnapshot.child("image").getValue().toString();

                        User user = new User(userid, name, status, imageUrl);
                        mChatsList.add(user);
                        mUserAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void intializeVars() {
        mChatsList = new ArrayList<>();
        recyclerViewChats = chatsView.findViewById(R.id.chatsList);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChats.setNestedScrollingEnabled(false);
        recyclerViewChats.setHasFixedSize(false);
        mUserAdapter = new UserChatListAdapter(mChatsList, getContext());
        recyclerViewChats.setAdapter(mUserAdapter);



    }

}
