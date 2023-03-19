package com.languagetranslator.translate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
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
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.communicate_fragment.*
import kotlinx.android.synthetic.main.communicate_fragment.buttonSwitchLang
import kotlinx.android.synthetic.main.communicate_fragment.sourceLangSelector
import kotlinx.android.synthetic.main.communicate_fragment.sourceText
import kotlinx.android.synthetic.main.communicate_fragment.targetLangSelector
import kotlinx.android.synthetic.main.communicate_fragment.targetText
import kotlinx.android.synthetic.main.communicate_fragment.toggleButton1
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

/***
 * Fragment view for handling translations
 */
class CommunicateFragment : Fragment() {
    private lateinit var tts: TextToSpeech
    val handler = Handler()
    private val LOG_TAG = "VoiceRecognitionAct"
    private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(requireActivity()) }
    private lateinit var recognizerIntent: Intent
    var audioPath = ""
    private lateinit var srcLang: String
    private lateinit var targetLang: String
    private var srcInput: Boolean = false
    private var targetInput: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.communicate_fragment, container, false)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSpeechRecognition()

        activity?.findViewById<TextView>(R.id.title)?.text = "Communicate"
        activity?.findViewById<BottomNavigationView>(R.id.nav)?.visibility  = View.VISIBLE
        val viewModel = ViewModelProviders.of(this).get(
            TranslateViewModel::class.java
        )
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

        sourceLangSelector.adapter = adapter
        targetLangSelector.adapter = adapter
        sourceLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("ur")))
        targetLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("ar")))
        srcLang = adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2)
        targetLang = adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0,2)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2))
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        toggleButton1.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(
                buttonView: CompoundButton?,
                isChecked: Boolean
            ) {
                srcInput = true
                targetInput = false
                srcLang = adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2)
                targetLang = adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0,2)
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2))
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                if (isChecked) {
                    speechRecognizer.startListening(recognizerIntent)
                } else {
                    speechRecognizer.stopListening()
                }
            }
        })
        toggleButton2.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(
                buttonView: CompoundButton?,
                isChecked: Boolean
            ) {
                srcInput = false
                targetInput = true
                srcLang = adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0,2)
                targetLang = adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2)
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0,2))
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                if (isChecked) {
                    speechRecognizer.startListening(recognizerIntent)
                } else {
                    speechRecognizer.stopListening()
                }
            }
        })
        sourceLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
//                setProgressText(targetText)
                viewModel.sourceLang.setValue(adapter.getItem(position))
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2))
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                srcLang = adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2)
                targetLang = adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0,2)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                targetText.setText("")
            }
        }
        targetLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                setProgressText(targetText)
                viewModel.targetLang.setValue(adapter.getItem(position))
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2))
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                srcLang = adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2)
                targetLang = adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0,2)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                targetText.setText("")
            }
        }
        buttonSwitchLang.setOnClickListener {
            val targetTxt = targetText.text.toString()
            setProgressText(targetText)
            val sourceLangPosition = sourceLangSelector.selectedItemPosition
            sourceLangSelector.setSelection(targetLangSelector.selectedItemPosition)
            targetLangSelector.setSelection(sourceLangPosition)

            // Also update srcTextView with targetText
            sourceText.setText(targetTxt)
            viewModel.sourceText.setValue(targetTxt)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2))
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            srcLang = adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0,2)
            targetLang = adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0,2)
        }
        sourceText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
//                setProgressText(targetText)
                viewModel.sourceText.postValue(s.toString())
            }
        })
        targetText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
//                setProgressText(sourceText)
                viewModel.sourceText.postValue(s.toString())
            }
        })
        viewModel.translatedText.observe(
            viewLifecycleOwner
        ) { resultOrError ->
            if (resultOrError.error != null) {
                sourceText.error = resultOrError.error!!.localizedMessage
            } else {
                if(srcInput && !targetInput) {
                    targetText.setText(resultOrError.result)
                    speak(resultOrError.result + "", targetLang)
                }
                else {
                    sourceText.setText(resultOrError.result)
                    speak(resultOrError.result + "", srcLang)
                }
            }
        }
        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(
            viewLifecycleOwner
        ) { translateRemoteModels ->
            val output = requireActivity().getString(
                R.string.downloaded_models_label,
                translateRemoteModels
            )
        }
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
                sourceText.setText("Listening...")
            }

            override fun onBufferReceived(buffer: ByteArray) {
                Log.i(LOG_TAG, "onBufferReceived: $buffer")
            }

            override fun onEndOfSpeech() {
                Log.i(LOG_TAG, "onEndOfSpeech")
                toggleButton1.setChecked(false)
            }

            override fun onError(errorCode: Int) {
                val errorMessage = getErrorText(errorCode)
                Log.d(LOG_TAG, "FAILED $errorMessage")
                sourceText.setText("")
                toggleButton1.setChecked(false)
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
                if (srcInput && !targetInput)
                    sourceText.setText(text)
                else
                    targetText.setText(text)
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