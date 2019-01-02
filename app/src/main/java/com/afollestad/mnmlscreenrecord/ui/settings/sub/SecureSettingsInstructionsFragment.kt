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
package com.afollestad.mnmlscreenrecord.ui.settings.sub

import android.R.attr
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.utils.MDUtil.resolveColor
import com.afollestad.mnmlscreenrecord.BuildConfig
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.misc.toast
import com.afollestad.mnmlscreenrecord.common.view.onScroll
import com.afollestad.mnmlscreenrecord.engine.overlay.ShowTouchesManager
import com.afollestad.mnmlscreenrecord.ui.main.viewUrl
import com.afollestad.mnmlscreenrecord.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.secure_settings_instructions.adb_command
import kotlinx.android.synthetic.main.secure_settings_instructions.check_button
import kotlinx.android.synthetic.main.secure_settings_instructions.install_adb_option1
import kotlinx.android.synthetic.main.secure_settings_instructions.install_adb_option2
import kotlinx.android.synthetic.main.secure_settings_instructions.install_adb_option3
import kotlinx.android.synthetic.main.secure_settings_instructions.scrollView
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class SecureSettingsInstructionsFragment : Fragment() {

  private val showTouchesManager by inject<ShowTouchesManager>()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.secure_settings_instructions, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    scrollView.onScroll {
      val context = activity as? SettingsActivity ?: return@onScroll
      context.invalidateToolbarElevation(it)
    }

    val context = activity as SettingsActivity
    view.setBackgroundColor(resolveColor(context, attr = attr.windowBackground))
    context.setIsInRoot(false)

    adb_command.text =
        getString(R.string.write_secure_settings_step4_command, BuildConfig.APPLICATION_ID)

    install_adb_option1.setOnClickListener {
      activity?.viewUrl(getString(R.string.write_secure_settings_step3_link1))
    }
    install_adb_option2.setOnClickListener {
      activity?.viewUrl(getString(R.string.write_secure_settings_step3_link2))
    }
    install_adb_option3.setOnClickListener {
      activity?.viewUrl(getString(R.string.write_secure_settings_step3_link3))
    }

    check_button.setOnClickListener {
      if (showTouchesManager.canShowTouches()) {
        fragmentManager?.popBackStack()
      } else {
        activity?.toast(R.string.write_secure_settings_verify_denied)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    (activity as? SettingsActivity)?.run {
      setIsInRoot(false)
      invalidateToolbarElevation(scrollView.scrollY)
    }
  }
}
