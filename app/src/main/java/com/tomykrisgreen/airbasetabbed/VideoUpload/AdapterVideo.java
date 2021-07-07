package com.tomykrisgreen.airbasetabbed.VideoUpload;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.HolderVideo> {
    // Context
    Context context;
    // Array list
    private ArrayList<ModelVideo> videoArrayList;

    public AdapterVideo(Context context, ArrayList<ModelVideo> videoArrayList) {
        this.context = context;
        this.videoArrayList = videoArrayList;
    }

    @NonNull
    @Override
    public HolderVideo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout row_video.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);
        return new HolderVideo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderVideo holder, int position) {
        // Get, format, set data, handle clicks

        // Get data
        final ModelVideo modelVideo = videoArrayList.get(position);

        String id = modelVideo.getId();
        String title = modelVideo.getTitle();
        String timestamp = modelVideo.getTimestamp();
        String videoUrl = modelVideo.getVideoUrl();

        // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String formattedDateTime = DateFormat.format("dd/MM/yyy K:mm a", calendar).toString();

        // Set data
        holder.titleTv.setText(title);
        holder.timeTv.setText(formattedDateTime);
        setVideoUrl(modelVideo, holder);

        // Handle click download fab
        holder.downloadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadVideo(modelVideo);
            }
        });

        // Handle click delete fab
        holder.deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show alert dialog to confirm delete
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete video? " + title)
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

    private void setVideoUrl(ModelVideo modelVideo, HolderVideo holder) {
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
                // Video is ready to play
                mediaPlayer.start();
            }
        });

        holder.videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                // To check if buffering, rendering etc.
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                        // Rendering started
                        holder.progressBar.setVisibility(View.VISIBLE);
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                        // Buffering started
                        holder.progressBar.setVisibility(View.VISIBLE);
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                        // Buffering ended
                        holder.progressBar.setVisibility(View.GONE);
                        return true;
                    }
                }
                return false;
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();  // Restart video if completed
            }
        });

    }

    private void deleteVideo(ModelVideo modelVideo) {
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
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Videos");
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

    private void downloadVideo(ModelVideo modelVideo) {
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

    @Override
    public int getItemCount() {
        return videoArrayList.size();
    }

    // View holder class, holds,  inits the UI view
    class HolderVideo extends RecyclerView.ViewHolder {

        // UI view of row_video.xml
        VideoView videoView;
        TextView titleTv, timeTv;
        ProgressBar progressBar;
        FloatingActionButton deleteFab, downloadFab;

        public HolderVideo(@NonNull View itemView) {
            super(itemView);

            // Init UI view of row_video.xml
            videoView = itemView.findViewById(R.id.videoViewShow);
            titleTv = itemView.findViewById(R.id.titleTv);
            timeTv = itemView.findViewById(R.id.timeTvShow);
            progressBar = itemView.findViewById(R.id.progressBarVideo);
            deleteFab = itemView.findViewById(R.id.deleteFab);
            downloadFab = itemView.findViewById(R.id.downloadFab);
        }
    }
}
