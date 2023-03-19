/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.languagetranslator.translate

import HistoryData
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_translate_main.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.translate_fragment.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


/***
 * Fragment view for handling translations
 */
class TranslateFragment : Fragment() {
  private lateinit var tts: TextToSpeech
  val handler = Handler()
  private val LOG_TAG = "VoiceRecognitionAct"
  private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(requireContext()) }
  private lateinit var recognizerIntent: Intent
  var audioPath = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.translate_fragment, container, false)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    startSpeechRecognition()

    targetText.movementMethod = ScrollingMovementMethod()
    if (arguments?.getBoolean(MIC_INPUT.toString()) == true) {
      activity?.findViewById<TextView>(R.id.title)?.text = "Voice Input"
      toggleButton1.visibility = View.VISIBLE
      outputText.visibility = View.GONE
    } else {
      activity?.findViewById<TextView>(R.id.title)?.text = "Welcome"
      waves.visibility = View.GONE
      toggleButton1.visibility = View.GONE
      outputText.visibility = View.VISIBLE
    }
    activity?.findViewById<BottomNavigationView>(R.id.nav)?.visibility = View.VISIBLE
    val switchButton = view.findViewById<Button>(R.id.buttonSwitchLang)
//    val sourceSyncButton = view.findViewById<ToggleButton>(R.id.buttonSyncSource)
//    val targetSyncButton = view.findViewById<ToggleButton>(R.id.buttonSyncTarget)
    val srcTextView: TextInputEditText = view.findViewById(R.id.sourceText)
    val targetTextView = view.findViewById<TextView>(R.id.targetText)
    val downloadedModelsTextView = view.findViewById<TextView>(R.id.downloadedModels)
    val sourceLangSelector = view.findViewById<Spinner>(R.id.sourceLangSelector)
    val targetLangSelector = view.findViewById<Spinner>(R.id.targetLangSelector)
    val speakButton = view.findViewById<ImageButton>(R.id.speakBtn)
    val copyButton = view.findViewById<ImageButton>(R.id.copyBtn)
    val shareButton = view.findViewById<ImageButton>(R.id.shareBtn)
    val delButton = view.findViewById<ImageButton>(R.id.delBtn)
    val viewModel = ViewModelProviders.of(this).get(
      TranslateViewModel::class.java
    )
    // Get available language list and set up source and target language spinners
    // with default selections.
    val adapter = ArrayAdapter(
      requireContext(),
      R.layout.spinner_layout, viewModel.availableLanguages
    )

    tts = TextToSpeech(requireContext(), TextToSpeech.OnInitListener { status ->
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
    registerForContextMenu(shareButton)
    recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(
        RecognizerIntent.EXTRA_LANGUAGE,
        adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0, 2)
      )
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    toggleButton1.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
      override fun onCheckedChanged(
        buttonView: CompoundButton?,
        isChecked: Boolean
      ) {
        if (isChecked) {
          waves.setVisibility(View.VISIBLE)
          speechRecognizer.startListening(recognizerIntent)
        } else {
          waves.setVisibility(View.GONE)
          speechRecognizer.stopListening()
        }
      }
    })
    sourceLangSelector.onItemSelectedListener = object : OnItemSelectedListener {

      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long,
      ) {
        setProgressText(targetTextView)
        viewModel.sourceLang.setValue(adapter.getItem(position))
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0, 2)
          )
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {
        targetTextView.text = ""
      }
    }
    targetLangSelector.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long,
      ) {
        setProgressText(targetTextView)
        viewModel.targetLang.setValue(adapter.getItem(position))
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0, 2)
          )
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {
        targetTextView.text = ""
      }

    }
    switchButton.setOnClickListener {
      val targetText = targetTextView.text.toString()
      setProgressText(targetTextView)
      val sourceLangPosition = sourceLangSelector.selectedItemPosition
      sourceLangSelector.setSelection(targetLangSelector.selectedItemPosition)
      targetLangSelector.setSelection(sourceLangPosition)

      // Also update srcTextView with targetText
      srcTextView.setText(targetText)
      viewModel.sourceText.setValue(targetText)
      recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
          RecognizerIntent.EXTRA_LANGUAGE,
          adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0, 2)
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
      }
    }
    // Set up toggle buttons to delete or download remote models locally.
