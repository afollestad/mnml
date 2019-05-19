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
package com.afollestad.mnmlscreenrecord.common

import android.app.Activity
import com.afollestad.mnmlscreenrecord.common.Qualifiers.IO_DISPATCHER
import com.afollestad.mnmlscreenrecord.common.Qualifiers.MAIN_DISPATCHER
import com.afollestad.mnmlscreenrecord.common.intent.RealUrlLauncher
import com.afollestad.mnmlscreenrecord.common.intent.UrlLauncher
import com.afollestad.mnmlscreenrecord.common.permissions.PermissionChecker
import com.afollestad.mnmlscreenrecord.common.permissions.RealPermissionChecker
import com.afollestad.mnmlscreenrecord.common.providers.RealSdkProvider
import com.afollestad.mnmlscreenrecord.common.providers.SdkProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

object Qualifiers {
  const val MAIN_DISPATCHER = "main_dispatcher"
  const val IO_DISPATCHER = "io_dispatcher"
}

/** @author Aidan Follestad (@afollestad) */
val commonModule = module {

  factory<CoroutineDispatcher>(named(MAIN_DISPATCHER)) { Dispatchers.Main }

  factory(named(IO_DISPATCHER)) { Dispatchers.IO }

  factory { RealPermissionChecker(get()) } bind PermissionChecker::class

  factory { RealSdkProvider() } bind SdkProvider::class

  factory { (activity: Activity) -> RealUrlLauncher(activity) } bind UrlLauncher::class
}
