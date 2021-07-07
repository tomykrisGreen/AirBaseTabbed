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
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterUsers;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    AdapterUsers adapterUser;
    List<ModelUser> userList;

    FirebaseAuth firebaseAuth;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        // Actionbar and its properties
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.users_recyclerView);
        //Set its properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Init user list
        userList = new ArrayList<>();

        // Get all users
        getAllUsers();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void getAllUsers() {
        // Get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get path of database name 'Users' containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //Get all user except for currently signed in user
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }

                    // Adapters
//                    adapterUser = new AdapterUsers(getActivity(), userList, false);
                    adapterUser = new AdapterUsers(UsersActivity.this, userList);
                    // Set adapter to recycler view
                    recyclerView.setAdapter(adapterUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String query) {
        // Get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get path of database name 'Users' containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    /* Condition to fulfil search:
                    1. User not current user
                    2. The user name or email contains text entered in searchView (case insensitive)
                     */

                    //Get all searched user except for currently signed in user
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }

                    }

                    // Adapters
//                    adapterUser = new AdapterUsers(getActivity(), userList, false);
                    adapterUser = new AdapterUsers(UsersActivity.this, userList);
                    //Refresh adapter
                    adapterUser.notifyDataSetChanged();
                    // Set adapter to recycler view
                    recyclerView.setAdapter(adapterUser);
                }
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
            startActivity(new Intent(UsersActivity.this, MainActivity.class));
            finish();
        }
    }


    // Inflate options menu
    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        // Inflating menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Hide add post icon from this fragment
        menu.findItem(R.id.action_shop).setVisible(false);
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
                    searchUsers(s);
                }
                else {
                    // Search text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Called when user pressed any key button

                // If search query is not empty, then search
                if (!TextUtils.isEmpty(s.trim())){
                    // Search text contains text, search
                    searchUsers(s);
                }
                else {
                    // Search text empty, get all users
                    getAllUsers();
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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
            startActivity(new Intent(UsersActivity.this, SettingsActivity.class));
        }
        else if (id == R.id.action_create_group){
            // Go to settings activity
            startActivity(new Intent(UsersActivity.this, GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}