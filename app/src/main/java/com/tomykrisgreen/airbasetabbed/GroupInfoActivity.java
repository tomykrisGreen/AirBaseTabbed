package com.tomykrisgreen.airbasetabbed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterParticipantAdd;
import com.tomykrisgreen.airbasetabbed.Models.ModelUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {
    private String groupId;
    private String myGroupRole = "";

    private FirebaseAuth firebaseAuth;

    private ActionBar actionBar;

    private ImageView groupIconIv_Group_Info;
    private TextView description_Group_Info, createdByTv_Group_Info, editGroupTv, addParticipantTv, leaveGroupTv, participantTv;
    private RecyclerView participantRV;

    private ArrayList<ModelUser> userList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        firebaseAuth = FirebaseAuth.getInstance();

        // Actionbar and its properties
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        groupIconIv_Group_Info = findViewById(R.id.groupIconIv_Group_Info);
        description_Group_Info = findViewById(R.id.description_Group_Info);
        createdByTv_Group_Info = findViewById(R.id.createdByTv_Group_Info);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantTv = findViewById(R.id.participantTv);
        participantRV = findViewById(R.id.participantRV);

        groupId = getIntent().getStringExtra("groupId");

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user is participant/admin: Leave group
                // If user is creator: Delete group

                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";
                if (myGroupRole.equals("creator")) {
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure you want to delete group permanently?";
                    positiveButtonTitle = "DELETE";
                } else {
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure you want to leave group?";
                    positiveButtonTitle = "LEAVE";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (myGroupRole.equals("creator")){
                                    // I'm creator of group, delete group
                                    deleteGroup();
                                }
                                else {
                                    // I'm participant/admin, leave group
                                    leaveGroup();
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        loadGroupInfo();
        loadGroupRole();
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Left group successfully
                        Toast.makeText(GroupInfoActivity.this, "Left Group successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed leaving group
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Group deleted
                        Toast.makeText(GroupInfoActivity.this, "Group successfully deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to delete group
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get group info
                    String groupId = "" + ds.child("groupId").getValue();
                    String groupTitle = "" + ds.child("groupTitle").getValue();
                    String groupDescription = "" + ds.child("groupDescription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String createdBy = "" + ds.child("createdBy").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();

                    // Convert timeStamp to dd/mm/yyyy hh:mm am/pm
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyy hh:mm aa", cal).toString();

                    loadCreatorInfo(dateTime, createdBy);

                    // Set group info
                    actionBar.setTitle(groupTitle);
                    description_Group_Info.setText(groupDescription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(groupIconIv_Group_Info);
                    } catch (Exception e) {
                        groupIconIv_Group_Info.setImageResource(R.drawable.ic_group_primary);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCreatorInfo(String dateTime, String createdBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    createdByTv_Group_Info.setText("Created by:  " + name + "   on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + " (" + myGroupRole + ")");

                            if (myGroupRole.equals("participant")) {
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Leave Group");
                            } else if (myGroupRole.equals("admin")) {
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Leave Group");
                            } else if (myGroupRole.equals("creator")) {
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Delete Group");
                            }
                        }

                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get uid from Group > Participants
                    String uid = "" + ds.child("uid").getValue();

                    // Get info of user using uid we got above
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelUser modelUser = ds.getValue(ModelUser.class);

                                userList.add(modelUser);
                            }
                            // Adapter
                            adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this, userList, groupId, myGroupRole);
                            // Set adapter
                            participantRV.setAdapter(adapterParticipantAdd);
                            participantTv.setText("Participants (" + userList.size() + ")");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}