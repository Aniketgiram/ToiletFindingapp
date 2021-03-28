package com.example.thetoiletfindingapp.ui.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.thetoiletfindingapp.R;
import com.example.thetoiletfindingapp.aboutus;
import com.example.thetoiletfindingapp.loginActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    Button signOut;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "USERDATA" ;
    TextView fullname,gender;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        Button fed = root.findViewById(R.id.feedbackBtn);
        signOut = root.findViewById(R.id.signout);
        fullname = root.findViewById(R.id.fullname);
        gender = root.findViewById(R.id.gender);
        Button aboutusbtn = root.findViewById(R.id.aboutus);
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        String name = sharedpreferences.getString("name",null);
        String Gender = sharedpreferences.getString("gender",null);

        fullname.setText("Name : "+name);
        gender.setText("Gender : "+Gender);

        fed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialouge();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(getContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent i = new Intent(getContext(), loginActivity.class);
                                startActivity(i);
                            }
                        });
            }
        });

        aboutusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), aboutus.class);
                startActivity(i);
            }
        });
        return root;
    }

    private void openDialouge() {
        final String nameData = sharedpreferences.getString("name",null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        View v = getLayoutInflater().inflate(R.layout.dialouge_layout,null);

        final EditText feed = v.findViewById(R.id.feedbackText);
        Button feedbutton = v.findViewById(R.id.feedbtn);

        alert.setView(v);

        final AlertDialog alertDialog = alert.create();

        feedbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = feed.getText().toString();
                DatabaseReference myRef = database.getReference("users").child(nameData);
                Map<String, Object> feedback = new HashMap<>();
                feedback.put("feedback", data);
                myRef.updateChildren(feedback);
                alertDialog.dismiss();
                Toast.makeText(getContext(),"Feedback Saved Thank you",Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();

    }
}

