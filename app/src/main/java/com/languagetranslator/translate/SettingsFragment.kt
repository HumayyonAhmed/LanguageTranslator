package com.languagetranslator.translate

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: TranslateViewModel

    private var urModel: SwitchPreferenceCompat? = null
    private var arModel: SwitchPreferenceCompat? = null
    private var enModel: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // initialize view model and preferences
        viewModel = ViewModelProviders.of(this).get(TranslateViewModel::class.java)
        urModel = findPreference("ur")
        arModel = findPreference("ar")
        enModel = findPreference("en")

        val defaultSourceLangPref = findPreference<ListPreference>("defaultSourceLang")
        defaultSourceLangPref?.setOnPreferenceChangeListener { preference, newValue ->
            val selectedValue = newValue as String
            MainActivity.sourceLang = selectedValue
            true
        }
        val defaultTargetLangPref = findPreference<ListPreference>("defaultTargetLang")
        defaultTargetLangPref?.setOnPreferenceChangeListener { preference, newValue ->
            val selectedValue = newValue as String
            MainActivity.targetLang = selectedValue
            true
        }
        urModel?.isChecked = !viewModel.requiresModelDownload("ur", viewModel.availableModels.value)
        arModel?.isChecked = !viewModel.requiresModelDownload("ar", viewModel.availableModels.value)
        enModel?.isChecked = !viewModel.requiresModelDownload("en", viewModel.availableModels.value)

        urModel?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean) {
                viewModel.downloadLanguage("ur")
            } else {
                viewModel.deleteLanguage("ur")
            }
            true
        }
        arModel?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean) {
                viewModel.downloadLanguage("ar")
            } else {
                viewModel.deleteLanguage("ar")
            }
            true
        }
        enModel?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean) {
                viewModel.downloadLanguage("en")
            } else {
                viewModel.deleteLanguage("en")
            }
            true
        }
    }
    override fun onResume() {
        super.onResume()

        // observe available models and update preference switches
        viewModel.availableModels.observe(
            viewLifecycleOwner,
            { translateRemoteModels ->
                urModel?.isChecked = !viewModel.requiresModelDownload("ur", translateRemoteModels)
                arModel?.isChecked = !viewModel.requiresModelDownload("ar", translateRemoteModels)
                enModel?.isChecked = !viewModel.requiresModelDownload("en", translateRemoteModels)
            }
        )
    }
}