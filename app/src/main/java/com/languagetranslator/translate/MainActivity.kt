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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
//
//  private val translateFragment = TranslateFragment()
//  private val cameraFragment = CameraFragment()
//  private val micFragment = MicFragment()
//  private val communicateFragment = CommunicateFragment()
//  private val bottom_navigation:BottomNavigationView = findViewById(R.id.nav)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_translate_main)

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(
          R.id.container,
          TranslateFragment.newInstance()
        )
        .commitNow()
    }
  }
//    bottom_navigation.setOnNavigationItemSelectedListener{
//      when(it.itemId){
//        R.id.translate -> supportFragmentManager.beginTransaction()
//          .replace(
//            R.id.container,
//            TranslateFragment.newInstance()
//          )
//          .commitNow()
//        R.id.camera -> makeFragment(CameraFragment.newInstance())
//        R.id.mic -> makeFragment(MicFragment.newInstance())
//        R.id.communicate -> makeFragment(CommunicateFragment.newInstance())
//      }
//      true
//    }
//  }
//
//  private fun makeFragment(fragment: Fragment) {
//    supportFragmentManager.beginTransaction()
//      .replace(
//        R.id.container,
//        fragment
//      )
//      .commitNow()
//  }
}
