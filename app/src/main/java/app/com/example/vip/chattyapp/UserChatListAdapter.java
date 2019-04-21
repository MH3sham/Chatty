package app.com.example.vip.chattyapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserChatListAdapter extends RecyclerView.Adapter<UserChatListAdapter.UserViewHolder> {


    ArrayList<User> mUserList;
    Context context; //create as context so u can use it with diff activities.

    public UserChatListAdapter(ArrayList<User> mUserList , Context context) {
        this.mUserList = mUserList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserChatListAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        UserChatListAdapter.UserViewHolder rcv = new UserChatListAdapter.UserViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserChatListAdapter.UserViewHolder holder, final int position) {

        holder.username.setText(mUserList.get(position).getName());
        holder.userstatus.setText(mUserList.get(position).getStatus());
        Glide.with(context).load(Uri.parse(mUserList.get(position).getImageUrl())).placeholder(R.drawable.profile_image).into(holder.profileImage);

        /*
        if (mUserList.get(position).getStateOnOff().equals("online")){
            holder.stateImage.setVisibility(View.VISIBLE);
        }else {
            holder.stateImage.setVisibility(View.INVISIBLE);
        }

       */

        //click on item listener > go to profile of clicked user
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send user to clicked user to view his profile along with his data
                String visit_userid = mUserList.get(position).getUserid();
                String username =  mUserList.get(position).getName();
                String status =  mUserList.get(position).getStatus();
                String imageProfile =  mUserList.get(position).getImageUrl();

                Intent intent = new Intent(context, PrivateChatActivity.class);
                intent.putExtra("id", visit_userid);
                intent.putExtra("username", username);
                intent.putExtra("status", status);
                intent.putExtra("imageprofile", imageProfile);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        TextView username, userstatus;
        ImageView profileImage;
        //ImageView stateImage;

        UserViewHolder(View view){
            super(view);

            username = view.findViewById(R.id.username_item);
            userstatus = view.findViewById(R.id.userstatus_item);
            profileImage = view.findViewById(R.id.profile_image_user_item);
            //stateImage = view.findViewById(R.id.user_online_image);


        }
    }
}
