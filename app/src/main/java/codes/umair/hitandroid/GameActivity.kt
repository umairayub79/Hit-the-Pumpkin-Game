package codes.umair.hitandroid

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import kotlinx.android.synthetic.main.activity_game.*
import spencerstudios.com.jetdblib.JetDB
import umairayub.madialog.MaDialog
import umairayub.madialog.MaDialogListener
import java.util.*


class GameActivity : AppCompatActivity() {

    private lateinit var r: Random
    private var score: Int? = 0
    private var fps: Long? = 1000
    private var left: Int? = 5
    private var templeft: Int? = 0
    private var which: Int? = 0
    private var last: Int? = 0
    private var animationDrawable: AnimationDrawable? = null
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var handler: Handler
    private lateinit var handler1: Handler
    private lateinit var runnable: Runnable
    private lateinit var runnable1: Runnable
    private var isRunnin = false
    private var isSoundON = true
    private var isVibrationON = true
    private lateinit var mp: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        r = Random()

        MobileAds.initialize(this) {}
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mp = MediaPlayer.create(this, R.raw.click)
        isSoundON = JetDB.getBoolean(this, "sound", true)
        isVibrationON = JetDB.getBoolean(this, "vibration", true)
        hideImageViews()

        imageView.setOnClickListener {
            update(imageView)
        }
        imageView2.setOnClickListener {
            update(imageView2)
        }
        imageView3.setOnClickListener {
            update(imageView3)
        }
        imageView4.setOnClickListener {
            update(imageView4)
        }
        imageView5.setOnClickListener {
            update(imageView5)
        }
        imageView6.setOnClickListener {
            update(imageView6)
        }
        button.setOnClickListener {
            left = 5
            score = 0
            tv_lives.text = "Lives : $left"
            tv_score.text = "Score : $score"
            handler = Handler()
            runnable = Runnable {
                theGameActions()
                isRunnin = true
            }
            handler.postDelayed(runnable, 1000)
            button.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("ResourceType")
    private fun theGameActions() {
        when {
            score!! < 100 -> {
                fps = 1000
            }
            score!! < 200 -> {
                fps = 900
            }
            score!! < 400 -> {
                fps = 800
            }
            score!! < 600 -> {
                fps = 750
            }
            score!! < 800 -> {
                fps = 700
            }
            score!! < 1000 -> {
                fps = 650
            }
            score!! < 1500 -> {
                fps = 600
            }

        }
        try {
            animationDrawable =
                ContextCompat.getDrawable(this@GameActivity, R.anim.anim) as AnimationDrawable
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
        }

        do {
            which = r.nextInt(6) + 1
        } while (last == which)
        last = which

        when (which) {
            1 -> {
                imageView.setImageDrawable(animationDrawable)
                imageView.visibility = View.VISIBLE
                imageView.isEnabled = true
            }
            2 -> {
                imageView2.setImageDrawable(animationDrawable)
                imageView2.visibility = View.VISIBLE
                imageView2.isEnabled = true
            }
            3 -> {
                imageView3.setImageDrawable(animationDrawable)
                imageView3.visibility = View.VISIBLE
                imageView3.isEnabled = true
            }
            4 -> {
                imageView4.setImageDrawable(animationDrawable)
                imageView4.visibility = View.VISIBLE
                imageView4.isEnabled = true
            }
            5 -> {
                imageView5.setImageDrawable(animationDrawable)
                imageView5.visibility = View.VISIBLE
                imageView5.isEnabled = true
            }
            6 -> {
                imageView6.setImageDrawable(animationDrawable)
                imageView6.visibility = View.VISIBLE
                imageView6.isEnabled = true
            }
        }

        animationDrawable?.start()

        handler1 = Handler()
        runnable1 = Runnable {
            isRunnin = true
            hideImageViews()

            when (templeft) {
                0 -> {
                    left = left?.minus(1)
                    tv_lives.text = "Lives : $left"
                }
                1 -> {
                    templeft = 0
                }
            }

            if (left!! < 1) {
                gameOver()
            } else {
                theGameActions()
            }
        }
        handler1.postDelayed(runnable1, fps!!)
    }


    private fun update(view: ImageView) {
        if (isSoundON) {
            playAudioEffect()
        }
        if (isVibrationON) {
            window.decorView.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }

        templeft = 1
        view.setImageResource(R.drawable.hit)
        score = score?.plus(10)
        tv_score.text = "Score : $score"
        view.isEnabled = false
    }

    override fun onBackPressed() {
        val highscore = JetDB.getInt(this, "highscore", 0)
        if (score!! > highscore) {
            JetDB.putInt(this, score!!, "highscore")
            GoogleSignIn.getLastSignedInAccount(this)?.let {
                Games.getLeaderboardsClient(this, it)
                    .submitScore(getString(R.string.leaderboard_score), score!!.toLong())
            }
        }
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.")
        }

        if (isRunnin) {
            handler.removeCallbacks(runnable)
            handler1.removeCallbacks(runnable1)
        }
        super.onBackPressed()
    }

    override fun onPause() {
        if (isRunnin) {
            handler.removeCallbacks(runnable)
            handler1.removeCallbacks(runnable1)
        }

        hideImageViews()

        button.visibility = View.VISIBLE
        val highscore = JetDB.getInt(this, "highscore", 0)
        if (score!! > highscore) {
            JetDB.putInt(this, score!!, "highscore")
        }
        super.onPause()
    }


    private fun gameOver() {

        hideImageViews()

        val highscore = JetDB.getInt(this, "highscore", 0)
        if (score!! > highscore) {
            JetDB.putInt(this, score!!, "highscore")
            GoogleSignIn.getLastSignedInAccount(this)?.let {
                Games.getLeaderboardsClient(this, it)
                    .submitScore(getString(R.string.leaderboard_score), score!!.toLong())
            }
        }
        MaDialog.Builder(this)
            .setCustomFont(R.font.bubblegum_sans)
            .setTitle("Game Over")
            .setMessage("Score $score")
            .setPositiveButtonText("OK")
            .setPositiveButtonListener(object : MaDialogListener {
                override fun onClick() {
                    left = 5
                    score = 0
                    tv_lives.text = "Lives : $left"
                    tv_score.text = "Score : $score"
                }
            })
            .setCancelableOnOutsideTouch(false)
            .build()
        button.visibility = View.VISIBLE
    }

    private fun hideImageViews() {
        imageView.visibility = View.INVISIBLE
        imageView2.visibility = View.INVISIBLE
        imageView3.visibility = View.INVISIBLE
        imageView4.visibility = View.INVISIBLE
        imageView5.visibility = View.INVISIBLE
        imageView6.visibility = View.INVISIBLE

        imageView.isEnabled = false
        imageView2.isEnabled = false
        imageView3.isEnabled = false
        imageView4.isEnabled = false
        imageView5.isEnabled = false
        imageView6.isEnabled = false
    }

    private fun playAudioEffect() {
        try {
            if (mp.isPlaying) {
                mp.stop()
                mp.release()
                mp = MediaPlayer.create(this, R.raw.click)
            }
            mp.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
