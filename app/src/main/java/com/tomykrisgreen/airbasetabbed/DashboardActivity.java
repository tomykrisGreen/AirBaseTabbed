package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tomykrisgreen.airbasetabbed.Fragments.ChatListFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.ChatListViewPagerFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.GroupChatsFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.HomeKotlinFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.NotificationsFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.ProfileFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.ProfileKotlinFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.ProfileViewPagerFragment;
import com.tomykrisgreen.airbasetabbed.Fragments.UsersFragment;
import com.tomykrisgreen.airbasetabbed.Notification.Token;

public class DashboardActivity extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    private BottomNavigationView navigationView;

    boolean isPressed = false;

    // To display - Internet Connectivity
    TextView internetConnectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Actionbar and its Title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        firebaseAuth = FirebaseAuth.getInstance();

        internetConnectivity = findViewById(R.id.internetConnectivity);

        // Bottom Navigation
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        // Home Fragment Transaction (Default on start)
        actionBar.setTitle("Home"); // Change actionbar title
        HomeKotlinFragment fragment1 = new HomeKotlinFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.setCustomAnimations(R.anim.fade_out, R.anim.slide_in);
        ft1.replace(R.id.content, fragment1, "").addToBackStack(null);
        ft1.setReorderingAllowed(true).commit();


        checkUserStatus();

        checkConnectivity();

//        // Update Token
//        updateToken(FirebaseInstanceId.getInstance().toString());

    }

    private void checkConnectivity() {
        // Initialize intent filter
        IntentFilter intentFilter = new IntentFilter();
        // Add action
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // Register receiver
        registerReceiver(new ConnectionReceiver(), intentFilter);

        // Initialize listener
        ConnectionReceiver.listener = this;

        // Initialize connectivity manager
        ConnectivityManager manager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize network info
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        // Get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        //Display snackBar
        showSnackBar(isConnected);
    }

    private void showSnackBar(boolean isConnected) {
        // Initialize color and message
        String message;
        int color;

        // Check condition
        if (isConnected){
            // When internet is connected, set message
            message = "   Connected to the internet";
            // Set color
            color = Color.WHITE;
        }else {
            // When internet is disconnected, set message
            message = "   No internet connection";
            // Set color
            color = Color.RED;
        }

        // Initialize snackBar
        Snackbar snackbar = Snackbar.make(findViewById(R.id.internetConnectivity), message, Snackbar.LENGTH_LONG);

        // Initialize view
        View view = snackbar.getView();

        snackbar.setAnchorView(navigationView);
//        // Show snackBar at top of screen
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
//        params.gravity = Gravity.TOP;
//        view.setLayoutParams(params);
        // Assign variable
        TextView textView = view.findViewById(R.id.snackbar_text);
        // Set text color
        textView.setTextColor(color);

        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_wifi_alert_outlined, 0, 0,0);

//        // Set snackBar Background Color
//        snackbar.setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
        // Show snackBar
        snackbar.show();
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        // Display snackBar
        showSnackBar(isConnected);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // Call method
//        checkConnectivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Call method
//        checkConnectivity();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // Handle item clicks
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            // Home fragment transaction
                            actionBar.setTitle("Home"); // Change actionbar title
                            HomeKotlinFragment fragment1 = new HomeKotlinFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.setCustomAnimations(R.anim.fade_out, R.anim.slide_in);
                            ft1.replace(R.id.content, fragment1, "");   //.addToBackStack(null);
                            ft1.setReorderingAllowed(true).commit();
//                            break;
                            return true;

                        // Profile fragment transaction
                        case R.id.nav_chats:
                            // Chats fragment transaction
                            actionBar.setTitle("Chats"); // Change actionbar title
                            ChatListViewPagerFragment fragment4 = new ChatListViewPagerFragment();   //ChatListFragment fragment4 = new ChatListFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.setCustomAnimations(R.anim.fade_out, R.anim.slide_in);;
                            ft2.replace(R.id.content, fragment4, "");   //.addToBackStack(null);
                            ft2.setReorderingAllowed(true).commit();
//                            break;
                            return true;

                        // Users fragment transaction
                        case R.id.nav_add_posts:

                            Intent intentAddPost = new Intent(DashboardActivity.this, AddPostActivity.class);
                            intentAddPost.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                            startActivity(intentAddPost);
