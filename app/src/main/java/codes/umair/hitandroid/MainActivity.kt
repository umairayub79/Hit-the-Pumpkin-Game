package codes.umair.hitandroid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games
import kotlinx.android.synthetic.main.activity_main.*
import spencerstudios.com.jetdblib.JetDB
import umairayub.madialog.MaDialog
import umairayub.madialog.MaDialogListener


class MainActivity : AppCompatActivity() {

    // Request code used to invoke sign in user interactions.
    private val RC_SIGN_IN = 9001
    private val RC_LEADERBOARD_UI = 9004
    private var isSoundON = true
    private var isVibrationON = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        isVibrationON = JetDB.getBoolean(this, "vibration", true)
        isSoundON = JetDB.getBoolean(this, "sound", true)

        if (isVibrationON) {
            vibration_btn.text = getString(R.string.vibration_on)
        } else {
            vibration_btn.text = getString(R.string.vibration_off)
        }
        if (isSoundON) {
            sound_btn.text = getString(R.string.sound_on)
        } else {
            sound_btn.text = getString(R.string.sound_off)
        }

        tv_play.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        tv_highscore.setOnClickListener {
            val highscore = JetDB.getInt(this, "highscore", 0)

            MaDialog.Builder(this)
                .setCustomFont(R.font.bubblegum_sans)
                .setTitle(getString(R.string.high_score))
                .setMessage("Your highest score is $highscore")
                .setPositiveButtonText("OK")
                .setPositiveButtonListener(object : MaDialogListener {
                    override fun onClick() {}
                })
                .build()
        }

        tv_leaderboard.setOnClickListener {
            showLeaderboard()
        }

        sound_btn.setOnClickListener {
            if (isSoundON) {
                isSoundON = false
                JetDB.putBoolean(this, isSoundON, "sound")
                sound_btn.text = getString(R.string.sound_off)
            } else {
                isSoundON = true
                JetDB.putBoolean(this, isSoundON, "sound")
                sound_btn.text = getString(R.string.sound_on)
            }
        }
        vibration_btn.setOnClickListener {
            if (isVibrationON) {
                isVibrationON = false
                JetDB.putBoolean(this, isVibrationON, "vibration")
                vibration_btn.text = getString(R.string.vibration_off)
            } else {
                isVibrationON = true
                JetDB.putBoolean(this, isVibrationON, "vibration")
                vibration_btn.text = getString(R.string.vibration_on)
            }
        }

        tv_help.setOnClickListener {
            MaDialog.Builder(this)
                .setCustomFont(R.font.bubblegum_sans)
                .setTitle(getString(R.string.help))
                .setMessage(getString(R.string.helpMessage))
                .setPositiveButtonText("OK")
                .setPositiveButtonListener(object : MaDialogListener {
                    override fun onClick() {}
                })
                .build()
        }

        signin_btn.setOnClickListener {
            startSignInIntent()
        }
        signout_btn.setOnClickListener {
            signOut()
        }
    }


    override fun onResume() {
        signInSilently()
        super.onResume()
    }

    private fun startSignInIntent() {
        val signInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        )
        val intent = signInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                // The signed in account is stored in the result.
                signin_btn.visibility = View.GONE
                signout_btn.visibility = View.VISIBLE
            } else {
                var message = result.status.statusMessage
                if (message == null || message.isEmpty()) {
                    signin_btn.visibility = View.VISIBLE
                    signout_btn.visibility = View.GONE
                    Toast.makeText(this, "Error signing in", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun signInSilently() {
        val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)) {
            // Already signed in.
            // The signed in account is stored in the 'account' variable.
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            val signInClient = GoogleSignIn.getClient(this, signInOptions)
            signInClient
                .silentSignIn()
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        signin_btn.visibility = View.GONE
                        signout_btn.visibility = View.VISIBLE
                    } else {
                        signin_btn.visibility = View.VISIBLE
                        signout_btn.visibility = View.GONE
                        // Player will need to sign-in explicitly using via UI.
                        // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                        // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                        // Interactive Sign-in.
                    }
                }
        }
    }

    private fun signOut() {
        val signInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        )
        signInClient.signOut().addOnCompleteListener(
            this
        ) {
            // at this point, the user is signed out.
            signin_btn.visibility = View.VISIBLE
            signout_btn.visibility = View.GONE
        }
    }

    private fun showLeaderboard() {
        if (isSignedIn()) {
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .getLeaderboardIntent(getString(R.string.leaderboard_score))
                .addOnSuccessListener { intent ->
                    startActivityForResult(
                        intent,
                        RC_LEADERBOARD_UI
                    )
                }
        } else {
            Toast.makeText(this, "You need to sign-in first", Toast.LENGTH_LONG).show()
        }
    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }
}