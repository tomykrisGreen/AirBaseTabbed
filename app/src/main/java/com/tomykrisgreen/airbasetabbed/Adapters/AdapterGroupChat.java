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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.ZoomImager.ImageChatViewerActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelGroupChat;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    Context context;
    ArrayList<ModelGroupChat> modelGroupChatsList;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatsList) {
        this.context = context;
        this.modelGroupChatsList = modelGroupChatsList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout: row_chat_left for receiver, row_chat_right for sender
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        // Get data
        ModelGroupChat model = modelGroupChatsList.get(position);
        String timestamp = model.getTimestamp();
        String message = model.getMessage();  // If text message then contains message; if image message then contains url of the image stored in firebase storage
        String senderUid = model.getSender();
        String messageType = model.getType();

        // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyy hh:mm aa", cal).toString();

        // Set data
        if (messageType.equals("text")) {
            // Hide imageView
            holder.messageIv_GroupChat.setVisibility(View.GONE);
            holder.messageTv_GroupChat.setVisibility(View.VISIBLE);
            holder.messageTv_GroupChat.setText(message);

            holder.timeTv_GroupChat_Iv.setVisibility(View.GONE);
        } else {
            // Hide text view
            holder.messageIv_GroupChat.setVisibility(View.VISIBLE);
            holder.messageTv_GroupChat.setVisibility(View.GONE);

            holder.timeTv_GroupChat.setVisibility(View.GONE);
            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv_GroupChat);
            } catch (Exception e) {
                holder.messageIv_GroupChat.setImageResource(R.drawable.ic_image_black);
            }
        }

        holder.timeTv_GroupChat.setText(dateTime);
        holder.timeTv_GroupChat_Iv.setText(dateTime);

        setUserName(model, holder);

        holder.messageIv_GroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open imageView in new window
                Intent intent = new Intent(holder.itemView.getContext(), ImageChatViewerActivity.class);
                intent.putExtra("url", modelGroupChatsList.get(position).getMessage());
                holder.itemView.getContext().startActivity(intent);
            }
        });


//        // Click to show delete dialog
//        holder.messageLayoutGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Show delete message confirm dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Delete");
//                builder.setMessage("Are sure you want to delete this message?");
//                // Delete button
//                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        deleteMessage(position);
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
//            }
//        });
    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Logic:
        - Get time stamp of clicked message
        - Compare time stamp of clicked message with all messages in chats
        - Where both values matches, delete message
         */
        String msgTimeStamp = modelGroupChatsList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Groups");
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

    private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();

                            holder.nameTv_GroupChat.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatsList.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {
        private TextView nameTv_GroupChat, messageTv_GroupChat, timeTv_GroupChat, timeTv_GroupChat_Iv;
        private ImageView messageIv_GroupChat;
        LinearLayout messageLayoutGroup; // For click listener - to show delete

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            nameTv_GroupChat = itemView.findViewById(R.id.nameTv_GroupChat);
            messageTv_GroupChat = itemView.findViewById(R.id.messageTv_GroupChat);
            timeTv_GroupChat = itemView.findViewById(R.id.timeTv_GroupChat);
            timeTv_GroupChat_Iv = itemView.findViewById(R.id.timeTv_GroupChat_Iv);
            messageIv_GroupChat = itemView.findViewById(R.id.messageIv_GroupChat);
            messageLayoutGroup = itemView.findViewById(R.id.messageLayoutGroup);
        }
    }
}
