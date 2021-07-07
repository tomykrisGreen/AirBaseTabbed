package com.tomykrisgreen.airbasetabbed.CatalogShop

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tomykrisgreen.airbasetabbed.Adapters.CatalogAdapter
import com.tomykrisgreen.airbasetabbed.Models.Catalog
import com.tomykrisgreen.airbasetabbed.R

class CatalogDetailsActivity : AppCompatActivity() {
    private var catalogAdapter: CatalogAdapter? = null
    private var catalogList: MutableList<Catalog>? = null
    private var catalogid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog_details)

        val preferences = this?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (preferences!= null)
        {
            catalogid = preferences.getString("catalogid", "none").toString()
        }

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycler_view_catalog_details)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        catalogList = ArrayList()
        catalogAdapter = this?.let { CatalogAdapter(it, catalogList as ArrayList<Catalog>) }
        recyclerView.adapter = catalogAdapter


        retrievePosts()
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference
                .child("Catalog")
                .child(catalogid)

        postsRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
                catalogList?.clear()

                val catalog = p0.getValue(Catalog::class.java)

                catalogList!!.add(catalog!!)

                catalogAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
}