//                            break;

//                            startActivity(new Intent(DashboardActivity.this, AddPostActivity.class));


//                            // Users fragment transaction
//                            actionBar.setTitle("Users"); // Change actionbar title
//                            UsersFragment fragment3 = new UsersFragment();     //  UsersFragment fragment3 = new UsersFragment();
//                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
//                            ft3.replace(R.id.content, fragment3, "").addToBackStack(null);
//                            ft3.commit();
                            return true;

                        // Chats fragment transaction
                        case R.id.nav_profile:
                            // Profile fragment transaction
                            actionBar.setTitle("Profile"); // Change actionbar title
                            ProfileViewPagerFragment fragment2 = new ProfileViewPagerFragment();    // ProfileKotlinFragment fragment2 = new ProfileKotlinFragment();    // Changed from ProfileFragment to ProfileKotlinFragment
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.setCustomAnimations(R.anim.fade_out, R.anim.slide_in);
                            ft4.replace(R.id.content, fragment2, "");   //.addToBackStack(null);
                            ft4.setReorderingAllowed(true).commit();
//                            break;
                            return true;

                        // Notifications fragment transaction
                        case R.id.nav_more:
                            // Notifications fragment transaction
                            showMoreOptions();
//                            actionBar.setTitle("More"); // Change actionbar title
//                            NotificationsFragment fragment5 = new NotificationsFragment();
//                            FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
//                            ft5.replace(R.id.content, fragment5, "");
//                            ft5.commit();
                            return true;
                    }
                    return false;
                }
            };

    private void showMoreOptions() {
        // Popup menu to show more options
        PopupMenu popupMenu = new PopupMenu(this, navigationView, Gravity.END);
        // Items to show in menu
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Notifications");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Group Chats");

        // Menu clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0){
                    // Notifications clicked

                    // Notifications fragment transaction
                    actionBar.setTitle("Notifications"); // Change actionbar title
                    NotificationsFragment fragment5 = new NotificationsFragment();
                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                    ft5.setCustomAnimations(R.anim.fade_out, R.anim.slide_in);
                    ft5.replace(R.id.content, fragment5, "");   //.addToBackStack(null);
                    ft5.setReorderingAllowed(true).commit();
                }
                else if (id == 1){
                    // Group chat clicked

                    // Notifications fragment transaction
                    actionBar.setTitle("Group Chat"); // Change actionbar title
                    GroupChatsFragment fragment6 = new GroupChatsFragment();
                    FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                    ft6.setCustomAnimations(R.anim.fade_out, R.anim.slide_in);
                    ft6.replace(R.id.content, fragment6, "");   //.addToBackStack(null);
                    ft6.setReorderingAllowed(true).commit();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void checkUserStatus(){
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            // User is signed in
            // Set email of logged in user
            //mProfileTV.setText(user.getEmail());
            mUID = user.getUid();

            // Save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            // Update Token
            updateToken(FirebaseInstanceId.getInstance().toString());
        }
        else {
            // User not signed in, go to Main Activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        HomeKotlinFragment fragment1 = new HomeKotlinFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "").addToBackStack(null);
        ft1.setReorderingAllowed(true).commit();

        // Check condition
        if (isPressed){
            // When double back pressed within a second, clear all activity
            finishAffinity();
            // Exit
            System.exit(0);
        }else {
            // When double back pressed delayed 2 seconds, display toast
            Toast.makeText(this, "Please, click again to exit", Toast.LENGTH_SHORT).show();

            // Set true
            isPressed = true;
        }
        // Initialize Runnable
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Set false
                isPressed = false;
            }
        };
        // Handler delay for 2 seconds
        new Handler().postDelayed(runnable, 2000);

//        super.onBackPressed();
////        finish();
//
//        HomeKotlinFragment fragment1 = new HomeKotlinFragment();
//        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
//        ft1.replace(R.id.content, fragment1, "").addToBackStack(null);
//        ft1.setReorderingAllowed(true).commit();
    }

    @Override
    protected void onStart() {
        // Check on start of app
        checkUserStatus();
//        checkConnectivity();
        super.onStart();
    }


}