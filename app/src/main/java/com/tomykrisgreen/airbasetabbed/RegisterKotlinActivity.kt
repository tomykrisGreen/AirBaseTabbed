package com.tomykrisgreen.airbasetabbed

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class RegisterKotlinActivity : AppCompatActivity() {
    // Views
    var mEmailET: EditText? = null
    var mPasswordET: EditText? = null
    var mRegisterBtn: Button? = null
    var mHaveAccount: TextView? = null

    //ProgressBar to display while registering user
    var progressDialog: ProgressDialog? = null

    // Declare an instance of FirebaseAuth
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_kotlin)

        // Actionbar and its Title

        // Actionbar and its Title
        val actionBar = supportActionBar
        actionBar!!.title = "Create Account"
        //Enable Back button
        //Enable Back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        // Initialize the instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Initialize Views
        mEmailET = findViewById(R.id.emailET)
        mPasswordET = findViewById(R.id.passwordET)
        mRegisterBtn = findViewById(R.id.registerBtn)
        mHaveAccount = findViewById(R.id.have_accountTV)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Registering Account...")

        //Handle Register Button click
        mRegisterBtn?.setOnClickListener(View.OnClickListener {
            // Input email, password
            val email = mEmailET?.getText().toString().trim { it <= ' ' }
            val password = mPasswordET?.getText().toString().trim { it <= ' ' }
            //Validate
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Set error and focus of email EditText
                mEmailET?.setError("Invalid email")
                mEmailET?.isFocusable = true
            } else if (password.length < 6) {
                // Set error and focus of password EditText
                mPasswordET?.setError("Password length must be at least 6 characters")
                mPasswordET?.isFocusable = true
            } else {
                registerUser(email, password)
            }
        })

        mHaveAccount?.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@RegisterKotlinActivity, LoginKotlinActivity::class.java))
            finish()
        })
    }

    private fun registerUser(email: String, password: String) {
//        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

        progressDialog!!.show()

        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, dismiss dialog and start register activity
                        progressDialog!!.dismiss()
                        val user = mAuth!!.currentUser
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


                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(uid)
                                .child("Following").child(uid)
                                .setValue(true)



                        Toast.makeText(this@RegisterKotlinActivity, "Registered \n" + user.getEmail(), Toast.LENGTH_SHORT).show()
//                        Toast.makeText(this@RegisterKotlinActivity, """Registered${user.email}""".trimIndent(), Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@RegisterKotlinActivity, DashboardActivity::class.java))
                        finish()

                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog!!.dismiss()
                        Toast.makeText(this@RegisterKotlinActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e -> // If error, dismiss progress dialog and show error message
                    FirebaseAuth.getInstance().signOut()
                    progressDialog!!.dismiss()
                    Toast.makeText(this@RegisterKotlinActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Go to previous activity
        return super.onSupportNavigateUp()
    }
}