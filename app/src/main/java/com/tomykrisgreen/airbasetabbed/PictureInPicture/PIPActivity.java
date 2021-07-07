package com.tomykrisgreen.airbasetabbed.PictureInPicture;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.tomykrisgreen.airbasetabbed.R;

public class PIPActivity extends AppCompatActivity {
    private VideoView videoView;
    private ImageButton pipBtn;

    private Uri videoUri;
    private static final String TAG = "PIP_TAG";

    private ActionBar actionBar;

    private PictureInPictureParams.Builder pictureInPictureParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_i_p);

        actionBar = getSupportActionBar();

        videoView = findViewById(R.id.videoView);
        pipBtn = findViewById(R.id.pipBtn);

        setVideoView(getIntent()); // Get and pass intent to a method that will handle video playback, intent contains url of video

        // Init PictureInPictureParams, requires Android O and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            pictureInPictureParams = new PictureInPictureParams.Builder();
        }

        pipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureInPictureMode();
            }
        });
    }

    private void setVideoView(Intent intent) {
        String videoURL = intent.getStringExtra("videoURL");
        Log.d(TAG, "setVideoView: URL:"+ videoURL);

        // MediaController for play, pause, seekbar, time etc.
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        videoUri = Uri.parse(videoURL);

        // Set media controller to videwView
        videoView.setMediaController(mediaController);
        // Set video uri to videoView
        videoView.setVideoURI(videoUri);

        // Add video prepare listener
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // When video is ready, play it
                Log.d(TAG, "onPrepared: Video Prepared, Playing...");
                mediaPlayer.start();
            }
        });
    }

    public void pictureInPictureMode(){
        // Requires Android O and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(TAG, "pictureInPictureMode: Supports PIP");

            // Set up height and width of PIP window
            Rational aspectRation = new Rational(videoView.getWidth(), videoView.getHeight());
            pictureInPictureParams.setAspectRatio(aspectRation).build();
            enterPictureInPictureMode(pictureInPictureParams.build());
        }else {
            Log.d(TAG, "pictureInPictureMode: Doesn't supports PIP");
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // Called when user presses Home button, enter in PIP mode, requires Android N
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            if (!isInPictureInPictureMode()){
                Log.d(TAG, "onUserLeaveHing: was not in PIP");
                pictureInPictureMode();
            }else {
                Log.d(TAG, "onUserLeaveHing: Already in PIP");
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode){
            Log.d(TAG, "onPictureInPictureModeChanged: Entered PIP");
            // Hide PIP button and actionBar
            pipBtn.setVisibility(View.GONE);
            actionBar.hide();
        }else {
            Log.d(TAG, "onPictureInPictureModeChanged: Exited PIP");
            pipBtn.setVisibility(View.VISIBLE);
            actionBar.show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // When video ONE is playing in PIP mode, and the user clicks video TWO, handle/play new video
        Log.d(TAG, "onNewIntent: Play new video");
        setVideoView(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoView.isPlaying()){
            videoView.stopPlayback();
        }
    }
}