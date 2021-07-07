package com.tomykrisgreen.airbasetabbed.Adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.AddPostActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelPost;
import com.tomykrisgreen.airbasetabbed.PictureInPicture.PIPActivity;
import com.tomykrisgreen.airbasetabbed.PostDetailActivity;
import com.tomykrisgreen.airbasetabbed.PostLikedByActivity;
import com.tomykrisgreen.airbasetabbed.R;
import com.tomykrisgreen.airbasetabbed.ThereProfileActivity;
import com.tomykrisgreen.airbasetabbed.VideoUpload.AdapterVideo;
import com.tomykrisgreen.airbasetabbed.VideoUpload.ModelVideo;
import com.tomykrisgreen.airbasetabbed.ZoomImager.ZoomImageActivity;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef;  // For likes database node
    private DatabaseReference postsRef;  // Reference of posts

    boolean mProcessLike = false;

    public AdapterPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.keepSynced(true);
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsRef.keepSynced(true);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int i) {
        final ModelPost modelVideo = postList.get(i);

        // Get data
        String uid = postList.get(i).getUid();
        String uEmail = postList.get(i).getuEmail();
        String uName = postList.get(i).getuName();
        String uDp = postList.get(i).getuDp();
        String pId = postList.get(i).getpId();
//        String pTitle = postList.get(i).getpTitle();
        String pDescription = postList.get(i).getpDescr();
        String pImage = postList.get(i).getpImage();
        String pTimeStamp = postList.get(i).getpTime();
        String pLikes = postList.get(i).getpLikes();   // Total no of likes
        String pComments = postList.get(i).getpComments();   // Total no of comments

        //  ------ For Video Aspect
        String videoUrl = postList.get(i).getVideoUrl();

        // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyy hh:mm aa", calender).toString();

        // Set data --- For Video Aspect ----- //
//        holder.titleTv.setText(title);
//        holder.timeTv.setText(pTime);
        setVideoUrl(modelVideo, holder);

        // Set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
//        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes +"Likes");  // E.g. 10 Likes
        holder.pCommentsTv.setText(pComments +"Comments");  // E.g. 10 Comments
        // Set likes for each post
        setLikes(holder, pId);

        // set user dp
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.profile).into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        // To display description of post only
        if (!pDescription.equals("") && pImage.equals("noImage") && videoUrl.equals("")){
            holder.pImageIv.setVisibility(View.GONE);
            holder.pDescriptionTv.setVisibility(View.VISIBLE);

            holder.videoView.setVisibility(View.GONE);
//            holder.timeTv.setVisibility(View.GONE);
            holder.downloadFab.setVisibility(View.GONE);
//            holder.deleteFab.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.videoViewLayout.setVisibility(View.GONE);
        }

        // set post image
        // If there is no image, i.e. pImage.equals("noImage"), then hide image view
        if (pImage.equals("noImage")){
            // Hide imageView
            holder.pImageIv.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
        }
        else {
            // Show imageView and description
            holder.pImageIv.setVisibility(View.VISIBLE);
            holder.pDescriptionTv.setVisibility(View.VISIBLE);

            holder.videoView.setVisibility(View.GONE);
//            holder.timeTv.setVisibility(View.GONE);
            holder.downloadFab.setVisibility(View.GONE);
//            holder.deleteFab.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.videoViewLayout.setVisibility(View.GONE);

            try{
                Picasso.get().load(pImage).into(holder.pImageIv);
            }
            catch (Exception e){

            }
        }



        // For Picture-In-Picture view
        holder.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start activity with video url
                Intent intent = new Intent(context, PIPActivity.class);
                intent.putExtra("videoURL", videoUrl);
                context.startActivity(intent);
            }
        });


        // Handle button clicks
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOption(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        holder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ZoomImageActivity.class);
                intent.putExtra("postId", pId);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        holder.pDescriptionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);  // Will get details of post using this id, its the id of the post clicked
                context.startActivity(intent);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Get total no of likes for the post whose like button clicked
                // If currently signed in user has not liked it before, increase value by +1, else, decrease by -1
                int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                mProcessLike = true;
                // Get id of the post clicked
                String postId = postList.get(i).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike){
                            if (snapshot.child(postId).hasChild(myUid)){
                                // Animation effect increase in likes count
                                Animation animationSlideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out);
                                holder.pLikesTv.startAnimation(animationSlideOut);

                                // Animation effect on Like image heart
                                Animation animationZoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
                                holder.likeBtn.startAnimation(animationZoomIn);

                                Animation animationZoomOutBack = AnimationUtils.loadAnimation(context, R.anim.zoom_out_back);
                                holder.likeBtn.startAnimation(animationZoomOutBack);

                                Animation animationBounce2 = AnimationUtils.loadAnimation(context, R.anim.bounce_2);
                                holder.likeBtn.startAnimation(animationBounce2);


                                // Already liked, so remove like
                                postsRef.child(postId).child("pLikes").setValue("" +(pLikes -1));
                                likesRef.child(postId).child(myUid).removeValue();



                                mProcessLike = false;
                            }
                            else {
                                // Animation effect decrease in likes count
                                Animation animationSlideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in);
                                holder.pLikesTv.startAnimation(animationSlideIn);

                                // Animation effect on like image heart
                                Animation animationZoomOut = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                                holder.likeBtn.startAnimation(animationZoomOut);

                                Animation animationBounce2 = AnimationUtils.loadAnimation(context, R.anim.bounce_2);
                                holder.likeBtn.startAnimation(animationBounce2);


                                // Not liked, like it
                                postsRef.child(postId).child("pLikes").setValue("" +(pLikes +1));
                                likesRef.child(postId).child(myUid).setValue("Liked");  // Set any value


                                mProcessLike = false;

                                addToHisNotification(""+uid, ""+pId, "liked your post");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);  // Will get details of post using this id, its the id of the post clicked
                context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Some posts contain only texts and some contains image and text, so, we'll handle both
                // Get image from ImageView
                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.pImageIv.getDrawable();

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



