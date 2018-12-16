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

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@SuppressLint("RestrictedApi")
class InstantTaskExecutorRule : TestRule {

  private val immediateExecutor = object : TaskExecutor() {
    override fun executeOnDiskIO(runnable: Runnable) {
      runnable.run()
    }

    override fun postToMainThread(runnable: Runnable) {
      runnable.run()
    }

    override fun isMainThread(): Boolean {
      return true
    }
  }

  override fun apply(base: Statement, description: Description?): Statement {
    val taskExecutor = ArchTaskExecutor.getInstance()
    return object : Statement() {
      override fun evaluate() {
        taskExecutor.setDelegate(immediateExecutor)
        base.evaluate()
        taskExecutor.setDelegate(null)
      }
    }
  }
}
