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
package com.afollestad.mnmlscreenrecord.common.lifecycle

import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.lifecycle.LifecycleOwner
import com.afollestad.mnmlscreenrecord.common.R

/** @author Aidan Follestad (@afollestad) */
class ViewLifecycleOwner(view: View) : LifecycleOwner, OnAttachStateChangeListener {

  private val lifecycle = SimpleLifecycle(this)

  init {
    view.addOnAttachStateChangeListener(this)
  }

  override fun getLifecycle() = lifecycle

  override fun onViewAttachedToWindow(v: View?) = lifecycle.onCreate()

  override fun onViewDetachedFromWindow(v: View) = lifecycle.onDestroy()
}

fun View.lifecycleOwner(): LifecycleOwner {
  val tagOwner = getTag(R.id.view_lifecycle_registry) as? ViewLifecycleOwner
  return if (tagOwner != null) {
    tagOwner
  } else {
    val newOwner = ViewLifecycleOwner(this)
    setTag(R.id.view_lifecycle_registry, newOwner)
    newOwner
  }
}
