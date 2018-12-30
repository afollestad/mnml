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
package com.afollestad.mnmlscreenrecord

import com.afollestad.mnmlscreenrecord.common.permissions.PermissionChecker
import com.afollestad.mnmlscreenrecord.engine.capture.CaptureEngine
import com.afollestad.mnmlscreenrecord.engine.overlay.OverlayManager
import com.afollestad.mnmlscreenrecord.engine.recordings.Recording
import com.afollestad.mnmlscreenrecord.engine.recordings.RecordingManager
import com.afollestad.mnmlscreenrecord.engine.recordings.RecordingScanner
import com.afollestad.mnmlscreenrecord.engine.service.ServiceController
import com.afollestad.mnmlscreenrecord.notifications.Notifications
import com.afollestad.mnmlscreenrecord.testutil.InstantTaskExecutorRule
import com.afollestad.mnmlscreenrecord.testutil.test
import com.afollestad.mnmlscreenrecord.ui.main.MainViewModel
import com.afollestad.rxkprefs.Pref
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.lang.System.currentTimeMillis

/** @author Aidan Follestad (@afollestad) */
class MainViewModelTest {

  @Rule
  @JvmField
  var liveDataRule = InstantTaskExecutorRule()

  private val onCaptureStart = PublishSubject.create<Unit>()
  private val onCaptureStop = PublishSubject.create<File>()
  private val onCaptureCancel = PublishSubject.create<Unit>()
  private val onScan = PublishSubject.create<Recording>()

  private val notifications = mock<Notifications>()
  private val permissionChecker = mock<PermissionChecker> {
    on { hasOverlayPermission() } doReturn false
    on { hasStoragePermission() } doReturn false
  }
  private val captureEngine = mock<CaptureEngine> {
    on { onStart() } doReturn onCaptureStart
    on { onStop() } doReturn onCaptureStop
    on { onCancel() } doReturn onCaptureCancel
    on { isStarted() } doReturn false
  }
  private val recordingManager = mock<RecordingManager>()
  private val recordingScanner = mock<RecordingScanner> {
    on { onScan() } doReturn onScan
  }
  private val serviceController = mock<ServiceController>()
  private val overlayManager = mock<OverlayManager>()
  private val alwaysShowNotificationPref = mock<Pref<Boolean>>()

  private lateinit var viewModel: MainViewModel

  @Before
  @ExperimentalCoroutinesApi
  fun setup() {
    viewModel = MainViewModel(
        Dispatchers.Unconfined,
        Dispatchers.Unconfined,
        notifications,
        permissionChecker,
        captureEngine,
        recordingManager,
        recordingScanner,
        serviceController,
        overlayManager,
        alwaysShowNotificationPref
    )
  }

  @After
  fun tearDown() = viewModel.destroy()

  @Test
  fun onResume() {
    whenever(alwaysShowNotificationPref.get()).doReturn(true)
    val fabEnabled = viewModel.onFabEnabled()
        .test()

    viewModel.onResume()

    // Call to refreshRecordings() triggers this...
    verify(permissionChecker, times(1)).hasStoragePermission()

    viewModel.setFabEnabled(false)
    onCaptureStart.onNext(Unit)
    fabEnabled.assertValues(true, false, true)

    viewModel.setFabEnabled(false)
    onCaptureStop.onNext(mock())
    fabEnabled.assertValues(false, true)

    viewModel.setFabEnabled(false)
    onCaptureCancel.onNext(Unit)
    fabEnabled.assertValues(false, true)

    onScan.onNext(Recording(0, "", "", 0L, 0L))
    // Call to refreshRecordings() triggers this...
    verify(permissionChecker, times(2)).hasStoragePermission()
  }

  @Test
  fun onResume_alwaysShowNotification() {
    whenever(alwaysShowNotificationPref.get()).doReturn(true)

    viewModel.onResume()

    verify(notifications, times(1)).setIsAppOpen(true)
    verify(serviceController, times(1)).startService()
  }

  @Test
  fun onResume_doNotAlwaysShowNotification() {
    whenever(alwaysShowNotificationPref.get()).doReturn(false)

    viewModel.onResume()

    verify(notifications, times(1)).setIsAppOpen(true)
    verify(serviceController, times(1)).stopService()
  }

