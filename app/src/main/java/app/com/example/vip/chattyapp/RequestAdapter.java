package app.com.example.vip.chattyapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    ArrayList<User> mUserList;
    Context findFriendsActivity; //create as context so u can use it with diff activities.

    String currentUserid; //for buttons accept and cancel requests
    DatabaseReference databaseReferenceChatReq, refContactsCurrentUser, refContactsVisitedUser;

    public RequestAdapter(ArrayList<User> mUserList , Context findFriendsActivity, String currentUserid) {
        this.mUserList = mUserList;
        this.findFriendsActivity = findFriendsActivity;
        this.currentUserid = currentUserid;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        RequestViewHolder rcv = new RequestViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestViewHolder holder, final int position) {

        holder.username.setText(mUserList.get(position).getName());
        holder.userstatus.setText(mUserList.get(position).getStatus());
        Glide.with(findFriendsActivity).load(Uri.parse(mUserList.get(position).getImageUrl())).placeholder(R.drawable.profile_image).into(holder.profileImage);


        //click on item listener > go to profile of clicked user
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send user to clicked user to view his profile along with his data
                String visit_userid = mUserList.get(position).getUserid();
                String username =  mUserList.get(position).getName();
                String status =  mUserList.get(position).getStatus();
                String imageProfile =  mUserList.get(position).getImageUrl();

                Intent intent = new Intent(findFriendsActivity , ProfileActivity.class);
                intent.putExtra("id", visit_userid);
                intent.putExtra("username", username);
                intent.putExtra("status", status);
                intent.putExtra("imageprofile", imageProfile);
                findFriendsActivity.startActivity(intent);

            }
        });


        holder.btnAccept.setVisibility(View.VISIBLE);
        holder.btnCancel.setVisibility(View.VISIBLE);

        //Accept request button
        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReferenceChatReq = FirebaseDatabase.getInstance().getReference("chat_requests");
                final String visted_userid = mUserList.get(position).getUserid();
                refContactsCurrentUser = FirebaseDatabase.getInstance().getReference("users").child(currentUserid).child("contacts");
                refContactsVisitedUser = FirebaseDatabase.getInstance().getReference("users").child(visted_userid).child("contacts");

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
                                                            Toast.makeText(findFriendsActivity, mUserList.get(position).getName() + " added to your contacts", Toast.LENGTH_SHORT).show();
                                                            //removeItemAt(position);
                                                            mUserList.remove(position);
                                                            RequestsFragment.mRequestAdapter.notifyDataSetChanged();
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
        });

        //Cancel request button
        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReferenceChatReq = FirebaseDatabase.getInstance().getReference("chat_requests");
                final String visted_userid = mUserList.get(position).getUserid();
                //delete all values .. so it's all clear and can send request again
                databaseReferenceChatReq.child(currentUserid).child(visted_userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            databaseReferenceChatReq.child(visted_userid).child(currentUserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(findFriendsActivity, "Request deleted", Toast.LENGTH_SHORT).show();
                                    //removeItemAt(position);
                                    mUserList.remove(position);
                                    RequestsFragment.mRequestAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView username, userstatus;
        ImageView profileImage;

        Button btnAccept, btnCancel;

        RequestViewHolder(View view){
            super(view);

            username = view.findViewById(R.id.username_item);
            userstatus = view.findViewById(R.id.userstatus_item);
            profileImage = view.findViewById(R.id.profile_image_user_item);
            btnAccept = view.findViewById(R.id.btn_accept);
            btnCancel = view.findViewById(R.id.btn_cancel);
        }
    }


    public void removeItemAt(int position) {
        mUserList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mUserList.size());
    }
}
