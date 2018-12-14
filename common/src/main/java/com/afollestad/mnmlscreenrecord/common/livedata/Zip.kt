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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

typealias Zipper<T, K, R> = (T, K) -> R

/** @author Aidan Follestad (@afollestad) */
class ZipLiveData<T, K, R>(
  source1: LiveData<T>,
  source2: LiveData<K>,
  private val distinctUntilChanged: Boolean,
  private val resetAfterEmission: Boolean,
  private val zipper: Zipper<T, K, R>
) : MediatorLiveData<R>() {

  private var data1: T? = null
  private var data2: K? = null
  private var lastNotified: R? = null

  init {
    super.addSource(source1) {
      if (data1 == it) return@addSource
      data1 = it
      maybeNotify()
    }
    super.addSource(source2) {
      if (data2 == it) return@addSource
      data2 = it
      maybeNotify()
    }
  }

  private fun maybeNotify() {
    if (data1 != null && data2 != null) {
      val zippedUp = zipper(data1!!, data2!!)

      if (!distinctUntilChanged || zippedUp != lastNotified) {
        value = zippedUp
        lastNotified = zippedUp

        if (resetAfterEmission) {
          data1 = null
          data2 = null
        }
      }
    }
  }

  override fun <S : Any?> addSource(
    source: LiveData<S>,
    onChanged: Observer<in S>
  ) {
    throw UnsupportedOperationException()
  }

  override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
    throw UnsupportedOperationException()
  }
}

fun <T, K, R> zip(
  source1: LiveData<T>,
  source2: LiveData<K>,
  distinctUntilChanged: Boolean = true,
  resetAfterEmission: Boolean = false,
  zipper: Zipper<T, K, R>
) = ZipLiveData(
  source1 = source1,
  source2 = source2,
  distinctUntilChanged = distinctUntilChanged,
  resetAfterEmission = resetAfterEmission,
  zipper = zipper
)

fun <T, K> zip(
  source1: LiveData<T>,
  source2: LiveData<K>,
  distinctUntilChanged: Boolean = true,
  resetAfterEmission: Boolean = false
) = zip(
  source1 = source1,
  source2 = source2,
  distinctUntilChanged = distinctUntilChanged,
  resetAfterEmission = resetAfterEmission,
  zipper = { left, right -> Pair(left, right) })
