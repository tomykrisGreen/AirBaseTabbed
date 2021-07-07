//package com.tomykrisgreen.airbasetabbed.Adapters;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.squareup.picasso.Picasso;
//import com.tomykrisgreen.airbasetabbed.ChatActivity;
//import com.tomykrisgreen.airbasetabbed.Models.ModelUser;
//import com.tomykrisgreen.airbasetabbed.R;
//import com.tomykrisgreen.airbasetabbed.ThereProfileActivity;
//
//import java.util.HashMap;
//import java.util.List;
//
//public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {
//    Context context;
//    List<ModelUser> userList;
//
//    // For getting current users uid
//    FirebaseAuth firebaseAuth;
//    String myUid;
//
//    public AdapterUsers(Context context, List<ModelUser> userList) {
//        this.context = context;
//        this.userList = userList;
//
//        firebaseAuth = FirebaseAuth.getInstance();
//        myUid = firebaseAuth.getUid();
//    }
//
//    @NonNull
//    @Override
//    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        // Inflate layout (row_user.xml)
//        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);
//        return new MyHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
//        // Get data
//        final String hisUID = userList.get(i).getUid();
//        String userImage = userList.get(i).getImage();
//        String userName = userList.get(i).getName();
//        final String userEmail = userList.get(i).getEmail();
//
//        // Set data
//        myHolder.mNameTv.setText(userName);
//        myHolder.mEmailTv.setText(userEmail);
//        try{
//            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(myHolder.mAvatarIv);
//        }
//        catch (Exception e){
//
//        }
//
//        myHolder.blockIv.setImageResource(R.drawable.ic_unblocked);
//        // Check if each user is blocked or not
//        checkIsBlocked(hisUID, myHolder, i);
//
//        // Handle item click
//        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Click user from user list to start chatting/messaging
//                // Start activity by putting UID of receiver
//                // the UID will be used to identify the user to chat with
//
//                // Show dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        if (which == 0){
//                            // Profile clicked
//                            // Click to go to ThereProfileActivity
//                            Intent intent = new Intent(context, ThereProfileActivity.class);
//                            intent.putExtra("uid", hisUID);
//                            context.startActivity(intent);
//                        }
//                        if (which == 1){
//                            // Chat clicked
////                            Intent intent = new Intent(context, ChatActivity.class);
////                            intent.putExtra("hisUid", hisUID);
////                            context.startActivity(intent);
//                            //Toast.makeText(context, ""+ userEmail, Toast.LENGTH_SHORT).show();
//                            imBlockedOrNot(hisUID);
//                        }
//                    }
//                });
//                builder.create().show();
//            }
//        });
//
//        // Click to block/unblock a user
//        myHolder.blockIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (userList.get(i).isBlocked()){
//                    unBlockUser(hisUID);
//                }
//                else {
//                    blockUser(hisUID);
//                }
//            }
//        });
//
//    }
//
//    private void imBlockedOrNot(final String hisUID){
//        // First check if sender (current user) is blocked by receiver or not
//        // Logic: if uid of the sender (current user) exists in "BlockedUsers" of receiver, then sender (current user) is blocked, otherwise, not blocked
//        // If blocked, then just display a message e.g. You're blocked by the user; Can't send message
//        // If not blocked, then simply start the chat activity
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot ds: snapshot.getChildren()){
//                            if (ds.exists()){
//                                Toast.makeText(context, "You're blocked by the user; Can't send message", Toast.LENGTH_SHORT).show();
//                                // Blocked, don't proceed further
//                                return;
//                            }
//                        }
//                        // Not blocked, start activity
//                        Intent intent = new Intent(context, ChatActivity.class);
//                        intent.putExtra("hisUid", hisUID);
//                        context.startActivity(intent);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }
//
//    private void checkIsBlocked(String hisUID, MyHolder myHolder, final int i) {
//        // Check each user if blocked or not
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot ds: snapshot.getChildren()){
//                            if (ds.exists()){
//                                myHolder.blockIv.setImageResource(R.drawable.ic_blocked);
//                                userList.get(i).setBlocked(true);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }
//
//    private void blockUser(String hisUID) {
//        // Block the user by adding uid to current users "BlockedUsers" node
//
//        // Put values in hashMap to put in db
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("uid", hisUID);
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        // Blocked
//                        Toast.makeText(context, "Blocked", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // Failed
//                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void unBlockUser(String hisUID) {
//        // Unblock the user by removing uid to current users "BlockedUsers" node
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot ds: snapshot.getChildren()){
//                            if (ds.exists()){
//                                // Remove blocked user data from current users BlockedUsers list
//                                ds.getRef().removeValue()
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                // Unblocked
//                                                Toast.makeText(context, "Unblocked", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        // Failed to unblock
//                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }
//
//    @Override
//    public int getItemCount() {
//        return userList.size();
//    }
//
//    //View holder class
//    class MyHolder extends RecyclerView.ViewHolder{
//        ImageView mAvatarIv, blockIv;
//        TextView mNameTv, mEmailTv;
//
//        public MyHolder(@NonNull View itemView) {
//            super(itemView);
//
//            //Init views
//            mAvatarIv = itemView.findViewById(R.id.avatarIv);
//            mNameTv = itemView.findViewById(R.id.nameTV);
//            mEmailTv = itemView.findViewById(R.id.emailTV);
//            blockIv = itemView.findViewById(R.id.blockIv);
//        }
//    }
//}
