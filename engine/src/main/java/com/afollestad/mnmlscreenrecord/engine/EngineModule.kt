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
package com.afollestad.mnmlscreenrecord.engine

import android.app.Application
import android.content.Context
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.media.projection.MediaProjectionManager
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.view.ContextThemeWrapper
import com.afollestad.mnmlscreenrecord.common.misc.systemService
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_COUNTDOWN
import com.afollestad.mnmlscreenrecord.engine.capture.CaptureEngine
import com.afollestad.mnmlscreenrecord.engine.loader.RecordingQueryer
import com.afollestad.mnmlscreenrecord.engine.overlay.OverlayManager
import org.koin.dsl.module.module

/** @author Aidan Follestad (@afollestad) */
val engineModule = module {

  factory<LayoutInflater> {
    val themedContext = ContextThemeWrapper(get(), R.style.AppTheme)
    LayoutInflater.from(themedContext)
  }

  factory<WindowManager> {
    get<Application>().systemService(Context.WINDOW_SERVICE)
  }

  single<MediaProjectionManager> {
    get<Application>().systemService(MEDIA_PROJECTION_SERVICE)
  }

  factory { RecordingQueryer(get()) }

  single { CaptureEngine(get(), get()) }

  factory { OverlayManager(get(), get(), get(name = PREF_COUNTDOWN)) }
}