  @Test
  fun onResume_doNotAlwaysShowNotification_captureStarted() {
    whenever(alwaysShowNotificationPref.get()).doReturn(false)
    whenever(captureEngine.isStarted()).doReturn(true)

    viewModel.onResume()

    verify(notifications, times(1)).setIsAppOpen(true)
    verify(serviceController, never()).stopService()
  }

  @Test
  fun onPause() {
    viewModel.onPause()
    verify(notifications).setIsAppOpen(false)

    onScan.onNext(Recording(0, "", "", 0L, 0L))
    // Call to refreshRecordings() triggers this...
    verify(permissionChecker, never()).hasStoragePermission()
  }

  @Test
  fun refreshRecordings_needStoragePermission() {
    whenever(permissionChecker.hasStoragePermission()).doReturn(false)
    val emptyViewVisibility = viewModel.onEmptyViewVisibility()
        .test()
    val needStoragePermission = viewModel.onNeedStoragePermission()
        .test()
    val onRecordings = viewModel.onRecordings()
        .test()
    viewModel.refreshRecordings()

    emptyViewVisibility.assertValues(true)
    needStoragePermission.assertValueCount(1)
    onRecordings.assertNoValues()
    verify(recordingManager, never()).getRecordings()
  }

  @Test
  fun refreshRecordings() {
    val recording = Recording(
        1,
        "/sdcard/hello.mp4",
        "Hello",
        currentTimeMillis(),
        1024
    )
    val recordings = listOf(recording)
    whenever(permissionChecker.hasStoragePermission()).doReturn(true)
    whenever(recordingManager.getRecordings()).doReturn(recordings)

    val emptyViewVisibility = viewModel.onEmptyViewVisibility()
        .test()
    val needStoragePermission = viewModel.onNeedStoragePermission()
        .test()
    val onRecordings = viewModel.onRecordings()
        .test()
    viewModel.refreshRecordings()

    emptyViewVisibility.assertValues(false, false)
    needStoragePermission.assertNoValues()
    onRecordings.assertValues(recordings)
  }

  @Test
  fun deleteRecording() {
    val recording = Recording(
        1,
        "/sdcard/hello.mp4",
        "Hello",
        currentTimeMillis(),
        1024
    )
    viewModel.deleteRecordings(listOf(recording))

    verify(recordingManager, times(1)).deleteRecording(recording)
    // Call to refreshRecordings() triggers this...
    verify(permissionChecker, times(1)).hasStoragePermission()
  }

  @Test
  fun fabClicked_alreadyStarted() {
    whenever(captureEngine.isStarted()).doReturn(true)
    val fabEnabled = viewModel.onFabEnabled()
        .test()
    viewModel.fabClicked()

    fabEnabled.assertValues(false)
    verify(serviceController, times(1)).stopRecording(true)
    verify(serviceController, never()).startRecording()
  }

  @Test
  fun fabClicked_needStoragePermission() {
    whenever(permissionChecker.hasStoragePermission()).doReturn(false)
    whenever(captureEngine.isStarted()).doReturn(false)
    val fabEnabled = viewModel.onFabEnabled()
        .test()
    val onNeedStoragePermission = viewModel.onNeedStoragePermission()
        .test()
    val onNeedOverlayPermission = viewModel.onNeedOverlayPermission()
        .test()
    viewModel.fabClicked()

    fabEnabled.assertValues(false)
    onNeedStoragePermission.assertValueCount(1)
    onNeedOverlayPermission.assertNoValues()
    verify(serviceController, never()).stopRecording(any())
    verify(serviceController, never()).startRecording()
  }

  @Test
  fun fabClicked_needOverlayPermission() {
    whenever(permissionChecker.hasStoragePermission()).doReturn(true)
    whenever(permissionChecker.hasOverlayPermission()).doReturn(false)
    whenever(overlayManager.willCountdown()).doReturn(true)
    whenever(captureEngine.isStarted()).doReturn(false)
    val fabEnabled = viewModel.onFabEnabled()
        .test()
    val onNeedStoragePermission = viewModel.onNeedStoragePermission()
        .test()
    val onNeedOverlayPermission = viewModel.onNeedOverlayPermission()
        .test()
    viewModel.fabClicked()

    fabEnabled.assertValues(false)
    onNeedStoragePermission.assertNoValues()
    onNeedOverlayPermission.assertValueCount(1)
    verify(serviceController, never()).stopRecording(any())
    verify(serviceController, never()).startRecording()
  }

