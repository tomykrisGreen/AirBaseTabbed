package com.tomykrisgreen.airbasetabbed.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Models.ModelNotification;
import com.tomykrisgreen.airbasetabbed.PostDetailActivity;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotificaiton> {

    private Context context;
    private ArrayList<ModelNotification> notificationsList;

    private FirebaseAuth firebaseAuth;

    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotificaiton onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate view row_notification
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent,false);
        return new HolderNotificaiton(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotificaiton holder, int position) {
        // Get and set data to views

        // Get data
        ModelNotification model = notificationsList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();
        String pId = model.getpId();

        // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyy hh:mm aa", calender).toString();

        // We will get the name, email and image of the user of notification from his uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String image = ""+ds.child("image").getValue();
                            String email = ""+ds.child("email").getValue();

                            // Add to model
                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            // Set to views
                            holder.nameTvNotification.setText(name);

                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIvNotification);
                            }
                            catch (Exception e){
                                holder.avatarIvNotification.setImageResource(R.drawable.ic_default_img);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Set to views
        holder.nameTvNotification.setText(name);
        holder.notificationTv.setText(notification);
        holder.timeTvNotification.setText(pTime);

        // Click notification to open post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);  // Will get details of post using this id, its the id of the post clicked
                context.startActivity(intent);
            }
        });

        // Long press to show delete notification option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete notification?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Delete notification
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp)
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Deleted successfully
                                Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class HolderNotificaiton extends RecyclerView.ViewHolder{
        // Declare views
        ImageView avatarIvNotification;
        TextView nameTvNotification, notificationTv, timeTvNotification;

        public HolderNotificaiton(@NonNull View itemView) {
            super(itemView);

            // Init views
            avatarIvNotification = itemView.findViewById(R.id.avatarIvNotification);
            nameTvNotification = itemView.findViewById(R.id.nameTvNotification);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTvNotification = itemView.findViewById(R.id.timeTvNotification);
        }
    }
}
