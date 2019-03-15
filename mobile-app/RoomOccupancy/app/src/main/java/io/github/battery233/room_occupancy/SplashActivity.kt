package io.github.battery233.room_occupancy

import android.content.Intent
import android.os.Bundle
import android.os.Handler

import androidx.appcompat.app.AppCompatActivity

/**
 * This is the entrance activity. The activity will least for 3 seconds
 * In this activity, files will be download and login status will be checked
 */

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }, 1200)
    }

    override fun onBackPressed() {
        // We don't want the splash screen to be interrupted
    }
}