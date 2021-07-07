package com.tomykrisgreen.airbasetabbed.Fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tomykrisgreen.airbasetabbed.*
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost
import com.tomykrisgreen.airbasetabbed.CatalogShop.CatalogShopActivity
import com.tomykrisgreen.airbasetabbed.Models.ModelPost
import com.tomykrisgreen.airbasetabbed.Models.Story
import com.tomykrisgreen.airbasetabbed.Story.StoryAdapter
import com.tomykrisgreen.airbasetabbed.VideoUpload.AddVideoActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class HomeKotlinFragment : Fragment() {
    var firebaseAuth: FirebaseAuth? = null

    var recyclerView: RecyclerView? = null
    var postList: List<ModelPost>? = null
    var adapterPost: AdapterPost? = null
    var followingList: MutableList<String>? = null

    var recyclerViewStory: RecyclerView? = null
    private var storyAdapter: StoryAdapter? = null
    private var storyList: MutableList<Story>? = null

    var user: FirebaseUser? = null
    var nameTVPoster: TextView? = null
    var avatarIvPoster: CircleImageView? = null

    var progressBar: ProgressBar? = null
    var progressBarProfileImage: ProgressBar? = null

    private lateinit var firebaseUser: FirebaseUser

//    var usersListParent: FloatingActionsMenu? = null

    var usersList: FloatingActionButton? = null

    var swipeRefresh: SwipeRefreshLayout? = null

//    var noPostsAvailable: TextView? = null

//    var recyclerViewStory: RecyclerView? = null
//    var storyList: List<ModelPost>? = null
//    var adapterStory: AdapterPost? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_kotlin, container, false)

        progressBar = view.findViewById(R.id.progressBarHome)

        var recyclerView: RecyclerView? = null
//        var recyclerViewStory: RecyclerView? = null

        // Init Firebase
        firebaseAuth = FirebaseAuth.getInstance()

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        user = firebaseAuth!!.currentUser

        nameTVPoster = view.findViewById(R.id.uNameTv)
        avatarIvPoster = view.findViewById(R.id.uPictureIv)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)


//        LinearLayout ll = new LinearLayout(this);
//        mRecordBtn = new RecordButton(this);
//        ll.addView(mRecordBtn,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        playButton = new PlayButton(this);
//        ll.addView(playButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        setContentView(ll);


//        LinearLayout ll = new LinearLayout(this);
//        mRecordBtn = new RecordButton(this);
//        ll.addView(mRecordBtn,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        playButton = new PlayButton(this);
//        ll.addView(playButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        setContentView(ll);



//        LinearLayout ll = new LinearLayout(this);
//        mRecordBtn = new RecordButton(this);
//        ll.addView(mRecordBtn,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        playButton = new PlayButton(this);
//        ll.addView(playButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        setContentView(ll);


//        noPostsAvailable = view.findViewById(R.id.no_posts)

//        usersListParent = view.findViewById(R.id.usersListParent)
        usersList = view.findViewById(R.id.usersList)


        // RecyclerView and its properties
        recyclerView = view.findViewById(R.id.postRecyclerView)
        val layoutManager = LinearLayoutManager(activity)
        // Show newest post first
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        // Set layout to recyclerView
        recyclerView.layoutManager = layoutManager

        // Init post list
        postList = ArrayList()
        adapterPost = context?.let { AdapterPost(it, postList as ArrayList<ModelPost>) }
        recyclerView.adapter = adapterPost


        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        recyclerViewStory!!.setHasFixedSize(true)
        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory!!.layoutManager = linearLayoutManager2

        // Init post list
        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        recyclerViewStory!!.adapter = storyAdapter

        // FAB Icon to load Users lists activity
        usersList!!.setOnClickListener {
//            (context as FragmentActivity).supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, UsersFragment()).addToBackStack(null)
//                        .commit()

            startActivity(Intent(context, UsersActivity::class.java))
        }


        // Not working to get user profile picture and name
//            val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child("uid")
//            usersRef.keepSynced(true)
//
//            usersRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(p0: DataSnapshot) {
//                    if (p0.exists()) {
//                        val user = p0.getValue<ModelUser>(ModelUser::class.java)
//
//                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
//                                .into(
//                                        avatarIvPoster
//                                )
//
//                        nameTVPoster!!.text = "@" + user!!.getName()
//                    }
//                }
//
//                override fun onCancelled(p0: DatabaseError) {
//
//                }
//            })



        checkFollowings()

        retrievePosts()
        retrieveStories()

        swipeRefresh!!.setOnRefreshListener {
            retrievePosts()
            retrieveStories()

            swipeRefresh!!.isRefreshing = false
        }



//        loadPosts()
//        retrieveStories()

