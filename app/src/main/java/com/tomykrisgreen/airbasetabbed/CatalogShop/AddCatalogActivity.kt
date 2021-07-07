package com.tomykrisgreen.airbasetabbed.CatalogShop

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.tomykrisgreen.airbasetabbed.MainActivity
import com.tomykrisgreen.airbasetabbed.MorphButton.MorphButton
import com.tomykrisgreen.airbasetabbed.MorphButton.getColorX
import com.tomykrisgreen.airbasetabbed.R
import kotlinx.android.synthetic.main.activity_add_catalog.*
import kotlinx.coroutines.delay

class AddCatalogActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageCatalogRef: StorageReference? = null

    private lateinit var binding: AddCatalogActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_catalog)


        storageCatalogRef = FirebaseStorage.getInstance().reference.child("Catalog Products")  //Images

        add_post_catalog.setOnClickListener { uploadCatalog() }



        // Enable image cropping from the layout if image not selected
        add_catalog_image.setOnClickListener { //uploadImage()
            CropImage.activity()
                    .setMultiTouchEnabled(true)
                    .start(this@AddCatalogActivity)}

//        CropImage.activity()
//            .setMultiTouchEnabled(true)
//            .start(this@AddPostActivity)
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            add_catalog_image.setImageURI(imageUri)
        }
    }


    private fun uploadCatalog()
    {
        when
        {
            imageUri == null -> Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
//            TextUtils.isEmpty(description_post.text.toString()) -> Toast.makeText(this, "Please write a description", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Add New Catalog")
                progressDialog.setMessage("Please wait, updating your catalog...")
                progressDialog.show()

                val fileRef = storageCatalogRef!!.child(System.currentTimeMillis().toString() + ".jpg")

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

                                val ref = FirebaseDatabase.getInstance().reference.child("Catalog")
                                val catalogId = ref.push().key

                                val mapCatalog = HashMap<String, Any>()
                                mapCatalog["catalogid"] = catalogId!!
                                mapCatalog["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                                mapCatalog["catalogimage"] = myUrl
                                mapCatalog["titlecatalog"] = add_product_title.text.toString().toLowerCase()
                                mapCatalog["descriptioncatalog"] = add_product_description.text.toString().toLowerCase()
                                mapCatalog["productprice"] = add_product_price.text.toString().toLowerCase()
//                            mapCatalog["profileimagecatalog"] = myUrl
//                            mapCatalog["companydescription"] = ""
//                            mapCatalog["companyname"] = ""

                                ref.child(catalogId).updateChildren(mapCatalog)

                                Toast.makeText(this, "Catalog update successful", Toast.LENGTH_LONG).show()

                                val intent = Intent(this@AddCatalogActivity, MainActivity::class.java)
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
}