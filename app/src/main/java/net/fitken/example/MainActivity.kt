package net.fitken.example

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import net.fitken.mlselfiecamera.selfie.SelfieActivity


class MainActivity : AppCompatActivity() {

    companion object {
        const val SELFIE_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_open_camera.setOnClickListener {
            startActivityForResult(Intent(this, SelfieActivity::class.java), SELFIE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELFIE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imagePath = data?.getStringExtra(SelfieActivity.KEY_IMAGE_PATH)
            Toast.makeText(
                this,
                imagePath,
                Toast.LENGTH_SHORT
            ).show()
            val bmImg = BitmapFactory.decodeFile(imagePath)
            profile_image.setImageBitmap(bmImg)
        }
    }
}
