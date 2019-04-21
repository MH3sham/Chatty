package app.com.example.vip.chattyapp;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    View requestView;
    RecyclerView recyclerViewRequests;
    public static RecyclerView.Adapter mRequestAdapter;
    public static ArrayList<User> mRequestsList;
    DatabaseReference requestsRef , userRef;
    FirebaseAuth mAuth;
    String currentUserid;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestView =  inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getUid();

        requestsRef = FirebaseDatabase.getInstance().getReference("chat_requests");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        intitializeVars();
        getUsersRequests();
        return requestView;
    }

    private void getUsersRequests() {
        //go to database reqRef and get all ids of user's of request type received then go to userRef and get info of those users.
        requestsRef.child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot userid : dataSnapshot.getChildren()){
                        if (userid.getKey() != null){
                            if (userid.child("request_type").getValue().equals("received")){
                                userRef.child(userid.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        mRequestsList.clear();
                                        if (dataSnapshot.exists()) {
                                            String userid ="", name = "", status = "", imageUrl = "";
                                            if (dataSnapshot.hasChild("uid"))
                                                userid = dataSnapshot.child("uid").getValue().toString();
                                            if (dataSnapshot.hasChild("name"))
                                                name = dataSnapshot.child("name").getValue().toString();
                                            if (dataSnapshot.hasChild("status"))
                                                status = dataSnapshot.child("status").getValue().toString();
                                            if (dataSnapshot.hasChild("image"))
                                                imageUrl = dataSnapshot.child("image").getValue().toString();

                                            User user = new User(userid, name, status, imageUrl);
                                            mRequestsList.add(user);
                                            mRequestAdapter.notifyDataSetChanged();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void intitializeVars() {

        mRequestsList = new ArrayList<>();
        recyclerViewRequests = requestView.findViewById(R.id.requests_listView);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRequests.setNestedScrollingEnabled(false);
        recyclerViewRequests.setHasFixedSize(false);
        mRequestAdapter = new RequestAdapter(mRequestsList , getContext(), currentUserid);
        recyclerViewRequests.setAdapter(mRequestAdapter);

    }


}
