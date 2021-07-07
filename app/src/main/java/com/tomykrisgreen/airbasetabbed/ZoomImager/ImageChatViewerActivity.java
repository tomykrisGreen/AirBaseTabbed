package com.tomykrisgreen.airbasetabbed.ZoomImager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.R;
import com.zolad.zoominimageview.ZoomInImageView;

public class ImageChatViewerActivity extends AppCompatActivity {
    private ZoomInImageView imageView;
    private String imageUrl;

    private ProgressBar progressBarImageChatViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_chat_viewer);

        progressBarImageChatViewer = findViewById(R.id.progressBarImageChatViewer);

        imageView = findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("url");

        progressBarImageChatViewer.setVisibility(View.VISIBLE);

        Picasso.get().load(imageUrl).into(imageView);

        progressBarImageChatViewer.setVisibility(View.GONE);
    }
}