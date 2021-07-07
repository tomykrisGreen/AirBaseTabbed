package com.tomykrisgreen.airbasetabbed.Fragments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.tomykrisgreen.airbasetabbed.*
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost
import com.tomykrisgreen.airbasetabbed.Adapters.MyCatalogAdapter
import com.tomykrisgreen.airbasetabbed.CatalogShop.AddCatalogActivity
import com.tomykrisgreen.airbasetabbed.Models.Catalog
import com.tomykrisgreen.airbasetabbed.Models.ModelPost
import com.tomykrisgreen.airbasetabbed.Models.ModelUser
import com.tomykrisgreen.airbasetabbed.R
import com.tomykrisgreen.airbasetabbed.Story.ShowUsersActivity
import com.tomykrisgreen.airbasetabbed.UploadFileTypes.FilesUploadedActivity
import com.tomykrisgreen.airbasetabbed.UploadFileTypes.UploadFilesActivity
import com.tomykrisgreen.airbasetabbed.VideoUpload.AddVideoActivity
import kotlinx.android.synthetic.main.fragment_profile_kotlin.*
import kotlinx.android.synthetic.main.fragment_profile_kotlin.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ProfileKotlinFragment : Fragment() {
    //Firebase
    var firebaseAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    //Storage
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    // Path to store profile and cover pictures of users
    var storagePath = "Users_Profile_Cover_Imgs/"

    //Views from Xml
    var avatarIv: ImageView? = null
    var coverIv: ImageView? = null
    var nameTV: TextView? = null
    var usernameTv: TextView? = null
    var countryTv: TextView? = null
    var stateTv: TextView? = null
    var statusTv: TextView? = null
    var weblinkTv: TextView? = null
    var resume_view: TextView? = null

    var imageSaveBtn: Button? = null

    //    var emailTV: TextView? = null
//    var phoneTV: TextView? = null
    var videoViewPro: TextView? = null
    var options_view: ImageView? = null
    var postsRecyclerView: RecyclerView? = null

    var progressDialog: ProgressDialog? = null

    var progressBarProfileImage: ProgressBar? = null

    // Uri of picked image
    var image_uri: Uri? = null

    // For checking profile or cover picture
    var profileOrCoverPhoto: String? = null

    // Permission constants
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200
    private val IMAGE_PICK_GALLERY_CODE = 300
    private val IMAGE_PICK_CAMERA_CODE = 400

    //Arrays of permissions to be requested
    lateinit var cameraPermissions: Array<String>
    lateinit var storagePermissions: Array<String>

    var postList: List<ModelPost>? = null
    var adapterPost: AdapterPost? = null
    var uid: String? = null

    var followerCounter = 0
    var followingCounter = 0


    var catalogList: List<Catalog>? = null
    var myCatalogAdapter: MyCatalogAdapter? = null
    lateinit var likeButton: ImageView


    lateinit var uploadedImagesBtn: Button
    lateinit var savedImagesBtn: Button
    lateinit var catalogImagesBtn: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_kotlin, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

//        // Get id of post using intent
//        val intent: Intent = intent
//        uid = intent.getStringExtra("uid")
//        val intent = getIntent("uid")
//        uid = intent.getStringExtra("profileId")

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }


        if (profileId != firebaseUser.uid) {
            view.edit_account_settings_btn.text = "Edit Profile"
        } else if (profileId == firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }


        setHasOptionsMenu(true)

        // Init Firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference

        // Init arrays of permissions
        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // Init views
        avatarIv = view.findViewById(R.id.avatarIv_Profile)
        coverIv = view.findViewById(R.id.coverIv)
        nameTV = view.findViewById(R.id.nameTV)
        usernameTv = view.findViewById(R.id.profile_fragment_username)
        countryTv = view.findViewById(R.id.profile_fragment_country)
        stateTv = view.findViewById(R.id.profile_fragment_state)
        statusTv = view.findViewById(R.id.profile_fragment_status)
        weblinkTv = view.findViewById(R.id.profile_fragment_web_link)

        resume_view = view.findViewById(R.id.resume_view)

