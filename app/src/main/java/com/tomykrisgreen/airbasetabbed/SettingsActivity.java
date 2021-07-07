package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
    SwitchCompat postSwitch;

    // Use shared preferences to save the state of switch
    SharedPreferences sp;
    SharedPreferences.Editor editor;  // To edit value of shared preference

    // Constant for Topic
    private static final String TOPIC_POST_NOTIFICATION = "POST";   // Assign any value but use same for this kind of notifications

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        postSwitch = findViewById(R.id.postSwitch);

        // Init sp
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+TOPIC_POST_NOTIFICATION, false);
        // If enabled, check switch, otherwise, uncheck switch - by default unchecked/false
        if (isPostEnabled){
            postSwitch.setChecked(true);
        }
        else {
            postSwitch.setChecked(false);
        }

        // Implement switch change listener
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // Edit switch state
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();

                if (isChecked){
                    subscribePostNotification();
                }
                else {
                    unsubscribePostNotification();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void unsubscribePostNotification() {
        // Unsubscribe to a topic (POST) to disable its notification
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+ TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive post notifications";
                        if (!task.isSuccessful()){
                            msg = "Un-subscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribePostNotification() {
        // Subscribe to a topic (POST) to enable its notification
        FirebaseMessaging.getInstance().subscribeToTopic(""+ TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive post notifications";
                        if (!task.isSuccessful()){
                            msg = "Subscription failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}