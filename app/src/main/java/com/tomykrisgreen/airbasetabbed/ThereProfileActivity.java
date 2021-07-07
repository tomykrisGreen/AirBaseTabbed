package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost;
import com.tomykrisgreen.airbasetabbed.Models.ModelPost;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

    //Views from Xml
    CircleImageView avatarIv;
    ImageView coverIv;
    TextView nameTV;
    TextView emailTV;
    TextView phoneTV;

    RecyclerView postsRecyclerView;

    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        // Init views
        avatarIv = findViewById(R.id.avatarIV);
        coverIv = findViewById(R.id.coverIv);
        nameTV = findViewById(R.id.nameTV);
        emailTV = findViewById(R.id.emailTV);
        phoneTV = findViewById(R.id.phoneTV);

        postsRecyclerView = findViewById(R.id.recyclerView_posts);

        // Get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check until required data is found
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    // Get Data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    // Set Data
                    nameTV.setText(name);
                    emailTV.setText(email);
                    phoneTV.setText(phone);

                    try {
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarIv);
                    }

                    try {
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postList = new ArrayList<>();

        checkUserStatus();

        loadHisPosts();

    }

    private void loadHisPosts() {
        // LinearLayout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // Set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        // Init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        // Query to load posts
        /* Whenever user publishes a post, the uid of the user is also saved as info of post
        So we are retrieving posts having uid equal to uid of current user
         */
        Query query = ref.orderByChild("uid").equalTo(uid);
        // Get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    // Add to list
                    postList.add(myPosts);

                    // Adapter
                    adapterPost = new AdapterPost(ThereProfileActivity.this, postList);
                    // Set adapter to recyclerView
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchHisPosts(final String searchQuery) {
        // LinearLayout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // Set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        // Init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        // Query to load posts
        /* Whenever user publishes a post, the uid of the user is also saved as info of post
        So we are retrieving posts having uid equal to uid of current user
         */
        Query query = ref.orderByChild("uid").equalTo(uid);
        // Get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        // Add to list
                        postList.add(myPosts);
                    }

                    // Adapter
                    adapterPost = new AdapterPost(ThereProfileActivity.this, postList);
                    // Set adapter to recyclerView
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_shop).setVisible(false);  // Hide add post from this activity
        menu.findItem(R.id.action_create_group).setVisible(false);  // Hide add post from this activity

        MenuItem item = menu.findItem(R.id.action_search);
        // SearchView to search user specific posts
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Called when user presses the search button
                if (!TextUtils.isEmpty(s)){
                    // Search
                    searchHisPosts(s);
                }
                else {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Called when user type any letter
                if (!TextUtils.isEmpty(s)){
                    // Search
                    searchHisPosts(s);
                }
                else {
                    loadHisPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout){
            firebaseAuth.signOut();

            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}