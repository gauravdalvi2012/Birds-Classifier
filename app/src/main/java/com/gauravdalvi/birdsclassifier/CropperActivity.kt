package com.gauravdalvi.birdsclassifier

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*

class CropperActivity : AppCompatActivity() {
    private lateinit var result : String
    private lateinit var fileUri : Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cropper)

        readIntent()

        val destinationUri: String = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()

        val options : UCrop.Options = UCrop.Options()

        UCrop.of(fileUri, Uri.fromFile(File(cacheDir, destinationUri)))
            .withOptions(options)
            .withAspectRatio(1F, 1F)
            .useSourceImageAspectRatio()
            .withMaxResultSize(224,224)
            .start(this)
    }

    private fun readIntent() {
        val intent = intent
        if (intent.extras != null) {
            result = intent.getStringExtra("DATA").toString()
            fileUri = Uri.parse(result)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri : Uri? = data?.let { UCrop.getOutput(it) }
            val returnIntent = Intent()
            returnIntent.putExtra("RESULT", resultUri.toString())
            setResult(-1, returnIntent)
            finish()
        }
        else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }
}