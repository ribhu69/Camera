package com.example.android.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    private var selectorID = 1
    private var shutterTimerId = 0
    private var flashControlId = 0
    private var shutterSoundId = 0
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraControlX: CameraControl
    private lateinit var outputDirectory: File
    private lateinit var viewFinder : PreviewView
    private lateinit var cameraCaptureButton: FloatingActionButton
    private lateinit var shutterSound: ImageButton
    private lateinit var shutterTimer: ImageButton
    private lateinit var imageButton: ImageButton
    private lateinit var cameraChooser: FloatingActionButton
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var flashControl: FloatingActionButton

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera(shutterSoundId)
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Initialization of Buttons
        cameraCaptureButton = findViewById(R.id.camera_capture_button)
        cameraChooser = findViewById(R.id.camera_selector)
        shutterSound = findViewById(R.id.shutter_sound)
        shutterTimer = findViewById(R.id.shutter_timer)
        viewFinder = findViewById(R.id.viewFinder)
        flashControl = findViewById(R.id.flash_control)
        imageButton = findViewById(R.id.imageButton2)



        cameraCaptureButton.setOnClickListener {
//
            when(shutterSoundId)
            {
                0 -> {
                    MediaPlayer.create(this, R.raw.capture_1).start()
                    takePhoto()
                }
                1 -> {
                    takePhoto(); Toast.makeText(this, "Photo Saved", Toast.LENGTH_SHORT).show()
                }

                2 -> {
                    takePhoto()
                    vibratePhone()
                }
            }
                }



        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraChooser.setOnClickListener { cameraSelector() }

        shutterTimer.setOnClickListener { shutterTimer() }

        shutterSound.setOnClickListener { shutterSound() }

        flashControl.setOnClickListener()
        {
            flashControl()
        }

