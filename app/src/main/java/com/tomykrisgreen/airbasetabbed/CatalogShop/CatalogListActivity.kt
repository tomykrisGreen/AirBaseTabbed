package com.tomykrisgreen.airbasetabbed.CatalogShop

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tomykrisgreen.airbasetabbed.Adapters.MyCatalogAdapter
import com.tomykrisgreen.airbasetabbed.Models.Catalog
import com.tomykrisgreen.airbasetabbed.R
import kotlinx.android.synthetic.main.activity_catalog_list.*

class CatalogListActivity : AppCompatActivity() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    private var recyclerView: RecyclerView? = null
    var myCatalogAdapter: MyCatalogAdapter? = null
    private var mCatalog: MutableList<Catalog>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog_list)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        //to search for product lists
        recyclerView = findViewById(R.id.recycler_view_catalog_search_frag)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        mCatalog = ArrayList()
        myCatalogAdapter = this.let { MyCatalogAdapter(it, mCatalog as ArrayList<Catalog>, true) }
        recyclerView?.adapter = myCatalogAdapter



        search_edit_text_catalog.addTextChangedListener(object: TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (search_edit_text_catalog.text.toString() == "")
                {
                }
                else
                {
                    recyclerView?.visibility = View.VISIBLE

                    retrieveCatalogList()
                    searchCatalog(s.toString().toLowerCase())
                }
            }

            override fun afterTextChanged(s: Editable?)
            {
            }
        })
    }

    private fun searchCatalog(input: String)
    {
        val query = FirebaseDatabase.getInstance().getReference()
                .child("Catalog")
                .orderByChild("titlecatalog")
                .startAt(input)
                .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                mCatalog?.clear()

                for (snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(Catalog::class.java)
                    if (user != null)
                    {
                        mCatalog?.add(user)
                    }
                }

                myCatalogAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError)
            {
            }
        })
    }

    private fun retrieveCatalogList()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Catalog")
        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if (search_edit_text_catalog?.text.toString() == "")
                {
                    mCatalog?.clear()

                    for (snapshot in dataSnapshot.children)
                    {
                        val user = snapshot.getValue(Catalog::class.java)
                        if (user != null)
                        {
                            mCatalog?.add(user)
                        }
                    }

                    myCatalogAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError)
            {
            }
        })
    }
}