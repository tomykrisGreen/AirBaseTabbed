package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterComments;
import com.tomykrisgreen.airbasetabbed.Models.ModelComment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

    // To get details of user and post
    String hisUid, myUid, myEmial, myName, myDp, postId, pLikes, hisDp, hisName, pImage;

    //Views
    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    ImageView likeBtn, shareBtn;
    LinearLayout profileLayout;

    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    // Add comments view
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    ProgressDialog progressDialog;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    // Views of Video
    VideoView videoView;
    TextView titleTv, timeTv;
    ProgressBar progressBar;
    ImageView deleteFab, downloadFab;
    RelativeLayout videoViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        firebaseAuth = FirebaseAuth.getInstance();

        // Actionbar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTv = findViewById(R.id.pTimeTv);
//        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);


        // Init UI view of video
        videoView = findViewById(R.id.videoViewShow);
        titleTv = findViewById(R.id.titleTv);
        timeTv = findViewById(R.id.timeTvShow);
        progressBar = findViewById(R.id.progressBarPostDetail);
        deleteFab = findViewById(R.id.deleteFab);
        downloadFab = findViewById(R.id.downloadFab);
        videoViewLayout = findViewById(R.id.videoViewLayout);


//        if (pDescriptionTv != null && pImage.equals("noImage") && videoView == null){
//            pImageIv.setVisibility(View.GONE);
//            pDescriptionTv.setVisibility(View.VISIBLE);
//
//            videoView.setVisibility(View.GONE);
////            holder.timeTv.setVisibility(View.GONE);
//            downloadFab.setVisibility(View.GONE);
////            holder.deleteFab.setVisibility(View.GONE);
//            progressBar.setVisibility(View.GONE);
//            videoViewLayout.setVisibility(View.GONE);
//        }
//
//        // set post image
//        // If there is no image, i.e. pImage.equals("noImage"), then hide image view
//        if (pImage.equals("noImage")){
//            // Hide imageView
//            pImageIv.setVisibility(View.GONE);
//            videoView.setVisibility(View.VISIBLE);
//        }
//        else {
//            // Show imageView
//            pImageIv.setVisibility(View.VISIBLE);
//            pDescriptionTv.setVisibility(View.VISIBLE);
//
//            videoView.setVisibility(View.GONE);
////            holder.timeTv.setVisibility(View.GONE);
//            downloadFab.setVisibility(View.GONE);
////            holder.deleteFab.setVisibility(View.GONE);
//            progressBar.setVisibility(View.GONE);
//            videoViewLayout.setVisibility(View.GONE);
//
//            try{
//                Picasso.get().load(pImage).into(pImageIv);
//            }
//            catch (Exception e){
//
//            }
//        }

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();

        loadComments();

        // Set subTitle of actionbar
        actionBar.setSubtitle("Signed In as: "+ myEmial);

        // Set Comment button click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();
            }
        });

        // Like button click
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });

        // More button click
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOption();
            }
        });

        // Share button click
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String pTitle = pTitleTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
                if (bitmapDrawable == null){
                    // Post without image
                    shareTextOnly(pDescription);
                }
                else {
                    // Post with image
                    // Convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pDescription, bitmap);
                }
            }
        });

        // Click like count to start PostLikedByActivity, and pass the post id
        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostDetailActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });

    }

    private void addToHisNotification(String hisUid, String pId, String notification){
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Added successfully
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed
            }
        });
    }

    private void shareTextOnly(String pDescription) {
        // Concatenate title and description to share
        String shareBody = pDescription;
//        String shareBody = pTitle +"\n"+ pDescription;

        // Share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");  // In case you share via email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);   // Text to share
        startActivity(Intent.createChooser(sIntent, "Share via"));  // Message to show share dialog
    }

    private void shareImageAndText(String pDescription, Bitmap bitmap) {
        // Concatenate title and description to share
        String shareBody = pDescription;
//        String shareBody = pTitle +"\n"+ pDescription;

        // First, we will save image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        // Share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Share via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try{
            imageFolder.mkdir(); // Create if not exist
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.tomykrisgreen.airbasetabbed.fileprovider", file);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void loadComments() {
        // Layout (Linear) for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        // Set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        // Init comments list
        commentList = new ArrayList<>();

        // Path of the post to get its comment
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    // Pass myUid and postId as parameters of contructor of Comment adapter

                    // set up adapter
                    adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
                    // Set adapter
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOption() {
        // Creating popup menu with delete option
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        // Show delete option only in current user post
        if (hisUid.equals(myUid)){
            // Add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }

        // Popup click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0){
                    // Delete is clicked
                    beginDelete();
                }
                else if (id == 1){
                    // Edit is clicked
                    // Start AddPost Activity with key 'editPost' and id of the post clicked
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        // Show menu
        popupMenu.show();
    }

    private void beginDelete() {
        // Post can be with or without image
        if (pImage.equals("noImage")){
            // Post is without image
            deleteWithoutImage();
        }
        else {
            // Post is with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        // ProgressBar
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");

        /* Steps:
        1. Delete image using url
        2. Delete from database using post id
         */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted, delete from database
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();  // Remove value from firebase where pId matches
                        }
                        // Deleted
                        Toast.makeText(PostDetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteWithoutImage() {
        // ProgressBar
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();  // Remove value from firebase where pId matches
                }
                // Deleted
                Toast.makeText(PostDetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        // When details of post is loading, also check if current user has liked it or not
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)){
                    // User has liked this post
                    /* To indicate that the post is liked by (signedIn) user,
                    change drawable left icon of like button
                    change text of like button from 'Like' to 'Liked'
                     */
                    likeBtn.setImageResource(R.drawable.heart_clicked);
                    likeBtn.setTag("Liked");
//                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
//                    likeBtn.setText("Liked");
                }
                else {
                    // User has not liked this post
                     /* To indicate that the post is not liked by (signedIn) user,
                    change drawable left icon of like button
                    change text of like button from 'Liked' to 'Like'
                     */
                    likeBtn.setImageResource(R.drawable.heart_not_clicked);
                    likeBtn.setTag("Like");
//                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
//                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        // Get total no of likes for the post whose like button clicked
        // If currently signed in user has not liked it before, increase value by +1, else, decrease by -1
        mProcessLike = true;
        // Get id of the post clicked
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike){
                    if (snapshot.child(postId).hasChild(myUid)){
                        // Already liked, so remove like
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                    }
                    else {
                        // Not liked, like it
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");  // Set any value
                        mProcessLike = false;

                        addToHisNotification(""+hisUid, ""+postId, "liked your post");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding comment...");

        // Get data from comment edit text
        String comment = commentEt.getText().toString().trim();
        // Validate
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Enter comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        // Each post will have a child 'Comments' that will contain comments of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        // Put info in hasMap
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmial);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        // Put data in database
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Added
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");

                        updateCommentCount();

                        addToHisNotification(""+hisUid, ""+postId, "commented on your post");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateCommentCount() {
        // Increase comment count whenever user adds comment
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment){
                    String comments = ""+ snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) +1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        // Get Current user info
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    // Get data
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();

                    // Set user image in comment part
                    try{
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvatarIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        // Get post using the id of the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Keep checking the post until the required post is found
                for (DataSnapshot ds: snapshot.getChildren()){
                    // Get data
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();

                    // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyy hh:mm aa", calendar).toString();

                    // Set data
//                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes + "Likes");
                    pTimeTv.setText(pTime);
                    pCommentsTv.setText(commentCount + "Comments");

                    uNameTv.setText(hisName);

                    if (pDescriptionTv != null && pImageIv.equals("noImage") && videoView == null){
                        pImageIv.setVisibility(View.GONE);
                        pDescriptionTv.setVisibility(View.VISIBLE);

                        videoView.setVisibility(View.GONE);
//            holder.timeTv.setVisibility(View.GONE);
                        downloadFab.setVisibility(View.GONE);
//            holder.deleteFab.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        videoViewLayout.setVisibility(View.GONE);
                    }

                    // Set image of user who posted
                    // If there is no image i.e. pImage.equals("noImage"), then, hide ImageView
                    if (pImageIv.equals("noImage")){
                        // Hide imageView
                        pImageIv.setVisibility(View.GONE);
                        videoView.setVisibility(View.VISIBLE);
                    }
                    else {
                        // Show imageView
                        pImageIv.setVisibility(View.VISIBLE);
                        pDescriptionTv.setVisibility(View.VISIBLE);

                        videoView.setVisibility(View.GONE);
//            holder.timeTv.setVisibility(View.GONE);
                        downloadFab.setVisibility(View.GONE);
//            holder.deleteFab.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        videoViewLayout.setVisibility(View.GONE);

                        try{
                            Picasso.get().load(pImage).into(pImageIv);
                        }
                        catch (Exception e){

                        }
                    }

                    // Set user image in comment part
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img).into(uPictureIv);
                    }
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
            myEmial = user.getEmail();
            myUid = user.getUid();
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

    // Inflate options menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Hide some menu items
        menu.findItem(R.id.action_shop).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    // Handle menu items clicks

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();

            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}