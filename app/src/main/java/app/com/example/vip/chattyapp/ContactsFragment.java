package app.com.example.vip.chattyapp;


import android.os.Bundle;
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

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    View contactsView;
    RecyclerView recyclerViewContacts;
    RecyclerView.Adapter mUserAdapter;
    ArrayList<User> mContactsList;
    DatabaseReference contactsRef , userRef;
    FirebaseAuth mAuth;
    String currentUserid;

    public ContactsFragment() {
        // Required empty public constructor
    }


    //in this fragment we display all user's contacts (friends that he accept their request)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getUid();

        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        //contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        intializeVars();
        //getContacts();

        return contactsView;
    }


    @Override
    public void onPause() {
        super.onPause();
        //mContactsList.clear();
        //mUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mContactsList.clear();
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
                //mContactsList.clear();
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
    }


    private void updateChatList(DataSnapshot dataSnapshot){

        if (dataSnapshot.exists()){
            userRef.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //mContactsList.clear();
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
                        mContactsList.add(user);
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
        mContactsList = new ArrayList<>();
        recyclerViewContacts = contactsView.findViewById(R.id.contactsListView);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewContacts.setNestedScrollingEnabled(false);
        recyclerViewContacts.setHasFixedSize(false);
        mUserAdapter = new UserAdapter(mContactsList , getContext());
        recyclerViewContacts.setAdapter(mUserAdapter);

    }

}
