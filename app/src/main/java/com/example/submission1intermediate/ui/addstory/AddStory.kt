package com.example.submission1intermediate.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.submission1intermediate.R
import com.example.submission1intermediate.databinding.ActivityAddStoryBinding
import com.example.submission1intermediate.utils.*
import com.example.submission1intermediate.vstate.State
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileOutputStream

class AddStory : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein by kodein()
    private val viewModelFactory:ViewModelFactory by instance()
    private lateinit var addStoryViewModel: AddStoryViewModel
    private lateinit var binding: ActivityAddStoryBinding
    private var getFile: File? = null
    private var token: String = ""
    private var location: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    companion object{
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSION = 10
        private const val TAG = "AddStoryActivity"

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addStoryViewModel = obtainViewModel(this@AddStory)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launchWhenResumed {
            launch {
                addStoryViewModel.getAuthToken().collect { authToken ->
                    if (!authToken.isNullOrEmpty()) token = authToken
                }

            }
        }

        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnUpload.setOnClickListener { uploadStory() }
            binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                getLastLocation()
            } else{
                this.location = null
            }
        }

    }



    private fun obtainViewModel(activity: AppCompatActivity): AddStoryViewModel{
        return ViewModelProvider(activity, viewModelFactory)[AddStoryViewModel::class.java]
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION){
            if (!allPermissionsGrated()){
                Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGrated() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera(){
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)

    }

    private val launcherIntentCameraX = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == CAMERA_X_RESULT){
            val myfile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            getFile = myfile

           val result = rotateBitmap(BitmapFactory.decodeFile(getFile?.path), isBackCamera)
            result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(getFile))

            binding.imagePreview.setImageBitmap(result)

        }
    }


    private fun startGallery(){
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, "Pilih gambar")
        launcherIntentGallery.launch(chooser)

    }

    private val launcherIntentGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if (result.resultCode == RESULT_OK){
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddStory)

            getFile = myFile
            binding.imagePreview.setImageURI(selectedImg)
        }
    }

    private fun uploadStory(){

        val etDescription = binding.tiDescription
        var isValid = true

        if (etDescription.text.toString().isBlank()){
            etDescription.error = "Please fill the description field"
            isValid = false
        }

        if (getFile == null){
            Snackbar.make(binding.root, getString(R.string.select_image), Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (isValid){
            val file = reduceSizeFileImage(getFile as File)
            val description = etDescription.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
            val imageMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            var lat: RequestBody? = null
            var lon: RequestBody? = null

            if (location != null){
                lat = location?.latitude.toString().toRequestBody("text/plain".toMediaType())
                lon = location?.longitude.toString().toRequestBody("text/plain".toMediaType())

            }

            lifecycleScope.launch {
                addStoryViewModel.uploadImage(token, imageMultiPart, description,lat, lon).observeForever {
                    it.let { resource ->
                        when(resource.status){
                            State.SUCCESS -> {
                                Toast.makeText(this@AddStory, getString(R.string.add_story_success), Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            State.ERROR -> {
                                Snackbar.make(binding.root, "Add StoryEntity Failure", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            //permission granted
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null){
                    this.location = location
                    Log.d(TAG, "getLastLocation: ${location.latitude}, ${location.longitude}")

                } else{
                    Toast.makeText(this, "Please active your location", Toast.LENGTH_SHORT).show()
                    binding.switchLocation.isChecked = false
                }
            }
        } else {
            //permission denied
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){permission ->
        Log.d(TAG, "$permission")
        when{
            permission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false ->{
                getLastLocation()
            }
            else -> {
                Snackbar.make(binding.root, "Location permission not allowed", Snackbar.LENGTH_SHORT)
                    .setActionTextColor(getColor(R.color.white))
                    .setAction("Change Setting"){
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }.show()
                binding.switchLocation.isChecked = false
            }
        }
    }




}


