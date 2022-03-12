package com.example.fotoart_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    Button btnSignUp;
    User u;
    EditText firstName, lastName, email, password;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        u = new User();
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        btnSignUp=findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    u.setFirstName(firstName.getText().toString());
                    u.setLastName(lastName.getText().toString());
                    u.setEmail(email.getText().toString());
                    u.setPassword(password.getText().toString());

                    mAuth.createUserWithEmailAndPassword(u.getEmail(), u.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("OK", u);
                                Intent intent = new Intent();
                                intent.putExtra("userbundle", bundle);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private Boolean isValid(){
        if(firstName.getText().toString().isEmpty()){
            firstName.setError("Invalid!");
            return false;
        }
        if(lastName.getText().toString().isEmpty()){
            lastName.setError("Invalid!");
            return false;
        }
        if(email.getText().toString().isEmpty() || !email.getText().toString().matches(emailPattern)) {
            email.setError("Invalid!");
            return false;
        }
        if(password.getText().toString().isEmpty() || password.getText().toString().length() < 6){
            password.setError("Password too short!");
            return false;
        }
        return true;
    }

}