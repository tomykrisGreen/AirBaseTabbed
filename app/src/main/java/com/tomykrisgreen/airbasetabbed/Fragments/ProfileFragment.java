package com.tomykrisgreen.airbasetabbed.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterPost;
import com.tomykrisgreen.airbasetabbed.AddPostActivity;
import com.tomykrisgreen.airbasetabbed.AudioUpload.AudioActivity;
import com.tomykrisgreen.airbasetabbed.MainActivity;
import com.tomykrisgreen.airbasetabbed.Models.ModelPost;
import com.tomykrisgreen.airbasetabbed.R;
import com.tomykrisgreen.airbasetabbed.SettingsActivity;
import com.tomykrisgreen.airbasetabbed.VideoUpload.AddVideoActivity;
import com.tomykrisgreen.airbasetabbed.VideoUpload.VideoActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //Storage
    FirebaseStorage storage;
    StorageReference storageReference;

    // Path to store profile and cover pictures of users
    String storagePath = "Users_Profile_Cover_Imgs/";

    //Views from Xml
    ImageView avatarIv, coverIv;
    TextView nameTV, emailTV, phoneTV, videoViewPro, videoViewActivityPage, audioActivityPage, audioVoiceActivityPage;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView;

    ProgressDialog progressDialog;

    // Uri of picked image
    Uri image_uri;

    // For checking profile or cover picture
    String profileOrCoverPhoto;

    // Permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //Arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Init Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        // Init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Init views
        avatarIv = view.findViewById(R.id.avatarIV);
        coverIv = view.findViewById(R.id.coverIv);
        nameTV = view.findViewById(R.id.nameTV);
        emailTV = view.findViewById(R.id.emailTV);
        phoneTV = view.findViewById(R.id.phoneTV);
        fab = view.findViewById(R.id.fab);
        videoViewPro = view.findViewById(R.id.videoViewPro);
        videoViewActivityPage = view.findViewById(R.id.videoViewActivityPage);
        audioActivityPage = view.findViewById(R.id.audioActivityPage);
        postsRecyclerView = view.findViewById(R.id.recyclerView_posts);

        progressDialog = new ProgressDialog(getActivity());

        // Getting info of currently signed in user
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check until required data is found
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Get Data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    // Set Data
                    nameTV.setText(name);
                    emailTV.setText(email);
                    phoneTV.setText(phone);

                    try {
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarIv);
                    }

                    try {
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // FAB button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        videoViewPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddVideoActivity.class));
            }
        });

        videoViewActivityPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), VideoActivity.class));
            }
        });

        audioActivityPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AudioActivity.class));
            }
        });


        postList = new ArrayList<>();

        checkUserStatus();

        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {
        // LinearLayout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // Show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // Set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        // Init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        // Query to load posts
        /* Whenever user publishes a post, the uid of the user is also saved as info of post
        So we are retrieving posts having uid equal to uid of current user
         */
        Query query = ref.orderByChild("uid").equalTo(uid);
        // Get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    // Add to list
                    postList.add(myPosts);

                    // Adapter
                    adapterPost = new AdapterPost(getActivity(), postList);
                    // Set adapter to recyclerView
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMyPosts(String searchQuery) {
        // LinearLayout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // Show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // Set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        // Init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        // Query to load posts
        /* Whenever user publishes a post, the uid of the user is also saved as info of post
        So we are retrieving posts having uid equal to uid of current user
         */
        Query query = ref.orderByChild("uid").equalTo(uid);
        // Get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        // Add to list
                        postList.add(myPosts);
                    }

                    // Adapter
                    adapterPost = new AdapterPost(getActivity(), postList);
                    // Set adapter to recyclerView
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission() {
        // Check if storage permission is enabled or not
        // Return true if enable
        // else Return false
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        // Request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        // Check if camera permission is enabled or not
        // Return true if enable
        // else Return false
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        // Request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        /* Show dialog containing options
        1. Edit Profile Picture
        2. Edit Cover Photo
        3. Edit Name
        4. Edit Phone
        5. Change Password
         */

        // Options to show dialog
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone", "Change Password"};
        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set Title
        builder.setTitle("Choose an action");
        // Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Handle Dialog Items
                if (which == 0) {
                    // Edit profile click
                    progressDialog.setMessage("Updating profile picture...");
                    profileOrCoverPhoto = "image";  // Changing profile picture, make sure to assign same value
                    showImagePicDialog();
                } else if (which == 1) {
                    // Edit Cover Photo click
                    progressDialog.setMessage("Updating cover photo...");
                    profileOrCoverPhoto = "cover";   // Changing cover picture, make sure to assign same value
                    showImagePicDialog();
                } else if (which == 2) {
                    // Edit Name click
                    progressDialog.setMessage("Updating name...");
                    // Calling method and passing key 'name' as parameter to update its value in database
                    showNamePhoneUpdateDialog("name");
                } else if (which == 3) {
                    // Edit Phone click
                    progressDialog.setMessage("Updating phone number...");
                    // Calling method and passing key 'phone' as parameter to update its value in database
                    showNamePhoneUpdateDialog("phone");
                }else if (which == 4) {
                    // Edit Phone click
                    progressDialog.setMessage("Changing Password...");
                    // Calling method and passing key 'phone' as parameter to update its value in database
                    showChangePasswordDialog();
                }
            }
        });
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        // Password change dialog with custom layout having current password, new password and update button

        // Inflate layout for dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password, null);
        EditText passwordEt = view.findViewById(R.id.passwordEt);
        EditText newPasswordET = view.findViewById(R.id.newPasswordET);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);  // Set view to dialog

        AlertDialog dialog = builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate data
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordET.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter your current password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password length must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        progressDialog.show();

        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // Before changing password, re-authenticate user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully authenticated, begin update
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Password updaged
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Authentication failed, show reason
                progressDialog.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNamePhoneUpdateDialog(String key) {
        /* Parameter 'key' will contain value:
        either "name" which is key in users database which is used to update users name
        or "phone" which is key in users database which is used to update users phone
         */

        // Custom Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);  // e.g. Update name or phone
        // Set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // Add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key); // e.g. Edit name or Edit phone

        linearLayout.addView(editText);

        builder.setView(linearLayout);

        // Add buttons in dialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Input text from EditText
                String value = editText.getText().toString().trim();
                // Validate if user has entered a value or not
                if (!TextUtils.isEmpty(value)) {
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // If user edit his name, effect change on his posts too
                    if (key.equals("name")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        // Update name in current users comments on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments")) {
                                        String child1 = "" + snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds: snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();

    }

    private void showImagePicDialog() {

        // Options to show dialog
        String options[] = {"Camera", "Gallery"};
        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set Title
        builder.setTitle("Pick Image from");
        // Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Handle Dialog Items
                if (which == 0) {
                    // Camera click
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    // Gallery click
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }

            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // This method is called when user press Allow or Denied from request dialog
        // Handle permission cases
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                // Camera usage
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "Accept camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                // Gallery usage
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Accept storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // This method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Image is picked from gallery, get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Image is picked from camera, get uri of image

                uploadProfileCoverPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        progressDialog.show();
        // To add check, I'll add a string variable and assign it value "image" when user clicks
        // "Edit Profile Picture", and assign it value"cover" when user clicks "Edit Cover Photo"
        // Here: image is the key in each user containing url of users' profile picture
        // cover is the key in each user containing url of users' cover photo

        /* The parameter "image_uri" contains the uri of image picked from camera or gallery
        We will use UID of the currently signed in user as name of the image so there will be only one image profile
        and one image for cover picture for each user
         */

        // Path and name of image to be stored in firebase storage
        // e.g. Users_Profile_Cover_Imgs/image_e123123.jpg
        // e.g. Users_Profile_Cover_Imgs/cover_e456123.jpg
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image is uploaded to storage, now, get its url and store in users database
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                Uri downloadUri = uriTask.getResult();

                // Check if image is uploaded or not
                if (uriTask.isSuccessful()) {
                    // Image uploaded

                    // Add/update url in users' database
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profileOrCoverPhoto, downloadUri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // url in database of user is added successfully
                            // dismiss progressDialog
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Image updated", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Error updating image", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // If user edit his name, effect change on his posts too
                    if (profileOrCoverPhoto.equals("image")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        // Update user image in current users comment on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments")) {
                                        String child1 = "" + snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds: snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    // Error occurred
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void pickFromCamera() {
        // Intent of getting image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        // Put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        // Pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }


    private void checkUserStatus() {
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            // Set email of logged in user
            //mProfileTV.setText(user.getEmail());
            uid = user.getUid();
        } else {
            // User not signed in, go to Main Activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);  // To show menu in fragment
        super.onCreate(savedInstanceState);
    }

    // Inflate options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        // Hide some options
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        // SearchView to search user specific posts
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Called when user presses the search button
                if (!TextUtils.isEmpty(s)) {
                    // Search
                    searchMyPosts(s);
                } else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Called when user type any letter
                if (!TextUtils.isEmpty(s)) {
                    // Search
                    searchMyPosts(s);
                } else {
                    loadMyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Handle menu items clicks

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();

            checkUserStatus();
        }

        else if (id == R.id.action_shop) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        else if (id == R.id.action_settings){
            // Go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}