//        emailTV = view.findViewById(R.id.emailTV)
//        phoneTV = view.findViewById(R.id.phoneTV)
        options_view = view.findViewById(R.id.options_view)
//        videoViewPro = view.findViewById(R.id.videoViewPro)
        postsRecyclerView = view.findViewById(R.id.recycler_view_upload_pic)

        progressDialog = ProgressDialog(activity)

        progressBarProfileImage = view.findViewById(R.id.progressBar_profile_image)



        uploadedImagesBtn = view.findViewById(R.id.images_grid_view_btn)
        savedImagesBtn = view.findViewById(R.id.images_save_btn)
        catalogImagesBtn = view.findViewById(R.id.catalog_btn)



        //Recycler View for Catalog images
        var recyclerViewCatalogImages: RecyclerView
        recyclerViewCatalogImages = view.findViewById(R.id.recycler_view_catalog_products)
        recyclerViewCatalogImages.setHasFixedSize(true)
        val linearLayoutManager3: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewCatalogImages.layoutManager = linearLayoutManager3

        catalogList = ArrayList()
        myCatalogAdapter = context?.let { MyCatalogAdapter(it, catalogList as ArrayList<Catalog>) }
        recyclerViewCatalogImages.adapter = myCatalogAdapter


        //Recycler View for saved images
        var recyclerViewSavedImages: RecyclerView
        recyclerViewSavedImages = view.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImages.setHasFixedSize(true)



        recyclerViewSavedImages.visibility = View.GONE
        postsRecyclerView!!.visibility = View.VISIBLE
        recyclerViewCatalogImages.visibility = View.GONE



        val catalogImagesBtn: Button
        catalogImagesBtn = view.findViewById(R.id.catalog_btn)
        catalogImagesBtn.setOnClickListener {
//            recyclerViewSavedImages.visibility = View.GONE
            postsRecyclerView!!.visibility = View.GONE

            recyclerViewCatalogImages.visibility = View.VISIBLE

            uploadedImagesBtn.setBackgroundColor(Color.WHITE)
            savedImagesBtn.setBackgroundColor(Color.WHITE)
//            catalogImagesBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.drawable.color_selector))
            catalogImagesBtn.setBackgroundResource(R.drawable.color_selector)
        }

        uploadedImagesBtn.setOnClickListener {
//            recyclerViewSavedImages.visibility = View.GONE
            postsRecyclerView!!.visibility = View.VISIBLE

            recyclerViewCatalogImages.visibility = View.GONE

            uploadedImagesBtn.setBackgroundColor(Color.WHITE)
            catalogImagesBtn.setBackgroundColor(Color.WHITE)
//            catalogImagesBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.drawable.color_selector))
            uploadedImagesBtn.setBackgroundResource(R.drawable.color_selector)
        }


        // Getting info of currently signed in user
        val query = databaseReference!!.orderByChild("email").equalTo(user!!.getEmail())
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check until required data is found
                for (ds in dataSnapshot.children) {
                    // Get Data
                    val name = "" + ds.child("name").value
                    val email = "" + ds.child("email").value
                    val phone = "" + ds.child("phone").value
                    val image = "" + ds.child("image").value
                    val cover = "" + ds.child("cover").value
//                    val country = "" + ds.child("country").value
//                    val state = "" + ds.child("state").value
//                    val website = "" + ds.child("website").value
//                    val status = "" + ds.child("status").value

                    // Set Data
                    nameTV!!.text = name
//                    countryTv!!.text = country
//                    stateTv!!.text = state
//                    weblinkTv!!.text = website
//                    statusTv!!.text = status
//                    emailTV.setText(email)
//                    phoneTV.setText(phone)
                    try {
                        Picasso.get().load(image).into(avatarIv)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarIv)
                    }
                    try {
                        Picasso.get().load(cover).into(coverIv)
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        resume_view?.setOnClickListener {
            startActivity(Intent(activity, UploadFilesActivity::class.java))
        }

//        savedImagesBtn?.setOnClickListener {
//            startActivity(Intent(activity, FilesUploadedActivity::class.java))
//        }

        // FAB button click
        options_view?.setOnClickListener(View.OnClickListener { showEditProfileDialog() })

        videoViewPro?.setOnClickListener(View.OnClickListener { startActivity(Intent(activity, AddVideoActivity::class.java)) })


        //total_followers
        view.no_of_followers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        //total_following
        view.no_of_following.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        //no_of_posts // not so necessary because post grid is available on profile
        view.no_of_posts.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "posts")
            startActivity(intent)
        }

        // Profile image on Profile clicks only if equal to user
        if (profileId == firebaseUser.uid) {
            view.avatarIv_Profile.setOnClickListener {
                startActivity(Intent(context, AccountSettingsActivity::class.java))
            }

        }

        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity(
                        Intent(
                                context,
                                AccountSettingsActivity::class.java
                        )
                )

                getButtonText == "Follow" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(profileId)
                                .setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1.toString())
                                .setValue(true)
                    }

                    addNotification()
                }

                getButtonText == "Following" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(profileId)
                                .removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1.toString())
                                .removeValue()
                    }
                }
            }

        }