//                // Will implement later
//                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click to go to ThereProfileActivity
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        // Click like count to start PostLikedByActivity, and pass the post id
        holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostLikedByActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        //  -----------  Video Aspects -=---------
        // Handle click download fab
        holder.downloadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadVideo(modelVideo);
            }
        });

//         Handle click delete fab
        holder.deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show alert dialog to confirm delete
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete video? ")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Confirmed to delete
                                deleteVideo(modelVideo);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

    }

    private void setVideoUrl(ModelPost modelVideo, AdapterPost.MyHolder holder) {
        holder.progressBar.setVisibility(View.VISIBLE);

        // Get videoUrl
        String videoUrl = modelVideo.getVideoUrl();

        // Media controller for play, pause, seekbar, timer etc. ...
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(holder.videoView);

        Uri videoUri = Uri.parse(videoUrl);
        holder.videoView.setMediaController(mediaController);
        holder.videoView.setVideoURI(videoUri);

        holder.videoView.requestFocus();
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                holder.videoView.seekTo(1);

                // Video is ready to play
//                mediaPlayer.pause();
//                mediaPlayer.start();
                if (!holder.videoView.isPlaying()){
                    holder.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                            Log.d(TAG, "onError: Error playing video");
                            return false;
                        }
                    });
                }

                holder.progressBar.setVisibility(View.GONE);
            }
        });

        holder.videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                // To check if buffering, rendering etc.
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                        // Rendering started
//                        holder.progressBar.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.GONE);
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                        // Buffering started
                        holder.progressBar.setVisibility(View.VISIBLE);
                        return true;
                    }// Buffering ended
//                        holder.progressBar.setVisibility(View.GONE);
                }
                return false;
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
//                mediaPlayer.stop();
                mediaPlayer.start();  // Restart video if completed
