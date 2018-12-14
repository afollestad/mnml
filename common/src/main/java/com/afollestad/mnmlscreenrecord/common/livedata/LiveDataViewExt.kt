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
package com.afollestad.mnmlscreenrecord.common.livedata

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.afollestad.mnmlscreenrecord.common.view.onTextChanged
import com.afollestad.mnmlscreenrecord.common.view.showOrHide
import kotlin.math.min

fun EditText.setTextAndMaintainSelection(text: CharSequence) {
  val formerStart = min(selectionStart, text.length)
  val formerEnd = min(selectionEnd, text.length)
  setText(text)
  if (formerEnd <= formerStart) {
    setSelection(formerStart)
  } else {
    setSelection(formerStart, formerEnd)
  }
}

inline fun <reified T> EditText.attachLiveData(
  lifecycleOwner: LifecycleOwner,
  data: MutableLiveData<T>,
  debounce: Int = 100,
  pushOutChanges: Boolean = true,
  pullInChanges: Boolean = false
) {
  // Initial value
  if (T::class == String::class) {
    if (data.value != null) {
      this.setText(data.value as? String)
    } else {
      data.value = this.text.trim().toString() as T
    }
  } else if (T::class == Int::class) {
    if (data.value != null) {
      this.setText(data.value.toString())
    } else {
      data.value = (this.text.trim().toString().toIntOrNull() ?: 0) as T
    }
  }
  // Out
  if (pushOutChanges) {
    when {
      T::class == Int::class -> {
        onTextChanged(debounce) { data.postValue(it.trim().toInt() as T) }
      }
      T::class == Long::class -> {
        onTextChanged(debounce) { data.postValue(it.trim().toLong() as T) }
      }
      T::class == String::class -> {
        onTextChanged(debounce) { data.postValue(it as T) }
      }
      else -> {
        throw IllegalArgumentException("Can't send EditText text changes into ${T::class}")
      }
    }
  }
  // In
  if (pullInChanges) {
    data.distinct()
      .observe(lifecycleOwner, Observer {
        when {
          T::class == Int::class -> setText(it as Int)
          T::class == String::class -> setTextAndMaintainSelection(it as String)
        }
      })
  }
}

fun LiveData<Int?>.toViewError(
  owner: LifecycleOwner,
  view: View,
  setter: (String?) -> Unit
) = observe(owner, Observer { error ->
  setter(
    if (error != null) {
      view.resources.getString(error)
    } else {
      null
    }
  )
})

fun LiveData<Int?>.toViewError(
  owner: LifecycleOwner,
  view: EditText
) = toViewError(owner, view, view::setError)

fun LiveData<*>.toViewText(
  owner: LifecycleOwner,
  view: TextView
) = distinct().observe(owner, Observer {
  when (it) {
    is Int -> view.setText(it)
    is String -> view.text = it
    else -> throw IllegalStateException("Can't set $it to a text view.")
  }
})

fun LiveData<Boolean>.toViewVisibility(
  owner: LifecycleOwner,
  view: View
) = distinct().observe(owner, Observer { view.showOrHide(it) })
