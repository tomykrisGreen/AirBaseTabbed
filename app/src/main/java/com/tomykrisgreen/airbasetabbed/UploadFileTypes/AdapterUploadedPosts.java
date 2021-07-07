package com.tomykrisgreen.airbasetabbed.UploadFileTypes;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost;
import com.tomykrisgreen.airbasetabbed.Models.ModelPost;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterUploadedPosts extends RecyclerView.Adapter<AdapterUploadedPosts.MyHolder> {
    Context context;
    List<ModelUploadedFiles> uploadedPostsList;

    public AdapterUploadedPosts(Context context, List<ModelUploadedFiles> uploadedPostsList) {
        this.context = context;
        this.uploadedPostsList = uploadedPostsList;
    }

    @NonNull
    @Override
    public AdapterUploadedPosts.MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup,false);

        return new AdapterUploadedPosts.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        final ModelUploadedFiles modelVideo = uploadedPostsList.get(i);

        // Get data
        String pId = uploadedPostsList.get(i).getpId();
        String pImage = uploadedPostsList.get(i).getpImage();
        String pTimeStamp = uploadedPostsList.get(i).getpTime();

        //  ------ For Video Aspect
        String videoUrl = uploadedPostsList.get(i).getVideoUrl();

        // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyy hh:mm aa", calender).toString();

        // Set data --- For Video Aspect ----- //
//        holder.titleTv.setText(title);
//        holder.timeTv.setText(pTime);
        setVideoUrl(modelVideo, holder);

        try{
            Picasso.get().load(pImage).into(holder.pImageIv);
        }
        catch (Exception e){

        }


    }

    private void setVideoUrl(ModelUploadedFiles modelVideo, AdapterUploadedPosts.MyHolder holder) {
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
//                mediaPlayer.pause();
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
//                        holder.progressBar.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.GONE);
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                        // Buffering started
                        holder.progressBar.setVisibility(View.VISIBLE);
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                        // Buffering ended
//                        holder.progressBar.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.VISIBLE);
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

    @Override
    public int getItemCount() {
        return uploadedPostsList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        // Views from row_post.xml
        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        ImageView likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;


        // UI view of row_video.xml
        VideoView videoView;
        TextView titleTv, timeTv;
        ProgressBar progressBar;
        ImageView deleteFab, downloadFab;
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
            videoViewLayout = itemView.findViewById(R.id.videoViewLayout);
        }
    }
}