//                holder.replayVideo.setVisibility(View.VISIBLE);
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
        context.startActivity(Intent.createChooser(sIntent, "Share via"));  // Message to show share dialog
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
        context.startActivity(Intent.createChooser(sIntent, "Share via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try{
            imageFolder.mkdir(); // Create if not exist
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.tomykrisgreen.airbasetabbed.fileprovider", file);
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }


    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){
                    // User has liked this post
                    /* To indicate that the post is liked by (signedIn) user,
                    change drawable left icon of like button
                    change text of like button from 'Like' to 'Liked'
                     */
                    holder.likeBtn.setImageResource(R.drawable.heart_clicked);
//                    holder.likeBtn.setTag("Liked");
//                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
//                    holder.likeBtn.setText("Liked");
                }
                else {
                    // User has not liked this post
                     /* To indicate that the post is not liked by (signedIn) user,
                    change drawable left icon of like button
                    change text of like button from 'Liked' to 'Like'
                     */
                    holder.likeBtn.setImageResource(R.drawable.heart_not_clicked);
//                    holder.likeBtn.setTag("Like");
//                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
//                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOption(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        // Creating popup menu with delete option
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        // Show delete option only in current user post
        if (uid.equals(myUid)){
            // Add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Post Details");

        // Popup click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0){
                    // Delete is clicked
                    beginDelete(pId, pImage);
                }
                else if (id == 1){
                    // Edit is clicked
                    // Start AddPost Activity with key 'editPost' and id of the post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                else if (id == 2){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);  // Will get details of post using this id, its the id of the post clicked
                    context.startActivity(intent);
                }
                return false;
            }
        });
        // Show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        // Post can be with or without image
        if (pImage.equals("noImage")){
            // Post is without image
            deleteWithoutImage(pId);
        }
        else {
            // Post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        // ProgressBar
        ProgressDialog progressDialog = new ProgressDialog(context);
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
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();  // Remove value from firebase where pId matches
                        }
                        // Deleted
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteWithoutImage(String pId) {
        // ProgressBar
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();  // Remove value from firebase where pId matches
                }
                // Deleted
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //  --------  Video Aspect
    private void deleteVideo(ModelPost modelVideo) {
        String videoId = modelVideo.getId();
        String videoUrl = modelVideo.getVideoUrl();

        // Delete from firebase storage
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Deleted from firebase storage

                        // 2. Delete from firebase database
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        databaseReference.child(videoId).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Delete from firebase database
                                        Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed delete from firebase database
                                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed delete from firebase storage, show error message
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadVideo(ModelPost modelVideo) {
        String videoUrl = modelVideo.getVideoUrl(); // url of video will be used to download video

        // Get video reference using video url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        storageReference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        // Get file/video basic info e.g. title, type
                        String fileName = storageMetadata.getName();  // File name in firebase storage
                        String fileType = storageMetadata.getContentType();  // File type in firebase storage  e.g. video/mp4
                        String fileDirectory = Environment.DIRECTORY_DOWNLOADS;  // Video will be saved in this folder i.e. Downloads

                        // Init Download Manager
                        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

                        // Get uri of file to be downloaded
                        Uri uri = Uri.parse(videoUrl);

                        // Create download request; new request for each download (we can download multiple files at once)
                        DownloadManager.Request request = new DownloadManager.Request(uri);

                        // Notification visibility
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        // Set destination path
                        request.setDestinationInExternalPublicDir(""+ fileDirectory, ""+fileName+".mp4");

                        // Add request to queue - can be multiple requests
                        downloadManager.enqueue(request);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to download
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
 //  Video Aspect ends

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // View Holder class
    class MyHolder extends RecyclerView.ViewHolder{

        // Views from row_post.xml
        ImageView uPictureIv;
        ZoomInImageView pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        ImageView likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;


        // UI view of row_video.xml
        VideoView videoView;
        TextView titleTv, timeTv;
        ProgressBar progressBar;
        ImageView deleteFab, downloadFab, replayVideo;
        RelativeLayout videoViewLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.uTimeTv);
//            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);


            // Init UI view of row_video.xml
            videoView = itemView.findViewById(R.id.videoViewShow);
            titleTv = itemView.findViewById(R.id.titleTv);
            timeTv = itemView.findViewById(R.id.timeTvShow);
            progressBar = itemView.findViewById(R.id.progressBarPost);
            deleteFab = itemView.findViewById(R.id.deleteFab);
            downloadFab = itemView.findViewById(R.id.downloadFab);
//            replayVideo = itemView.findViewById(R.id.replayVideo);
            videoViewLayout = itemView.findViewById(R.id.videoViewLayout);
        }
    }
}
