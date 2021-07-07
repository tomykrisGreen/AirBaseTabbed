package com.tomykrisgreen.airbasetabbed.Story

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterUsers
import com.tomykrisgreen.airbasetabbed.Models.ModelUser
import com.tomykrisgreen.airbasetabbed.R

class ShowUsersActivity : AppCompatActivity() {
    var id: String = ""
    var title: String = ""

    var userAdapter: AdapterUsers? = null
    var userList: List<ModelUser>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!

//        val toolbar: Toolbar =  findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        toolbar.setNavigationOnClickListener {
//            finish()
//        }

        var recyclerView: RecyclerView
        recyclerView =  findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
//        userAdapter = AdapterUsers(this, userList as ArrayList<ModelUser>, false)
        userAdapter = AdapterUsers(this, userList as ArrayList<ModelUser>)
        recyclerView.adapter = userAdapter

        idList = ArrayList()
//        userAdapter = AdapterUsers(this, idList as ArrayList<ModelUser>, false)
        userAdapter = AdapterUsers(this, idList as ArrayList<ModelUser>)
        recyclerView.adapter = userAdapter

        when(title)
        {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "view" -> getViews()
        }
    }

    private fun getViews() {
        val ref = FirebaseDatabase.getInstance().reference
                .child("Story").child(id!!)
                .child(intent.getStringExtra("storyid")!!)
                .child("views")

        ref.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                (idList as ArrayList<String>).clear()

                for (snapshot in p0.children)
                {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(id!!)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                (idList as ArrayList<String>).clear()

                for (snapshot in p0.children)
                {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowing() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(id!!)
                .child("Following")


        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                (idList as ArrayList<String>).clear()

                for (snapshot in p0.children)
                {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getLikes() {
        val LikesRef =  FirebaseDatabase.getInstance().reference
                .child("Likes").child(id!!)

        LikesRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    (idList as ArrayList<String>).clear()

                    for (snapshot in p0.children)
                    {
                        (idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun showUsers() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                (userList as ArrayList<ModelUser>).clear()

                for (snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(ModelUser::class.java)

                    for (id in idList!!)
                    {
                        if (user!!.getUid() == id)
                        {
                            (userList as ArrayList<ModelUser>).add(user!!)
                        }
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError)
            {
            }
        })
    }
}