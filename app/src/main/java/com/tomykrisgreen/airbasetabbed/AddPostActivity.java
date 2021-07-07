package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.VideoUpload.AddVideoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    ActionBar actionBar;

    FirebaseAuth firebaseAuth;

    DatabaseReference userDbRef;

    //Views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    // User info
    String name, email, uid, dp;

    // Info of post to be edited
    String editTitle, editDescription, editImage;

    // Permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    // Image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    // -----------  For Video Upload  --------- //
    private static final int VIDEO_PICK_GALLERY_CODE = 1000;
    private static final int VIDEO_PICK_CAMERA_CODE = 2000;
    private Uri videoUri = null; // Uri of picked video

    private FloatingActionButton pickVideoFab, pickImageFAB, pickAudioFAB;
    private VideoView videoView;
    private Button videoUploadBtn;


    ProgressDialog progressDialog;

    // Uri of picked image
    Uri image_uri = null;

    //Arrays of permissions to be requested
    String[] cameraPermissions;
    String[] storagePermissions;

    private String descriptionVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        firebaseAuth = FirebaseAuth.getInstance();

        // Actionbar and its Title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //Enable Back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);

        actionBar.setSubtitle(email);

        // Init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Init views
//        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt_Post);
        imageIv = findViewById(R.id.pImageIv_Post);
        uploadBtn = findViewById(R.id.pUploadBtn_Post);
        videoUploadBtn = findViewById(R.id.videoUploadBtn_Post);

        // ------- Init views for Video Upload
        videoView = findViewById(R.id.videoView_post);
        pickVideoFab = findViewById(R.id.pickVideoFAB_Post);
        pickImageFAB = findViewById(R.id.pickImageFAB_Post);
        pickAudioFAB = findViewById(R.id.pickAudioFAB_Post);

        // Get data from previous activities' adapter
        Intent intent = getIntent();

        // Get data and its type from intent
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // text type data
                handleSendText(intent);
            } else if (type.startsWith("image")) {
                // image type data
                handleSendImage(intent);
            }
//            // Added to show share video option
//            else if (type.startsWith("video")){
//                // Video type data
//                handleSendVideo(intent);
//            }
        }


        String isUpdateKey = "" + intent.getStringExtra("key");
        String editPostId = "" + intent.getStringExtra("editPostId");
        // Validate if we came here to update post i.e. AdapterPost
        if (isUpdateKey.equals("editPost")) {
            // Update
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        } else {
            // Add
            actionBar.setTitle("Add New Post");
            uploadBtn.setText(" Upload ");
        }


        descriptionEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Drawable drawable = imageIv.getDrawable();
                final boolean b = charSequence.toString().trim().length() == 0;
                if (b) {
                    uploadBtn.setVisibility(View.GONE);
                } else {
                    uploadBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


//        imageIv.setVisibility(View.GONE);
//        uploadBtn.setVisibility(View.GONE);
//        videoView.setVisibility(View.GONE);
//        videoUploadBtn.setVisibility(View.GONE);
//
//        Drawable drawable = imageIv.getDrawable();
//
//        if (imageIv.getDrawable() != drawable){
//            imageIv.setVisibility(View.VISIBLE);
//            uploadBtn.setVisibility(View.VISIBLE);
////            videoView.setVisibility(View.GONE);
////            videoUploadBtn.setVisibility(View.GONE);
//            uploadBtn.setText("Upload Picture");
//        }
//        else if (!videoView.equals("")){
////            uploadBtn.setVisibility(View.GONE);
////            imageIv.setVisibility(View.GONE);
//            videoView.setVisibility(View.VISIBLE); // Might not be necessary
//            videoUploadBtn.setVisibility(View.VISIBLE);
//        }

        // Another way to work around the buttons
//        Drawable drawable = imageIv.getDrawable();
//
//        if (imageIv.getDrawable() == drawable) {
//            imageIv.setVisibility(View.VISIBLE);
//            uploadBtn.setVisibility(View.VISIBLE);
//
//            videoUploadBtn.setVisibility(View.GONE);
//            videoView.setVisibility(View.GONE);
//
//            uploadBtn.setText("Upload Picture");
//
//        } else if (!(videoView == null)) {
//            uploadBtn.setVisibility(View.GONE);
//            imageIv.setVisibility(View.GONE);
//
//            videoView.setVisibility(View.VISIBLE);
//            videoUploadBtn.setVisibility(View.VISIBLE);
//        }


        // Get some info of current user
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        //Init views
//        titleEt = findViewById(R.id.pTitleEt);
//        descriptionEt = findViewById(R.id.pDescriptionEt);
//        imageIv = findViewById(R.id.pImageIv);
//        uploadBtn = findViewById(R.id.pUploadBtn);

        // Get image from camera/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        pickImageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        // Handle click upload video
        pickVideoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPickDialog();
            }
        });

        // Upload button click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get data (title and description from Edit Text)
