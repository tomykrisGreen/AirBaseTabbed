package com.tomykrisgreen.airbasetabbed.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.GroupChatActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelGroupChatList;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_list, parent,false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
        // Get data
        ModelGroupChatList model = groupChatLists.get(position);
        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTvGroup.setText("");
        holder.timeTvGroup.setText("");
        holder.messageTvGroup.setText("");

        // load Last Message and message-time
        loadLastMessage(model, holder);

        // Set data
        holder.groupTitleTv.setText(groupTitle);

        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(holder.groupIconIv_Group);
        }
        catch (Exception e){
            holder.groupIconIv_Group.setImageResource(R.drawable.ic_group_primary);
        }

        // Handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open Group Chat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(ModelGroupChatList model, HolderGroupChatList holder) {
        // Get last message from grou
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)  // Get last item (message) from that child
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            // Get data
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String messageType = ""+ds.child("type").getValue();

                            // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyy hh:mm aa", cal).toString();

                            if (messageType.equals("image")){
                                holder.messageTvGroup.setText("Sent Photo");
                            }
                            else {
                                holder.messageTvGroup.setText(message);
                            }
//                            holder.messageTvGroup.setText(message);
                            holder.timeTvGroup.setText(dateTime);

                            // Get info of sender of lastmessage
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds: snapshot.getChildren()){
                                                String name = ""+ds.child("name").getValue();
                                                holder.nameTvGroup.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    // View Holder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{

        // UI views
        private ImageView groupIconIv_Group;
        private TextView groupTitleTv, nameTvGroup, messageTvGroup,timeTvGroup;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconIv_Group = itemView.findViewById(R.id.groupIconIv_Group);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTvGroup = itemView.findViewById(R.id.nameTvGroup);
            messageTvGroup = itemView.findViewById(R.id.messageTvGroup);
            timeTvGroup = itemView.findViewById(R.id.timeTvGroup);
        }
    }
}
