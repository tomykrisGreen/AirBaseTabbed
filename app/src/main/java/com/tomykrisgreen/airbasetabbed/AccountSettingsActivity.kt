package com.tomykrisgreen.airbasetabbed

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.tomykrisgreen.airbasetabbed.Models.ModelUser
import kotlinx.android.synthetic.main.activity_account_settings.*
import java.util.*
import kotlin.collections.HashMap

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    private var mToolbar: Toolbar? = null

    var progressDialog: ProgressDialog? = null
    var profileOrCoverPhoto: String? = null

    // Permission constants
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    // Image pick constants
    private val IMAGE_PICK_CAMERA_CODE = 300
    private val IMAGE_PICK_GALLERY_CODE = 400

    // Uri of picked image
    private var image_uri: Uri? = null

    //Arrays of permissions to be requested
    lateinit var cameraPermissions: Array<String>
    lateinit var storagePermissions: Array<String>

    var user: FirebaseUser? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null
    var firebaseAuth: FirebaseAuth? = null
    var uid: String? = null

    var storagePath = "Users_Profile_Cover_Imgs/"
    var storageReference: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        // Init Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth!!.currentUser

//        mToolbar = findViewById<View>(R.id.account_settings_toolbar) as Toolbar
//        setSupportActionBar(mToolbar)
//        supportActionBar!!.title = "Account Settings"
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDisplayShowHomeEnabled(true)

        progressDialog = ProgressDialog(this)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference

        // Init arrays of permissions

        // Init arrays of permissions
        cameraPermissions =
                arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)


        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, LoginKotlinActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_text_btn.setOnClickListener {
            checker = "clicked"

            CropImage.activity()
                    .setMultiTouchEnabled(true)
                    //.setAspectRatio(1, 1)
                    .start(this@AccountSettingsActivity)
        }

        profile_image_view_profile_frag.setOnClickListener {
            checker = "clicked"

            showEditProfileDialog()
//            CropImage.activity()
//                .setMultiTouchEnabled(true)
//                //.setAspectRatio(1, 1)
//                .start(this@AccountSettingsActivity)
        }

        cover.setOnClickListener {
            showEditProfileDialog()
        }

        country_flag.setOnClickListener {
            //To Do (Country code picker)
//            CropImage.activity()
//                .setMultiTouchEnabled(true)
//                //.setAspectRatio(1, 1)
//                .start(this@AccountSettingsActivity)
        }

        change_country_flag_btn.setOnClickListener {
            //To Do (Country code picker)
//            CropImage.activity()
//                .setMultiTouchEnabled(true)
//                //.setAspectRatio(1, 1)
//                .start(this@AccountSettingsActivity)
        }

        save_info_profile_btn.setOnClickListener {
//            if (checker == "clicked") {
//                uploadImageAndUpdateInfo()
//            } else {
            updateUserInfoOnly()
//            }
        }


        userInfo()
    }

    private fun showEditProfileDialog() {
        /* Show dialog containing options
        1. Edit Profile Picture
        2. Edit Cover Photo
        3. Edit Name
        4. Edit Phone
        5. Change Password
         */

        // Options to show dialog
        val options = arrayOf(
                "Edit Profile Picture",
                "Edit Cover Photo",
                "Edit Name",
//            "Edit Phone",
                "Change Password"
        )

        // Alert Dialog
        val builder = AlertDialog.Builder(this)
        // Set Title
        builder.setTitle("Choose an action")
        // Set items to dialog
        builder.setItems(options) { dialogInterface, which ->
            // Handle Dialog Items
            if (which == 0) {
                // Edit profile click
                progressDialog?.setMessage("Updating profile picture...")
                profileOrCoverPhoto = "image" // Changing profile picture, make sure to assign same value
                showImagePickDialog()
            } else if (which == 1) {
                // Edit Cover Photo click
                progressDialog?.setMessage("Updating cover photo...")
                profileOrCoverPhoto = "cover" // Changing cover picture, make sure to assign same value
                showImagePickDialog()
            } else if (which == 2) {
                // Edit Name click
                progressDialog?.setMessage("Updating name...")
                // Calling method and passing key 'name' as parameter to update its value in database
                showNamePhoneUpdateDialog("name")
//            } else if (which == 3) {
//                // Edit Phone click
//                progressDialog?.setMessage("Updating phone number...")
//                // Calling method and passing key 'phone' as parameter to update its value in database
//                showNamePhoneUpdateDialog("phone")
            } else if (which == 3) {
                // Edit Phone click
                progressDialog?.setMessage("Changing Password...")
                // Calling method and passing key 'phone' as parameter to update its value in database
                showChangePasswordDialog()
            }
        }
        builder.create().show()
    }

    private fun showImagePickDialog() {
        // Options (Camera, Gallery) to show in dialog
        val options = arrayOf("Camera", "Gallery")

        // Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose image from")
        // Set option to dialog
        builder.setItems(
                options
        ) { dialogInterface, which ->
            // Item click handle
            if (which == 0) {
                // Camera clicked
                // We need to check permission first
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    pickFromCamera()
                }
            }
            if (which == 1) {
                // Gallery clicked
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
            }
        }
        // Create and show dialog
        builder.create().show()
    }

    private fun showNamePhoneUpdateDialog(key: String) {
        /* Parameter 'key' will contain value:
        either "name" which is key in users database which is used to update users name
        or "phone" which is key in users database which is used to update users phone
         */

        // Custom Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update $key") // e.g. Update name or phone
        // Set layout of dialog
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        // Add edit text
        val editText = EditText(this)
        editText.hint = "Enter $key" // e.g. Edit name or Edit phone
        linearLayout.addView(editText)
        builder.setView(linearLayout)

        // Add buttons in dialog
        builder.setPositiveButton(
                "Update"
        ) { dialogInterface, i ->
            // Input text from EditText
            val value = editText.text.toString().trim { it <= ' ' }
            // Validate if user has entered a value or not
            if (!TextUtils.isEmpty(value)) {
                progressDialog!!.show()
                val result = java.util.HashMap<String, Any>()
                result[key] = value

                databaseReference!!.child(user!!.getUid()).updateChildren(result).addOnSuccessListener(
                        OnSuccessListener<Void?> {
                            progressDialog!!.dismiss()
                            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                        }).addOnFailureListener(OnFailureListener { e ->
                    progressDialog!!.dismiss()
                    Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
                })

                // If user edit his name, effect change on his posts too
                if (key == "name") {
                    val ref = FirebaseDatabase.getInstance().getReference("Posts")
                    val query: Query = ref.orderByChild("uid").equalTo(uid)
                    query.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val child = ds.key
                                snapshot.ref.child(child!!).child("uid").setValue(value)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                    // Update name in current users comments on posts
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val child = ds.key
                                if (snapshot.child(child!!).hasChild("Comments")) {
                                    val child1 = "" + snapshot.child(child).key
                                    val child2: Query =
                                            FirebaseDatabase.getInstance().getReference("Posts")
                                                    .child(child1).child("Comments").orderByChild("uid")
                                                    .equalTo(uid)
                                    child2.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (ds in snapshot.children) {
                                                val child = ds.key
                                                snapshot.ref.child(child!!).child("uid")
                                                        .setValue(value)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            } else {
                Toast.makeText(this, "Please enter $key", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(
                "Cancel"
        ) { dialogInterface, i -> }
        builder.create().show()
    }

    private fun showChangePasswordDialog() {
        // Password change dialog with custom layout having current password, new password and update button

        // Inflate layout for dialog
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null)
        val passwordEt = view.findViewById<EditText>(R.id.passwordEt)
        val newPasswordET = view.findViewById<EditText>(R.id.newPasswordET)
        val updatePasswordBtn = view.findViewById<Button>(R.id.updatePasswordBtn)
        val builder = AlertDialog.Builder(this)
        builder.setView(view) // Set view to dialog
        val dialog = builder.create()
        dialog.show()
        updatePasswordBtn.setOnClickListener(View.OnClickListener { // Validate data
            val oldPassword = passwordEt.text.toString().trim { it <= ' ' }
            val newPassword = newPasswordET.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(oldPassword)) {
                Toast.makeText(this, "Enter your current password", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (newPassword.length < 6) {
                Toast.makeText(this, "Password length must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            dialog.dismiss()
            updatePassword(oldPassword, newPassword)
        })
    }

    private fun updatePassword(oldPassword: String, newPassword: String) {
        progressDialog!!.show()

        // Get current user
        val user = firebaseAuth!!.currentUser

        // Before changing password, re-authenticate user
        val authCredential = EmailAuthProvider.getCredential(user!!.email!!, oldPassword)
        user.reauthenticate(authCredential)
                .addOnSuccessListener {
                    // Successfully authenticated, begin update
                    user.updatePassword(newPassword)
                            .addOnSuccessListener { // Password updaged
                                progressDialog!!.dismiss()
                                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { e -> // Failed
                                progressDialog!!.dismiss()
                                Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
                            }
                }.addOnFailureListener { e -> // Authentication failed, show reason
                    progressDialog!!.dismiss()
                    Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    private fun pickFromCamera() {
        // Intent of getting image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        // Put image uri
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        // Intent to start camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun pickFromGallery() {
        // Pick from gallery
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        // Check if storage permission is enabled or not
        // Return true if enable
        // else Return false
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermission() {
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        // Check if camera permission is enabled or not
        // Return true if enable
        // else Return false
        val result = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        val result1 =
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
        return result && result1
    }

    private fun requestCameraPermission() {
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // This method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Image is picked from gallery, get uri of image
                image_uri = data!!.data
                profile_image_view_profile_frag.setImageURI(imageUri)
                cover_profile.setImageURI(imageUri)

                uploadProfileCoverPhoto(image_uri!!)
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Image is picked from camera, get uri of image
                profile_image_view_profile_frag.setImageURI(imageUri)
                cover_profile.setImageURI(imageUri)

                uploadProfileCoverPhoto(image_uri!!)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        // This method is called when user press Allow or Denied from request dialog
        // Handle permission cases
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {

                // Camera usage
                if (grantResults.size > 0) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera()
                    } else {
                        Toast.makeText(
                                this,
                                "Accept camera and storage permission",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {

                // Gallery usage
                if (grantResults.size > 0) {
                    val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(this, "Accept storage permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun uploadProfileCoverPhoto(uri: Uri) {
        progressDialog!!.show()
        // To add check, I'll add a string variable and assign it value "image" when user clicks
        // "Edit Profile Picture", and assign it value"cover" when user clicks "Edit Cover Photo"
        // Here: image is the key in each user containing url of users' profile picture
        // cover is the key in each user containing url of users' cover photo

        /* The parameter "image_uri" contains the uri of image picked from camera or gallery
        We will use UID of the currently signed in user as name of the image so there will be only one image profile
        and one image for cover picture for each user
         */

        // Path and name of image to be stored in firebase storage
        // e.g. Users_Profile_Cover_Imgs/image_e123123.jpg
        // e.g. Users_Profile_Cover_Imgs/cover_e456123.jpg
        val filePathAndName: String = storagePath + "" + profileOrCoverPhoto + "_" + user!!.uid
        val storageReference2nd: StorageReference = storageReference!!.child(filePathAndName)
        storageReference2nd.putFile(uri).addOnSuccessListener { taskSnapshot ->
            // Image is uploaded to storage, now, get its url and store in users database
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val downloadUri = uriTask.result

            // Check if image is uploaded or not
            if (uriTask.isSuccessful) {
                // Image uploaded

                // Add/update url in users' database
                val results = java.util.HashMap<String, Any>()
                results[profileOrCoverPhoto!!] = downloadUri.toString()
                databaseReference!!.child(user!!.uid).updateChildren(results)
                        .addOnSuccessListener { // url in database of user is added successfully
                            // dismiss progressDialog
                            progressDialog!!.dismiss()
                            Toast.makeText(this, "Image updated", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            progressDialog!!.dismiss()
                            Toast.makeText(this, "Error updating image", Toast.LENGTH_SHORT)
                                    .show()
                        }

                // If user edit his name, effect change on his posts too
                if (profileOrCoverPhoto == "image") {
                    val ref = FirebaseDatabase.getInstance().getReference("Posts")
                    val query = ref.orderByChild("uid").equalTo(uid)
                    query.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val child = ds.key
                                snapshot.ref.child(child!!).child("image")
                                        .setValue(downloadUri.toString())
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                    // Update user image in current users comment on posts
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val child = ds.key
                                if (snapshot.child(child!!).hasChild("Comments")) {
                                    val child1 = "" + snapshot.child(child).key
                                    val child2 =
                                            FirebaseDatabase.getInstance().getReference("Posts")
                                                    .child(child1).child("Comments").orderByChild("uid")
                                                    .equalTo(uid)
                                    child2.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (ds in snapshot.children) {
                                                val child = ds.key
                                                snapshot.ref.child(child!!).child("image")
                                                        .setValue(downloadUri.toString())
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            } else {
                // Error occurred
                progressDialog!!.dismiss()
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            progressDialog!!.dismiss()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
//            val result = CropImage.getActivityResult(data)
//            imageUri = result.uri
//            profile_image_view_profile_frag.setImageURI(imageUri)
//        }
//    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(full_name_profile_frag_1.text.toString()) ->
                Toast.makeText(this, "Please, enter your full name", Toast.LENGTH_LONG).show()

            username_profile_frag.text.toString() == "" ->
                Toast.makeText(this, "Please, enter your user name", Toast.LENGTH_LONG).show()

            status_profile_frag_1.text.toString() == "" ->
                Toast.makeText(this, "Please, enter your status", Toast.LENGTH_LONG).show()

            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["name"] = full_name_profile_frag_1.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["username"] = username_profile_frag.text.toString().toLowerCase(Locale.getDefault())    //  .capitalize()
                userMap["status"] = status_profile_frag_1.text.toString().toLowerCase(Locale.getDefault()).capitalize()

                userMap["country"] = country_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["dob"] = dob_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["gender"] = gender_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["relationshipstatus"] = rel_status_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["surname"] = surname_profile_frag_1.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["state"] = state_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["othernames"] = other_names_profile_frag_1.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["designation"] = designation_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["level"] = level_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                userMap["website"] = website_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Account settings successfully updated", Toast.LENGTH_LONG)
                        .show()

                val intent = Intent(this@AccountSettingsActivity, AccountSettingsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun userInfo() {
        val usersRef =
                FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<ModelUser>(ModelUser::class.java)

                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_view_profile_frag)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.ic_image).into(profile_image_view_profile_frag)
                    }


                    try {
                        Picasso.get().load(user!!.getCover()).placeholder(R.drawable.ic_image).into(
                                cover_profile
                        )
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.ic_image).into(cover_profile)
                    }

                    username_profile_frag.setText(user!!.getUsername())
                    full_name_profile_frag_1.setText(user!!.getName())
                    status_profile_frag_1.setText(user!!.getStatus())
                    website_profile_frag.setText(user!!.getWebsite())

                    country_profile_frag.setText(user!!.getCountry())
                    dob_profile_frag.setText(user!!.getDob())
                    gender_profile_frag.setText(user!!.getGender())
                    rel_status_profile_frag.setText(user!!.getRelationshipstatus())
                    surname_profile_frag_1.setText(user!!.getSurname())
                    state_profile_frag.setText(user!!.getState())
                    other_names_profile_frag_1.setText(user!!.getOthernames())
                    designation_profile_frag.setText(user!!.getDesignation())
                    level_profile_frag.setText(user!!.getLevel())
                    website_profile_frag.setText(user!!.getWebsite())

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun uploadImageAndUpdateInfo() {
        when {
//            imageUri == null -> Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(full_name_profile_frag_1.text.toString()) ->
                Toast.makeText(this, "Please write your full name", Toast.LENGTH_LONG).show()

            username_profile_frag.text.toString() == "" ->
                Toast.makeText(this, "Please write your user name", Toast.LENGTH_LONG).show()

            status_profile_frag_1.text.toString() == "" ->
                Toast.makeText(this, "Please write your bio", Toast.LENGTH_LONG).show()


            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["name"] = full_name_profile_frag_1.text.toString()//.toLowerCase() //.toLowerCase(Locale.getDefault())
                        userMap["username"] = username_profile_frag.text.toString().toLowerCase(Locale.getDefault())   //  .capitalize()
                        userMap["status"] = status_profile_frag_1.text.toString().toLowerCase(Locale.getDefault())
                                .capitalize()
                        userMap["image"] = myUrl

                        userMap["cover"] = myUrl
                        userMap["country"] = country_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["dob"] = dob_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["gender"] = gender_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["relationshipstatus"] = rel_status_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["surname"] = surname_profile_frag_1.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["state"] = state_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["othernames"] = other_names_profile_frag_1.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["designation"] = designation_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["level"] = level_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()
                        userMap["website"] = website_profile_frag.text.toString().toLowerCase(Locale.getDefault()).capitalize()

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Account Information has been updated successfully.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }

    }
}