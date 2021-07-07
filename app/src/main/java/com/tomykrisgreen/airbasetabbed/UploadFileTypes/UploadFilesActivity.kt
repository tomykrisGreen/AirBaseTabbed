package com.tomykrisgreen.airbasetabbed.UploadFileTypes

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.tomykrisgreen.airbasetabbed.R
import java.util.*

class UploadFilesActivity : AppCompatActivity() {
    val PDF : Int = 0
    val DOCX : Int = 1
    val AUDIO : Int = 2
    val VIDEO : Int = 3
    lateinit var uri : Uri
    lateinit var mStorage : StorageReference

    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_files)

        val pdfBtn = findViewById<View>(R.id.pdfBtn) as Button
        val docxBtn = findViewById<View>(R.id.docxBtn) as Button
        val musicBtn = findViewById<View>(R.id.musicBtn) as Button
        val videoBtn = findViewById<View>(R.id.videoBtn) as Button

        val uploadFilesBtn = findViewById<View>(R.id.uploadFilesBtn) as Button

        progressDialog = ProgressDialog(this)

        mStorage = FirebaseStorage.getInstance().getReference("Uploads")

        pdfBtn.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent()
            intent.setType("pdf/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF)
        })

        docxBtn.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent()
            intent.setType("docx/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select DOCX"), DOCX)
        })

        musicBtn.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent()
            intent.setType("audio/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), AUDIO)
        })

        videoBtn.setOnClickListener(View.OnClickListener { view: View? ->
            val intent = Intent()
            intent.setType("video/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO)
        })

        uploadFilesBtn.setOnClickListener {
            uploadFiles()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uriTxt = findViewById<View>(R.id.uriTxt) as TextView
        if (resultCode == RESULT_OK) {
            if (requestCode == PDF) {
                uri = data!!.data!!
                uriTxt.text = uri.toString()
//                upload ()
            }else if (requestCode == DOCX) {
                uri = data!!.data!!
                uriTxt.text = uri.toString()
//                upload ()
            }else if (requestCode == AUDIO) {
                uri = data!!.data!!
                uriTxt.text = uri.toString()
//                upload ()
            }else if (requestCode == VIDEO) {
                uri = data!!.data!!
                uriTxt.text = uri.toString()
//                upload ()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadFiles() {
        progressDialog!!.setTitle("Add New Posts Files")
        progressDialog!!.setMessage("Publishing posts files ...")
        progressDialog!!.show()

        // For post-image name, post-id, post-publish-time
        val timeStamp = System.currentTimeMillis().toString()

        val filePathAndName = "Posts Files/post_$timeStamp"


        // Post with image
        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(uri).addOnSuccessListener { taskSnapshot ->
            // Image uploaded to firebase storage, now, get its uri
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val downloadUri = uriTask.result.toString()
            if (uriTask.isSuccessful) {
                // url received, upload post to firebase database

                val hashMap = HashMap<Any, String>()
                // Put post info
                hashMap["pId"] = timeStamp
                hashMap["pImage"] = downloadUri
                hashMap["pTime"] = timeStamp


                // Path to store post data
                val ref = FirebaseDatabase.getInstance().getReference("Posts Files")
                // Put data in this ref
                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener {
                    progressDialog!!.dismiss()
                    Toast.makeText(this@UploadFilesActivity, "Post published", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    progressDialog!!.dismiss()
                    Toast.makeText(this@UploadFilesActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun upload() {
        var mReference = uri.lastPathSegment?.let { mStorage.child(it) }
        try {
            mReference!!.putFile(uri).addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? -> var url = taskSnapshot!!.storage.downloadUrl
                val dwnTxt = findViewById<View>(R.id.dwnTxt) as TextView
                dwnTxt.text = url.toString()
                Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()
            }
        }catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

    }

}
