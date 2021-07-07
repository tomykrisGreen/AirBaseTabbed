package com.tomykrisgreen.airbasetabbed.VideoUpload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity {
    // UI Views
    FloatingActionButton addVideoBtn;
    private RecyclerView videoRV;

    // Array List
    private ArrayList<ModelVideo> videoArrayList;
    // Adapter
    private AdapterVideo adapterVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Change actionbar title
        setTitle("Videos");

        // Init views
        addVideoBtn =findViewById(R.id.addVideoBtn);
        videoRV =findViewById(R.id.videoRV);

        //Function call, load videos
        loadVideosFromFirebase();

        // Handle click
        addVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start activity to add video
                startActivity(new Intent(VideoActivity.this, AddVideoActivity.class));
            }
        });
    }

    private void loadVideosFromFirebase() {
        // Init Array list
        videoArrayList = new ArrayList<>();

        // DB reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Videos");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear list before adding data into it
                videoArrayList.clear();

                for (DataSnapshot ds: snapshot.getChildren()){
                    // Get data
                    ModelVideo modelVideo = ds.getValue(ModelVideo.class);
                    // Add model/data into list
                    videoArrayList.add(modelVideo);
                }
                // Setup adapter
                adapterVideo = new AdapterVideo(VideoActivity.this, videoArrayList);
                // Set adapter to recyclerView
                videoRV.setAdapter(adapterVideo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}