package com.languagetranslator.translate

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.languagetranslator.translate.databinding.CameraFragmentBinding
import com.languagetranslator.translate.databinding.CommunicateFragmentBinding
import java.util.*

/***
 * Fragment view for handling translations
 */
class CommunicateFragment : Fragment() {
    private var _binding: CommunicateFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var tts: TextToSpeech
    private val LOG_TAG = "VoiceRecognitionAct"
    private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(requireActivity()) }
    private lateinit var recognizerIntent: Intent
    private lateinit var recognizerIntent2: Intent
    private lateinit var srcLang: String
    private lateinit var targetLang: String
    private var srcInput: Boolean = false
    private var targetInput: Boolean = false
    private lateinit var viewModel:TranslateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = CommunicateFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSpeechRecognition()

        activity?.findViewById<TextView>(R.id.title)?.text = "Communicate"
        activity?.findViewById<BottomNavigationView>(R.id.nav)?.visibility  = View.VISIBLE
        viewModel = ViewModelProvider(this)[TranslateViewModel::class.java]
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_layout, viewModel.availableLanguages
        )

        tts = TextToSpeech(requireActivity(), TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.i("TTS", "Successfully initialized")
            } else {
                Log.e("TTS", "Initialization failed")
            }
        })

        binding.sourceLangSelector.adapter = adapter
        binding.targetLangSelector.adapter = adapter
        binding.sourceLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("ur")))
        binding.targetLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("ar")))
        srcLang = adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2)
        targetLang = adapter.getItem(binding.targetLangSelector.selectedItemPosition).toString().substring(0,2)
        binding.toggleButton1.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(
                buttonView: CompoundButton?,
                isChecked: Boolean
            ) {
                srcInput = true
                srcLang = adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2)
                targetLang = adapter.getItem(binding.targetLangSelector.selectedItemPosition).toString().substring(0,2)
                if (isChecked) {
                    binding.waves.setVisibility(View.VISIBLE)
                    speechRecognizer.startListening(recognizerIntent)
                } else {
                    binding.waves.setVisibility(View.GONE)
                    speechRecognizer.stopListening()
                }
            }
        })
        binding.sourceLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
//                setProgressText(binding.targetText)
                viewModel.sourceLang.setValue(adapter.getItem(position))
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2))
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                srcLang = adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2)
                targetLang = adapter.getItem(binding.targetLangSelector.selectedItemPosition).toString().substring(0,2)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.targetText.setText("")
            }
        }
        binding.targetLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                setProgressText(binding.targetText)
                viewModel.targetLang.setValue(adapter.getItem(position))
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2))
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                srcLang = adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2)
                targetLang = adapter.getItem(binding.targetLangSelector.selectedItemPosition).toString().substring(0,2)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.targetText.setText("")
            }
        }
        binding.buttonSwitchLang.setOnClickListener {
            val targetTxt = binding.targetText.text.toString()
            setProgressText(binding.targetText)
            val sourceLangPosition = binding.sourceLangSelector.selectedItemPosition
            binding.sourceLangSelector.setSelection(binding.targetLangSelector.selectedItemPosition)
            binding.targetLangSelector.setSelection(sourceLangPosition)

            // Also update srcTextView with binding.targetText
            binding.sourceText.setText(targetTxt)
            viewModel.sourceText.setValue(targetTxt)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2))
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            srcLang = adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0,2)
            targetLang = adapter.getItem(binding.targetLangSelector.selectedItemPosition).toString().substring(0,2)
        }

        binding.speakBtn1.setOnClickListener {
            if (binding.sourceText.text.toString() != "") {
                binding.speakBtn1.visibility = View.GONE
            }

            val lang =
                adapter.getItem(binding.sourceLangSelector.selectedItemPosition).toString().substring(0, 2);
            val text = binding.sourceText.text.toString()
            speak(text, lang)
                binding.speakBtn1.visibility = View.VISIBLE
        }
        binding.speakBtn2.setOnClickListener {
            if (binding.targetText.text.toString() != "") {
                binding.speakBtn2.visibility = View.GONE
            }

            val lang =
                adapter.getItem(binding.targetLangSelector.selectedItemPosition).toString().substring(0, 2);
            val text = binding.targetText.text.toString()
            speak(text, lang)
            binding.speakBtn2.visibility = View.VISIBLE
        }
        binding.copyBtn1.setOnClickListener {
            if (binding.sourceText.text.toString() != "") {
                val clipboard =
                    ContextCompat.getSystemService(
                        requireContext(),
                        ClipboardManager::class.java
                    ) as ClipboardManager?
                val clip = ClipData.newPlainText("translated text", binding.sourceText.text.toString())
                clipboard!!.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.copyBtn2.setOnClickListener {
            if (binding.targetText.text.toString() != "") {
                val clipboard =
                    ContextCompat.getSystemService(
                        requireContext(),
                        ClipboardManager::class.java
                    ) as ClipboardManager?
                val clip = ClipData.newPlainText("translated text", binding.targetText.text.toString())
                clipboard!!.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.delBtn1.setOnClickListener {
            binding.sourceText.setText("")
            binding.targetText.setText("")
        }
        viewModel.translatedText.observe(
            viewLifecycleOwner
        ) { resultOrError ->
            if (resultOrError.error != null) {
                binding.sourceText.error = resultOrError.error!!.localizedMessage
            } else {
                binding.targetText.setText(resultOrError.result)
                speak(resultOrError.result + "", targetLang)
            }
        }

    }
    private fun startSpeechRecognition() {
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
                binding.sourceText.setText("Listening...")
            }

            override fun onBufferReceived(buffer: ByteArray) {
                Log.i(LOG_TAG, "onBufferReceived: $buffer")
            }

            override fun onEndOfSpeech() {
                Log.i(LOG_TAG, "onEndOfSpeech")
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
                viewModel.sourceText.postValue(text)
            }
            override fun onRmsChanged(rmsdB: Float) {
                Log.i(LOG_TAG, "onRmsChanged: $rmsdB")
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
    private fun speak(text: String, lang: String) {
        // Set the language
        val result = tts.setLanguage(Locale(lang))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Language not supported")
        } else {
            val maleVoice = findMaleVoice(tts)
            if (maleVoice != null) {
                // Set the male voice as default
                tts.voice = maleVoice
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        }
    }
    private fun findMaleVoice(tts: TextToSpeech): Voice? {
        val voices = tts.voices
        for (voice in voices) {
            if (voice.name.toLowerCase(Locale.US).contains("male")) {
                return voice
            }
        }
        return null
    }

    private fun setProgressText(tv: TextView) {
        tv.text = requireActivity().getString(R.string.translate_progress)
    }
    companion object {
        fun newInstance(): CommunicateFragment {
            return CommunicateFragment()
        }
    }
}