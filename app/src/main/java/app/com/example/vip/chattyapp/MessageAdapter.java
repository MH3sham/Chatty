package app.com.example.vip.chattyapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    ArrayList<MessageObject> messageList;
    FirebaseAuth mAuth;
    DatabaseReference refUsers;

    public MessageAdapter(ArrayList<MessageObject> messageList){
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MessageViewHolder rcv = new MessageViewHolder(layoutView);

        mAuth = FirebaseAuth.getInstance();

        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String currentUserId = mAuth.getCurrentUser().getUid();
        //get receiver Data
        refUsers = FirebaseDatabase.getInstance().getReference().child("users").child(messageList.get(position).getFromId());
        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (messageList.get(position).getType().equals("text")){

            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.receiverText.setVisibility(View.INVISIBLE);
            holder.senderText.setVisibility(View.INVISIBLE);
            holder.timeSenderText.setVisibility(View.INVISIBLE);
            holder.timeReceiverText.setVisibility(View.INVISIBLE);
            //holder.dateReceiverText.setVisibility(View.INVISIBLE);
            //holder.dateSenderText.setVisibility(View.INVISIBLE);


            //here we gonna check if the fromId = logged in user id
            if (messageList.get(position).getFromId().equals(currentUserId)){ //sender msg
                holder.senderText.setVisibility(View.VISIBLE);
                holder.timeSenderText.setVisibility(View.VISIBLE);
                holder.senderText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderText.setTextColor(Color.WHITE);
                //holder.dateSenderText.setVisibility(View.VISIBLE);

                holder.senderText.setText(messageList.get(position).getMessage());
                holder.timeSenderText.setText(messageList.get(position).getTime());
                //holder.dateSenderText.setText(messageList.get(position).getDate());

            }
            else { //receiver msg
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverText.setVisibility(View.VISIBLE);
                holder.timeReceiverText.setVisibility(View.VISIBLE);
                holder.receiverText.setBackgroundResource(R.drawable.reciver_messages_layout);
                holder.receiverText.setTextColor(Color.BLACK);
                //holder.dateReceiverText.setVisibility(View.VISIBLE);

                holder.receiverText.setText(messageList.get(position).getMessage());
                holder.timeReceiverText.setText(messageList.get(position).getTime());
                //holder.dateReceiverText.setText(messageList.get(position).getDate());

            }
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }





    class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView senderText, receiverText, dateSenderText, timeSenderText, dateReceiverText, timeReceiverText;
        CircleImageView receiverProfileImage;

        MessageViewHolder(View view){
            super(view);

            senderText = view.findViewById(R.id.sender_message_text);
            receiverText = view.findViewById(R.id.receiver_message_text);
            receiverProfileImage = view.findViewById(R.id.message_profileImage);

            //dateSenderText = view.findViewById(R.id.messageSender_date);
            timeSenderText = view.findViewById(R.id.messageSender_time);
            //dateReceiverText = view.findViewById(R.id.messageReceiver_date);
            timeReceiverText = view.findViewById(R.id.messageReceiver_time);

        }
    }
}
