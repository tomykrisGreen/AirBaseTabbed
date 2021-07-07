package com.tomykrisgreen.airbasetabbed.ZoomImager

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tomykrisgreen.airbasetabbed.R
import kotlinx.android.synthetic.main.activity_zoom_image.*

class ZoomImageActivity : AppCompatActivity() {
    private var postId = ""
    private var postimage = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom_image)

//        val preferences = this?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
//        if (preferences != null) {
//            postId = preferences.getString("postId", "none").toString()
//            postimage = preferences.getString("pImage", "none").toString()
//        }
        

        // Get id of post using intent
        val intent = intent
        postId = intent.getStringExtra("postId")!!


        firebaseUser = FirebaseAuth.getInstance().currentUser


        getPostImage()
    }

    private fun getPostImage() {
        val postRef = FirebaseDatabase.getInstance()
                .reference.child("Posts")
                .child(postId!!).child("pImage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val image = p0.value.toString()

                    try {
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(zoom_image)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.profile).into(zoom_image)
                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}