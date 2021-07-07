package com.tomykrisgreen.airbasetabbed

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class LoginKotlinActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 100
    var mGoogleSignInClient: GoogleSignInClient? = null

    // Views
    var mEmailET: EditText? = null
    var mPasswordET: EditText? = null
    var notHaveAccountTV: TextView? = null
    var mRecoverPasswordTV: TextView? = null
    var mLoginBtn: Button? = null
    var mGoogleLoginBtn: SignInButton? = null

    //ProgressBar to display while registering user
    var progressDialog: ProgressDialog? = null

    // Declare an instance of FirebaseAuth
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_kotlin)

        // Actionbar and its Title

        // Actionbar and its Title
        val actionBar = supportActionBar
        actionBar!!.setTitle("Login")
        //Enable Back button
        //Enable Back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        // Before mAuth
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        // Before mAuth
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //                .requestIdToken(getString(R.string.default_web_client_id))  // TODO later
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize the instance of FirebaseAuth

        // Initialize the instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        mEmailET = findViewById(R.id.emailET)
        mPasswordET = findViewById(R.id.passwordET)
        mLoginBtn = findViewById(R.id.loginBtn)
        notHaveAccountTV = findViewById(R.id.no_accountTV)
        mRecoverPasswordTV = findViewById(R.id.recoverPasswordTV)
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn)

        progressDialog = ProgressDialog(this)


        mLoginBtn?.setOnClickListener(View.OnClickListener {
            // Input data
            val email = mEmailET?.getText().toString().trim { it <= ' ' }
            val password = mPasswordET?.getText().toString().trim { it <= ' ' }

            //Validate
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Set error and focus of email EditText
                mEmailET?.setError("Invalid email")
                mEmailET?.setFocusable(true)
            } else {
                loginUser(email, password)
            }
        })

        notHaveAccountTV?.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@LoginKotlinActivity, RegisterKotlinActivity::class.java))
            finish()
        })

        mRecoverPasswordTV?.setOnClickListener(View.OnClickListener { showRecoverPasswordDialog() })

        // Handle Google Login button

        // Handle Google Login button
        mGoogleLoginBtn?.setOnClickListener(View.OnClickListener {
            val signInIntent = mGoogleSignInClient!!.getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        })
    }

    private fun showRecoverPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recover Password")

        //Set Layout Linear Layout
        val linearLayout = LinearLayout(this)
        // Set views in Dialog
        val emailET = EditText(this)
        emailET.hint = "Email"
        emailET.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        // Sets the min width of an EditView to fit a text of n 'M' letters regardless of the actual text extension and text size
        emailET.minEms = 16
        linearLayout.addView(emailET)
        linearLayout.setPadding(10, 10, 10, 10)
        builder.setView(linearLayout)

        // Buttons
        builder.setPositiveButton("Recover") { dialogInterface, i -> // Input email
            val email = emailET.text.toString().trim { it <= ' ' }
            beginRecovery(email)
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun beginRecovery(email: String) {
        progressDialog!!.show()
        progressDialog!!.setMessage("Sending Recovery email")
        mAuth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            progressDialog!!.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this@LoginKotlinActivity, "Recovery email sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@LoginKotlinActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            progressDialog!!.dismiss()
            Toast.makeText(this@LoginKotlinActivity, "" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(email: String, password: String) {
        progressDialog!!.show()
        progressDialog!!.setMessage("Signing In...")
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        progressDialog!!.dismiss()

                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.currentUser
                        startActivity(Intent(this@LoginKotlinActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        progressDialog!!.dismiss()

                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@LoginKotlinActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e -> // If error, dismiss progress dialog and show error message
                    progressDialog!!.dismiss()
                    Toast.makeText(this@LoginKotlinActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Go to previous activity
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this@LoginKotlinActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.currentUser

                        // If user is signing in for the first time, then, get and show user info from Google account
                        if (task.result.additionalUserInfo.isNewUser) {
                            // Get user email and uid from auth
                            val email = user.email
                            val uid = user.uid
                            // When user is registered store user info in firebase realtime database too
                            //Using HashMap
                            val hashMap = HashMap<Any, String>()
                            // Put info in hashMap
                            hashMap["email"] = email
                            hashMap["uid"] = uid
                            hashMap["name"] = "" // To be added later
                            hashMap["onlineStatus"] = "Online" // To be added later
                            hashMap["typingTo"] = "noOne" // To be added later
                            hashMap["phone"] = "" // To be added later
                            hashMap["image"] = "" // To be added later
                            hashMap["cover"] = "" // To be added later

                            //Firebase database instance
                            val database = FirebaseDatabase.getInstance()
                            // Path to store user data named "Users"
                            val reference = database.getReference("Users")
                            // Put data within hashMap in databse
                            reference.child(uid).setValue(hashMap)
                        }

                        //Show User email in Toast
                        Toast.makeText(this@LoginKotlinActivity, "" + user.email, Toast.LENGTH_SHORT).show()

                        //Direct to Profile Activity after logged in
                        startActivity(Intent(this@LoginKotlinActivity, DashboardActivity::class.java))
                        finish()
                        //                            updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@LoginKotlinActivity, "Login failed", Toast.LENGTH_SHORT).show()
                        //                            updateUI(null);
                    }
                }.addOnFailureListener { e -> // Get and show error message
                    Toast.makeText(this@LoginKotlinActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }
}