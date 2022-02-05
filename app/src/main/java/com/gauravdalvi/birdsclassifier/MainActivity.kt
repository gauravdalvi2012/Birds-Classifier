package com.gauravdalvi.birdsclassifier

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.gauravdalvi.birdsclassifier.databinding.ActivityMainBinding
import com.gauravdalvi.birdsclassifier.ml.BirdsModel
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private lateinit var image : ImageView
    private lateinit var selectImage : Button
    private lateinit var resultTv : TextView
    private lateinit var moreInfo : TextView

    private lateinit var getImageFromGallery : ActivityResultLauncher<String>
    private lateinit var getCroppedImage : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        image = binding.ImageView
        selectImage = binding.button
        resultTv = binding.textView
        moreInfo = binding.moreInfo

        getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) {uri : Uri? ->
            val intent = Intent(this, CropperActivity::class.java)
            intent.putExtra("DATA", uri.toString())
            getCroppedImage.launch(intent)
        }

        getCroppedImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = Uri.parse(result.data?.getStringExtra("RESULT"))
                val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                image.setImageBitmap(bitmap)
                outputGenerator(bitmap)
            }
            else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        selectImage.setOnClickListener {
            getImageFromGallery.launch("image/*")
        }

        moreInfo.setOnClickListener {
            if (resultTv.text != "Result" && resultTv.text != "None") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + resultTv.text))
                startActivity(intent)
            }
        }
    }

    private fun outputGenerator(bitmap: Bitmap) {
        val model = BirdsModel.newInstance(this)
        val image = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(image)
        val probability = outputs.probabilityAsCategoryList

        var maxIndex = 0
        var max = probability[0]

        for ((index, value) in probability.withIndex()) {
            if (max.score < value.score) {
                max = value
                maxIndex = index
            }
        }

        resultTv.text = probability[maxIndex].label

        model.close()
    }
}