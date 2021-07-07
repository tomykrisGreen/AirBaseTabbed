package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterUsers;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;

import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {
    String postId;

    RecyclerView recyclerView_Post_Liked;

    private List<ModelUser> userList;
    private AdapterUsers adapterUsers;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        // Actionbar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Liked By");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());

        recyclerView_Post_Liked = findViewById(R.id.recyclerView_Post_Liked);

        // Get the post id
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        userList = new ArrayList<>();

        // Get the list of UIDs of users who liked the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String hisUid = ""+ ds.getRef().getKey();

                    // Get user info from each id
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsers(String hisUid) {
        // Get info of each user using uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelUser modelUser = ds.getValue(ModelUser.class);
                            userList.add(modelUser);
                        }
                        // Setup adapter
//                        adapterUsers = new AdapterUsers(PostLikedByActivity.this, userList, false);
                        adapterUsers = new AdapterUsers(PostLikedByActivity.this, userList);
                        // Set adapter to recyclerView
                        recyclerView_Post_Liked.setAdapter(adapterUsers);
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