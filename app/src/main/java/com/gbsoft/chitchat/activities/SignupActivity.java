package com.gbsoft.chitchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gbsoft.chitchat.R;
import com.gbsoft.chitchat.helper.KeysAndConstants;
import com.gbsoft.chitchat.helper.Utils;
import com.gbsoft.chitchat.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SignupActivity extends AppCompatActivity {

    private EditText edtTxtFullName, edtTxtPassWrd, edtTxtEmail;
    private Button btnSignUp;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        edtTxtFullName = findViewById(R.id.edtTxtFullName);
        edtTxtEmail = findViewById(R.id.edtTxtEmail);
        edtTxtPassWrd = findViewById(R.id.edtTxtPassSignUp);

        firebaseAuth = Utils.getmAuth();
        final DatabaseReference databaseReference = Utils.getmDatabaseRef();


        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = edtTxtFullName.getText().toString().trim();
                final String passWrd = edtTxtPassWrd.getText().toString().trim();
                final String email = edtTxtEmail.getText().toString().trim();
                final String usrName = emailTrimmer(email);

                if(!TextUtils.isEmpty(fullName) && !TextUtils.isEmpty(passWrd) &&
                        !TextUtils.isEmpty(email) && !TextUtils.isEmpty(usrName)){
                    firebaseAuth.createUserWithEmailAndPassword(email, passWrd).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Log.d("sign up", "sign up has been successful");
                                String currUsrId = databaseReference.push().getKey();
                                FirebaseUser thisUser = firebaseAuth.getCurrentUser();
                                Toast.makeText(SignupActivity.this, "Welcome "+ fullName,
                                        Toast.LENGTH_SHORT).show();
                                User newUser = new User(usrName, fullName, passWrd, email, currUsrId);
                                databaseReference.child("users").child(currUsrId).setValue(newUser);

                                // code for logging in
                                if(!TextUtils.isEmpty(usrName) && !TextUtils.isEmpty(passWrd)){
                                    firebaseAuth.signInWithEmailAndPassword(usrName, passWrd).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(task.isSuccessful()){
                                                Log.d("sign in", "sign in has been successful");
                                                FirebaseUser thisUser = firebaseAuth.getCurrentUser();
                                                Toast.makeText(SignupActivity.this, "Welcome "+ usrName,
                                                        Toast.LENGTH_SHORT).show();
                                                databaseReference.child("onlineUsrs").child(thisUser.getUid()).setValue(thisUser.getEmail());
                                                Intent chatActIntent = new Intent(SignupActivity.this, ChatActivity.class);
                                                chatActIntent.putExtra("currentUser",usrName );
                                                startActivity(chatActIntent);
                                            }else{
                                                Log.d("sign in", task.getException().getMessage());
                                                Toast.makeText(SignupActivity.this, "Log In process has failed.", Toast.LENGTH_SHORT).show();
                                                Intent registerIntent = new Intent(SignupActivity.this, LoginActivity.class);
                                                startActivity(registerIntent);
                                            }

                                        }

                                    });
                                }
                                finish();
                            }else{
                                Log.d("sign up", task.getException().getMessage());
                                Toast.makeText(SignupActivity.this, "Sign Up process has failed.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });
    }

    public static String emailTrimmer(String email){
        String trimmedEmail = null;
        int indexOfAt = email.indexOf("@");
        trimmedEmail = email.substring(0, indexOfAt);
        return trimmedEmail;
    }
}
