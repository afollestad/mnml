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

package com.afollestad.mnmlscreenrecord

import android.app.Application
import com.afollestad.mnmlscreenrecord.BuildConfig.DEBUG
import com.afollestad.mnmlscreenrecord.common.commonModule
import com.afollestad.mnmlscreenrecord.common.prefModule
import com.afollestad.mnmlscreenrecord.di.mainModule
import com.afollestad.mnmlscreenrecord.di.viewModelModule
import com.afollestad.mnmlscreenrecord.engine.engineModule
import com.afollestad.mnmlscreenrecord.logging.FabricTree
import com.afollestad.mnmlscreenrecord.notifications.Notifications
import com.afollestad.mnmlscreenrecord.notifications.notificationsModule
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

/** @author Aidan Follestad (@afollestad) */
class MnmlApp : Application() {

  override fun onCreate() {
    super.onCreate()

    if (DEBUG) {
      Timber.plant(DebugTree())
    }

    Timber.plant(FabricTree())
    Fabric.with(this, Crashlytics())

    startKoin {
      androidLogger()
      androidContext(this@MnmlApp)
      modules(
          commonModule,
          notificationsModule,
          prefModule,
          engineModule,
          mainModule,
          viewModelModule
      )
    }

    val notifications by inject<Notifications>()
    notifications.createChannels()
  }
}
