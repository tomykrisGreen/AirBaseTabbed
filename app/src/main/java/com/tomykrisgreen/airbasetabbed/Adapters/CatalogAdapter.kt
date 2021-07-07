package com.tomykrisgreen.airbasetabbed.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.tomykrisgreen.airbasetabbed.CatalogShop.CatalogListActivity
import com.tomykrisgreen.airbasetabbed.Fragments.ProfileFragment
import com.tomykrisgreen.airbasetabbed.Models.Catalog
import com.tomykrisgreen.airbasetabbed.R

class CatalogAdapter
(
        private val mContext: Context,
        private val mCatalog: List<Catalog>,
        private var isFragment: Boolean = false
) : RecyclerView.Adapter<CatalogAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.catalog_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mCatalog.size
    }

    override fun onBindViewHolder(holder: CatalogAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val user = mCatalog[position]

        //Picasso.get().load(user.getProfileimagecatalog()).placeholder(R.drawable.profile).into(holder.profileImageCatalog)

        Picasso.get().load(user.getCatalogimage()).placeholder(R.drawable.profile).into(holder.catalogImage)

//        holder.companyDescription.text = user!!.getCompanydescription()
//        holder.companyName.text = user!!.getCompanyname()
        holder.productTitle.text = user!!.getTitlecatalog()
        holder.productDesc.text = user!!.getDescriptioncatalog()
        holder.productPrice.text = user!!.getProductprice()

//        publisherInfo(holder.profileImageCatalog, holder.companyName, holder.companyDescription,
//            holder.productTitle, holder.companyName, holder.productTitle, holder.productDesc as EditText, holder.catalogImage)

        holder.itemView.setOnClickListener(View.OnClickListener {
            if (isFragment)
            {
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", user.getCatalogid())
                pref.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).addToBackStack(null).commit()
            }
            else
            {
//                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, CatalogListFragment()).addToBackStack(null).commit()

                val intent = Intent(mContext, CatalogListActivity::class.java)
                intent.putExtra("uid", user.getUID())
                mContext.startActivity(intent)
            }
            addNotification(user.getUID()) // To check site visitations
        })
    }


    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var profileImageCatalog: ImageView = itemView.findViewById(R.id.profile_cover_catalog_setup)
//        var publisher: TextView = itemView.findViewById(R.id.publisher)
//        var companyName: TextView = itemView.findViewById(R.id.company_name)
//        var companyDescription: TextView = itemView.findViewById(R.id.company_description)
        var productTitle: TextView = itemView.findViewById(R.id.product_title)
        var productDesc: TextView = itemView.findViewById(R.id.product_description)
        var catalogImage: ImageView = itemView.findViewById(R.id.catalog_image_add)
        var productPrice: TextView = itemView.findViewById(R.id.product_price)

    }

    // Added to check site visitations
    private fun addNotification(userId: String)
    {
        val notiRef = FirebaseDatabase.getInstance().reference
                .child("Notifications (Catalog)")
                .child(userId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "viewed your product"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)

    }

//    private fun publisherInfo(
//        profileImageCatalog: ImageView,
//        publisher: TextView,
//        companyDescription: EditText,
//        companyName: EditText,
//        productTitle: EditText,
//        productDesc: EditText,
//        catalogImage: ImageView
//    )
//    {
//        val usersRef = FirebaseDatabase.getInstance().reference.child("Catalog").child(publisherID)
//
//        usersRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(p0: DataSnapshot) {
//                if (p0.exists()) {
//                    val user = p0.getValue<Catalog>(Catalog::class.java)
//
//                    Picasso.get().load(user!!.getProfileimagecatalog()).placeholder(R.drawable.profile)
//                        .into(profileImageCatalog)
//                    companyDescription.text = user!!.getCompanydescription()
//                    publisher.text = user!!.getPublisher()
//                    publisher.text = user!!.getPublisher()
//                }
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })
//    }

}
