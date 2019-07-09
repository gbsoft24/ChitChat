package com.gbsoft.chitchat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister;
    private EditText edtTxtUsrName, edtTxtPass;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogIn);
        btnRegister = findViewById(R.id.btnRegister);

        edtTxtUsrName = findViewById(R.id.edtTxtUsrNameSignIn);
        edtTxtPass = findViewById(R.id.edtTxtPassSignIn);

        firebaseAuth = Utils.getmAuth();
        databaseReference = Utils.getmDatabaseRef();

        if(!ChatActivity.checkInternetConnection(this)){
            ChatActivity.showAlertDialog(this);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = edtTxtUsrName.getText().toString().trim();
                String passWord = edtTxtPass.getText().toString().trim();
                if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(passWord)){
                    firebaseAuth.signInWithEmailAndPassword(userName, passWord).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Log.d("sign in", "sign in has been successful");
                                FirebaseUser thisUser = firebaseAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Welcome "+ userName,
                                        Toast.LENGTH_SHORT).show();
                                databaseReference.child("onlineUsrs").child(thisUser.getUid()).setValue(thisUser.getEmail());
                                Intent chatActIntent = new Intent(LoginActivity.this, ChatActivity.class);
                                chatActIntent.putExtra("currentUser",userName );
                                startActivity(chatActIntent);
                                finish();
                            }else{
                                Log.d("sign in", task.getException().getMessage());
                                Toast.makeText(LoginActivity.this, "Log In process has failed.", Toast.LENGTH_SHORT).show();
                            }

                        }

                    });
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(signUp);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = Utils.getmAuth().getCurrentUser();
        if(currentUser != null ){
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//            sharedPreferences.edit().putBoolean(KeysAndConstants.KEY_IS_USR_LOGGED_IN, true)
//                    .putString(KeysAndConstants.KEY_USR_EMAIL, currentUser.getEmail()).apply();

            Intent chatActIntent = new Intent(this, ChatActivity.class);
            chatActIntent.putExtra("currentUser", currentUser.getEmail());
            startActivity(chatActIntent);
        }
    }
}
