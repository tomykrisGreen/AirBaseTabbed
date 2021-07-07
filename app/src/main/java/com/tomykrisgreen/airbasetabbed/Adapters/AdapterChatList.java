package com.tomykrisgreen.airbasetabbed.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.ChatActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<ModelUser> userList;  // Get user info
    private HashMap<String, String> lastMessageMap;

    public AdapterChatList(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Inflate layout from row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        // Get data
        String hisUid = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        // Set data
        holder.nameTvChatList.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }
        else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_profile_white).into(holder.profileIvChatList);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_profile_white).into(holder.profileIvChatList);
        }

        // Set online status of other users in chat list
        if (userList.get(i).getOnlineStatus().equals("online")){
            // Online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {
            // Offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        // Handle click of user in chat list
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start chat activity with the user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();  // Size of the list
    }

    class MyHolder extends RecyclerView.ViewHolder{
        // Declare views from chatlist.xml
        ImageView profileIvChatList, onlineStatusIv;
        TextView nameTvChatList, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIvChatList = itemView.findViewById(R.id.profileIvChatList);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTvChatList = itemView.findViewById(R.id.nameTvChatList);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);

        }
    }
}
