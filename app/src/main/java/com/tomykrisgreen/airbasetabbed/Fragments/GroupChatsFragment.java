package com.tomykrisgreen.airbasetabbed.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
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
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterGroupChatList;
import com.tomykrisgreen.airbasetabbed.GroupCreateActivity;
import com.tomykrisgreen.airbasetabbed.MainActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelGroupChatList;
import com.tomykrisgreen.airbasetabbed.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class GroupChatsFragment extends Fragment {
    private RecyclerView groupRV;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelGroupChatList> groupChatLists;
    private AdapterGroupChatList adapterGroupChatList;

    ProgressBar progressBarGroupChat;
//    TextView noGroupChat;


    public GroupChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_chats, container, false);

        groupRV = view.findViewById(R.id.groupsRV);

        progressBarGroupChat = view.findViewById(R.id.progressBarGroupChat);
//        noGroupChat = view.findViewById(R.id.noGroupChat);

        firebaseAuth = FirebaseAuth.getInstance();


        loadGroupChatsList();

        return view;
    }

    private void loadGroupChatsList() {
        progressBarGroupChat.setVisibility(View.VISIBLE);
//        noGroupChat.setVisibility(View.VISIBLE);

        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    // If current users uid exist in participants group list, then, show that group
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChatList model = ds.getValue(ModelGroupChatList.class);
                        groupChatLists.add(model);

                        progressBarGroupChat.setVisibility(View.GONE);
//                        noGroupChat.setVisibility(View.GONE);
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
                groupRV.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarGroupChat.setVisibility(View.GONE);
//                noGroupChat.setVisibility(View.GONE);
            }
        });
    }

    private void searchGroupChatsList(String query) {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    // If current users uid exist in participants group list, then, show that group
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){

                        // Search by group title
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupChatList model = ds.getValue(ModelGroupChatList.class);
                            groupChatLists.add(model);
                        }
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
                groupRV.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

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
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);


        //Search view
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Called when user pressed search button
                // If search query is not empty, then search
                if (!TextUtils.isEmpty(s.trim())){
                    // Search text contains text, search
                    searchGroupChatsList(s);
                }
                else {
                    // Search text empty, get all users
                    loadGroupChatsList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Called when user pressed any key button

                // If search query is not empty, then search
                if (!TextUtils.isEmpty(s.trim())){
                    // Search text contains text, search
                    searchGroupChatsList(s);
                }
                else {
                    // Search text empty, get all users
                    loadGroupChatsList();
                }

                return false;
            }
        });

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
        else if (id == R.id.action_create_group){
            // Go to settings activity
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            // User not signed in, go to Main Activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();

        }
    }
}