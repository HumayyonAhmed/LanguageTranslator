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
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_translate_main.*


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
    val bottomnavigation = findViewById<BottomNavigationView>(R.id.nav)
    if (savedInstanceState == null) {
      findViewById<TextView>(R.id.title).text = "Welcome"
      supportFragmentManager.beginTransaction()
        .replace(
          R.id.container,
          TranslateFragment.newInstance()
        )
        .commitNow()
    }

    bottomnavigation.setOnNavigationItemSelectedListener {
      when(it.itemId){
        R.id.translate -> {
          findViewById<TextView>(R.id.title).text = "Translate"
          makeFragment(TranslateFragment.newInstance())
        }
        R.id.camera -> {
          findViewById<TextView>(R.id.title).text = "Image Input"
          makeFragment(CameraFragment.newInstance())
        }
        R.id.mic -> {
          findViewById<TextView>(R.id.title).text = "Voice Input"
          val bundle = Bundle()
          bundle.putBoolean(TranslateFragment.MIC_INPUT.toString(), true)
          val fragment = TranslateFragment.newInstance()
          fragment.arguments = bundle
          makeFragment(fragment)
        }
        R.id.communicate -> {
          findViewById<TextView>(R.id.title).text = "Conversation"
          makeFragment(CommunicateFragment.newInstance())
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
      when (menuItem.itemId) {
        R.id.saved_key_phrases -> {
          // Handle Saved Key Phrases click
          true
        }
        R.id.downloaded_models -> {
          // Handle Downloaded Models click
          true
        }
        R.id.document_translation -> {
          // Handle Document Translation click
          true
        }
        R.id.history -> {
          // Handle History click
          true
        }
        R.id.settings -> {
          // Handle Settings click
          true
        }
        else -> false
      }
    }
  }
  override fun onDataReceived(data: String) {
    val bundle = Bundle()
    bundle.putString(TranslateFragment.INPUT_STRING, data)
    val fragment = TranslateFragment.newInstance()
    fragment.arguments = bundle
    makeFragment(fragment)
  }
  private fun makeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.container,
        fragment
      )
      .commitNow()
  }
}
