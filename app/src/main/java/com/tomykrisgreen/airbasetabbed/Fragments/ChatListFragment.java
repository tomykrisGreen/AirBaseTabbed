package com.tomykrisgreen.airbasetabbed.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterChatList;
import com.tomykrisgreen.airbasetabbed.GroupCreateActivity;
import com.tomykrisgreen.airbasetabbed.MainActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelChat;
import com.tomykrisgreen.airbasetabbed.Models.ModelChatList;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;
import com.tomykrisgreen.airbasetabbed.R;
import com.tomykrisgreen.airbasetabbed.SettingsActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatList> chatListList;
    List<ModelUser> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatList adapterChatList;

    ProgressBar progressBarChatList;
//    TextView noChatList;

    public ChatListFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView_chatList);

        progressBarChatList = view.findViewById(R.id.progressBarChatList);
//        noChatList = view.findViewById(R.id.noChatList);

        chatListList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatListList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatList chatList = ds.getValue(ModelChatList.class);
                    chatListList.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        progressBarChatList.setVisibility(View.VISIBLE);
//        noChatList.setVisibility(View.VISIBLE);

        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);
                    for (ModelChatList chatList: chatListList){
                        if (user.getUid() != null && user.getUid().equals(chatList.getId())){
                            userList.add(user);

                            progressBarChatList.setVisibility(View.GONE);
//                            noChatList.setVisibility(View.GONE);
                            break;

                        }
                    }
                    // Adapter
                    adapterChatList = new AdapterChatList(getContext(), userList);
                    // Set adapter
                    recyclerView.setAdapter(adapterChatList);

                    // Set last message
                    for (int i = 0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarChatList.setVisibility(View.GONE);
//                noChatList.setVisibility(View.GONE);
            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null){
                        continue;
                    }

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())){
                        // instead of displaying url in message, show image
                        if (chat.getType().equals("image")){
                            theLastMessage = "Sent a photo";
                        }
                        else {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatList.setLastMessageMap(userId, theLastMessage);
                adapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            // User is signed in
            // Set email of logged in user
            //mProfileTV.setText(user.getEmail());
        }
        else {
            // User not signed in, go to Main Activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);  // To show menu in fragment
        super.onCreate(savedInstanceState);
    }

    // Inflate options menu
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        // Inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        // Hide add post icon from this fragment
        menu.findItem(R.id.action_shop).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }



    // Handle menu items clicks

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();

            checkUserStatus();
        }
        else if (id == R.id.action_settings){
            // Go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        else if (id == R.id.action_create_group){
            // Go to settings activity
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}