//package com.tomykrisgreen.airbasetabbed;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Patterns;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.HashMap;
//
//public class RegisterActivity extends AppCompatActivity {
//    // Views
//    EditText mEmailET, mPasswordET;
//    Button mRegisterBtn;
//    TextView mHaveAccount;
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
//        setContentView(R.layout.activity_register);
//
//        // Actionbar and its Title
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle("Create Account");
//        //Enable Back button
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
//
//        // Initialize the instance of FirebaseAuth
//        mAuth = FirebaseAuth.getInstance();
//
//        // Initialize Views
//        mEmailET = findViewById(R.id.emailET);
//        mPasswordET = findViewById(R.id.passwordET);
//        mRegisterBtn = findViewById(R.id.registerBtn);
//        mHaveAccount = findViewById(R.id.have_accountTV);
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Registering Account...");
//
//        //Handle Register Button click
//        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Input email, password
//                String email = mEmailET.getText().toString().trim();
//                String password = mPasswordET.getText().toString().trim();
//                //Validate
//                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//                    // Set error and focus of email EditText
//                    mEmailET.setError("Invalid email");
//                    mEmailET.setFocusable(true);
//                }
//                else if (password.length()<6){
//                    // Set error and focus of password EditText
//                    mPasswordET.setError("Password length must be at least 6 characters");
//                    mPasswordET.setFocusable(true);
//                }
//                else {
//                    registerUser(email, password);
//                }
//            }
//        });
//
//        mHaveAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
//                finish();
//            }
//        });
//    }
//
//    private void registerUser(String email, String password) {
//        progressDialog.show();
//
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, dismiss dialog and start register activity
//                            progressDialog.dismiss();
//
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            // Get user email and uid from auth
//                            String email = user.getEmail();
//                            String uid = user.getUid();
//                            // When user is registered store user info in firebase realtime database too
//                            //Using HashMap
//                            HashMap<Object, String> hashMap = new HashMap<>();
//                            // Put info in hashMap
//                            hashMap.put("email", email);
//                            hashMap.put("uid", uid);
//                            hashMap.put("name", ""); // To be added later
//                            hashMap.put("onlineStatus", "Online"); // To be added later
//                            hashMap.put("typingTo", "noOne"); // To be added later
//                            hashMap.put("phone", ""); // To be added later
//                            hashMap.put("image", ""); // To be added later
//                            hashMap.put("cover", ""); // To be added later
//
//                            //Firebase database instance
//                            FirebaseDatabase database = FirebaseDatabase.getInstance();
//                            // Path to store user data named "Users"
//                            DatabaseReference reference = database.getReference("Users");
//                            // Put data within hashMap in databse
//                            reference.child(uid).setValue(hashMap);
//
//                            Toast.makeText(RegisterActivity.this, "Registered \n" + user.getEmail(), Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
//                            finish();
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            progressDialog.dismiss();
//                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // If error, dismiss progress dialog and show error message
//                progressDialog.dismiss();
//
//                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed(); // Go to previous activity
//        return super.onSupportNavigateUp();
//    }
//}