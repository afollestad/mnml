/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.mnmlscreenrecord.ui.settings

import android.content.Intent
import android.os.Bundle
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.theming.DarkModeSwitchActivity
import com.afollestad.mnmlscreenrecord.ui.main.MainActivity
import kotlinx.android.synthetic.main.include_appbar.toolbar
import kotlinx.android.synthetic.main.include_appbar.app_toolbar as appToolbar
import kotlinx.android.synthetic.main.include_appbar.toolbar_title as toolbarTitle

/** @author Aidan Follestad (afollestad) */
class SettingsActivity : DarkModeSwitchActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)

    toolbarTitle.setText(R.string.settings)
    setIsInRoot(true)
    toolbar.setNavigationOnClickListener {
      if (supportFragmentManager.backStackEntryCount > 0) {
        supportFragmentManager.popBackStack()
      } else {
        navigateUpTo(Intent(this, MainActivity::class.java))
      }
    }

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .replace(R.id.container, SettingsFragment())
          .commit()
    }
  }

  fun setIsInRoot(root: Boolean) {
    if (root) {
      toolbar.navigationIcon = getDrawable(R.drawable.ic_close)
    } else {
      toolbar.navigationIcon = getDrawable(R.drawable.ic_back)
    }
  }

  fun invalidateToolbarElevation(scrollY: Int) {
    if (scrollY > (toolbar.measuredHeight / 2)) {
      appToolbar.elevation = resources.getDimension(R.dimen.raised_toolbar_elevation)
    } else {
      appToolbar.elevation = 0f
    }
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount > 0) {
      supportFragmentManager.popBackStack()
    } else {
      super.onBackPressed()
    }
  }
}
