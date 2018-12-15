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
@file:Suppress("unused")

package com.afollestad.mnmlscreenrecord.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [ViewModel] which provides a [launch] method that creates coroutine jobs - the jobs
 * created in the scope are cancelled when the view model is cleared.
 *
 * @author Aidan Follestad (@afollestad)
 */
abstract class ScopedViewModel(mainDispatcher: CoroutineDispatcher) : ViewModel() {

  private val job = Job()
  private val scope = CoroutineScope(job + mainDispatcher)

  protected fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
  ) = scope.launch(context, start, block)

  override fun onCleared() {
    super.onCleared()
    job.cancel()
  }

  @TestOnly open fun destroy() = job.cancel()
}
