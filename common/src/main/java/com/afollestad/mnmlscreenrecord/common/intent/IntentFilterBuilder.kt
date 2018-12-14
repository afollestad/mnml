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
package com.afollestad.mnmlscreenrecord.common.intent

import android.content.IntentFilter
import com.afollestad.mnmlscreenrecord.common.intent.Instructions.OnAction
import com.afollestad.mnmlscreenrecord.common.intent.Instructions.OnCategory
import com.afollestad.mnmlscreenrecord.common.intent.Instructions.OnDataScheme

/** @author Aidan Follestad (@afollestad) */
class IntentFilterBuilder internal constructor() {

  private val filter = IntentFilter()
  private val instructions = mutableListOf<Instructions>()

  /** Adds an Intent action to the filter. */
  fun onAction(
    action: String,
    execution: Execution
  ) {
    filter.addAction(action)
    instructions.add(OnAction(action, execution))
  }

  /** Adds a data scheme, like "http", to the filter. */
  fun onDataScheme(
    scheme: String,
    execution: Execution
  ) {
    filter.addDataScheme(scheme)
    instructions.add(OnDataScheme(scheme, execution))
  }

  /** Adds a Intent category to the filter. */
  fun onCategory(
    category: String,
    execution: Execution
  ) {
    filter.addCategory(category)
    instructions.add(OnCategory(category, execution))
  }

  internal fun filter() = filter

  internal fun instructions() = instructions
}
