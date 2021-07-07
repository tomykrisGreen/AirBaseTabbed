package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterParticipantAdd;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {
    private RecyclerView usersRV_GroupParticipant;

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    private String groupId;
    private String myGroupRole;

    private ArrayList<ModelUser> userList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);

        firebaseAuth = FirebaseAuth.getInstance();

        // Actionbar and its Title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupId = getIntent().getStringExtra("groupId");

        usersRV_GroupParticipant = findViewById(R.id.usersRV_GroupParticipant);

        loadGroupInfo();
    }

    private void getAllUsers() {
        // Init list
        userList = new ArrayList<>();

        // Load users from db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    // Get all users accept currently signed in
                    if (!firebaseAuth.getUid().equals(modelUser.getUid())){
                        // Not my uid
                        userList.add(modelUser);
                    }
                }
                // Setup adapter
                adapterParticipantAdd = new AdapterParticipantAdd(GroupParticipantAddActivity.this, userList, ""+groupId, ""+myGroupRole);
                // Set adapter to recyclerView
                usersRV_GroupParticipant.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            // Get data
                            String groupId = ""+ds.child("groupId").getValue();
                            String groupTitle = ""+ds.child("groupTitle").getValue();
                            String groupDescription = ""+ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();

                            actionBar.setTitle("Add Participants");

                            ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                myGroupRole = ""+snapshot.child("role").getValue();
                                                actionBar.setTitle(groupTitle + "("+myGroupRole+")");

                                                getAllUsers();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}