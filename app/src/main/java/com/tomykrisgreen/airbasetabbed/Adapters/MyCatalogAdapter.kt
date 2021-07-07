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
import com.squareup.picasso.Picasso
import com.tomykrisgreen.airbasetabbed.CatalogShop.CatalogDetailsActivity
import com.tomykrisgreen.airbasetabbed.CatalogShop.CatalogListActivity
import com.tomykrisgreen.airbasetabbed.Models.Catalog
import com.tomykrisgreen.airbasetabbed.R

class MyCatalogAdapter (private val mContext: Context, mCatalog: List<Catalog>, private var isFragment: Boolean = false)
    : RecyclerView.Adapter<MyCatalogAdapter.ViewHolder?>() {

    private var mCatalog: List<Catalog>? = null


    init {
        this.mCatalog = mCatalog
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCatalogAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.catalog_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCatalogAdapter.ViewHolder, position: Int) {
        val catalog: Catalog = mCatalog!![position]

        Picasso.get().load(catalog.getCatalogimage()).into(holder.postImage)
        holder.descriptionCatalog.text = catalog!!.getDescriptioncatalog()

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

            editor.putString("catalogid", catalog.getCatalogid())

            editor.apply()

            val intent = Intent(mContext, CatalogDetailsActivity::class.java)
            intent.putExtra("uid", catalog.getUID())
            mContext.startActivity(intent)

//            (mContext as FragmentActivity).getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, CatalogDetailsFragment()).addToBackStack(null).commit()
        }
    }

    override fun getItemCount(): Int {
        return mCatalog!!.size
    }

    inner class ViewHolder(@NonNull itemView: View)
        : RecyclerView.ViewHolder(itemView)
    {
        var postImage: ImageView
        var descriptionCatalog: TextView

        init {
            postImage = itemView.findViewById(R.id.post_image_catalog)
            descriptionCatalog = itemView.findViewById(R.id.description_on_item_catalog)
        }
    }
}