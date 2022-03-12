package com.example.fotoart_v2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirstActivity extends AppCompatActivity {

    ImageView fundal;
    LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        fundal = findViewById(R.id.fundal);
        lottieAnimationView = findViewById(R.id.lottie);

        fundal.animate().translationX(-3000).setDuration(3000).setStartDelay(4000);
        lottieAnimationView.animate().translationX(-3000).setDuration(3000).setStartDelay(4000);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null){
                    Intent i = new Intent(FirstActivity.this, LogInActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(FirstActivity.this, EditPage.class);
                    startActivity(i);
                    finish();
                }
            }
        }, 3500);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}