package com.example.thetoiletfindingapp.ui.PublicToilet;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PublicToiletViewModel extends ViewModel {

    private MutableLiveData<HashMap<String,Object>> data;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("toilets").child("private");

    private HashMap<String, Object> dataNew = new HashMap<>();

    public PublicToiletViewModel() {

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataNew = (HashMap<String, Object>) dataSnapshot.getValue();
//                System.out.println("Data from Database => "+dataNew);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public HashMap<String, Object> getData() {
        return dataNew;
    }
}