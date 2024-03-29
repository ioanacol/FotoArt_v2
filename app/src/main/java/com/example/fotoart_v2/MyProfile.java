package com.example.fotoart_v2;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyProfile extends AppCompatActivity {

    TextView textName, textEmail, textMyPhotos, textSignOut, textChangePassword;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    UserDAO userDAO;
    ProgressDialog loadingBar;
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
        textChangePassword = findViewById(R.id.textChangePassword);

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
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MyProfile.this).toBundle();
                startActivity(i, bundle);
            }
        });

        textChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingBar = new ProgressDialog(MyProfile.this);
                loadingBar.setMessage("Sending Email....");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                FirebaseAuth.getInstance().sendPasswordResetEmail(firebaseUser.getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadingBar.dismiss();
                                    Toast.makeText(MyProfile.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                } else {
                                    loadingBar.dismiss();
                                    Toast.makeText(MyProfile.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MyProfile.this).toBundle();
                    startActivity(i, bundle);
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