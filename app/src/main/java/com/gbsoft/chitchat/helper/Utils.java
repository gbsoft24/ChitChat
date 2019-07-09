package com.gbsoft.chitchat.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chiranjeevi on 2/19/18.
 */

public class Utils {
    private static FirebaseDatabase mDatabase;
    private static FirebaseAuth mAuth;
    private static DatabaseReference mDatabaseRef;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static FirebaseAuth getmAuth(){
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    public static DatabaseReference getmDatabaseRef(){
        if(mDatabaseRef == null){
            mDatabaseRef = getDatabase().getReference();
        }
        return mDatabaseRef;
    }

}
