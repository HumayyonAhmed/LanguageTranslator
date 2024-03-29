package com.languagetranslator.translate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.languagetranslator.translate.databinding.MicFragmentBinding


/***
 * Fragment view for handling translations
 */
class MicFragment : Fragment() {
    private var _binding: MicFragmentBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private val LOG_TAG = "VoiceRecognitionAct"

    private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(requireContext()) }
    private lateinit var recognizerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = MicFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSpeechRecognition()
        val viewModel = ViewModelProviders.of(this).get(
            TranslateViewModel::class.java
        )
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_layout, viewModel.availableLanguages
        )
        binding.sourceLangSelector.adapter = adapter
        binding.targetLangSelector.adapter = adapter
        binding.sourceLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("ur")))
        binding.targetLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("ar")))
        Log.i("Lang", adapter.getItem(binding.targetLangSelector.selectedItemPosition).toString().substring(0,2))
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2))
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        binding.toggleButton1.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(
                buttonView: CompoundButton?,
                isChecked: Boolean
            ) {
                if (isChecked) {
                    binding.progressBar1.setVisibility(View.VISIBLE)
                    binding.progressBar1.setIndeterminate(true)
                    speechRecognizer.startListening(recognizerIntent)
                } else {
                    binding.progressBar1.setIndeterminate(false)
                    binding.progressBar1.setVisibility(View.INVISIBLE)
                    speechRecognizer.stopListening()
                }
            }
        })
    }

    fun startSpeechRecognition() {
//        speechRecognizer.startListening(recognizerIntent)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            fun onResume(){
                this.onResume()
            }
            protected fun onPause() {
                this.onPause()
                if (speechRecognizer != null) {
                    speechRecognizer.destroy()
                    Log.i(LOG_TAG, "destroy")
                }
            }

            override fun onBeginningOfSpeech() {
                Log.i(LOG_TAG, "onBeginningOfSpeech")
                binding.sourceText.setText("Listening")
                binding.progressBar1.setIndeterminate(false)
                binding.progressBar1.setMax(10)
            }

            override fun onBufferReceived(buffer: ByteArray) {
                Log.i(LOG_TAG, "onBufferReceived: $buffer")
            }

            override fun onEndOfSpeech() {
                Log.i(LOG_TAG, "onEndOfSpeech")
                binding.progressBar1.setIndeterminate(true)
                binding.toggleButton1.setChecked(false)
            }

            override fun onError(errorCode: Int) {
                val errorMessage = getErrorText(errorCode)
                Log.d(LOG_TAG, "FAILED $errorMessage")
                binding.sourceText.setText("")
                binding.toggleButton1.setChecked(false)
            }

            override fun onEvent(arg0: Int, arg1: Bundle?) {
                Log.i(LOG_TAG, "onEvent")
            }

            override fun onPartialResults(arg0: Bundle?) {
                Log.i(LOG_TAG, "onPartialResults")
            }

            override fun onReadyForSpeech(arg0: Bundle?) {
                Log.i(LOG_TAG, "onReadyForSpeech")
            }

            override fun onResults(results: Bundle) {
                Log.i(LOG_TAG, "onResults")
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                var text = ""
                for (result in matches!!) text += """$result""".trimIndent()
                binding.sourceText.setText(text)
            }
            override fun onRmsChanged(rmsdB: Float) {
                Log.i(LOG_TAG, "onRmsChanged: $rmsdB")

                binding.progressBar1.setProgress(rmsdB.toInt())
            }

            fun getErrorText(errorCode: Int): String {
                val message: String
                message = when (errorCode) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                    SpeechRecognizer.ERROR_SERVER -> "error from server"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Didn't understand, please try again."
                }
                return message
            }
        })
    }
    companion object {
        fun newInstance(): MicFragment {
            return MicFragment()
        }
    }

}