  @Test
  fun fabClicked_needOverlayPermission_but_countdownDisabled() {
    whenever(permissionChecker.hasStoragePermission()).doReturn(true)
    whenever(permissionChecker.hasOverlayPermission()).doReturn(false)
    whenever(overlayManager.willCountdown()).doReturn(false)
    whenever(captureEngine.isStarted()).doReturn(false)
    val fabEnabled = viewModel.onFabEnabled()
        .test()
    val onNeedStoragePermission = viewModel.onNeedStoragePermission()
        .test()
    val onNeedOverlayPermission = viewModel.onNeedOverlayPermission()
        .test()
    viewModel.fabClicked()

    fabEnabled.assertValues(false)
    onNeedStoragePermission.assertNoValues()
    onNeedOverlayPermission.assertNoValues()
    verify(serviceController, never()).stopRecording(any())
    verify(serviceController).startRecording()
  }

  @Test
  fun fabClicked_start() {
    whenever(permissionChecker.hasStoragePermission()).doReturn(true)
    whenever(permissionChecker.hasOverlayPermission()).doReturn(true)
    whenever(overlayManager.willCountdown()).doReturn(true)
    whenever(captureEngine.isStarted()).doReturn(false)
    val fabEnabled = viewModel.onFabEnabled()
        .test()
    val onNeedStoragePermission = viewModel.onNeedStoragePermission()
        .test()
    val onNeedOverlayPermission = viewModel.onNeedOverlayPermission()
        .test()
    viewModel.fabClicked()

    fabEnabled.assertValues(false)
    onNeedStoragePermission.assertNoValues()
    onNeedOverlayPermission.assertNoValues()
    verify(serviceController, never()).stopRecording(any())
    verify(serviceController).startRecording()
  }

  @Test
  fun permissionGranted_fabPreviouslyClicked() {
    whenever(permissionChecker.hasStoragePermission()).doReturn(true)
    whenever(captureEngine.isStarted()).doReturn(false)
    whenever(overlayManager.willCountdown()).doReturn(true)

    whenever(permissionChecker.hasOverlayPermission()).doReturn(false)
    viewModel.fabClicked()
    verifyNoMoreInteractions(serviceController)

    whenever(permissionChecker.hasOverlayPermission()).doReturn(true)
    viewModel.permissionGranted()
    verify(serviceController).startRecording()
  }

  @Test
  fun permissionGranted_noOp() {
    whenever(permissionChecker.hasStoragePermission()).doReturn(true)
    whenever(permissionChecker.hasOverlayPermission()).doReturn(true)
    whenever(captureEngine.isStarted()).doReturn(false)

    viewModel.permissionGranted()
    verifyNoMoreInteractions(serviceController)
  }

  @Test
  fun invalidateFab_isStarted() {
    whenever(captureEngine.isStarted()).doReturn(true)
    val fabEnabled = viewModel.onFabEnabled()
        .test()
    val fabColor = viewModel.onFabColorRes()
        .test()
    val fabIcon = viewModel.onFabIconRes()
        .test()
    val fabText = viewModel.onFabTextRes()
        .test()

    viewModel.invalidateFab()
    fabEnabled.assertValues(true)
    fabColor.assertValues(R.color.red)
    fabIcon.assertValues(R.drawable.ic_stop_32dp)
    fabText.assertValues(R.string.stop_recording)
  }

  @Test
  fun invalidateFab_notStarted() {
    whenever(captureEngine.isStarted()).doReturn(false)
    val fabEnabled = viewModel.onFabEnabled()
        .test()
    val fabColor = viewModel.onFabColorRes()
        .test()
    val fabIcon = viewModel.onFabIconRes()
        .test()
    val fabText = viewModel.onFabTextRes()
        .test()

    viewModel.invalidateFab()
    fabEnabled.assertValues(true)
    fabColor.assertValues(R.color.colorAccent)
    fabIcon.assertValues(R.drawable.ic_record_32dp)
    fabText.assertValues(R.string.start_recording)
  }
}
