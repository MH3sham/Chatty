package app.com.example.vip.chattyapp;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    View viewFragmentGroup;
    ListView listViewGroubs;
    ArrayList mGroupsChatList = new ArrayList();
    ArrayAdapter<String> arrayAdapter;
    DatabaseReference databaseReferenceRoot;

    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewFragmentGroup = inflater.inflate(R.layout.fragment_group, container, false);

        intializeVars();
        retrieveAndDisplayChatGroups();

        listViewGroubs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String curruntGroupName = adapterView.getItemAtPosition(i).toString(); //text of item clicked
                Intent intent = new Intent(getContext(), GroupChatActivity.class);
                intent.putExtra("groupName", curruntGroupName);
                startActivity(intent);
            }
        });

        return viewFragmentGroup;
    }

    private void retrieveAndDisplayChatGroups() {
        databaseReferenceRoot = FirebaseDatabase.getInstance().getReference().child("groups");
        databaseReferenceRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mGroupsChatList.clear();
                for (DataSnapshot groupChatName : dataSnapshot.getChildren()){
                    mGroupsChatList.add(groupChatName.getKey());
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void intializeVars() {
        listViewGroubs = viewFragmentGroup.findViewById(R.id.listViewGroubs);
        arrayAdapter = new ArrayAdapter<String>(getContext() , android.R.layout.simple_list_item_1, mGroupsChatList);
        listViewGroubs.setAdapter(arrayAdapter);
    }

}
