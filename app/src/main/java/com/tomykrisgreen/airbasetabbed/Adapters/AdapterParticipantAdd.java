package com.tomykrisgreen.airbasetabbed.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {
    private Context context;
    private ArrayList<ModelUser> userList;
    private String groupId, myGroupRole;  //  Creator/Admin/Participant

    public AdapterParticipantAdd(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add, parent,false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        // Get data
        ModelUser modelUser = userList.get(position);
        String name = modelUser.getName();
        String email = modelUser.getEmail();
        String image = modelUser.getImage();
        String uid = modelUser.getUid();

        // Set data
        holder.nameTV_Participant.setText(name);
        holder.emailTV_Participant.setText(email);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv_Participant);
        }
        catch (Exception e){
            holder.avatarIv_Participant.setImageResource(R.drawable.ic_default_img);
        }

        checkIfAlreadyExist(modelUser, holder);

        // Handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Check if user is already added or not
                * If added:  show remove participant/make-admin/remove-admin option (Admin will not be able to change role of creator)
                * If not added:  show add participant option
                 */

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    // User exits/participant
                                    String hisPreviousRole = ""+snapshot.child("role").getValue();

                                    // Options to display in dialog
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose option");
                                    if (myGroupRole.equals("creator")){
                                        if (hisPreviousRole.equals("admin")){
                                            // I'm creator, he is admin
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    // Handle item click
                                                    if (which == 0){
                                                        // Remove admin clicked
                                                        removeAdmin(modelUser);
                                                    }
                                                    else {
                                                        // Remove user clicked
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            // I'm creator, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    // Handle item click
                                                    if (which == 0){
                                                        // Remove admin clicked
                                                        makeAdmin(modelUser);
                                                    }
                                                    else {
                                                        // Remove user clicked
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if (myGroupRole.equals("admin")){
                                        if (hisPreviousRole.equals("creator")){
                                            // I'm admin, he's creator
                                            Toast.makeText(context, "Creator of Group", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (hisPreviousRole.equals("admin")){
                                            // I'm admin, he's admin too
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    // Handle item click
                                                    if (which == 0){
                                                        // Remove admin clicked
                                                        removeAdmin(modelUser);
                                                    }
                                                    else {
                                                        // Remove user clicked
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            // I'm Admin, he's Participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    // Handle item click
                                                    if (which == 0){
                                                        // Remove admin clicked
                                                        makeAdmin(modelUser);
                                                    }
                                                    else {
                                                        // Remove user clicked
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                else {
                                    // User doesn't exits/not participant: Add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add user to group?")
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // Add user
                                                    addParticipant(modelUser);
                                                }
                                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });
    }

    private void addParticipant(ModelUser modelUser) {
        // Setup user data - add user in group
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", modelUser.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);

        // Add that user in group>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Added successfully
                        Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed adding user to group
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(ModelUser modelUser) {
        // Setup user data  - change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");  // Roles are:  participant/admin/creator

        // Update role in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make Admin
                        Toast.makeText(context, "The user is not Admin", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed making admin
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeParticipant(ModelUser modelUser) {
        // Remove participant from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Removed successfully
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed removing
            }
        });
    }

    private void removeAdmin(ModelUser modelUser) {
        // Setup user data - remove adming - just change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");  // Roles are:  participant/admin/creator

        // Update role in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make Admin
                        Toast.makeText(context, "The user is no longer Admin", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed making admin
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExist(ModelUser modelUser, HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            // Already exist
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.statusTV_Participant.setText(hisRole);
                        }
                        else {
                            // Doesn't exist
                            holder.statusTV_Participant.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{
        private ImageView avatarIv_Participant;
        private TextView nameTV_Participant, emailTV_Participant, statusTV_Participant;

        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);

            avatarIv_Participant = itemView.findViewById(R.id.avatarIv_Participant);
            nameTV_Participant = itemView.findViewById(R.id.nameTV_Participant);
            emailTV_Participant = itemView.findViewById(R.id.emailTV_Participant);
            statusTV_Participant = itemView.findViewById(R.id.statusTV_Participant);
        }
    }
}