//    sourceSyncButton.setOnCheckedChangeListener { buttonView, isChecked ->
//      val language = adapter.getItem(sourceLangSelector.selectedItemPosition)
//      if (isChecked) {
//        viewModel.downloadLanguage(language!!)
//      } else {
//        viewModel.deleteLanguage(language!!)
//      }
//    }
//    targetSyncButton.setOnCheckedChangeListener { buttonView, isChecked ->
//      val language = adapter.getItem(targetLangSelector.selectedItemPosition)
//      if (isChecked) {
//        viewModel.downloadLanguage(language!!)
//      } else {
//        viewModel.deleteLanguage(language!!)
//      }
//    }
    favBtn.setOnClickListener {
      val database =
        FirebaseDatabase.getInstance("https://languagetranslator-a722c-default-rtdb.firebaseio.com/")
      val ref = database.getReference("user/history")
      val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
      val favoriteTranslation = HistoryData(
        adapter.getItem(sourceLangSelector.selectedItemPosition).toString().substring(0, 2)
          .lowercase(Locale.ROOT),
        adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0, 2)
          .lowercase(Locale.ROOT),
        sourceText.text.toString(), targetTextView.text.toString(), sdf.format(Date()), true
      )
      ref.push().setValue(favoriteTranslation)
      Toast.makeText(requireContext(), "Added to Favourites", Toast.LENGTH_SHORT).show()
    }
    pauseBtn.setOnClickListener {
      if (sourceText.text.toString() != "") {
        pauseBtn.visibility = View.GONE
        speakButton.visibility = View.VISIBLE
      }
      tts.stop()
    }
    speakButton.setOnClickListener {
      if (sourceText.text.toString() != "") {
        pauseBtn.visibility = View.VISIBLE
        speakButton.visibility = View.GONE
      }

      val lang =
        adapter.getItem(targetLangSelector.selectedItemPosition).toString().substring(0, 2);
      val text = targetTextView.text.toString()
      speak(text, lang)
    }
    copyButton.setOnClickListener {
      if (targetTextView.text.toString() != "") {
        val clipboard =
          getSystemService(requireContext(), ClipboardManager::class.java) as ClipboardManager?
        val clip = ClipData.newPlainText("translated text", targetTextView.text.toString())
        clipboard!!.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
      }
    }
    shareButton.setOnClickListener {
      if (sourceText.text.toString() != "") {
        activity?.openContextMenu(shareButton)
      }

    }
    delButton.setOnClickListener {
      sourceText.text?.clear()
    }
    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String) {}

      override fun onDone(utteranceId: String) {
        handler.post {
          speakButton.visibility = View.VISIBLE
          pauseBtn.visibility = View.GONE
        }
      }

      override fun onError(p0: String?) {
        TODO("Not yet implemented")
      }

      @Deprecated("Deprecated in Java")
      override fun onError(utteranceId: String, errorCode: Int) {
      }
    })
    // Translate input text as it is typed
    srcTextView.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable) {
        if (arguments?.getBoolean(MIC_INPUT.toString()) == true) {
          if (s.toString().trim() != "") {
            outputText.visibility = View.VISIBLE
          } else {
            outputText.visibility = View.GONE
          }
        }
        setProgressText(targetTextView)
        viewModel.sourceText.postValue(s.toString())
      }
    })
    viewModel.translatedText.observe(
      viewLifecycleOwner,
      { resultOrError ->
        if (resultOrError.error != null) {
          srcTextView.setError(resultOrError.error!!.localizedMessage)
        } else {
          targetTextView.text = resultOrError.result
        }
      }
    )

    // Update sync toggle button states based on downloaded models list.
    viewModel.availableModels.observe(
      viewLifecycleOwner,
      { translateRemoteModels ->
        val output = requireContext().getString(
          R.string.downloaded_models_label,
          translateRemoteModels
        )
        downloadedModelsTextView.text = output

//        sourceSyncButton.isChecked = !viewModel.requiresModelDownload(
//          adapter.getItem(sourceLangSelector.selectedItemPosition)!!,
//          translateRemoteModels)
//        targetSyncButton.isChecked = !viewModel.requiresModelDownload(
//          adapter.getItem(targetLangSelector.selectedItemPosition)!!,
//          translateRemoteModels)
      }
    )
    val bundle = arguments
    if (bundle != null) {
      val txt = bundle.getString("INPUT_STRING")
      val src = bundle.getString("SRC_LANG")
      val tar = bundle.getString("TAR_LANG")
      if (txt != null && src != null && tar != null) {
        Log.i("src", src)
        Log.i("tar", tar)
        Log.i("txt", txt)
        sourceLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language(src)))
        targetLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language(tar)))
        srcTextView.setText(txt)
      }
    }
  }

  override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
    super.onCreateContextMenu(menu, v, menuInfo)
    requireActivity().menuInflater.inflate(R.menu.context_menu, menu)
  }

  override fun onContextItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.share_audio -> {
        shareAsAudio()
        return true
      }
      R.id.share_text -> {
        shareAsText()
        return true
      }
      else -> return super.onContextItemSelected(item)
    }
  }

  private fun shareAsText() {
      if(targetText.text.toString()!="") {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, targetText.text.toString())
        startActivity(Intent.createChooser(shareIntent, "Share using"))
      }
  }

  private fun shareAsAudio() {

    val file = getAudioFile()
    tts.synthesizeToFile(targetText.text, null, file, null)

    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "audio/*"
    val uri: Uri
    uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
      FileProvider.getUriForFile(requireActivity(),
        "com.languagetranslator.translate.provider",
        file
      ) else Uri.fromFile(file)

    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    val chooser = Intent.createChooser(shareIntent, "Share TTS Output")
    val resInfoList: List<ResolveInfo> =
      requireContext().packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

    for (resolveInfo in resInfoList) {
      val packageName = resolveInfo.activityInfo.packageName
      requireContext().grantUriPermission(
        packageName,
        uri,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
      )
    }
    startActivity(chooser)
  }

  private fun getAudioFile(): File {
    val imageFileName = "AUDIO_" + System.currentTimeMillis() + "_"
    val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),"Recordings")
    val file = File.createTempFile(
      imageFileName, ".mp3", storageDir
    )
    audioPath = "file:" + file.absolutePath
    return file
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
        sourceText.setText(text)
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

  fun speak(text: String, lang: String) {
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
    tv.text = requireContext().getString(R.string.translate_progress)
  }

  companion object {
    val SRC_LANG = null
    val TAR_LANG = null
    val INPUT_STRING = null
    const val MIC_INPUT = false
    fun newInstance(): TranslateFragment {
      return TranslateFragment()
    }
  }
}
