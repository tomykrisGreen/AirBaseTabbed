package com.tomykrisgreen.airbasetabbed

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost
import com.tomykrisgreen.airbasetabbed.Models.ModelPost
import com.tomykrisgreen.airbasetabbed.Models.ModelUser
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile_kotlin.*
import java.util.*

class ProfileKotlinActivity : AppCompatActivity() {
    //Firebase
    var firebaseAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null
    private lateinit var firebaseUser: FirebaseUser

    //Storage
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    // Path to store profile and cover pictures of users
    var storagePath = "Users_Profile_Cover_Imgs/"

    //Views from Xml
    var avatarIv: CircleImageView? = null
    var coverIv: ImageView? = null
    var nameTV: TextView? = null
    var emailTV: TextView? = null
    var phoneTV: TextView? = null
    var usernameTv: TextView? = null
    var countryTv: TextView? = null
    var stateTv: TextView? = null
    var statusTv: TextView? = null
    var weblinkTv: TextView? = null

    var postsRecyclerView: RecyclerView? = null

    var postList: List<ModelPost>? = null
    var adapterPost: AdapterPost? = null
    var uid: String? = null

    var progressDialog: ProgressDialog? = null

    var progressBarProfileImage: ProgressBar? = null

    private lateinit var profileId: String


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_kotlin)

        val actionBar = supportActionBar
        actionBar!!.setTitle("Profile")
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)


        // Init Firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference

        progressDialog = ProgressDialog(this)

        progressBarProfileImage = findViewById(R.id.progressBar_profile_image)

        // Init views
        avatarIv = findViewById(R.id.avatarIv_Profile_activity)
        coverIv = findViewById(R.id.coverIv_activity)
        nameTV = findViewById(R.id.nameTV_activity)
        emailTV = findViewById(R.id.emailTV)
        phoneTV = findViewById(R.id.phoneTV)
        usernameTv = findViewById(R.id.profile_activity_username)
        countryTv = findViewById(R.id.profile_activity_country)
        stateTv = findViewById(R.id.profile_activity_state)
        statusTv = findViewById(R.id.profile_activity_status)
        weblinkTv = findViewById(R.id.profile_activity_web_link)

        postsRecyclerView = findViewById(R.id.recycler_view_upload_pic)

        // Get uid of clicked user to retrieve his posts
        val intent = intent
        uid = intent.getStringExtra("uid")

//        val pref = this?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
//        if (pref != null) {
//            this.profileId = pref.getString("profileId", "none").toString()
//        }

        if (uid == firebaseUser.uid) {
            edit_account_settings_activity_btn.text = "Edit Profile"
        } else if (uid != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        edit_account_settings_activity_btn.setOnClickListener {
            val getButtonText = edit_account_settings_activity_btn.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity(
                        Intent(
                                this,
                                AccountSettingsActivity::class.java
                        )
                )

                getButtonText == "Follow" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(uid!!)
                                .setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(uid!!)
                                .child("Followers").child(it1.toString())
                                .setValue(true)
                    }

                    addNotification()
                }

                getButtonText == "Following" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(uid!!)
                                .removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(uid!!)
                                .child("Followers").child(it1.toString())
                                .removeValue()
                    }
                }
            }

        }


