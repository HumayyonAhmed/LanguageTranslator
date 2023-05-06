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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.languagetranslator.translate.databinding.ActivityTranslateMainBinding


class MainActivity : AppCompatActivity(), DataListener {
  private lateinit var binding: ActivityTranslateMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityTranslateMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    val navigationView = findViewById<NavigationView>(R.id.navigation_view)
    val headerView = navigationView.getHeaderView(0)
    val emailView = headerView.findViewById<TextView>(R.id.nav_header_email)
    val nameView = headerView.findViewById<TextView>(R.id.nav_header_name)

    val prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val email = prefs.getString("email", null)
    val username = prefs.getString("username", null)
    if (email != null && username != null) {
      emailView.text = email;
      nameView.text = username;
    }

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
    }
    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController
    val bottomnavigation = findViewById<BottomNavigationView>(R.id.nav)

    bottomnavigation.setOnNavigationItemSelectedListener {
      when(it.itemId){
        R.id.translate -> {
          navController.navigate(R.id.translateFragment)
        }
        R.id.camera -> {
          navController.navigate(R.id.cameraFragment)
        }
        R.id.mic -> {
          val bundle = Bundle()
          bundle.putBoolean(TranslateFragment.MIC_INPUT.toString(), true)
          navController.navigate(R.id.translateFragment, bundle)
        }
        R.id.communicate -> {
          navController.navigate(R.id.communicateFragment)
        }
      }
      true
    }

    val toggle = ActionBarDrawerToggle(
      this,
      binding.drawerLayout,
      binding.toolbar,
      R.string.open_drawer,
      R.string.close_drawer
    )


    binding.drawerLayout.addDrawerListener(toggle)
    toggle.syncState()

    binding.navigationView.setNavigationItemSelectedListener { menuItem ->
      bottomnavigation.visibility = View.GONE
      when (menuItem.itemId) {
        R.id.home_page -> {
          navController.navigate(R.id.translateFragment)
          true
        }
        R.id.saved_key_phrases -> {
          val bundle = Bundle()
          bundle.putBoolean(HistoryFragment.Favourites.toString(), true)
          navController.navigate(R.id.historyFragment, bundle)
          true
        }
        R.id.document_translation -> {
          navController.navigate(R.id.documentFragment)
          true
        }
        R.id.history -> {
          navController.navigate(R.id.historyFragment)
          true
        }
        R.id.settings -> {
          navController.navigate(R.id.settingsFragment)
          true
        }
        R.id.logout -> {
          val prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
          with(prefs.edit()) {
            clear()
            apply()
          }
          val intent = Intent(this, LoginActivity::class.java)
          startActivity(intent)
          finish()
          true
        }
        else -> false
      }
    }
  }
  override fun onDataReceived(data: String, sourceLang: String, targetLang: String) {
    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController
    var bundle = Bundle()
    bundle.putString("INPUT_STRING", data)
    bundle.putString("SRC_LANG", sourceLang)
    bundle.putString("TAR_LANG", targetLang)
    navController.navigate(R.id.translateFragment, bundle)
  }

  companion object {
    var sourceLang: String? = null
    var targetLang: String? = null
    // Other companion object variables
  }
}