//        view.resume_view.setOnClickListener {
//            val intent = Intent(context, ResumeViewActivity::class.java)
//            intent.putExtra("id", profileId)
//            intent.putExtra("title", "Resume")
//            startActivity(intent)
//        }

//        view.resume_btn.setOnClickListener {
//            val intent = Intent(context, ResumeViewActivity::class.java)
//            intent.putExtra("id", profileId)
//            intent.putExtra("title", "Resume")
//            startActivity(intent)
//        }


        postList = ArrayList()

        getFollowings()
        getFollowers()
        getTotalNumberOfPosts()

        checkUserStatus()

        userInfo()

        loadMyPosts()

        myCatalog()

        return view;
    }

    private fun getFollowers2() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
//                    var followerCounter = 0

                    followerCounter++

                    total_followers.text = " " + followerCounter

                }
//                else{
//                    followerCounter--
//                    total_followers.text = " " + followerCounter
//                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowings2() {
        val followingsRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(firebaseUser.uid)   // profileId replaced with firebaseUser.uid
                .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
//                    var followingCounter = 0

                    followingCounter++

                    total_following.text = " " + followingCounter

                }
//                else{
//                    followingCounter--
//                    total_following.text = " " + followingCounter
//                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun myCatalog() {
        val catalogRef = FirebaseDatabase.getInstance().reference.child("Catalog")

        catalogRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (catalogList as ArrayList<Catalog>).clear()

                    for (snapshot in p0.children) {
                        val catalog = snapshot.getValue(Catalog::class.java)!!
                        if (catalog.getPublisher().equals(uid)) {
                            (catalogList as ArrayList<Catalog>).add(catalog)
                        }
                        Collections.reverse(catalogList)
                        myCatalogAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowings() {
        val followingsRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(firebaseUser.uid)   // profileId replaced with firebaseUser.uid
                .child("Following")


        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.total_following?.text = p0.childrenCount.toString()
//                    view?.total_following?.invalidate()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.total_followers?.text = p0.childrenCount.toString()
//                    view?.total_followers?.invalidate()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getTotalNumberOfPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var postCounter = 0

                    for (snapShot in dataSnapshot.children) {
                        val post = snapShot.getValue(ModelPost::class.java)!!
                        if (post.getUid() == firebaseUser.uid) {
                            postCounter++
                        }
                    }
                    try {
                        total_posts.text = " " + postCounter
                    } catch (e: Exception) {

                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                .child(firebaseUser.uid) //profileId replace with firebaseUser!!.uid

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<ModelUser>(ModelUser::class.java)

                    try {
                        progressBarProfileImage!!.visibility = View.VISIBLE
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                                .into(
                                        view?.avatarIv_Profile
                                )

                        progressBarProfileImage!!.visibility = View.GONE
                    } catch (e: Exception) {
                        progressBarProfileImage!!.visibility = View.GONE
//                        Picasso.get().load(R.drawable.profile).into(view?.pro_image_profile_frag)
                    }

//                    Picasso.get().load(user!!.getProfileimage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)

//                    view?.profile_fragment_username?.text = "@" + user!!.getUsername()
                    view?.nameTV?.text = user!!.getName()
//                    view?.profile_fragment_status?.text = user!!.getStatus()
//                    view?.profile_fragment_state?.text = user!!.getState()
//                    view?.profile_fragment_country?.text = user!!.getCountry()

                    if (user.getStatus() != ("")) {
                        try {
                            view?.profile_fragment_status?.text = user!!.getStatus()
                            view?.profile_fragment_status?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
//                        view?.profile_fragment_status?.text = user!!.getStatus()
//                        view?.profile_fragment_status?.visibility = View.VISIBLE
                    }

                    if (user.getUsername() != ("")) {
                        try {
                            view?.profile_fragment_username?.text = "@" + user!!.getUsername()
                            view?.profile_fragment_username?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
//                        view?.profile_fragment_username?.text = "@" + user!!.getUsername()
//                        view?.profile_fragment_username?.visibility = View.VISIBLE
                    }

                    if (user.getState() != ("") && user.getCountry() != ("") ||
                            user.getState() != ("") && user.getCountry() == ("") ||
                            user.getState() == ("") && user.getCountry() != ("")
                    ) {
                        try {
                            view?.image_location!!.visibility = View.VISIBLE
                            view?.profile_fragment_state?.text = user!!.getState() //+ ", "
                            view?.profile_fragment_country?.text = user!!.getCountry()
                            view?.profile_fragment_country?.visibility = View.VISIBLE
                            view?.profile_fragment_state?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }

//                        view?.image_location!!.visibility = View.VISIBLE
//                        view?.profile_fragment_state?.text = user!!.getState() //+ ", "
//                        view?.profile_fragment_country?.text = user!!.getCountry()
//                        view?.profile_fragment_country?.visibility = View.VISIBLE
//                        view?.profile_fragment_state?.visibility = View.VISIBLE
                    } else {
                        try {
                            view?.image_location!!.visibility = View.GONE
                            view?.profile_fragment_state!!.visibility = View.GONE
                            view?.profile_fragment_country!!.visibility = View.GONE
                        } catch (e: Exception) {
                        }

//                        view?.image_location!!.visibility = View.GONE
//                        view?.profile_fragment_state!!.visibility = View.GONE
//                        view?.profile_fragment_country!!.visibility = View.GONE
                    }

                    if (user.getState() != ("") && user.getCountry() != ("")) {
                        try {
                            view?.profile_comma?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
//                        view?.profile_comma?.visibility = View.VISIBLE
                    }


                    if (user.getWebsite() != ("")) {
                        try {
                            view?.image_weblink!!.visibility = View.VISIBLE
                            view?.profile_fragment_web_link!!.visibility = View.VISIBLE
                            view?.profile_fragment_web_link!!.text = user.getWebsite()
                        } catch (e: Exception) {
                        }
//                        view?.image_weblink!!.visibility = View.VISIBLE
//                        view?.profile_fragment_web_link!!.visibility = View.VISIBLE
//                        view?.profile_fragment_web_link!!.text = user.getWebsite()
                    } else {
                        try {
                            view?.image_weblink!!.visibility = View.GONE
                            view?.profile_fragment_web_link!!.visibility = View.GONE
                        } catch (e: Exception) {
                        }
//                        view?.image_weblink!!.visibility = View.GONE
//                        view?.profile_fragment_web_link!!.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun addNotification() {
        val notiRef = FirebaseDatabase.getInstance().reference
                .child("Notifications")
                .child(profileId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it1.toString())
                    .child("Following")
        }

        if (followingRef == null) {                     //  (followingRef != null)
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists()) {
                        view?.edit_account_settings_btn?.text = "Following"
                    } else {
                        view?.edit_account_settings_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
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
        val options = arrayOf("Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone", "Change Password")
        // Alert Dialog
        val builder = AlertDialog.Builder(requireActivity())
        // Set Title
        builder.setTitle("Choose an action")
        // Set items to dialog
        builder.setItems(options) { dialogInterface, which ->
            // Handle Dialog Items
            if (which == 0) {
                // Edit profile click
                progressDialog!!.setMessage("Updating profile picture...")
                profileOrCoverPhoto = "image" // Changing profile picture, make sure to assign same value
                showImagePicDialog()
            } else if (which == 1) {
                // Edit Cover Photo click
                progressDialog!!.setMessage("Updating cover photo...")
                profileOrCoverPhoto = "cover" // Changing cover picture, make sure to assign same value
                showImagePicDialog()
            } else if (which == 2) {
                // Edit Name click
                progressDialog!!.setMessage("Updating name...")
                // Calling method and passing key 'name' as parameter to update its value in database
                showNamePhoneUpdateDialog("name")
            } else if (which == 3) {
                // Edit Phone click
                progressDialog!!.setMessage("Updating phone number...")
                // Calling method and passing key 'phone' as parameter to update its value in database
                showNamePhoneUpdateDialog("phone")
            } else if (which == 4) {
                // Edit Phone click
                progressDialog!!.setMessage("Changing Password...")
                // Calling method and passing key 'phone' as parameter to update its value in database
                showChangePasswordDialog()
            }
        }
        builder.create().show()
    }

    private fun loadMyPosts() {
        // LinearLayout for recyclerView
        val layoutManager = LinearLayoutManager(activity)
        // Show newest post first
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        // Set this layout to recyclerView
        postsRecyclerView!!.layoutManager = layoutManager

        // Init post list
        val ref = FirebaseDatabase.getInstance().getReference("Posts")
        // Query to load posts
        /* Whenever user publishes a post, the uid of the user is also saved as info of post
        So we are retrieving posts having uid equal to uid of current user
         */
        val query = ref.orderByChild("uid").equalTo(uid)
        // Get all data from this ref
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (postList as ArrayList<ModelPost>).clear()
//                postList.clear()
                for (ds in snapshot.children) {
                    val myPosts = ds.getValue(ModelPost::class.java)

                    // Add to list
                    (postList as ArrayList<ModelPost>).add(myPosts!!)
//                    postList.add(myPosts)

                    // Adapter
                    adapterPost = AdapterPost(activity, postList)
                    // Set adapter to recyclerView
                    postsRecyclerView!!.adapter = adapterPost
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchMyPosts(searchQuery: String) {
        // LinearLayout for recyclerView
        val layoutManager = LinearLayoutManager(activity)
        // Show newest post first
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        // Set this layout to recyclerView
        postsRecyclerView!!.layoutManager = layoutManager

        // Init post list
        val ref = FirebaseDatabase.getInstance().getReference("Posts")
        // Query to load posts
        /* Whenever user publishes a post, the uid of the user is also saved as info of post
        So we are retrieving posts having uid equal to uid of current user
         */
        val query = ref.orderByChild("uid").equalTo(uid)
        // Get all data from this ref
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (postList as ArrayList<ModelPost>).clear()
//                postList.clear()
                for (ds in snapshot.children) {
                    val myPosts = ds.getValue(ModelPost::class.java)
                    if (myPosts!!.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        // Add to list
                        (postList as ArrayList<ModelPost>).add(myPosts)
//                        postList.add(myPosts)
                    }

                    // Adapter
                    adapterPost = AdapterPost(activity, postList)
                    // Set adapter to recyclerView
                    postsRecyclerView!!.adapter = adapterPost
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkStoragePermission(): Boolean {
        // Check if storage permission is enabled or not
        // Return true if enable
        // else Return false
        return (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermission() {
        // Request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        // Check if camera permission is enabled or not
        // Return true if enable
        // else Return false
        val result = (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        val result1 = (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
        return result && result1
    }

    private fun requestCameraPermission() {
        // Request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE)
    }


    private fun showChangePasswordDialog() {
        // Password change dialog with custom layout having current password, new password and update button

        // Inflate layout for dialog
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_update_password, null)
        val passwordEt = view.findViewById<EditText>(R.id.passwordEt)
        val newPasswordET = view.findViewById<EditText>(R.id.newPasswordET)
        val updatePasswordBtn = view.findViewById<Button>(R.id.updatePasswordBtn)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view) // Set view to dialog
        val dialog = builder.create()
        dialog.show()
        updatePasswordBtn.setOnClickListener(View.OnClickListener { // Validate data
            val oldPassword = passwordEt.text.toString().trim { it <= ' ' }
            val newPassword = newPasswordET.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(oldPassword)) {
                Toast.makeText(activity, "Enter your current password", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (newPassword.length < 6) {
                Toast.makeText(activity, "Password length must be at least 6 characters", Toast.LENGTH_SHORT).show()
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
        val authCredential = EmailAuthProvider.getCredential(user.email, oldPassword)
        user.reauthenticate(authCredential)
                .addOnSuccessListener {
                    // Successfully authenticated, begin update
                    user.updatePassword(newPassword)
                            .addOnSuccessListener { // Password updaged
                                progressDialog!!.dismiss()
                                Toast.makeText(activity, "Password updated", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { e -> // Failed
                                progressDialog!!.dismiss()
                                Toast.makeText(activity, "" + e.message, Toast.LENGTH_SHORT).show()
                            }
                }.addOnFailureListener { e -> // Authentication failed, show reason
                    progressDialog!!.dismiss()
                    Toast.makeText(activity, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    private fun showNamePhoneUpdateDialog(key: String) {
        /* Parameter 'key' will contain value:
        either "name" which is key in users database which is used to update users name
        or "phone" which is key in users database which is used to update users phone
         */

        // Custom Dialog
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Update $key") // e.g. Update name or phone
        // Set layout of dialog
        val linearLayout = LinearLayout(activity)
        linearLayout.orientation = LinearLayout.VERTICAL
        // Add edit text
        val editText = EditText(activity)
        editText.hint = "Enter $key" // e.g. Edit name or Edit phone
        linearLayout.addView(editText)
        builder.setView(linearLayout)

        // Add buttons in dialog
        builder.setPositiveButton("Update") { dialogInterface, i ->
            // Input text from EditText
            val value = editText.text.toString().trim { it <= ' ' }
            // Validate if user has entered a value or not
            if (!TextUtils.isEmpty(value)) {
                progressDialog!!.show()
                val result = HashMap<String, Any>()
                result[key] = value
                databaseReference!!.child(user!!.uid).updateChildren(result).addOnSuccessListener {
                    progressDialog!!.dismiss()
                    Toast.makeText(activity, "Updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    progressDialog!!.dismiss()
                    Toast.makeText(activity, "" + e.message, Toast.LENGTH_SHORT).show()
                }

                // If user edit his name, effect change on his posts too
                if (key == "name") {
                    val ref = FirebaseDatabase.getInstance().getReference("Posts")
                    val query = ref.orderByChild("uid").equalTo(uid)
                    query.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val child = ds.key
                                snapshot.ref.child(child!!).child("uName").setValue(value)
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
                                    val child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid)
                                    child2.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (ds in snapshot.children) {
                                                val child = ds.key
                                                snapshot.ref.child(child!!).child("uName").setValue(value)
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
                Toast.makeText(activity, "Please enter $key", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i -> }
        builder.create().show()
    }

    private fun showImagePicDialog() {

        // Options to show dialog
        val options = arrayOf("Camera", "Gallery")
        // Alert Dialog
        val builder = AlertDialog.Builder(requireActivity())
        // Set Title
        builder.setTitle("Pick Image from")
        // Set items to dialog
        builder.setItems(options) { dialogInterface, which ->
            // Handle Dialog Items
            if (which == 0) {
                // Camera click
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    pickFromCamera()
                }
            } else if (which == 1) {
                // Gallery click
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
            }
        }
        builder.create().show()
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
                        Toast.makeText(activity, "Accept camera and storage permission", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(activity, "Accept storage permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // This method will be called after picking image from camera or gallery
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Image is picked from gallery, get uri of image
                image_uri = data!!.data
                uploadProfileCoverPhoto(image_uri!!)
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Image is picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri!!)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
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
        val filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user!!.uid
        val storageReference2nd = storageReference!!.child(filePathAndName)
        storageReference2nd.putFile(uri).addOnSuccessListener { taskSnapshot ->
            // Image is uploaded to storage, now, get its url and store in users database
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val downloadUri = uriTask.result

            // Check if image is uploaded or not
            if (uriTask.isSuccessful) {
                // Image uploaded

                // Add/update url in users' database
                val results = HashMap<String, Any>()
                results[profileOrCoverPhoto!!] = downloadUri.toString()
                databaseReference!!.child(user!!.uid).updateChildren(results).addOnSuccessListener { // url in database of user is added successfully
                    // dismiss progressDialog
                    progressDialog!!.dismiss()
                    Toast.makeText(activity, "Image updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    progressDialog!!.dismiss()
                    Toast.makeText(activity, "Error updating image", Toast.LENGTH_SHORT).show()
                }

                // If user edit his name, effect change on his posts too
                if (profileOrCoverPhoto == "image") {
                    val ref = FirebaseDatabase.getInstance().getReference("Posts")
                    val query = ref.orderByChild("uid").equalTo(uid)
                    query.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val child = ds.key
                                snapshot.ref.child(child!!).child("uDp").setValue(downloadUri.toString())
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
                                    val child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid)
                                    child2.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (ds in snapshot.children) {
                                                val child = ds.key
                                                snapshot.ref.child(child!!).child("uDp").setValue(downloadUri.toString())
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
                Toast.makeText(activity, "Error occurred", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            progressDialog!!.dismiss()
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickFromCamera() {
        // Intent of getting image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        // Put image uri
        image_uri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

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


    private fun checkUserStatus() {
        // Get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            // User is signed in
            // Set email of logged in user
            //mProfileTV.setText(user.getEmail());
            uid = user.uid
        } else {
            // User not signed in, go to Main Activity
            startActivity(Intent(activity, MainActivity::class.java))
            requireActivity().finish()
        }
    }

    // Inflate options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflating menu
        inflater.inflate(R.menu.menu_main, menu)

        // Hide some options
        menu.findItem(R.id.action_create_group).isVisible = false
        menu.findItem(R.id.action_add_participant).isVisible = false
        menu.findItem(R.id.action_group_info).isVisible = false
        val item = menu.findItem(R.id.action_search)
        // SearchView to search user specific posts
        val searchView = MenuItemCompat.getActionView(item) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                // Called when user presses the search button
                if (!TextUtils.isEmpty(s)) {
                    // Search
                    searchMyPosts(s)
                } else {
                    loadMyPosts()
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                // Called when user type any letter
                if (!TextUtils.isEmpty(s)) {
                    // Search
                    searchMyPosts(s)
                } else {
                    loadMyPosts()
                }
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Handle menu items clicks

    // Handle menu items clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Get item id
        val id = item.itemId
        if (id == R.id.action_logout) {
            firebaseAuth!!.signOut()
            checkUserStatus()
        } else if (id == R.id.action_shop) {
            startActivity(Intent(activity, AddCatalogActivity::class.java))
        } else if (id == R.id.action_settings) {
            // Go to settings activity
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


}