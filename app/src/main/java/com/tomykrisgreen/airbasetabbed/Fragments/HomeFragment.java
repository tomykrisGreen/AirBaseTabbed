//package com.tomykrisgreen.airbasetabbed.Fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.widget.SearchView;
//import androidx.core.view.MenuItemCompat;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.text.TextUtils;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.PopupMenu;
//import android.widget.Toast;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost;
//import com.tomykrisgreen.airbasetabbed.AddPostActivity;
//import com.tomykrisgreen.airbasetabbed.MainActivity;
//import com.tomykrisgreen.airbasetabbed.Models.ModelPost;
//import com.tomykrisgreen.airbasetabbed.R;
//import com.tomykrisgreen.airbasetabbed.SettingsActivity;
//import com.tomykrisgreen.airbasetabbed.VideoUpload.AdapterVideo;
//import com.tomykrisgreen.airbasetabbed.VideoUpload.AddVideoActivity;
//import com.tomykrisgreen.airbasetabbed.VideoUpload.ModelVideo;
//import com.tomykrisgreen.airbasetabbed.VideoUpload.VideoActivity;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A simple {@link Fragment} subclass.
// * create an instance of this fragment.
// */
//public class HomeFragment extends Fragment {
//    FirebaseAuth firebaseAuth;
//
//    RecyclerView recyclerView;
//    List<ModelPost> postList;
//    AdapterPost adapterPost;
//
//    RecyclerView recyclerViewStory;
//    List<ModelPost> storyList;
//    AdapterPost adapterStory;
//
//
//    public HomeFragment() {
//        // Required empty public constructor
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_home, container, false);
//
//        // Init Firebase
//        firebaseAuth = FirebaseAuth.getInstance();
//
//        // RecyclerView and its properties
//        recyclerView = view.findViewById(R.id.postRecyclerView);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        // Show newest post first
//        layoutManager.setStackFromEnd(true);
//        layoutManager.setReverseLayout(true);
//        // Set layout to recyclerView
//        recyclerView.setLayoutManager(layoutManager);
//
//        // Init post list
//        postList = new ArrayList<>();
//
//
//        // RecyclerView and its properties
//        recyclerView = view.findViewById(R.id.recycler_view_story);
//        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
//        // Show newest post first
//        layoutManager2.setStackFromEnd(true);
//        layoutManager2.setReverseLayout(true);
//        // Set layout to recyclerView
//        recyclerView.setLayoutManager(layoutManager2);
//
//        // Init post list
//        storyList = new ArrayList<>();
//
//        loadPosts();
////        retrieveStories();
//
////        loadVideosFromFirebase();
//
//        return view;
//    }
//
////    private void loadVideosFromFirebase() {
////        // Init Array list
////        postList = new ArrayList<>();
////
////        // DB reference
////        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Videos");
////        ref.addValueEventListener(new ValueEventListener() {
////            @Override
////            public void onDataChange(@NonNull DataSnapshot snapshot) {
////                // Clear list before adding data into it
////                postList.clear();
////
////                for (DataSnapshot ds: snapshot.getChildren()){
////                    // Get data
////                    ModelPost modelVideo = ds.getValue(ModelPost.class);
////                    // Add model/data into list
////                    postList.add(modelVideo);
////                }
////                // Setup adapter
////                adapterPost = new AdapterPost(getContext(), postList);
////                // Set adapter to recyclerView
////                recyclerView.setAdapter(adapterPost);
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError error) {
////
////            }
////        });
////    }
//
//    private void loadPosts() {
//        // Path of all post
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
//        // Get all data from this ref
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                postList.clear();
//                for (DataSnapshot ds: snapshot.getChildren()){
//                    ModelPost modelPost = ds.getValue(ModelPost.class);
//
//                    postList.add(modelPost);
//
//                    // Adapter
//                    adapterPost = new AdapterPost(getActivity(), postList);
//                    // Set adapter to recycler view
//                    recyclerView.setAdapter(adapterPost);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // In case of error
//                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void searchPosts(String searchQuery){
//        // Path of all post
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
//        // Get all data from this ref
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                postList.clear();
//                for (DataSnapshot ds: snapshot.getChildren()){
//                    ModelPost modelPost = ds.getValue(ModelPost.class);
//
//                    if (modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
//                        postList.add(modelPost);
//                    }
//
////                    if (modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) ||
////                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
////                        postList.add(modelPost);
////                    }
//
//                    // Adapter
//                    adapterPost = new AdapterPost(getActivity(), postList);
//                    // Set adapter to recycler view
//                    recyclerView.setAdapter(adapterPost);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // In case of error
//                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void checkUserStatus(){
//        // Get current user
//        FirebaseUser user = firebaseAuth.getCurrentUser();
//        if (user != null){
//            // User is signed in
//            // Set email of logged in user
//            //mProfileTV.setText(user.getEmail());
//        }
//        else {
//            // User not signed in, go to Main Activity
//            startActivity(new Intent(getActivity(), MainActivity.class));
//            getActivity().finish();
//        }
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        setHasOptionsMenu(true);  // To show menu in fragment
//        super.onCreate(savedInstanceState);
//    }
//
//    // Inflate options menu
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // Inflating menu
//        inflater.inflate(R.menu.menu_main, menu);
//
//        // Hide some options
//        menu.findItem(R.id.action_create_group).setVisible(false);
//        menu.findItem(R.id.action_add_participant).setVisible(false);
//        menu.findItem(R.id.action_group_info).setVisible(false);
//
//        // SearchView to search posts by post Title/Description
//        MenuItem item = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
//
//        // Search listener
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                // Called when user presses search button
//                if (!TextUtils.isEmpty(s)){
//                    searchPosts(s);
//                }
//                else {
//                    loadPosts();
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    // Handle menu items clicks
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        // Get item id
//        int id = item.getItemId();
//        if (id == R.id.action_logout){
//            firebaseAuth.signOut();
//
//            checkUserStatus();
//        }
//
//        else if (id == R.id.action_add_post){
//            startActivity(new Intent(getActivity(), AddPostActivity.class));
////            showMoreOptions();
//        }
//
//        else if (id == R.id.action_settings){
//            // Go to settings activity
//            startActivity(new Intent(getActivity(), SettingsActivity.class));
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showMoreOptions() {
//        // Popup menu to show more options
//        PopupMenu popupMenu = new PopupMenu(getContext(), getView(), Gravity.CENTER);
//        // Items to show in menu
//        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Add Post");
//        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Add Video");
//
//        // Menu clicks
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                int id = menuItem.getItemId();
//                if (id == 0){
//                    startActivity(new Intent(getActivity(), AddPostActivity.class));
//                }
//                else if (id == 1){
//                    startActivity(new Intent(getActivity(), AddVideoActivity.class));
//                }
//                return false;
//            }
//        });
//        popupMenu.show();
//    }
//}