package com.dicoding.picodiploma.loginwithanimation.ui.story

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityAddStoriesBinding
import com.dicoding.picodiploma.loginwithanimation.ui.ViewModelFactory
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStories : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoriesBinding
    private lateinit var viewModel: StoriesDetailViewModel
    private lateinit var storiesViewModel: StoriesViewModel
    private var selectedImageUri: Uri? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            showLoading(true)
            Glide.with(this)
                .load(it)
                .into(binding.imageView)
            showLoading(false)
            selectedImageUri = uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory(this)
        storiesViewModel = ViewModelProvider(this, factory)[StoriesViewModel::class.java]


        storiesViewModel.uploadSuccess.observe(this) { response ->
            if (response != null && !response.error) {
                //To Do
            }
        }

        storiesViewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }


        storiesViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        binding.btnGallery.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            startCamera()
        }

        binding.btnUpload.setOnClickListener {
            saveStory()
        }

        checkPermissions()

    }

    private fun checkPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Get
            } else {
                // Abort, do
                Toast.makeText(
                    this,
                    "Permission is required to use camera and gallery",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showImage() {
        selectedImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.imageView.setImageURI(it)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            selectedImageUri = null
        }
    }

    private fun saveStory() {
        val descriptionText = binding.descriptionEditText.text.toString()
        if (descriptionText.isBlank() || selectedImageUri == null) {
            showError("Descriptions and images cannot be empty")
            return

        }

        val file = getFileFromUri(selectedImageUri)
        if (file == null) {
            showError("Invalid image")
            return
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
        val description = descriptionText.toRequestBody("text/plain".toMediaTypeOrNull())

        lifecycleScope.launch {
            storiesViewModel.uploadStories(body, description)
        }

        storiesViewModel.uploadSuccess.observe(this) { response ->
            if (response != null && !response.error) {
                Toast.makeText(
                    this@AddStories,
                    "Story successfully uploaded",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@AddStories, StoriesActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@AddStories, "Upload gagal, coba lagi", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getImageUri(context: Context): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun getFileFromUri(uri: Uri?): File? {
        if (uri == null) return null

        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)

        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                val filePath = it.getString(columnIndex)
                it.close()
                return File(filePath)
            }
            it.close()
        }

        return null
    }


    private fun startCamera() {
        selectedImageUri = getImageUri(this)
        launcherIntentCamera.launch(selectedImageUri!!)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


}