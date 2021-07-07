//package com.tomykrisgreen.airbasetabbed;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.InputType;
//import android.util.Patterns;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.SignInButton;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.HashMap;
//
//public class LoginActivity extends AppCompatActivity {
//    private static final int RC_SIGN_IN = 100;
//    GoogleSignInClient mGoogleSignInClient;
//    // Views
//    EditText mEmailET, mPasswordET;
//    TextView notHaveAccountTV, mRecoverPasswordTV;
//    Button mLoginBtn;
//    SignInButton mGoogleLoginBtn;
//
//    //ProgressBar to display while registering user
//    ProgressDialog progressDialog;
//
//    // Declare an instance of FirebaseAuth
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        // Actionbar and its Title
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle("Login");
//        //Enable Back button
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
//
//        // Before mAuth
//        // Configure sign-in to request the user's ID, email address, and basic
//// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
////                .requestIdToken(getString(R.string.default_web_client_id))  // TODO later
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        // Initialize the instance of FirebaseAuth
//        mAuth = FirebaseAuth.getInstance();
//
//        mEmailET = findViewById(R.id.emailET);
//        mPasswordET = findViewById(R.id.passwordET);
//        mLoginBtn = findViewById(R.id.loginBtn);
//        notHaveAccountTV = findViewById(R.id.no_accountTV);
//        mRecoverPasswordTV = findViewById(R.id.recoverPasswordTV);
//        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);
//
//        progressDialog = new ProgressDialog(this);
//
//
//        mLoginBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Input data
//                String email = mEmailET.getText().toString().trim();
//                String password = mPasswordET.getText().toString().trim();
//
//                //Validate
//                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//                    // Set error and focus of email EditText
//                    mEmailET.setError("Invalid email");
//                    mEmailET.setFocusable(true);
//                }
//                else {
//                    loginUser(email, password);
//                }
//            }
//        });
//
//        notHaveAccountTV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
//                finish();
//            }
//        });
//
//        mRecoverPasswordTV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showRecoverPasswordDialog();
//            }
//        });
//
//        // Handle Google Login button
//        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
//
//            }
//        });
//    }
//
//    private void showRecoverPasswordDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Recover Password");
//
//        //Set Layout Linear Layout
//        LinearLayout linearLayout = new LinearLayout(this);
//        // Set views in Dialog
//        EditText emailET = new EditText(this);
//        emailET.setHint("Email");
//        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//
//        // Sets the min width of an EditView to fit a text of n 'M' letters regardless of the actual text extension and text size
//        emailET.setMinEms(16);
//
//        linearLayout.addView(emailET);
//        linearLayout.setPadding(10, 10, 10, 10);
//
//        builder.setView(linearLayout);
//
//        // Buttons
//        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                // Input email
//                String email = emailET.getText().toString().trim();
//
//                beginRecovery(email);
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//            }
//        });
//
//        builder.create().show();
//
//    }
//
//    private void beginRecovery(String email) {
//        progressDialog.show();
//        progressDialog.setMessage("Sending Recovery email");
//
//        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                progressDialog.dismiss();
//
//                if (task.isSuccessful()){
//                    Toast.makeText(LoginActivity.this, "Recovery email sent", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                progressDialog.dismiss();
//
//                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void loginUser(String email, String password) {
//        progressDialog.show();
//        progressDialog.setMessage("Signing In...");
//
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            progressDialog.dismiss();
//
//                            // Sign in success, update UI with the signed-in user's information
//                            FirebaseUser user = mAuth.getCurrentUser();
//
//                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
//                            finish();
//                        } else {
//                            progressDialog.dismiss();
//
//                            // If sign in fails, display a message to the user.
//                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // If error, dismiss progress dialog and show error message
//                progressDialog.dismiss();
//
//                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed(); // Go to previous activity
//        return super.onSupportNavigateUp();
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account.getIdToken());
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void firebaseAuthWithGoogle(String idToken) {
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            FirebaseUser user = mAuth.getCurrentUser();
//
//                            // If user is signing in for the first time, then, get and show user info from Google account
//                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
//                                // Get user email and uid from auth
//                                String email = user.getEmail();
//                                String uid = user.getUid();
//                                // When user is registered store user info in firebase realtime database too
//                                //Using HashMap
//                                HashMap<Object, String> hashMap = new HashMap<>();
//                                // Put info in hashMap
//                                hashMap.put("email", email);
//                                hashMap.put("uid", uid);
//                                hashMap.put("name", ""); // To be added later
//                                hashMap.put("onlineStatus", "Online"); // To be added later
//                                hashMap.put("typingTo", "noOne"); // To be added later
//                                hashMap.put("phone", ""); // To be added later
//                                hashMap.put("image", ""); // To be added later
//                                hashMap.put("cover", ""); // To be added later
//
//                                //Firebase database instance
//                                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                // Path to store user data named "Users"
//                                DatabaseReference reference = database.getReference("Users");
//                                // Put data within hashMap in databse
//                                reference.child(uid).setValue(hashMap);
//                            }
//
//                            //Show User email in Toast
//                            Toast.makeText(LoginActivity.this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
//
//                            //Direct to Profile Activity after logged in
//                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
//                            finish();
////                            updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
////                            updateUI(null);
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // Get and show error message
//                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}