//        val query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid)
//        query.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // Check until required data is found
//                for (ds in dataSnapshot.children) {
//                        val user = ds.getValue<ModelUser>(ModelUser::class.java)
//
//                        try {
//                            Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
//                                    .into(
//                                            avatarIv_Profile_activity
//                                    )
//                        } catch (e: Exception) {
////                        Picasso.get().load(R.drawable.profile).into(view?.pro_image_profile_frag)
//                        }
//
////                    Picasso.get().load(user!!.getProfileimage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)
//
////                    view?.profile_fragment_username?.text = "@" + user!!.getUsername()
//                        nameTV?.text = user!!.getName()
////                    view?.profile_fragment_status?.text = user!!.getStatus()
////                    view?.profile_fragment_state?.text = user!!.getState()
////                    view?.profile_fragment_country?.text = user!!.getCountry()
//
//                        if (user.getStatus() != ("")) {
//                            try {
//                                profile_activity_status.text = user!!.getStatus()
//                                profile_activity_status.visibility = View.VISIBLE
//                            } catch (e: Exception) {
//                            }
////                        view?.profile_fragment_status?.text = user!!.getStatus()
////                        view?.profile_fragment_status?.visibility = View.VISIBLE
//                        }
//
//                        if (user.getUsername() != ("")) {
//                            try {
//                                profile_activity_username.text = "@" + user!!.getUsername()
//                                profile_activity_username.visibility = View.VISIBLE
//                            } catch (e: Exception) {
//                            }
////                        view?.profile_fragment_username?.text = "@" + user!!.getUsername()
////                        view?.profile_fragment_username?.visibility = View.VISIBLE
//                        }
//
//                        if (user.getState() != ("") && user.getCountry() != ("") ||
//                                user.getState() != ("") && user.getCountry() == ("") ||
//                                user.getState() == ("") && user.getCountry() != ("")
//                        ) {
//                            try {
//                                image_location_activity!!.visibility = View.VISIBLE
//                                profile_activity_state?.text = user!!.getState() //+ ", "
//                                profile_activity_country?.text = user!!.getCountry()
//                                profile_activity_country?.visibility = View.VISIBLE
//                                profile_activity_state?.visibility = View.VISIBLE
//                            } catch (e: Exception) {
//                            }
//
////                        view?.image_location!!.visibility = View.VISIBLE
////                        view?.profile_fragment_state?.text = user!!.getState() //+ ", "
////                        view?.profile_fragment_country?.text = user!!.getCountry()
////                        view?.profile_fragment_country?.visibility = View.VISIBLE
////                        view?.profile_fragment_state?.visibility = View.VISIBLE
//                        } else {
//                            try {
//                                image_location_activity!!.visibility = View.GONE
//                                profile_activity_state!!.visibility = View.GONE
//                                profile_activity_country!!.visibility = View.GONE
//                            } catch (e: Exception) {
//                            }
//
////                        view?.image_location!!.visibility = View.GONE
////                        view?.profile_fragment_state!!.visibility = View.GONE
////                        view?.profile_fragment_country!!.visibility = View.GONE
//                        }
//
//                        if (user.getState() != ("") && user.getCountry() != ("")) {
//                            try {
//                                profile_comma_activity?.visibility = View.VISIBLE
//                            } catch (e: Exception) {
//                            }
////                        view?.profile_comma?.visibility = View.VISIBLE
//                        }
//
//
//                        if (user.getWebsite() != ("")) {
//                            try {
//                                image_weblink_activity!!.visibility = View.VISIBLE
//                                profile_activity_web_link!!.visibility = View.VISIBLE
//                                profile_activity_web_link!!.text = user.getWebsite()
//                            } catch (e: Exception) {
//                            }
////                        view?.image_weblink!!.visibility = View.VISIBLE
////                        view?.profile_fragment_web_link!!.visibility = View.VISIBLE
////                        view?.profile_fragment_web_link!!.text = user.getWebsite()
//                        } else {
//                            try {
//                                image_weblink_activity!!.visibility = View.GONE
//                                profile_activity_web_link!!.visibility = View.GONE
//                            } catch (e: Exception) {
//                            }
////                        view?.image_weblink!!.visibility = View.GONE
////                        view?.profile_fragment_web_link!!.visibility = View.GONE
//                        }
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {}
//        })

        postList = ArrayList()

        getFollowings()
        getFollowers()
        getTotalNumberOfPosts()
        getUserInfo()

        checkUserStatus()

        loadHisPosts()

    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it1.toString())
                    .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(uid!!).exists()) {
                        edit_account_settings_activity_btn?.text = "Following"
                    } else {
                        edit_account_settings_activity_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }


    private fun getUserInfo() {
        val query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check until required data is found
                for (ds in dataSnapshot.children) {
                    val user = ds.getValue<ModelUser>(ModelUser::class.java)

                    try {
                        progressBarProfileImage!!.visibility = View.VISIBLE

                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                                .into(
                                        avatarIv_Profile_activity
                                )

                        progressBarProfileImage!!.visibility = View.GONE
                    } catch (e: Exception) {
                        progressBarProfileImage!!.visibility = View.GONE
//                        Picasso.get().load(R.drawable.profile).into(view?.pro_image_profile_frag)
                    }

//                    Picasso.get().load(user!!.getProfileimage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)

//                    view?.profile_fragment_username?.text = "@" + user!!.getUsername()
                    nameTV?.text = user!!.getName()
//                    view?.profile_fragment_status?.text = user!!.getStatus()
//                    view?.profile_fragment_state?.text = user!!.getState()
//                    view?.profile_fragment_country?.text = user!!.getCountry()

                    if (user.getStatus() != ("")) {
                        try {
                            profile_activity_status.text = user!!.getStatus()
                            profile_activity_status.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
                        profile_activity_username.visibility = View.GONE
//                        view?.profile_fragment_status?.text = user!!.getStatus()
//                        view?.profile_fragment_status?.visibility = View.VISIBLE
                    }

                    if (user.getUsername() != ("")) {
                        try {
                            profile_activity_username.text = "@" + user!!.getUsername()
                            profile_activity_username.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
                        profile_activity_username.visibility = View.GONE
//                        view?.profile_fragment_username?.text = "@" + user!!.getUsername()
//                        view?.profile_fragment_username?.visibility = View.VISIBLE
                    }

                    if (user.getState() != ("") && user.getCountry() != ("") ||
                            user.getState() != ("") && user.getCountry() == ("") ||
                            user.getState() == ("") && user.getCountry() != ("")
                    ) {
                        try {
                            image_location_activity!!.visibility = View.VISIBLE
                            profile_activity_state?.text = user!!.getState() //+ ", "
                            profile_activity_country?.text = user!!.getCountry()
                            profile_activity_country?.visibility = View.VISIBLE
                            profile_activity_state?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }

//                        view?.image_location!!.visibility = View.VISIBLE
//                        view?.profile_fragment_state?.text = user!!.getState() //+ ", "
//                        view?.profile_fragment_country?.text = user!!.getCountry()
//                        view?.profile_fragment_country?.visibility = View.VISIBLE
//                        view?.profile_fragment_state?.visibility = View.VISIBLE
                    } else {
                        try {
                            image_location_activity!!.visibility = View.GONE
                            profile_activity_state!!.visibility = View.GONE
                            profile_activity_country!!.visibility = View.GONE
                        } catch (e: Exception) {
                        }

//                        view?.image_location!!.visibility = View.GONE
//                        view?.profile_fragment_state!!.visibility = View.GONE
//                        view?.profile_fragment_country!!.visibility = View.GONE
                    }

                    if (user.getState() != ("") && user.getCountry() != ("")) {
                        try {
                            profile_comma_activity?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
//                        view?.profile_comma?.visibility = View.VISIBLE
                    }


                    if (user.getWebsite() != ("")) {
                        try {
                            image_weblink_activity!!.visibility = View.VISIBLE
                            profile_activity_web_link!!.visibility = View.VISIBLE
                            profile_activity_web_link!!.text = user.getWebsite()
                        } catch (e: Exception) {
                        }
//                        view?.image_weblink!!.visibility = View.VISIBLE
//                        view?.profile_fragment_web_link!!.visibility = View.VISIBLE
//                        view?.profile_fragment_web_link!!.text = user.getWebsite()
                    } else {
                        try {
                            image_weblink_activity!!.visibility = View.GONE
                            profile_activity_web_link!!.visibility = View.GONE
                        } catch (e: Exception) {
                        }
//                        view?.image_weblink!!.visibility = View.GONE
//                        view?.profile_fragment_web_link!!.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getFollowings() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(uid!!)   // profileId replaced with firebaseUser.uid
                .child("Following")


        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    total_following_activity.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(uid!!)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    total_followers_activity.text = p0.childrenCount.toString()
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
                        if (post.getUid() == uid) {
                            postCounter++
                        }
                    }
                    total_posts.text = " " + postCounter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(uid)
                // .child(firebaseUser.uid) //profileId replace with firebaseUser!!.uid

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<ModelUser>(ModelUser::class.java)

                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                                .into(
                                        avatarIv_Profile_activity
                                )
                    } catch (e: Exception) {
//                        Picasso.get().load(R.drawable.profile).into(view?.pro_image_profile_frag)
                    }

//                    Picasso.get().load(user!!.getProfileimage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)

//                    view?.profile_fragment_username?.text = "@" + user!!.getUsername()
                    nameTV?.text = user!!.getName()
//                    view?.profile_fragment_status?.text = user!!.getStatus()
//                    view?.profile_fragment_state?.text = user!!.getState()
//                    view?.profile_fragment_country?.text = user!!.getCountry()

                    if (user.getStatus() != ("")) {
                        try {
                            profile_activity_status.text = user!!.getStatus()
                            profile_activity_status.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
//                        view?.profile_fragment_status?.text = user!!.getStatus()
//                        view?.profile_fragment_status?.visibility = View.VISIBLE
                    }

                    if (user.getUsername() != ("")) {
                        try {
                            profile_activity_username.text = "@" + user!!.getUsername()
                            profile_activity_username.visibility = View.VISIBLE
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
                            image_location_activity!!.visibility = View.VISIBLE
                            profile_activity_state?.text = user!!.getState() //+ ", "
                            profile_activity_country?.text = user!!.getCountry()
                            profile_activity_country?.visibility = View.VISIBLE
                            profile_activity_state?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }

//                        view?.image_location!!.visibility = View.VISIBLE
//                        view?.profile_fragment_state?.text = user!!.getState() //+ ", "
//                        view?.profile_fragment_country?.text = user!!.getCountry()
//                        view?.profile_fragment_country?.visibility = View.VISIBLE
//                        view?.profile_fragment_state?.visibility = View.VISIBLE
                    } else {
                        try {
                            image_location_activity!!.visibility = View.GONE
                            profile_activity_state!!.visibility = View.GONE
                            profile_activity_country!!.visibility = View.GONE
                        } catch (e: Exception) {
                        }

//                        view?.image_location!!.visibility = View.GONE
//                        view?.profile_fragment_state!!.visibility = View.GONE
//                        view?.profile_fragment_country!!.visibility = View.GONE
                    }

                    if (user.getState() != ("") && user.getCountry() != ("")) {
                        try {
                            profile_comma_activity?.visibility = View.VISIBLE
                        } catch (e: Exception) {
                        }
//                        view?.profile_comma?.visibility = View.VISIBLE
                    }


                    if (user.getWebsite() != ("")) {
                        try {
                            image_weblink_activity!!.visibility = View.VISIBLE
                            profile_activity_web_link!!.visibility = View.VISIBLE
                            profile_activity_web_link!!.text = user.getWebsite()
                        } catch (e: Exception) {
                        }
//                        view?.image_weblink!!.visibility = View.VISIBLE
//                        view?.profile_fragment_web_link!!.visibility = View.VISIBLE
//                        view?.profile_fragment_web_link!!.text = user.getWebsite()
                    } else {
                        try {
                            image_weblink_activity!!.visibility = View.GONE
                            profile_activity_web_link!!.visibility = View.GONE
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
                .child(uid!!)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }


    private fun loadHisPosts() {
        // LinearLayout for recyclerView
        val layoutManager = LinearLayoutManager(this)
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

                for (ds in snapshot.children) {
                    val myPosts = ds.getValue(ModelPost::class.java)

                    // Add to list
                    (postList as ArrayList<ModelPost>).add(myPosts!!)

                    // Adapter
                    adapterPost = AdapterPost(this@ProfileKotlinActivity, postList)
                    // Set adapter to recyclerView
                    postsRecyclerView!!.adapter = adapterPost
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileKotlinActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchHisPosts(searchQuery: String) {
        // LinearLayout for recyclerView
        val layoutManager = LinearLayoutManager(this)
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

                for (ds in snapshot.children) {
                    val myPosts = ds.getValue(ModelPost::class.java)
                    if (myPosts!!.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        // Add to list
                        (postList as ArrayList<ModelPost>).add(myPosts)
                    }

                    // Adapter
                    adapterPost = AdapterPost(this@ProfileKotlinActivity, postList)
                    // Set adapter to recyclerView
                    postsRecyclerView!!.adapter = adapterPost
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileKotlinActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun checkUserStatus() {
        // Get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            // User is signed in
            // Set email of logged in user
            //mProfileTV.setText(user.getEmail());
        } else {
            // User not signed in, go to Main Activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_shop).isVisible = false // Hide add post from this activity
        menu.findItem(R.id.action_create_group).isVisible = false // Hide add post from this activity
        val item = menu.findItem(R.id.action_search)
        // SearchView to search user specific posts
        val searchView = MenuItemCompat.getActionView(item) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                // Called when user presses the search button
                if (!TextUtils.isEmpty(s)) {
                    // Search
                    searchHisPosts(s)
                } else {
                    loadHisPosts()
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                // Called when user type any letter
                if (!TextUtils.isEmpty(s)) {
                    // Search
                    searchHisPosts(s)
                } else {
                    loadHisPosts()
                }
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_logout) {
            firebaseAuth!!.signOut()
            checkUserStatus()
        }
        return super.onOptionsItemSelected(item)
    }
}