//                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();

//                if (TextUtils.isEmpty(title)){
//                    Toast.makeText(AddPostActivity.this, "Enter Title", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (TextUtils.isEmpty(description)){
//                    Toast.makeText(AddPostActivity.this, "Enter description", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if (isUpdateKey.equals("editPost")) {
                    beginUpdate(description, editPostId);
                } else {
                    uploadData(description);
                }

            }
        });

        // --  Video Upload Button click
        videoUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                descriptionVideo = descriptionEt.getText().toString().trim();
                uploadVideoToFirebase();
            }
        });


        checkUserStatus();
    }

    // Added to show share video option
    private void handleSendVideo(Intent intent) {
        // handle the received image
        Uri videoURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (videoURI != null) {
            image_uri = videoURI;
            // Set to imageView

            setVideoToVideoView();

            videoUploadBtn.setVisibility(View.VISIBLE);
        }
    }

    private void handleSendImage(Intent intent) {
        // handle the received image
        Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null) {
            image_uri = imageURI;
            // Set to imageView
            imageIv.setImageURI(image_uri);

            uploadBtn.setVisibility(View.VISIBLE);
        }
    }

    private void handleSendText(Intent intent) {
        // handle the received text
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Set to description edit text
            descriptionEt.setText(sharedText);

            uploadBtn.setVisibility(View.VISIBLE);
        }
    }

    private void beginUpdate(String description, String editPostId) {
        progressDialog.setMessage("Updating Post...");
        progressDialog.show();

        if (!editImage.equals("noImage")) {
            // With image
            updateWithImage(description, editPostId);
        } else if (imageIv.getDrawable() != null) {
            // With image
            updateWithNewImage(description, editPostId);
        } else {
            //Without image
            updateWithoutImage(description, editPostId);
        }
    }

    private void updateWithoutImage(String description, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        // Put post info
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
//        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");
        hashMap.put("pLikes", "0");
        hashMap.put("pComments", "0");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNewImage(String description, String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        // Get image from imageView
        Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded, gets it url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUrl = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            // url received,upload to firebase database

                            HashMap<String, Object> hashMap = new HashMap<>();
                            // Put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
//                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", description);
                            hashMap.put("pImage", downloadUrl);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Image not uploaded
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float per=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading... "+(int)per+"%");
            }
        });;
    }

    private void updateWithImage(String description, String editPostId) {
        // Post is with image, delete previous image first
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted, upload new image
                // for post-image name, post-id,publish-time
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/" + "post_" + timeStamp;

                // Get image from imageView
                Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // Image compress
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Image uploaded, gets it url
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful()) ;

                                String downloadUrl = uriTask.getResult().toString();
                                if (uriTask.isSuccessful()) {
                                    // url received,upload to firebase database

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    // Put post info
                                    hashMap.put("uid", uid);
                                    hashMap.put("uName", name);
                                    hashMap.put("uEmail", email);
                                    hashMap.put("uDp", dp);
//                                    hashMap.put("pTitle", title);
                                    hashMap.put("pDescr", description);
                                    hashMap.put("pImage", downloadUrl);
                                    hashMap.put("pLikes", "0");
                                    hashMap.put("pComments", "0");

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                    ref.child(editPostId).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AddPostActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Image not uploaded
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        float per=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading... "+(int)per+"%");
                    }
                });;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        // Get details of post using id of post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get data
//                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = "" + ds.child("pDescr").getValue();
                    editImage = "" + ds.child("pImage").getValue();

                    // Set data to views
