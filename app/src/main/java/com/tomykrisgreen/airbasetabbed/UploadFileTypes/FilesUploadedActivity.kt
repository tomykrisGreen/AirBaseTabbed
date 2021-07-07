package com.tomykrisgreen.airbasetabbed.UploadFileTypes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost
import com.tomykrisgreen.airbasetabbed.Models.ModelPost
import com.tomykrisgreen.airbasetabbed.R
import java.util.ArrayList

class FilesUploadedActivity : AppCompatActivity() {
    var firebaseAuth: FirebaseAuth? = null

    var recyclerView: RecyclerView? = null
    var uploadedPostsList: List<ModelUploadedFiles>? = null
    var adapterUploadedPosts: AdapterUploadedPosts? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files_uploaded)

        // RecyclerView and its properties
        recyclerView = findViewById(R.id.uploadedRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        // Show newest post first
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        // Set layout to recyclerView
        recyclerView!!.layoutManager = layoutManager

        // Init post list
        uploadedPostsList = ArrayList()
        adapterUploadedPosts = this?.let { AdapterUploadedPosts(it, uploadedPostsList as ArrayList<ModelUploadedFiles>) }
        recyclerView!!.adapter = adapterUploadedPosts


        loadPosts()
    }

    private fun loadPosts() {
        // Path of all post
        val ref = FirebaseDatabase.getInstance().getReference("Posts Files")
        // Get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (uploadedPostsList as ArrayList<ModelUploadedFiles>).clear()

                for (ds in snapshot.children) {
                    val modelPost = ds.getValue(ModelUploadedFiles::class.java)

                    (uploadedPostsList as ArrayList<ModelUploadedFiles>).add(modelPost!!)

                    // Adapter
                    adapterUploadedPosts = AdapterUploadedPosts(this@FilesUploadedActivity, uploadedPostsList)
                    // Set adapter to recycler view
//                    recyclerView?.adapter = adapterPost
                    try {
                        recyclerView?.adapter = adapterUploadedPosts
                    }
                    catch (e: Exception){

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // In case of error
                Toast.makeText(this@FilesUploadedActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}