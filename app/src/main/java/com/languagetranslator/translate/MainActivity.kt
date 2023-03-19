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
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_translate_main.*
import kotlinx.android.synthetic.main.activity_translate_main.view.*


class MainActivity : AppCompatActivity(), DataListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_translate_main)

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
      drawer_layout,
      toolbar,
      R.string.open_drawer,
      R.string.close_drawer
    )


    drawer_layout.addDrawerListener(toggle)
    toggle.syncState()

    navigation_view.setNavigationItemSelectedListener { menuItem ->
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
//  private fun makeFragment(fragment: Fragment) {
//    supportFragmentManager.beginTransaction()
//      .replace(
//        R.id.container,
//        fragment
//      )
//      .commitNow()
//  }
}
