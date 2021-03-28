package com.example.thetoiletfindingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

public class personalInfo extends AppCompatActivity {

    RadioButton genderradioButton;
    RadioGroup radioGroup;
    Button save;
    String name,gender;
    EditText fullname;
    public static final String MyPREFERENCES = "USERDATA" ;
    public static final String MyPREFERENCESLOGIN = "USERLOGINDATA" ;
    SharedPreferences sharedpreferences,sharedpreferences1;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        radioGroup=(RadioGroup)findViewById(R.id.radioGroup);
        save = findViewById(R.id.save);
        fullname = (EditText) findViewById(R.id.editTextName);



        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        sharedpreferences1 = getSharedPreferences(MyPREFERENCESLOGIN, Context.MODE_PRIVATE);



        String data = sharedpreferences.getString("name",null);


        if (data != null){
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = fullname.getText().toString();
                int selectedId = radioGroup.getCheckedRadioButtonId();
                genderradioButton = (RadioButton) findViewById(selectedId);
                if(selectedId==-1){
                    Toast.makeText(personalInfo.this,"Please select Gender", Toast.LENGTH_SHORT).show();
                }
                else{
                    gender = genderradioButton.getText().toString();
                }
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("name",name);
                editor.putString("gender",gender);
                editor.commit();

                DatabaseReference myRef = database.getReference("users").child(name);
                User user = new User(name, gender,sharedpreferences1.getString("uid",null));

                myRef.setValue(user);

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}

@IgnoreExtraProperties
class User {

    public String name;
    public String gender;
    public String uid;

    public User() {
    }

    public User(String name, String gender, String uid) {
        this.name = name;
        this.gender = gender;
        this.uid = uid;
    }

}
