package com.tomykrisgreen.airbasetabbed.CatalogShop

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.tomykrisgreen.airbasetabbed.R
import kotlinx.android.synthetic.main.activity_company_info.*

class CompanyInfoActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageCompanyPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_info)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        storageCompanyPicRef = FirebaseStorage.getInstance().reference.child("Company Pictures")

        save_company_info.setOnClickListener { uploadCompanyInfo() }

        company_cover_catalog.setOnClickListener { //uploadImage()
            CropImage.activity()
                    .setMultiTouchEnabled(true)
                    .start(this@CompanyInfoActivity)
        }

        companyInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            company_cover_catalog.setImageURI(imageUri)
        }
    }


//    private fun updateUserInfoOnly() {
//        when {
//            imageUri == null -> Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
//
//            else -> {
//                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
//
//                val companyMap = HashMap<String, Any>()
//                companyMap["postid"] = companyId!!
//                companyMap["profileimagecatalog"] = myUrl
//                companyMap["companydescription"] = company_description_info.text.toString().toLowerCase()
//                companyMap["companyname"] = company_name_info.text.toString().toLowerCase()
//                companyMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
//                companyMap["catalogimage"] = myUrl
//                companyMap["titlecatalog"] = ""
//                companyMap["descriptioncatalog"] = ""
//                companyMap["productprice"] = ""
//
//                usersRef.child(firebaseUser.uid).updateChildren(companyMap)
//
//                Toast.makeText(this, "Account settings successfully updated", Toast.LENGTH_LONG).show()
//
//                val intent = Intent(this@CompanyInfoActivity, CompanyInfoActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }
//    }

    private fun uploadCompanyInfo()
    {
        when
        {
            imageUri == null -> Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
//            TextUtils.isEmpty(description_post.text.toString()) -> Toast.makeText(this, "Please write a description", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Save Info")
                progressDialog.setMessage("Please wait, saving information...")
                progressDialog.show()

                val fileRef = storageCompanyPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                })
                        .addOnCompleteListener (OnCompleteListener<Uri> { task ->
                            if (task.isSuccessful)
                            {
                                val downloadUrl = task.result
                                myUrl = downloadUrl.toString()

                                val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
                                val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Company Info")

//                            val ref = FirebaseDatabase.getInstance().reference.child("Company Info")
//                            val companyId = ref.push().key

                                val companyMap = HashMap<String, Any>()
                                companyMap["catalogid"] = currentUserID!!
                                companyMap["profileimagecatalog"] = myUrl
                                companyMap["companydescription"] = company_description_info.text.toString()
                                companyMap["companyname"] = company_name_info.text.toString()
                                companyMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
//                            companyMap["catalogimage"] = myUrl
//                            companyMap["titlecatalog"] = ""
//                            companyMap["descriptioncatalog"] = ""
//                            companyMap["productprice"] = ""

                                ref.child(currentUserID).updateChildren(companyMap)

                                Toast.makeText(this, "Information update successful", Toast.LENGTH_LONG).show()

                                val intent = Intent(this@CompanyInfoActivity, CompanyInfoActivity::class.java)
                                startActivity(intent)
                                finish()

                                progressDialog.dismiss()
                            }
                            else
                            {
                                progressDialog.dismiss()
                            }
                        } )
            }
        }
    }

    private fun companyInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Company Info").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<CompanyInfo>(CompanyInfo::class.java)

                    Picasso.get().load(user!!.getProfileimagecatalog()).placeholder(R.drawable.waterfall).into(company_cover_catalog)

                    company_name_info.setText(user!!.getCompanyname())
                    company_description_info.setText(user!!.getCompanydescription())
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}