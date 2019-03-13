package io.github.battery233.roomOccupancy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.battery233.roomOccupancy.data.loginTimeStamp;

public class SplashScreenActivity extends Activity {
    private static final int DURATION = 1000;
    FirebaseFirestore db;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //upload to fire store: login timestamp
        db = FirebaseFirestore.getInstance();
        CollectionReference data = db.collection("Login timestamp");
        loginTimeStamp loginTime = new loginTimeStamp(new SimpleDateFormat("yyyyMMddHHmmss", Locale.UK).format(new Date()));
        data.document("Last login").set(loginTime)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(SplashScreenActivity.this, "Send login record success!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(SplashScreenActivity.this, "Send login record fail!", Toast.LENGTH_SHORT).show());
        data.document("Login history").set(loginTime, SetOptions.merge());
        new Handler().postDelayed(() -> {
            final Intent intent = new Intent(this, ScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }, DURATION);
    }

    @Override
    public void onBackPressed() {
        // We don't want the splash screen to be interrupted
    }
}