//        imageButton.setOnClickListener{
//            setupAnimation()
//        }

       viewFinder.setOnTouchListener { _, event ->
           when(event.action)
           {
               MotionEvent.ACTION_DOWN -> return@setOnTouchListener true

               MotionEvent.ACTION_UP -> {
                   val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                           viewFinder.width.toFloat(), viewFinder.height.toFloat()
                   )
                   val autoFocusPoint = factory.createPoint(event.x, event.y)
                   try {
                       cameraControlX.startFocusAndMetering(
                               FocusMeteringAction.Builder(
                                       autoFocusPoint,
                                       FocusMeteringAction.FLAG_AF
                               ).apply {
                                   disableAutoCancel()
                               }.build()
                       )
                   } catch (e: CameraInfoUnavailableException) {
                       Log.d("Error", "Camera Not Accessible", e)
                   }
                   true
               }
               else -> false
           }
       }


    }

            override fun onRequestPermissionsResult(
                    requestCode: Int,
                    permissions: Array<out String>,
                    grantResults: IntArray
            ) {
                if (requestCode == REQUEST_CODE_PERMISSIONS) {
                    if (allPermissionsGranted()) {
                        startCamera()
                    } else {
                        Toast.makeText(
                                this,
                                "Permissions not granted by the user.",
                                Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }

    private fun shutterSound() {
        when (shutterSound.tag) {

            0 -> {
                shutterSoundId=1;shutterSound.tag=1; shutterSound.setImageResource(R.drawable.ic_baseline_notifications_off_24)
            }
            1 -> {
                shutterSoundId=2;shutterSound.tag=2; shutterSound.setImageResource(R.drawable.ic_baseline_vibration_24)
            }
            2 -> {
                shutterSoundId=3;shutterSound.tag=0; shutterSound.setImageResource(R.drawable.ic_baseline_notifications_active_24)
            }
            else ->   {shutterSound.tag=0; shutterSound.setImageResource(R.drawable.ic_baseline_notifications_active_24)}

        }

    }

//    private fun setupAnimation(){
//        val animation = findViewById<LottieAnimationView>(R.id.lottieAnimation)
//        animation.setAnimation(R.raw.sand_clock)
//        animation.speed = 2.0F // How fast does the animation play
//        animation.addAnimatorUpdateListener {
//            // Called everytime the frame of the animation changes
//        }
//        animation.repeatMode = LottieDrawable.RESTART // Restarts the animation (you can choose to reverse it as well)
//        animation.cancelAnimation() // Cancels the animation
//    }

    //For vibration in Vibrate Mode
    private fun vibratePhone() {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(50)
        }
    }

            private fun shutterTimer() {
                when (shutterTimerId) {
                    0 -> {
                        shutterTimerId =
                                3; shutterTimer.setImageResource(R.drawable.ic_baseline_timer_3_24)
                    }
                    3 -> {
                        shutterTimerId =
                                10; shutterTimer.setImageResource(R.drawable.ic_baseline_timer_10_24)
                    }
                    10 -> {
                        shutterTimerId =
                                0; shutterTimer.setImageResource(R.drawable.ic_baseline_timer_off_24)
                    }
                }

            }

            private fun flashControl() {
                when (flashControlId) {
                    1 -> {
                        flashControlId =
                                0; flashControl.setImageResource(R.drawable.ic_baseline_flash_off_24)
                    }
                    0 -> {
                        flashControlId =
                                1; flashControl.setImageResource(R.drawable.ic_baseline_flash_on_24)
                    }
                }
            }




            private fun takePhoto() {

                val imageCapture = imageCapture ?: return

                //Timestamped output file to hold the image
                val photoFile = File(
                        outputDirectory, SimpleDateFormat(
                        FILENAME_FORMAT,
                        Locale.US
                ).format(System.currentTimeMillis()) + ".jpg"
                )

                // contains output file (file + metadata)
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()



                imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exception: ImageCaptureException) {
                                Log.e(TAG, "Photo capture failed", exception)
                            }

                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                Log.i(TAG, "Successful")
                            }
                        })
            }


            private fun cameraSelector() {
                when (selectorID) {
                    1 -> {
                        selectorID = 2
                        startCamera(selectorID)
                        cameraChooser.setImageResource(R.drawable.ic_baseline_camera_rear_24)

                    }
                    2 -> {
                        selectorID = 1
                        startCamera(selectorID)
                        cameraChooser.setImageResource(R.drawable.ic_baseline_camera_front_24)

                    }
                }

            }

            private fun startCamera(cameraID: Int = 1) {

                val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
                viewFinder = findViewById(R.id.viewFinder)
                cameraProviderFuture.addListener(Runnable {
                    // Used to bind the lifecycle of cameras to the lifecycle owner
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    // Preview
                    val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                            }

                    imageCapture = ImageCapture.Builder()
                            .build()


                    // Select back camera as a default
                    // Camera Choosing based on value set by cameraSelector()
                    val cameraSelector =
                            when (cameraID) {
                                1 -> CameraSelector.DEFAULT_BACK_CAMERA
                                2 -> CameraSelector.DEFAULT_FRONT_CAMERA
                                else -> CameraSelector.DEFAULT_BACK_CAMERA
                            }

                    try {
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll()

                        // Bind use cases to camera
                        val camera = cameraProvider.bindToLifecycle(
                                this, cameraSelector, preview, imageCapture
                        )
                        cameraControlX = camera.cameraControl

                    } catch (exc: Exception) {
                        Log.e(TAG, "Use case binding failed", exc)
                    }

                }, ContextCompat.getMainExecutor(this))
            }

            private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                        baseContext, it
                ) == PackageManager.PERMISSION_GRANTED
            }

            private fun getOutputDirectory(): File {
                val mediaDir = externalMediaDirs.firstOrNull()?.let {
                    File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
                }
                return if (mediaDir != null && mediaDir.exists())
                    mediaDir else filesDir
            }

            override fun onDestroy() {
                super.onDestroy()
                cameraExecutor.shutdown()
            }

            companion object {
            private const val TAG = "CameraXBasic"
            private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
            private const val REQUEST_CODE_PERMISSIONS = 10
            private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE)
        }
        }