//                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    // Set image
                    if (!editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                        } catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String description) {
        progressDialog.setTitle("Add New Post");
        progressDialog.setMessage("Publishing post ...");
        progressDialog.show();

        // For post-image name, post-id, post-publish-time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

        if (imageIv.getDrawable() != null) {
            // Get image from imageView
            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();


            // Post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image uploaded to firebase storage, now, get its uri
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;

                    String downloadUri = uriTask.getResult().toString();

                    if (uriTask.isSuccessful()) {
                        // url received, upload post to firebase database
                        HashMap<Object, String> hashMap = new HashMap<>();
                        // Put post info
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uDp", dp);
                        hashMap.put("pId", timeStamp);
//                        hashMap.put("pTitle", title);
                        hashMap.put("pDescr", description);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", timeStamp);
                        hashMap.put("pLikes", "0");
                        hashMap.put("pComments", "0");

                        // Video Aspect
                        hashMap.put("id", timeStamp);
                        hashMap.put("timestamp", "" + timeStamp);
                        hashMap.put("videoUrl", "");

                        // Path to store post data
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        // Put data in this ref
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                // Reset views
//                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                image_uri = null;

                                // Send notification
                                prepareNotification(
                                        "" + timeStamp,   // since we are using timestamp for post
                                        "" + name + " aded new post",
//                                        "" + title+"\n"+description,
                                        "" + description,
                                        "PostNotification",
                                        "POST"
                                );
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    float per=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading... "+(int)per+"%");
                }
            });
        } else {
            // Post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            // Put post info
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
//            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");

            // Video Aspect
            hashMap.put("id", timeStamp);
            hashMap.put("timestamp", "" + timeStamp);
            hashMap.put("videoUrl", "");

            // Path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            // Put data in this ref
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                    // Reset views
//                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri = null;

                    // Send notification
                    prepareNotification(
                            "" + timeStamp,   // since we are using timestamp for post
                            "" + name + " aded new post",
//                            "" + title+"\n"+description,
                            "" + description,
                            "PostNotification",
                            "POST"
                    );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic) {
        // Prepare data for notification

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;  // Topic must match with what the receiver subscribed for
        String NOTIFICATION_TITLE = title;  // e.g.  Tom added new post
        String NOTIFICATION_MESSAGE = description;   // Content of post
        String NOTIFICATION_TYPE = notificationType;  // There are two notification types, chat & posts, so to differentiate in FirebaseMessaging.java class

        // Prepare JSON what and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid);   // uid of current user/sender
            notificationBodyJo.put("pId", pId);   // Post id
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);
            // Where to send
            notificationBodyJo.put("to", NOTIFICATION_TOPIC);

            notificationBodyJo.put("data", notificationBodyJo);  // Combine data to be sent
        } catch (JSONException e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendPostNotification(notificationJo);
    }

    private void sendPostNotification(JSONObject notificationJo) {
        // Send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error encountered
                        Toast.makeText(AddPostActivity.this, "" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAA4ZKsDUY:APA91bEmgpBrBPoCe3Y59aN3MhPo8knRjx5p4wKBmD9hWGPraoGnzPXwXPpv9bSsQU38uBEeb3EMDKuxm7jjxEeKFmqCBpKGA9jVwMY7k3S4Lua0-NjR7PPivl1ePkYvwUSWWKmjyCxW");  // Paste your fcm key
                return super.getHeaders();
            }
        };
        // Enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void showImagePickDialog() {
        // Options (Camera, Gallery) to show in dialog
        String[] options = {"Camera", "Gallery"};

        // Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        // Set option to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Item click handle
                if (which == 0) {
                    // Camera clicked
                    // We need to check permission first
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                if (which == 1) {
                    // Gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        // Create and show dialog
        builder.create().show();
    }

    private void pickFromCamera() {
        // Intent of getting image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        // Put image uri
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        // Pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission() {
        // Check if storage permission is enabled or not
        // Return true if enable
        // else Return false
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        // Check if camera permission is enabled or not
        // Return true if enable
        // else Return false
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        checkUserStatus();

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            // Set email of logged in user
            //mProfileTV.setText(user.getEmail());
            email = user.getEmail();
            uid = user.getUid();
        } else {
            // User not signed in, go to Main Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_shop).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();

            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // This method is called when user press Allow or Denied from request dialog
        // Handle permission cases
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                // Camera usage
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Accept camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                // Gallery usage
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Accept storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // This method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Image is picked from gallery, get uri of image
                if (data != null) {
                    final Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        try {
                            image_uri = data.getData();

                            imageIv.setImageURI(image_uri);

                            imageIv.setVisibility(View.VISIBLE);
//                            uploadBtn.setVisibility(View.VISIBLE);

                            // Control for buttons
                            if (selectedImageUri == null){
                                uploadBtn.setVisibility(View.GONE);

                                pickVideoFab.setVisibility(View.GONE);
                            }else {
                                uploadBtn.setVisibility(View.VISIBLE);

                                pickVideoFab.setVisibility(View.GONE);
                                videoUploadBtn.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Image is picked from camera, get uri of image

                if (data != null) {
                    final Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        try {
                            imageIv.setImageURI(image_uri);

                            imageIv.setVisibility(View.VISIBLE);
//                            uploadBtn.setVisibility(View.VISIBLE);

                            // Control for buttons
                            if (selectedImageUri == null){
                                uploadBtn.setVisibility(View.GONE);

                                pickVideoFab.setVisibility(View.GONE);
                            }else {
                                uploadBtn.setVisibility(View.VISIBLE);

                                pickVideoFab.setVisibility(View.GONE);
                                videoUploadBtn.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }


            //--------  For Video Upload  ------------------ //
            else if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                if (data != null) {
                    final Uri selectedVideoUri = data.getData();
                    if (selectedVideoUri != null) {
                        try {
                            videoUri = data.getData();
                            setVideoToVideoView();

                            videoView.setVisibility(View.VISIBLE);
//                            videoUploadBtn.setVisibility(View.VISIBLE);

                            // Control for buttons
                            if (selectedVideoUri == null){
                                videoUploadBtn.setVisibility(View.GONE);

                                pickImageFAB.setVisibility(View.GONE);
                            }else {
                                videoUploadBtn.setVisibility(View.VISIBLE);

                                pickImageFAB.setVisibility(View.GONE);
                                uploadBtn.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {

                        }
                    }

                }
            } else if (requestCode == VIDEO_PICK_CAMERA_CODE) {
                if (data != null) {
                    final Uri selectedVideoUri = data.getData();
                    if (selectedVideoUri != null) {
                        try {
                            videoUri = data.getData();
                            setVideoToVideoView();

                            videoView.setVisibility(View.VISIBLE);
//                            videoUploadBtn.setVisibility(View.VISIBLE);

                            // Control for buttons
                            if (selectedVideoUri == null){
                                videoUploadBtn.setVisibility(View.GONE);

                                pickImageFAB.setVisibility(View.GONE);
                            }else {
                                videoUploadBtn.setVisibility(View.VISIBLE);

                                pickImageFAB.setVisibility(View.GONE);
                                uploadBtn.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //  ------  For Video Upload --------------  //
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

    private void videoPickDialog() {
        // Options to display in dialog
        String[] options = {"Camera", "Gallery"};

        //Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Video From:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            if (!checkCameraPermission()) {
                                // Camera permission not allowed - request it
                                requestCameraPermission();
                            } else {
                                // Permission already allowed, take action
                                videoPickCamera();
                            }
                        } else if (i == 1) {
                            videoPickGallery();
                        }
                    }
                })
                .show();
    }

    private void uploadVideoToFirebase() {
        // Show progress dialog
        progressDialog.setTitle("Add Video Post");
        progressDialog.setMessage("Publishing post (Video) ...");
        progressDialog.show();

        // TimeStamp
        String timestamp = "" + System.currentTimeMillis();

        // File path and name in firebase storage
        String filePathAndName = "Videos/" + "video_" + timestamp;

        // Storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        // Upload video, you can upload any type of file using this method
        storageReference.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Video uploaded, get url of uploaded video
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            // url of uploaded video is received
                            // Now we can add details of video to firebase db
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", timestamp);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("videoUrl", "" + downloadUri);

                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pId", timestamp);
//            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", descriptionVideo);
                            hashMap.put("pImage", "noImage");
                            hashMap.put("pTime", timestamp);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                            reference.child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Video details uploaded to db
                                            progressDialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Failure
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed uploading to storage
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}