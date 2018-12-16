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
package com.afollestad.mnmlscreenrecord.testutil

import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/** @author Aidan Follestad (@afollestad) */
class TestLiveData<T>(data: LiveData<T>) {

  private val receivedValues = mutableListOf<T>()
  private val observer = Observer<T> { emission ->
    emission?.let { receivedValues.add(it) }
  }

  init {
    data.observeForever(observer)
  }

  fun assertNoValues() {
    if (receivedValues.isNotEmpty()) {
      throw AssertionError("Expected no values, but got: $receivedValues")
    }
  }

  fun assertValues(vararg assertValues: T) {
    val assertList = assertValues.toList()
    if (!assertList.contentEquals(receivedValues)) {
      throw AssertionError("Expected $assertList\n\t\tBut got: $receivedValues")
    }
    receivedValues.clear()
  }

  @CheckResult
  fun values(): List<T> = receivedValues

  private fun List<T>.contentEquals(other: List<T>): Boolean {
    if (this.size != other.size) {
      return false
    }
    for ((index, value) in this.withIndex()) {
      if (other[index] != value) {
        return false
      }
    }
    return true
  }
}

@CheckResult
fun <T> LiveData<T>.test() = TestLiveData(this)
