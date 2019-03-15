package io.github.battery233.room_occupancy

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_splash_screen.*

/**
 * This is the entrance splash activity. The activity will least for 2 seconds
 */

class SplashActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val typeFace: Typeface? = ResourcesCompat.getFont(this.applicationContext, R.font.lilitaone_regular)
        splashAppName.typeface = typeFace
        splashAppName.text = "Room Occupancy"
        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }, 2000)
    }

    override fun onBackPressed() {
        // We don't want the splash screen to be interrupted
    }
}