//        loadVideosFromFirebase();

        return view
    }

   // To change color of imageView randomly (Not used for now)
    fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    override fun onResume() {
        super.onResume()
        // Initialize Runnable
        val runnable = Runnable {
            retrievePosts()
            retrieveStories()
        }
        // Handler delay for 2 seconds
        Handler().postDelayed(runnable, 2000)
    }


    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")


        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear()

                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }

                    retrievePosts()
                    retrieveStories()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")

        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()

                (storyList as ArrayList<Story>).clear()

                (storyList as ArrayList<Story>).add(
                        Story(
                                "",
                                0,
                                0,
                                "",
                                FirebaseAuth.getInstance().currentUser!!.uid
                        )
                )

                for (id in followingList!!) {
                    var countStory = 0

                    var story: Story? = null

                    for (snapshot in dataSnapshot.child(id).children) {
                        story = snapshot.getValue(Story::class.java)

                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story!!.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    //    private void loadVideosFromFirebase() {
    //        // Init Array list
    //        postList = new ArrayList<>();
    //
    //        // DB reference
    //        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Videos");
    //        ref.addValueEventListener(new ValueEventListener() {
    //            @Override
    //            public void onDataChange(@NonNull DataSnapshot snapshot) {
    //                // Clear list before adding data into it
    //                postList.clear();
    //
    //                for (DataSnapshot ds: snapshot.getChildren()){
    //                    // Get data
    //                    ModelPost modelVideo = ds.getValue(ModelPost.class);
    //                    // Add model/data into list
    //                    postList.add(modelVideo);
    //                }
    //                // Setup adapter
    //                adapterPost = new AdapterPost(getContext(), postList);
    //                // Set adapter to recyclerView
    //                recyclerView.setAdapter(adapterPost);
    //            }
    //
    //            @Override
    //            public void onCancelled(@NonNull DatabaseError error) {
    //
    //            }
    //        });
    //    }

    private fun retrievePosts() {
//            noPostsAvailable!!.visibility = View.VISIBLE
            progressBar!!.visibility = View.VISIBLE

            val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

            postsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    (postList as ArrayList<ModelPost>).clear()

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(ModelPost::class.java)

                        for (id in (followingList as ArrayList<String>)) {
                            if (post!!.getUid() == id) {
                                (postList as ArrayList<ModelPost>).add(post!!)

                                progressBar!!.visibility = View.GONE
//                                noPostsAvailable!!.visibility = View.GONE
                            }

                            adapterPost!!.notifyDataSetChanged()
                        }
                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    progressBar!!.visibility = View.GONE
//                    noPostsAvailable!!.visibility = View.GONE

                }
            })


    }

    private fun loadPosts() {
        // Path of all post
        val ref = FirebaseDatabase.getInstance().getReference("Posts")
        // Get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (postList as ArrayList<ModelPost>).clear()

                for (ds in snapshot.children) {
                    val modelPost = ds.getValue(ModelPost::class.java)

                    (postList as ArrayList<ModelPost>).add(modelPost!!)

                    // Adapter
                    adapterPost = AdapterPost(activity, postList)
                    // Set adapter to recycler view
//                    recyclerView?.adapter = adapterPost
                    try {
                        recyclerView?.adapter = adapterPost
                    } catch (e: Exception) {

                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // In case of error
                Toast.makeText(activity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchPosts(searchQuery: String) {
        // Path of all post
        val ref = FirebaseDatabase.getInstance().getReference("Posts")
        // Get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                (postList as ArrayList<ModelPost>).clear()

                for (ds in snapshot.children) {
                    val modelPost = ds.getValue(ModelPost::class.java)
                    if (modelPost!!.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {

                        (postList as ArrayList<ModelPost>).add(modelPost!!)
                    }

//                    if (modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) ||
//                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
//                        postList.add(modelPost);
//                    }

                    // Adapter
                    adapterPost = AdapterPost(activity, postList)
                    // Set adapter to recycler view
                    try {
                        recyclerView?.adapter = adapterPost
                    } catch (e: Exception) {

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // In case of error
                Toast.makeText(activity, "" + error.message, Toast.LENGTH_SHORT).show()
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
            startActivity(Intent(activity, MainActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true) // To show menu in fragment
        super.onCreate(savedInstanceState)
    }

    // Inflate options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflating menu
        inflater.inflate(R.menu.menu_main, menu)

        // Hide some options
        menu.findItem(R.id.action_create_group).isVisible = false
        menu.findItem(R.id.action_add_participant).isVisible = false
        menu.findItem(R.id.action_group_info).isVisible = false

        // SearchView to search posts by post Title/Description
        val item = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(item) as SearchView

        // Search listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                // Called when user presses search button
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s)
                } else {
                    loadPosts()
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Called when user presses search button
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText)
                } else {
                    loadPosts()
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
            startActivity(Intent(activity, CatalogShopActivity::class.java))
            //            showMoreOptions();
        } else if (id == R.id.action_settings) {
            // Go to settings activity
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMoreOptions() {
        // Popup menu to show more options
        val popupMenu = PopupMenu(context, view, Gravity.CENTER)
        // Items to show in menu
        popupMenu.menu.add(Menu.NONE, 0, 0, "Add Post")
        popupMenu.menu.add(Menu.NONE, 1, 0, "Add Video")

        // Menu clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if (id == 0) {
                startActivity(Intent(activity, AddPostActivity::class.java))
            } else if (id == 1) {
                startActivity(Intent(activity, AddVideoActivity::class.java))
            }
            false
        }
        popupMenu.show()
    }

}