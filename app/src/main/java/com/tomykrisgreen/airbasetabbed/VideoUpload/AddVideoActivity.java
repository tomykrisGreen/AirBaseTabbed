package com.tomykrisgreen.airbasetabbed.VideoUpload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.HashMap;

public class AddVideoActivity extends AppCompatActivity {
    private ActionBar actionBar;

    //UI Views
    private EditText titleEt;
    private VideoView videoView;
    private Button uploadVideoBtn;
    private FloatingActionButton pickVideoFab;

    private static final int VIDEO_PICK_GALLERY_CODE = 100;
    private static final int VIDEO_PICK_CAMERA_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private String[] cameraPermissions;

    private Uri videoUri = null; // Uri of picked video
    private String title;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        // Init actionbar
        actionBar = getSupportActionBar();
        //Set Title
        actionBar.setTitle("Add Video");
        // Add back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init views
        titleEt = findViewById(R.id.titleEt);
        videoView = findViewById(R.id.videoView);
        uploadVideoBtn = findViewById(R.id.uploadVideoBtn);
        pickVideoFab = findViewById(R.id.pickVideoFAB);

        //Set up progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Uploading video...");
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Handle click upload video
        uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = titleEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddVideoActivity.this, "Title is required", Toast.LENGTH_SHORT).show();
                }
                else if (videoUri == null){
                    // Video not picked, pick video
                    Toast.makeText(AddVideoActivity.this, "Pick a video", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Upload video function call
                    uploadVideoToFirebase();
                }
            }
        });

        // Handle click upload video
        pickVideoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPickDialog();
            }
        });
    }

    private void uploadVideoToFirebase() {
        // Show progress dialog
        progressDialog.show();

        // TimeStamp
        String timestamp = ""+ System.currentTimeMillis();

        // File path and name in firebase storage
        String filePathAndName = "Videos/" + "video_"+ timestamp;

        // Storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        // Upload video, you can upload any type of file using this method
        storageReference.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Video uploaded, get url of uploaded video
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            // url of uploaded video is received
                            // Now we can add details of video to firebase db
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", timestamp);
                            hashMap.put("title", ""+ title);
                            hashMap.put("timestamp", ""+  timestamp);
                            hashMap.put("videoUrl", ""+  downloadUri);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
                            reference.child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Video details uploaded to db
                                            progressDialog.dismiss();
                                            Toast.makeText(AddVideoActivity.this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Failure
                                    progressDialog.dismiss();
                                    Toast.makeText(AddVideoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed uploading to storage
                progressDialog.dismiss();
                Toast.makeText(AddVideoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float per=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading... "+(int)per+"%");
            }
        });
    }

    private void videoPickDialog() {
        // Options to display in dialog
        String[] options = {"Camera", "Gallery"};

        //Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Video From:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            if (!checkCameraPermission()){
                                // Camera permission not allowed - request it
                                requestCameraPermission();
                            }
                            else {
                                // Permission already allowed, take action
                                videoPickCamera();
                            }
                        }
                        else if (i == 1){
                            videoPickGallery();
                        }
                    }
                })
                .show();
    }

    private void requestCameraPermission(){
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        // Check if camera permission is enabled or not
        // Return true if enable
        // else Return false
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == (PackageManager.PERMISSION_GRANTED);
        return result1 && result2;
    }

    private void videoPickGallery() {
        // Pick video from gallery - intent
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Videos"), VIDEO_PICK_GALLERY_CODE);
    }

    private void videoPickCamera() {
        // Intent to start camera
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
    }

    private void setVideoToVideoView() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        // Set media controller to video view
        videoView.setMediaController(mediaController);
        // Set video uri
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.pause();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0){
                    // Check if permission allowed or not
                    boolean cameraAccepted = grantResults [0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults [1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        videoPickCamera();
                    }
                    else {
                        Toast.makeText(this, "Accept camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Called after picking video from gallery
        if (resultCode == RESULT_OK){
            if (requestCode == VIDEO_PICK_GALLERY_CODE){
                videoUri = data.getData();
                setVideoToVideoView();
            }
            else if (requestCode == VIDEO_PICK_CAMERA_CODE){
                videoUri = data.getData();
                setVideoToVideoView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();

    }
}