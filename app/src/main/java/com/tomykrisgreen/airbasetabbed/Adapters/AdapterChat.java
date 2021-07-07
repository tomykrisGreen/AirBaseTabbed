package com.tomykrisgreen.airbasetabbed.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.ZoomImager.ImageChatViewerActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelChat;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fbUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Inflate layout: row_chat_left for receiver, row_chat_right for sender
        if (i == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup,false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup,false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int i) {
        // Get data
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimestamp();
        String type = chatList.get(i).getType();

        // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyy hh:mm aa", cal).toString();

        if (type.equals("text")){
            // Text message
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);

            holder.timeTvIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);
        }
        else {
            // Image message
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            holder.timeTv.setVisibility(View.GONE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
        }

        // Set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        holder.timeTvIv.setText(dateTime);

        try{
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_profile_white).into(holder.profileIv);
        }
        catch (Exception e){

        }

        holder.messageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open imageView in new window
                Intent intent = new Intent(holder.itemView.getContext(), ImageChatViewerActivity.class);
                intent.putExtra("url", chatList.get(i).getMessage());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // Click to show delete dialog
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                // Show delete message confirm dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Delete");
//                builder.setMessage("Are sure you want to delete this message?");
//                // Delete button
//                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        deleteMessage(i);
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        dialogInterface.dismiss();
//                    }
//                });
//                // Create and show dialog
//                builder.create().show();
            }
        });

        // Set seen/delivered status of messages
        if (i == chatList.size()-1){
            if (chatList.get(i).isSeen()){
                holder.isSeenTv.setText("Seen");
            }
            else {
                holder.isSeenTv.setText("Delivered");
            }
        }
        else {
            holder.isSeenTv.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Logic:
        - Get time stamp of clicked message
        - Compare time stamp of clicked message with all messages in chats
        - Where both values matches, delete message
         */
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    /* If you want user to delete only his own messages,then compare sender value with current users uid */
                    if (ds.child("sender").getValue().equals(myUID)){
                        /* We can do one of the two things
                    1. Remove message from chats
                    2. Set the value of message "This message was deleted"
                     */

                        //  1. Remove message from chats
                        ds.getRef().removeValue();
                        // 2. Set the value of message "This message was deleted"
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("message", "This message was deleted");
//                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "You can only delete your messages", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Get currently signed in user
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fbUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    // View holder class
    class MyHolder extends RecyclerView.ViewHolder{
        // Views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, timeTvIv, isSeenTv;
        LinearLayout messageLayout; // For click listener - to show delete

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            timeTvIv = itemView.findViewById(R.id.timeTvIv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
