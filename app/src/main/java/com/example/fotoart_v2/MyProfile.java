package com.example.fotoart_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyProfile extends AppCompatActivity {

    TextView textName, textEmail, textMyPhotos, textSignOut;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    UserDAO userDAO;
    List<User> listDB = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile_activity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        textName = findViewById(R.id.textName);
        textEmail = findViewById(R.id.textEmailAddress);
        textMyPhotos = findViewById(R.id.textMyPhotos);
        textSignOut = findViewById(R.id.textSignOut);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        textEmail.setText(firebaseUser.getEmail());

        readNameDB();

        textSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        textMyPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MyProfile.this, MyPhotos.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void signOut() {
        FirebaseAuth firebaseAuth;
        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent i = new Intent(MyProfile.this, LogInActivity.class);
                    startActivity(i);
                } else {
                }
            }
        };

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);
        firebaseAuth.signOut();
    }

    private void readNameDB() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                userDAO = DatabaseAccess.getInstance(MyProfile.this).getDatabase().utilizatorDAO();
                listDB = userDAO.getAll();
                for (int i = 0; i < listDB.size(); i++) {
                    if (firebaseUser.getEmail().equals(listDB.get(i).getEmail())) {
                        String name = listDB.get(i).getFirstName() + " " + listDB.get(i).getLastName();
                        textName.setText(name);
                    }
                }
            }
        });

        thread.start();
    }
}