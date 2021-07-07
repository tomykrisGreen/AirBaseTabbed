package com.tomykrisgreen.airbasetabbed.Adapters;

import android.content.Context;
import android.content.DialogInterface;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Models.ModelComment;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;
    String myUid, postId;

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        // Get the data
        String uid = commentList.get(i).getUid();
        String name = commentList.get(i).getuName();
        String email = commentList.get(i).getuEmail();
        String image = commentList.get(i).getuDp();
        String cid = commentList.get(i).getcId();
        String comment = commentList.get(i).getComment();
        String timestamp = commentList.get(i).getTimestamp();

        // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyy hh:mm aa", calender).toString();

        // Set data
        holder.mameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        // set user dp
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_profile_white).into(holder.avatarIv);
        }
        catch (Exception e){

        }

        // Comment click listener  (To Delete comment)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if comment clicked is from current signed in user or not
                if (myUid.equals(uid)){
                    // My comment
                    // Show delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure you want to delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    // Show dialog
                    builder.create().show();
                }
                else {
                    // Not my comment
                    Toast.makeText(context, "Can't delete other's comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue(); // It will delete the comment

        // Update comment count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = ""+ snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        // Declare views from row_comments.xml
        ImageView avatarIv;
        TextView mameTv, commentTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            mameTv = itemView.findViewById(R.id.nameTvC);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTvC);
        }
    }
}
