package com.tomykrisgreen.airbasetabbed.CatalogShop

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tomykrisgreen.airbasetabbed.Adapters.CatalogAdapter
import com.tomykrisgreen.airbasetabbed.Models.Catalog
import com.tomykrisgreen.airbasetabbed.R
import kotlinx.android.synthetic.main.activity_catalog_shop.*
import java.util.*
import kotlin.collections.ArrayList

class CatalogShopActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var profileId: String
    private var catalogAdapter: CatalogAdapter? = null
    private var catalogList: MutableList<Catalog>? = null

    var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog_shop)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = this?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        var recyclerView: RecyclerView? = null

        recyclerView = findViewById(R.id.recycler_view_catalog)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd =  true
        recyclerView.layoutManager = linearLayoutManager

        catalogList = ArrayList()
        catalogAdapter = this?.let { CatalogAdapter(it, catalogList as ArrayList<Catalog>) }
        recyclerView.adapter = catalogAdapter


        add_catalog.setOnClickListener {
            val intent = Intent(this, AddCatalogActivity::class.java)
            startActivity(intent)
        }

        edit_info.setOnClickListener {
            val intent = Intent(this, CompanyInfoActivity::class.java)
            startActivity(intent)
        }

        contact_owner.setOnClickListener {
            //To Do (Send to Sellers Chat page)
        }

        companyInfo()
        retrieveCatalogPosts()
        getTotalNumberOfCatalogLists()

    }

    private fun retrieveCatalogPosts()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Catalog")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    (catalogList as ArrayList<Catalog>).clear()

                    for (snapshot in p0.children)
                    {
                        val post = snapshot.getValue(Catalog::class.java)!!
                        if (post.getPublisher().equals(uid))
                        {
                            (catalogList as ArrayList<Catalog>).add(post)
                        }
                        Collections.reverse(catalogList)
                        catalogAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

//    private fun retrieveCatalogPosts() {
//        val catalogRef = FirebaseDatabase.getInstance().reference.child("Catalog")
//
//        catalogRef.addValueEventListener(object : ValueEventListener
//        {
//            override fun onDataChange(p0: DataSnapshot) {
//                catalogList?.clear()
//
//                for (snapshot in p0.children)
//                {
//                    val post = snapshot.getValue(Catalog::class.java)
//
////                    for (id in (followingList as ArrayList<String>))
////                    {
////                        if (post!!.getPublisher() == id)
////                        {
//                            catalogList!!.add(post!!)
////                        }
//
//                        catalogAdapter!!.notifyDataSetChanged()
////                    }
//                }
//
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })
//
//    }

    private fun companyInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Company Info").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<CompanyInfo>(CompanyInfo::class.java)

                    Picasso.get().load(user!!.getProfileimagecatalog()).placeholder(R.drawable.sea_rainbow).into(profile_cover_catalog)

                    company_name.setText(user.getCompanyname())
                    company_description.setText(user.getCompanydescription())
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getTotalNumberOfCatalogLists()
    {
        val photoRef = FirebaseDatabase.getInstance().reference.child("Catalog")

        photoRef.addValueEventListener(object : ValueEventListener
        {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    var catalogCounter = 0

                    for (snapShot in dataSnapshot.children)
                    {
                        val photo = snapShot.getValue(Catalog::class.java)!!
                        if (photo.getPublisher() == profileId)
                        {
                            catalogCounter++
                        }
                    }
                    total_catalogs_list.text = " " + catalogCounter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}