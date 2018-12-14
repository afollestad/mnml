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

import android.content.Intent

typealias Execution = (Intent) -> Unit

/**
 * A single instruction for a [IntentFilterBuilder]/[IntentReceiver].
 *
 * @author Aidan Follestad (@afollestad)
 */
sealed class Instructions {

  /** Return true if an Intent matches the instruction. */
  abstract fun matches(intent: Intent): Boolean

  /** The block to execute of [matches] returns true. */
  abstract fun execution(): Execution

  /** An instruction which matches intent actions. */
  data class OnAction(
    val action: String,
    val execution: Execution
  ) : Instructions() {

    override fun matches(intent: Intent): Boolean {
      return intent.action == action
    }

    override fun execution() = execution
  }

  /** An instruction which matches intent data schemes. */
  data class OnDataScheme(
    val scheme: String,
    val execution: Execution
  ) : Instructions() {
    override fun matches(intent: Intent): Boolean {
      return intent.data?.scheme == scheme
    }

    override fun execution() = execution
  }

  /** An instruction which matches intent categories. */
  data class OnCategory(
    val category: String,
    val execution: Execution
  ) : Instructions() {
    override fun matches(intent: Intent): Boolean {
      return intent.hasCategory(category)
    }

    override fun execution() = execution
  }
}
