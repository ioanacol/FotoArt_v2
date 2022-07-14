package com.example.fotoart_v2;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class LogInActivity extends AppCompatActivity {

    final int MainActivityRequestCode = 100;
    TextView textView, textEmail, textParola, textForgotPassword;
    Button btnLogIn;
    List<User> users = new ArrayList<>();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_activity);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        textView = findViewById(R.id.register);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivityForResult(intent, MainActivityRequestCode);
            }
        });

        textEmail = findViewById(R.id.email);
        textParola = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        btnLogIn = findViewById(R.id.btnLogIn);
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                perforLogin();
            }
        });

        textForgotPassword = findViewById(R.id.forgotPassword);
        textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(textEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LogInActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void perforLogin() {
        String email = textEmail.getText().toString();
        String password = textParola.getText().toString();

        if (!email.matches(emailPattern)) {
            textEmail.setError("Invalid!");
        } else if (password.isEmpty() || password.length() < 6) {
            textParola.setError("Invalid!");
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent i = new Intent(LogInActivity.this, EditPage.class);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(LogInActivity.this).toBundle();
                        startActivity(i, bundle);
                    } else {
                        Toast.makeText(LogInActivity.this, "Log in failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        textEmail = findViewById(R.id.email);
        textParola = findViewById(R.id.password);
        if (requestCode == MainActivityRequestCode) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Bundle bundle = data.getBundleExtra("userbundle");
                    User user = (bundle.getParcelable("OK"));
                    textEmail.setText(user.getEmail());
                    textParola.setText(user.getPassword());

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            userDAO = DatabaseAccess.getInstance(LogInActivity.this).getDatabase().utilizatorDAO();
                            userDAO.insertAll(user);
                            users = userDAO.getAll();
                        }
                    });

                    thread.start();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}