package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterGroupChat;
import com.tomykrisgreen.airbasetabbed.Models.ModelGroupChat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

    private String groupId, myGroupRole = "";

    private Toolbar toolbar;
    private ImageView groupIconIv_GroupChat;
    private ImageButton attachBtn_GroupChat;
    private FloatingActionButton sendBtn_GroupChat;
    private TextView groupTitleTv_GroupChat;
    private EditText messageEt_GroupChat;

    private RecyclerView chatRV_GroupChat;

    private ArrayList<ModelGroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;

    // Permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;

    // Image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;

    //Arrays of permissions to be requested
    private String[] cameraPermissions;
    private String[] storagePermissions;

    // Uri of picked image
    private Uri image_uri = null;

    private MediaPlayer mediaPlayer,mediaPlayerReceived, mediaPlayerDelete;

    String hisUid;
    String myUid;

    private String checker = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        firebaseAuth = FirebaseAuth.getInstance();

        // Initializing play sound when message sent
        mediaPlayer = MediaPlayer.create(this, R.raw.send_message);
        mediaPlayerDelete = MediaPlayer.create(this, R.raw.tone_del);
        mediaPlayerReceived = MediaPlayer.create(this, R.raw.tone_received);

        // Init views
        toolbar = findViewById(R.id.toolbar_group_chat);
        groupIconIv_GroupChat = findViewById(R.id.groupIconIv_GroupChat);
        attachBtn_GroupChat = findViewById(R.id.attachBtn_GroupChat);
        sendBtn_GroupChat = findViewById(R.id.sendBtn_GroupChat);
        groupTitleTv_GroupChat = findViewById(R.id.groupTitleTv_GroupChat);
        messageEt_GroupChat = findViewById(R.id.messageEt_GroupChat);

        setSupportActionBar(toolbar);

        chatRV_GroupChat = findViewById(R.id.chatRV_GroupChat);

        // Init required permissions
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Get Id of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        sendBtn_GroupChat.setVisibility(View.GONE);

        sendBtn_GroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input data
                String message = messageEt_GroupChat.getText().toString().trim();
                // Validate
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this, "Type a message to send", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Send message
                    sendMessage(message);

                    // Animation effect decrease in likes count
                    Animation animationSlideIn = AnimationUtils.loadAnimation(GroupChatActivity.this, R.anim.fade_out);
                    sendBtn_GroupChat.startAnimation(animationSlideIn);
                }
            }
        });


        // Check edit text change listener
        messageEt_GroupChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().length() == 0){
//                    checkTypingStatus("noOne");

                    Animation animationFadeOut = AnimationUtils.loadAnimation(GroupChatActivity.this, R.anim.fade_out);
                    sendBtn_GroupChat.startAnimation(animationFadeOut);

                    sendBtn_GroupChat.setVisibility(View.GONE);
                }
                else {
//                    checkTypingStatus(hisUid); // uid of receiver

                    sendBtn_GroupChat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        attachBtn_GroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pick image from gallery/camera
                showImageImportDialog();
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
                adapterGroupChat.notifyDataSetChanged();
            }
        }).attachToRecyclerView(chatRV_GroupChat);

        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        dbRef.updateChildren(hashMap);
    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* Logic:
        - Get time stamp of clicked message
        - Compare time stamp of clicked message with all messages in chats
        - Where both values matches, delete message
         */
        String msgTimeStamp = groupChatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Messages");
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

                        mediaPlayerDelete.start();

                        Toast.makeText(GroupChatActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(GroupChatActivity.this, "You can only delete your messages", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showImageImportDialog() {
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
                else {
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
        values.put(MediaStore.Images.Media.TITLE, "Group Image Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Description");
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
//        AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
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
//                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
//                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                    startActivityForResult(Intent.createChooser(intent, "Select PDF File"), 438);
//                }
//
//                if (i == 2) {
//                    checker = "docX";
//
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent.setType("application/MSWord");
//                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                    startActivityForResult(Intent.createChooser(intent, "Select MSWord File"), 438);
//                }
//            }
//        });
//        builder.show();

        // Pick from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
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

    private void sendImageMessage(Uri image_uri) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.show();

//        String timestamp = ""+ System.currentTimeMillis();
//
//        String fileNameAndPath = "ChatImages/"+"post_"+timestamp;
        String fileNameAndPath = "ChatImages/"+"post_"+ ""+System.currentTimeMillis();

        /* Chat node will be created and that will contain all images sent via chat */

        // Get bitmap from image uri
//        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();  // Converts image to bytes
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful());
                        Uri p_downloadUri = p_uriTask.getResult();

                        if (p_uriTask.isSuccessful()){
                            // Image url received, save in db
                            String timestamp = String.valueOf(System.currentTimeMillis());

                            // Send message data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", "" + firebaseAuth.getUid());
                            hashMap.put("message", "" + p_downloadUri);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("isSeen", false);
                            hashMap.put("type", "" + "image");  // text/image/file

                            // Add in db
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Message sent
                                            messageEt_GroupChat.setText("");

                                            mediaPlayer.start();

                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    // Failed
                                    Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed uploading image
                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float per=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading... "+(int)per+"%");
            }
        });;

    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            // Refresh menu items
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessages() {
        // Init list
        groupChatList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupChatList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                            groupChatList.add(model);
                        }
                        // Adapter
                        adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);

                        //This enables auto-scrolling of messages on the chat activity.
                        chatRV_GroupChat.smoothScrollToPosition(adapterGroupChat.getItemCount());

                        // Set adapter to recyclerView
                        chatRV_GroupChat.setAdapter(adapterGroupChat);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage(String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Send message data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "" + "text");  // text/image/file

        // Add in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Message sent
                        messageEt_GroupChat.setText("");

                        mediaPlayer.start();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed
                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String groupTitle = ""+ds.child("groupTitle").getValue();
                            String groupDescription = ""+ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();

                            groupTitleTv_GroupChat.setText(groupTitle);

                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconIv_GroupChat);
                            }
                            catch (Exception e){
                                groupIconIv_GroupChat.setImageResource(R.drawable.ic_group_white);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_shop).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")){
            // I'm admin/creator - show add person option
            menu.findItem(R.id.action_add_participant).setVisible(true);
        }
        else {
            menu.findItem(R.id.action_add_participant).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_participant){
            Intent intent = new Intent(this, GroupParticipantAddActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        else if (id == R.id.action_group_info){
            Intent intent = new Intent(this, GroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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
}