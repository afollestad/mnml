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
package com.afollestad.mnmlscreenrecord.views

import android.content.res.ColorStateList.valueOf
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.afollestad.mnmlscreenrecord.common.livedata.distinct
import com.afollestad.mnmlscreenrecord.common.view.showOrHide
import com.google.android.material.button.MaterialButton

/**
 * Emissions from the receiving LiveData are set as the [view]'s icon.
 */
fun LiveData<Int>.asIcon(
  owner: LifecycleOwner,
  view: MaterialButton
) = distinct().observe(owner, Observer { view.setIconResource(it) })

/**
 * Emissions from the receiving LiveData are set as the [view]'s background tint.
 */
fun LiveData<Int>.asBackgroundTint(
  owner: LifecycleOwner,
  view: MaterialButton
) = distinct().observe(owner, Observer {
  val actualColor = ContextCompat.getColor(view.context, it)
  view.backgroundTintList = valueOf(actualColor)
})

/**
 * Emissions from the receiving LiveData are set as the [view]'s text.
 */
fun LiveData<Int>.asText(
  owner: LifecycleOwner,
  view: MaterialButton
) = distinct().observe(owner, Observer { view.setText(it) })

/**
 * Emissions from the receiving LiveData are set as the [view]'s enabled state.
 */
fun LiveData<Boolean>.asEnabled(
  owner: LifecycleOwner,
  view: View
) = distinct().observe(owner, Observer { view.isEnabled = it })

/**
 * Emissions from the receiving LiveData are set as the [view]'s visibility (visible or gone).
 */
fun LiveData<Boolean>.asVisibility(
  owner: LifecycleOwner,
  view: View
) = distinct().observe(owner, Observer { view.showOrHide(it) })
