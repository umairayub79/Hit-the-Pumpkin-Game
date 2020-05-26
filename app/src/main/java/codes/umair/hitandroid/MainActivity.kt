package codes.umair.hitandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import spencerstudios.com.jetdblib.JetDB
import umairayub.madialog.MaDialog
import umairayub.madialog.MaDialogListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_play.setOnClickListener {
            val intent = Intent(this,GameActivity::class.java)
            startActivity(intent)
        }
        tv_highscore.setOnClickListener {
            val highscore = JetDB.getInt(this,"highscore",0)

            MaDialog.Builder(this)
                .setCustomFont(R.font.bubblegum_sans)
                .setTitle("High Score")
                .setMessage("Your highest score is $highscore")
                .setPositiveButtonText("OK")
                .setPositiveButtonListener(object : MaDialogListener{
                    override fun onClick() {

                    }
                })
                .build()
        }
    }
}
