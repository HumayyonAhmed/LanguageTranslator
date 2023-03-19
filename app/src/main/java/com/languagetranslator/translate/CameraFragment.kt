package com.languagetranslator.translate

import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


/***
 * Fragment view for handling translations
 */
class CameraFragment : Fragment() {
    private var listener: DataListener? = null
    var currentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<TextView>(R.id.title)?.text = "Camera Input"
        activity?.findViewById<BottomNavigationView>(R.id.nav)?.visibility  = View.VISIBLE
        captureButton.setOnClickListener {
            captureImage()
        }
        galleryButton.setOnClickListener {
            openGallery()
        }

    }
    private fun captureImage() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = getImageFile() // 1

        val uri: Uri
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 2
            FileProvider.getUriForFile(requireActivity(),
                "com.languagetranslator.translate.provider",
                file
            ) else Uri.fromFile(file) // 3

        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri) // 4
        startActivityForResult(pictureIntent, CAMERA_REQUEST_CODE)
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }
    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        UCrop.of(sourceUri, destinationUri)
            .withMaxResultSize(maxWidth, maxHeight)
            .start(requireContext(), this)
    }
    private fun showImage(imageUri: Uri) {
        val file = File(imageUri.path)
        val inputStream = FileInputStream(file)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        recognizeText(bitmap)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val uri = Uri.parse(currentPhotoPath)
                    openCropActivity(uri, uri)
                }
                GALLERY_REQUEST_CODE -> {
                    val sourceUri = data?.data
                    val file = getImageFile()
                    val destinationUri = Uri.fromFile(file)
                    if (sourceUri != null) {
                        openCropActivity(sourceUri, destinationUri)
                    }
                }
                UCrop.REQUEST_CROP -> {
                    val uri = UCrop.getOutput(data!!)
                    if (uri != null) {
                        showImage(uri)
                    }
                }
            }
        }
    }
    private fun getImageFile(): File {
        val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"
        val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera")
        val file = File.createTempFile(
            imageFileName, ".jpg", storageDir
        )
        currentPhotoPath = "file:" + file.absolutePath
        return file
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DataListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DataListener")
        }
    }

    private fun recognizeText(imageBitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(imageBitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                listener?.onDataReceived(text, "en", "ur")
            }
            .addOnFailureListener { e ->
                Log.i("ERrrorororrrrrrrrrrrrr", e.toString())
            }
    }
    companion object {
        private const val CAMERA_REQUEST_CODE = 101
        private const val GALLERY_REQUEST_CODE = 200
        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
interface DataListener {
    fun onDataReceived(data: String, srcLang: String, targetLang: String)
}