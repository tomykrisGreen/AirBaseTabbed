package com.tomykrisgreen.airbasetabbed.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tomykrisgreen.airbasetabbed.ChatActivity
import com.tomykrisgreen.airbasetabbed.Models.ModelUser
import com.tomykrisgreen.airbasetabbed.ProfileKotlinActivity
import com.tomykrisgreen.airbasetabbed.R


class AdapterUsers(
        var context: Context,
        var userList: List<ModelUser>,
//        private var isFragment: Boolean = false
) : RecyclerView.Adapter<AdapterUsers.MyHolder>() {
    // For getting current users uid
    var firebaseAuth: FirebaseAuth
    var myUid: String?

    // User info
    var name: String? = null
    var email: String? = null
    var uid: String? = null
    var dp: String? = null

    var userDbRef: DatabaseReference? = null

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
        // Inflate layout (row_user.xml)
        val view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(myHolder: MyHolder, i: Int) {
        val user = userList[i]

        // Trying to get
        // Get some info of current user
        userDbRef = FirebaseDatabase.getInstance().getReference("Users")
        val query: Query = userDbRef!!.orderByChild("email").equalTo(email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    name = "" + ds.child("name").value
                    email = "" + ds.child("email").value
                    dp = "" + ds.child("image").value
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        myHolder.mNameTv.text = name
        try {
            Picasso.get().load(dp).placeholder(R.drawable.profile).into(myHolder.mAvatarIv)
        }
        catch (e: Exception){

        }

        // End trying to get


        myHolder.mNameTv.text = "@" + user.getName()
        try {
            Picasso.get().load(user.image).placeholder(R.drawable.profile).into(myHolder.mAvatarIv)
        }
        catch (e: Exception){

        }

        // Get data
        val hisUID = userList[i].uid
        val userImage = userList[i].image
        val userName = userList[i].name
        val userEmail = userList[i].email

        // Set data
        myHolder.mNameTv.text = userName
        myHolder.mEmailTv.text = userEmail
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(myHolder.mAvatarIv)
        } catch (e: Exception) {
        }
        myHolder.blockTv.text = "Block"
        myHolder.blockIv.setImageResource(R.drawable.ic_unblocked)
        // Check if each user is blocked or not
        checkIsBlocked(hisUID, myHolder, i)


//        myHolder.itemView.setOnClickListener(View.OnClickListener {
//            if (isFragment) {
//                val pref = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
//                pref.putString("uid", user.getUid())
//                pref.apply()
//
//                (context as FragmentActivity).supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, ProfileKotlinFragment()).addToBackStack(null)
//                        .commit()
//            } else {
//                val intent = Intent(context, MainActivity::class.java)
//                intent.putExtra("uid", user.getUid())
//                context.startActivity(intent)
//            }
//        })

        // Handle item click
        myHolder.itemView.setOnClickListener { // Click user from user list to start chatting/messaging
            // Start activity by putting UID of receiver
            // the UID will be used to identify the user to chat with

            // Show dialog
            val builder = AlertDialog.Builder(context)
            builder.setItems(arrayOf("Profile", "Chat")) { dialogInterface, which ->
                if (which == 0) {
                    // Profile clicked
                    // Click to go to ThereProfileActivity
//                    val intent = Intent(context, ThereProfileActivity::class.java)
//                    intent.putExtra("uid", hisUID)
//                    context.startActivity(intent)

                    val intent = Intent(context, ProfileKotlinActivity::class.java)
                    intent.putExtra("uid", hisUID)
                    context.startActivity(intent)

//                    (context as FragmentActivity).supportFragmentManager.beginTransaction()
//                            .replace(R.id.fragment_container, ProfileKotlinFragment()).addToBackStack(null)
//                            .commit()
                }
                if (which == 1) {
                    // Chat clicked
                    //                            Intent intent = new Intent(context, ChatActivity.class);
                    //                            intent.putExtra("hisUid", hisUID);
                    //                            context.startActivity(intent);
                    //Toast.makeText(context, ""+ userEmail, Toast.LENGTH_SHORT).show();
                    imBlockedOrNot(hisUID)
                }
            }
            builder.create().show()
        }

        // Click to block/unblock a user
        myHolder.blockIv.setOnClickListener {
            if (userList[i].isBlocked) {
                unBlockUser(hisUID)
            } else {
                blockUser(hisUID)
            }
        }
    }

    private fun imBlockedOrNot(hisUID: String) {
        // First check if sender (current user) is blocked by receiver or not
        // Logic: if uid of the sender (current user) exists in "BlockedUsers" of receiver, then sender (current user) is blocked, otherwise, not blocked
        // If blocked, then just display a message e.g. You're blocked by the user; Can't send message
        // If not blocked, then simply start the chat activity
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            if (ds.exists()) {
                                Toast.makeText(context, "You're blocked by the user; Can't send message", Toast.LENGTH_SHORT).show()
                                // Blocked, don't proceed further
                                return
                            }
                        }
                        // Not blocked, start activity
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("hisUid", hisUID)
                        context.startActivity(intent)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun checkIsBlocked(hisUID: String, myHolder: MyHolder, i: Int) {
        // Check each user if blocked or not
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(myUid!!).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            if (ds.exists()) {
                                myHolder.blockTv.text = "Unblock"
                                myHolder.blockIv.setImageResource(R.drawable.ic_blocked)
                                userList[i].isBlocked = true
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun blockUser(hisUID: String) {
        // Block the user by adding uid to current users "BlockedUsers" node

        // Put values in hashMap to put in db
        val hashMap = HashMap<String, String>()
        hashMap["uid"] = hisUID
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(myUid!!).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener { // Blocked
                    Toast.makeText(context, "Blocked", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e -> // Failed
                    Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                }
    }

    private fun unBlockUser(hisUID: String) {
        // Unblock the user by removing uid to current users "BlockedUsers" node
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(myUid!!).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            if (ds.exists()) {
                                // Remove blocked user data from current users BlockedUsers list
                                ds.ref.removeValue()
                                        .addOnSuccessListener { // Unblocked
                                            Toast.makeText(context, "Unblocked", Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener { e -> // Failed to unblock
                                            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                                        }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    //View holder class
    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mAvatarIv: ImageView
        var blockIv: ImageView
        var mNameTv: TextView
        var mEmailTv: TextView
        var blockTv: TextView

        init {

            //Init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv)
            mNameTv = itemView.findViewById(R.id.nameTV)
            mEmailTv = itemView.findViewById(R.id.emailTV)
            blockIv = itemView.findViewById(R.id.blockIv)
            blockTv = itemView.findViewById(R.id.blockTv)
        }
    }

    init {
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = firebaseAuth.uid
    }
}
