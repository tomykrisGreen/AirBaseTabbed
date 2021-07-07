package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterChat;
import com.tomykrisgreen.airbasetabbed.AudioUpload.AudioActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelChat;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;
import com.tomykrisgreen.airbasetabbed.Notification.Data;
import com.tomykrisgreen.airbasetabbed.Notification.Sender;
import com.tomykrisgreen.airbasetabbed.Notification.Token;
import com.tomykrisgreen.airbasetabbed.ZoomImager.ImageChatViewerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    // Views from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv, blockIvChat;
    TextView nameTv, userStatusTv, blockTvChat;
    EditText messageEt;
    ImageButton attachBtn;
    FloatingActionButton sendBtn, micBtn, micStopBtn;

    // Permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    // Image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    ProgressDialog progressDialog;

    // Uri of picked image
    Uri image_uri = null;

    //Arrays of permissions to be requested
    String[] cameraPermissions;
    String[] storagePermissions;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    // For checking is message has been seen or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    boolean isBlocked = false;

//    APIService apiService;

    // Volley request queue for notification
    private RequestQueue requestQueue;

    private boolean notify = false;

    private MediaPlayer mediaPlayer,mediaPlayerReceived, mediaPlayerDelete;

    private String checker = "";

    // For sending Audio Message
    MediaRecorder mediaRecorder;
    public static String filename = "recorded.3gp";
    String file = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+filename;

    private ProgressDialog mProgress;
    private StorageReference mStorage;

    private static final int RECORD_AUDIO = 1;

    UploadTask uploadTask;


//    private void sendAudioMessage() {
//        Uri audioFile = Uri.fromFile(new File(file));
//    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");


        // Initializing play sound when message sent
        mediaPlayer = MediaPlayer.create(this, R.raw.send_message);
        mediaPlayerDelete = MediaPlayer.create(this, R.raw.tone_del);
        mediaPlayerReceived = MediaPlayer.create(this, R.raw.tone_received);


        mProgress = new ProgressDialog(this);


        //Init View
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chats_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        blockIvChat = findViewById(R.id.blockIvChat);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sentBtn);
        micBtn = findViewById(R.id.micBtn);
        micStopBtn = findViewById(R.id.micStopBtn);
        attachBtn = findViewById(R.id.attachBtn);

        // Init arrays of permissions
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Layout LinearLayout for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        // RecyclerView properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

//        // Create API service
//        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        /* On clicking user from users list, user's uid has been passed using intent
        So get that uid here to get the profile picture and name, and start chat with the user
         */
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        // Search user to get users info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        // Get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check until user info is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get data
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();

                    // Check typing status
                    if (typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");
                    }
                    else {
                        // Get value of online status
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("Online")){
                            userStatusTv.setText(onlineStatus);
                        }
                        else {
                            // Convert timestamp to proper time date
                            // Convert time date to dd/MM/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                            userStatusTv.setText(dateTime);
                        }
                    }



                    // Set data
                    nameTv.setText(name);
                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_profile_white).into(profileIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


////         For sending Audio Message
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
//
//        mediaRecorder.setOutputFile(file);
//
//
//        micBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    mediaRecorder.prepare();
//
//                    mediaRecorder.start();
////                    startRecording();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                Toast.makeText(ChatActivity.this, "Recording started...", Toast.LENGTH_SHORT).show();
//
//                micBtn.setVisibility(View.GONE);
//                sendBtn.setVisibility(View.GONE);
//                micStopBtn.setVisibility(View.VISIBLE);
//            }
//        });
//
//        micStopBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                stopRecording();
//            }
//        });

//        micBtn.setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
//                    checkAudioPermission();
//
////                    try {
////                        startRecording();
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                    Toast.makeText(ChatActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
////                    startRecording();
//
////                    mRecordLabel.setText("Recording Started...");
//                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
//                    mediaRecorder.stop();
//                    mediaRecorder.release();
//                    mediaRecorder = null;
//
//                    uploadAudio();
////                    stopRecording();
//                    Toast.makeText(ChatActivity.this, "Recorded", Toast.LENGTH_SHORT).show();
////                    stopRecording();
//
////                    mRecordLabel.setText("Recording Stopped");
//                }
//                return false;
//            }
//        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, AudioActivity.class);
                startActivity(intent);
            }
        });




        sendBtn.setVisibility(View.GONE);
