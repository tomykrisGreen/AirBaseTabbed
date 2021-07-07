package com.tomykrisgreen.airbasetabbed.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomykrisgreen.airbasetabbed.Adapters.AdapterNotification;
import com.tomykrisgreen.airbasetabbed.Models.ModelNotification;
import com.tomykrisgreen.airbasetabbed.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NotificationsFragment extends Fragment {
    RecyclerView notificationsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelNotification> notificationsList;
    private AdapterNotification adapterNotification;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        notificationsRv = view.findViewById(R.id.notificationsRv);

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {
        notificationsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            // Get data
                            ModelNotification model = ds.getValue(ModelNotification.class);

                            //Add to list
                            notificationsList.add(model);
                        }
                        //Adapter
                        adapterNotification = new AdapterNotification(getActivity(), notificationsList);
                        // Set adapter to recycler view
                        notificationsRv.setAdapter(adapterNotification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}