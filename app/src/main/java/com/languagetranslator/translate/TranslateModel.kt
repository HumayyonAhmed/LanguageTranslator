package com.languagetranslator.translate

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TranslateModel : AppCompatActivity() {
//    private lateinit var tfliteInterpreter: Interpreter
//    private lateinit var inputBuffer: ByteBuffer
//    private lateinit var outputBuffer: ByteBuffer
//    private val INPUT_SIZE: Int = 50// Specify the input size as per your model
//    private val OUTPUT_SIZE: Int = 50// Specify the output size as per your model
//
//    private lateinit var inputEditText: EditText
//    private lateinit var outputEditText: EditText
//    private lateinit var translateButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_translate_main)
//
//        inputEditText = findViewById(R.id.sourceText)
//        outputEditText = findViewById(R.id.targetText)
//        translateButton = findViewById(R.id.shareBtn)
//
//        try {
//            // Load the TFLite model
//            tfliteInterpreter = Interpreter(loadModelFile())
//
//            // Initialize input and output buffers
//            inputBuffer = ByteBuffer.allocateDirect(INPUT_SIZE)
//            outputBuffer = ByteBuffer.allocateDirect(OUTPUT_SIZE)
//
//            translateButton.setOnClickListener {
//                val inputText = inputEditText.text.toString()
//
//                val inputBytes = ByteBuffer.allocateDirect(INPUT_SIZE)
//                // Run inference
//                tfliteInterpreter.run(inputBytes, outputBuffer)
//
//                val outputText = outputBuffer // Replace this with your output data
//                outputEditText.setText(outputText)
//            }
//
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun loadModelFile(): MappedByteBuffer {
//        val assetManager: AssetManager = assets
//        val fileDescriptor: AssetFileDescriptor = assetManager.openFd("model.tflite")
//        val inputStream: FileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel: FileChannel = inputStream.channel
//        val startOffset: Long = fileDescriptor.startOffset
//        val declaredLength: Long = fileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }
}