//        micBtn.setVisibility(View.GONE);
        micStopBtn.setVisibility(View.GONE);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                // Get text from Edit Text
                String message = messageEt.getText().toString().trim();
                // Check if text is not empty
                if (TextUtils.isEmpty(message)) {
                    // Text empty
                    Toast.makeText(ChatActivity.this, "Write a message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);

                    // Animation effect decrease in likes count
                    Animation animationSlideIn = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.fade_out);
                    sendBtn.startAnimation(animationSlideIn);
                }

                // Reset edit text after message is sent
                messageEt.setText("");
            }
        });

        // Attach button click to import image
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        // Check edit text change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");

                    Animation animationFadeOut = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.fade_out);
                    sendBtn.startAnimation(animationFadeOut);

                    sendBtn.setVisibility(View.GONE);
                    micBtn.setVisibility(View.VISIBLE);
                }
                else {
                    checkTypingStatus(hisUid); // uid of receiver

                    sendBtn.setVisibility(View.VISIBLE);
                    micBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        blockIvChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBlocked){
                    unBlockUser();
                }
                else {
                    blockUser();
                }
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // When Item swipe: Remove item from arrayList
//                chatList.remove(viewHolder.getAdapterPosition());

                deleteMessage(viewHolder.getAdapterPosition());

                // Notify adapter
                adapterChat.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView);

        readMessages();

        checkIsBlocked();

        seenMessage();
    }

    private void checkAudioPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
        }else {
            try {
                mediaRecorder.prepare();

                mediaRecorder.start();

//                startRecording();

                Toast.makeText(ChatActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void startRecording() throws IOException {

//           mediaRecorder = new MediaRecorder();
//           mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//           mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//           mediaRecorder.setOutputFile(filename);
//           mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

           mediaRecorder.prepare();

           mediaRecorder.start();


    }

    private void stopRecording() {
        mProgress.dismiss();

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        uploadAudio();
    }

    private void uploadAudio() {
        mProgress.setMessage("Uploading Audio...");
        mProgress.show();

//        StorageReference filePath = mStorage.child("Audio").child("new_audio.3gp");

        Uri audioFile = Uri.fromFile(new File(filename));
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Audio Files").child("new_audio.3gp");
        final StorageReference reference = storageReference.child(System.currentTimeMillis() + filename);
        uploadTask =  reference.putFile(audioFile);

        storageReference.putFile(audioFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgress.dismiss();

                Toast.makeText(ChatActivity.this, "Audio uploaded", Toast.LENGTH_SHORT).show();
            }
        });

//        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                if (task.isSuccessful()){
//                    throw task.getException();
//                }
//                return reference.getDownloadUrl();
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//                if (task.isSuccessful()){
//                    Uri downloadUrl = task.getResult();
//
//                    Calendar cDate = Calendar.getInstance();
//                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
//                    final String saveDate = currentDate.format(cDate.getTime());
//
//                    Calendar cTime = Calendar.getInstance();
//                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
//                    final String saveTime = currentDate.format(cTime.getTime());
//
//                    String time = saveDate + ":"+ saveTime;
//
//                    HashMap<String, Object> hashMap = new HashMap<>();
//                    hashMap.put("sender", myUid);
//                    hashMap.put("receiver", hisUid);
//                    hashMap.put("message", downloadUrl);
//                    hashMap.put("timestamp", time);
//                    hashMap.put("isSeen", false);
//                    hashMap.put("type", "audio");
//
//                    String id = storageReference.push
//
//                    storageReference.child("Chats").push().setValue(hashMap);
//                }else {
//
//                }
//            }
//        });

    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Logic:
        - Get time stamp of clicked message
        - Compare time stamp of clicked message with all messages in chats
        - Where both values matches, delete message
         */
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    /* If you want user to delete only his own messages,then compare sender value with current users uid */
                    if (ds.child("sender").getValue().equals(myUID)){
                        /* We can do one of the two things
                    1. Remove message from chats
                    2. Set the value of message "This message was deleted"
                     */

                        //  1. Remove message from chats
                        ds.getRef().removeValue();
                        // 2. Set the value of message "This message was deleted"
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("message", "This message was deleted");
//                        ds.getRef().updateChildren(hashMap);

                        adapterChat.notifyItemRemoved(position);

                        mediaPlayerDelete.start();

                        Toast.makeText(ChatActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "You can only delete your messages", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkIsBlocked() {
        // Check each user if blocked or not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                blockIvChat.setImageResource(R.drawable.ic_blocked);
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser() {
        // Block the user by adding uid to current users "BlockedUsers" node

        // Put values in hashMap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Blocked
                        Toast.makeText(ChatActivity.this, "Blocked", Toast.LENGTH_SHORT).show();

                        blockIvChat.setImageResource(R.drawable.ic_blocked);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed
                Toast.makeText(ChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser() {
        // Unblock the user by removing uid to current users "BlockedUsers" node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                // Remove blocked user data from current users BlockedUsers list
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Unblocked
                                                Toast.makeText(ChatActivity.this, "Unblocked", Toast.LENGTH_SHORT).show();
                                                blockIvChat.setImageResource(R.drawable.ic_unblocked);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to unblock
                                        Toast.makeText(ChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                if (which == 0){
                    // Camera clicked
                    // We need to check permission first
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if (which == 1){
                    // Gallery clicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
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
//        CharSequence option[] = new CharSequence[]
//                {
//                        "Images",
//                        "PDF Files",
//                        "MS Word Files"
//                };
//        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
//        builder.setTitle("Select File");
//
//        builder.setItems(option, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (i == 0) {
//                    checker = "image";
//
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent.setType("image/*");
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);  //newly added
//                    startActivityForResult(Intent.createChooser(intent, "Select Image"), 438);   // startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
//                }
//
//                if (i == 1) {
//                    checker = "pdf";
//
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent.setType("application/pdf");
//                    startActivityForResult(Intent.createChooser(intent, "Select PDF File"), 438);
//                }
//
//                if (i == 2) {
//                    checker = "docX";
//
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent.setType("application/MSWord");
//                    startActivityForResult(Intent.createChooser(intent, "Select MSWord File"), 438);
//                }
//            }
//        });
//        builder.show();

        // Pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        // Check if storage permission is enabled or not
        // Return true if enable
        // else Return false
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        // Check if camera permission is enabled or not
        // Return true if enable
        // else Return false
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }

                    // Adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();

                    //This enables auto-scrolling of messages on the chat activity.
                    recyclerView.smoothScrollToPosition(adapterChat.getItemCount());

                    // Set adapter to recycler view
                    recyclerView.setAdapter(adapterChat);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image");
        progressDialog.show();

        String timestamp = ""+ System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/"+"post_"+timestamp;

        /* Chat node will be created and that will contain all images sent via chat */

        // Get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();  // Converts image to bytes
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded
                        progressDialog.dismiss();
                        // Get url of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());//{
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            // Add image uri and other info to database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            // Setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", timestamp);
                            hashMap.put("type", "image");
                            hashMap.put("isSeen", false);

                            // Put this data to firebase
                            databaseReference.child("Chats").push().setValue(hashMap);

                            mediaPlayer.start();

                            // Send notification
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelUser user = snapshot.getValue(ModelUser.class);

                                    if (notify){
                                        sendNotification(hisUid, user.getName(), "Sent you an image");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                                    .child(myUid).child(hisUid);

                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()){
                                        chatRef1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                                    .child(hisUid).child(myUid);

                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()){
                                        chatRef2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
//                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float per=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading... "+(int)per+"%");
            }
        });;
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //Set Online status
        checkOnlineStatus("Online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Get time stamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        // Set offline status with last seen time stamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //Set Online status
        checkOnlineStatus("Online");
        super.onResume();
    }


    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // This method is called when user press Allow or Denied from request dialog
        // Handle permission cases
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                // Camera usage
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults [0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults [1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this, "Accept camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                // Gallery usage
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults [0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this, "Accept storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case RECORD_AUDIO:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    try {
                        mediaRecorder.prepare();

                        mediaRecorder.start();
//                        startRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    // Permission denied
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // This method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                // Image is picked from gallery, get uri of image
                image_uri = data.getData();

                // Use this image uri to upload to firebase storage
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                // Image is picked from camera, get uri of image
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    private void sendMessage(String message) {
        /* Chats node will be created that will contain all chats
        Whenever a message is sent, it will contain the following key values:
        - Sender: uid of sender
        - Receiver: uid of receiver
        - Message: the actual message
         */
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");

        databaseReference.child("Chats").push().setValue(hashMap);

        mediaPlayer.start();

//        // Reset edit text after message is sent
//        messageEt.setText("");


        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);

                if (notify){
                    sendNotification(hisUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Create chatList node/child in firebase database
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myUid).child(hisUid);

        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(hisUid).child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String hisUid, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(
                            ""+myUid,
                            ""+name + ": " + message,
                            "New Message",
                            ""+hisUid,
                            "ChatNotification",
                            R.drawable.ic_default_img);

                    Sender sender = new Sender(data, token.getToken());

                    // FCM json object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com//fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // Response of the request
                                        Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                // Put params
                                Map<String, String> headers =new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAA4ZKsDUY:APA91bEmgpBrBPoCe3Y59aN3MhPo8knRjx5p4wKBmD9hWGPraoGnzPXwXPpv9bSsQU38uBEeb3EMDKuxm7jjxEeKFmqCBpKGA9jVwMY7k3S4Lua0-NjR7PPivl1ePkYvwUSWWKmjyCxW");
                                return super.getHeaders();
                            }
                        };

                        // Add this request to queue
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


//                    apiService.sendNotification(sender)
//                            .enqueue(new Callback<Response>() {
//                                @Override
//                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onFailure(Call<Response> call, Throwable t) {
//
//                                }
//                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            // Set email of logged in user
            //mProfileTV.setText(user.getEmail());
            myUid = user.getUid();  // Currently signed in users uid

        } else {
            // User not signed in, go to Main Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        dbRef.updateChildren(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Hide searchView, add post as we don't need it here
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_shop).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);  // Hide add post from this activity
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();

            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}