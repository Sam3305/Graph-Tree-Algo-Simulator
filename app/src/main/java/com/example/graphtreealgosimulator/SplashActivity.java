package com.example.graphtreealgosimulator;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Wait 2 seconds, then launch MainActivity
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new android.content.Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 3500);
    }
}