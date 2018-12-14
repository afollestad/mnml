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
package com.afollestad.mnmlscreenrecord.ui.main

import androidx.annotation.CheckResult
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.files.FileScanner
import com.afollestad.mnmlscreenrecord.common.permissions.PermissionChecker
import com.afollestad.mnmlscreenrecord.common.rx.plusAssign
import com.afollestad.mnmlscreenrecord.engine.capture.CaptureEngine
import com.afollestad.mnmlscreenrecord.engine.loader.Recording
import com.afollestad.mnmlscreenrecord.engine.loader.RecordingManager
import com.afollestad.mnmlscreenrecord.engine.service.ServiceController
import com.afollestad.mnmlscreenrecord.notifications.Notifications
import com.afollestad.mnmlscreenrecord.ui.ScopedViewModel
import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * The view model/presenter for the [MainActivity].
 *
 * @author Aidan Follestad (@afollestad)
 */
class MainViewModel(
  mainDispatcher: CoroutineDispatcher,
  private val ioDispatcher: CoroutineDispatcher,
  private val notifications: Notifications,
  private val permissionChecker: PermissionChecker,
  private val captureEngine: CaptureEngine,
  private val recordingManager: RecordingManager,
  private val fileScanner: FileScanner,
  private val serviceController: ServiceController
) : ScopedViewModel(mainDispatcher), LifecycleObserver {

  private lateinit var disposables: CompositeDisposable
  private var wantToStartCapture: Boolean = false

  // One-off Subjects (these do not want to storage values, just emit events)
  private val needStoragePermission = PublishSubject.create<Unit>()
  private val needOverlayPermission = PublishSubject.create<Unit>()

  // Main
  private val recordings = MutableLiveData<List<Recording>>()
  private val emptyViewVisibility = MutableLiveData<Boolean>().apply {
    value = true
  }

  // FAB
  private val fabColorRes = MutableLiveData<Int>()
  private val fabIconRes = MutableLiveData<Int>()
  private val fabTextRes = MutableLiveData<Int>()
  private val fabEnabled = MutableLiveData<Boolean>()

  // Expose properties as immutable
  @CheckResult fun onRecordings(): LiveData<List<Recording>> = recordings

  @CheckResult fun onEmptyViewVisibility(): LiveData<Boolean> = emptyViewVisibility

  @CheckResult fun onFabColorRes(): LiveData<Int> = fabColorRes

  @CheckResult fun onFabIconRes(): LiveData<Int> = fabIconRes

  @CheckResult fun onFabTextRes(): LiveData<Int> = fabTextRes

  @CheckResult fun onFabEnabled(): LiveData<Boolean> = fabEnabled

  // Expose Subjects
  @CheckResult fun onNeedStoragePermission(): Observable<Unit> = needStoragePermission

  @CheckResult fun onNeedOverlayPermission(): Observable<Unit> = needOverlayPermission

  // Lifecycle Events
  @OnLifecycleEvent(ON_CREATE)
  fun onCreate() {
    notifications.createChannels()
  }

  @OnLifecycleEvent(ON_RESUME)
  fun onResume() {
    disposables = CompositeDisposable()
    notifications.setIsAppOpen(true)

    invalidateFab()

    disposables +=
        merge(captureEngine.onStart(), captureEngine.onStop(), captureEngine.onCancel())
            .subscribe {
              fabEnabled.value = true
              invalidateFab()
            }
    disposables +=
        fileScanner.onScan()
            .subscribe { refreshRecordings() }
  }

  @OnLifecycleEvent(ON_PAUSE)
  fun onPause() {
    disposables.clear()
    notifications.setIsAppOpen(false)
  }

  // Actions
  private fun refreshRecordings() = launch {
    if (!permissionChecker.hasStoragePermission()) {
      // Can't access recordings yet
      emptyViewVisibility.value = true
      needStoragePermission.onNext(Unit)
      return@launch
    }

    val result = withContext(ioDispatcher) {
      recordingManager.getRecordings()
    }
    recordings.value = result
    emptyViewVisibility.value = result.isEmpty()
  }

  fun deleteRecording(recording: Recording) = launch {
    withContext(ioDispatcher) {
      recordingManager.deleteRecording(recording)
    }
    refreshRecordings()
  }

  fun fabClicked() {
    fabEnabled.value = false
    if (captureEngine.isStarted()) {
      serviceController.stopRecording(true)
    } else {
      if (!permissionChecker.hasStoragePermission()) {
        wantToStartCapture = true
        needStoragePermission.onNext(Unit)
        return
      } else if (!permissionChecker.hasOverlayPermission()) {
        wantToStartCapture = true
        needOverlayPermission.onNext(Unit)
        return
      }
      wantToStartCapture = false
      serviceController.startRecording()
    }
  }

  fun permissionGranted() {
    if (wantToStartCapture) {
      fabClicked()
    }
  }

  // Utility Methods
  private fun invalidateFab() {
    if (captureEngine.isStarted()) {
      fabColorRes.value = R.color.red
      fabIconRes.value = R.drawable.ic_stop_32dp
      fabTextRes.value = R.string.stop_recording
    } else {
      fabColorRes.value = R.color.colorAccent
      fabIconRes.value = R.drawable.ic_record_32dp
      fabTextRes.value = R.string.start_recording
    }
  }
}
