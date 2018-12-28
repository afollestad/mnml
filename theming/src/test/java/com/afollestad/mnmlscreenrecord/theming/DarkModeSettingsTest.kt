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
package com.afollestad.mnmlscreenrecord.theming

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

/** @author Aidan Follestad (afollestad) */
class DarkModeSettingsTest {

  @Test fun isNotEnabled_off() {
    val settings = DarkModeSettings(
        false,
        false,
        "22:30",
        "8:30",
        nowProvider("23:00")
    )
    assertThat(settings.isEnabled()).isFalse()
  }

  @Test fun isNotEnabled_earlierHour() {
    val settings = DarkModeSettings(
        true,
        true,
        "22:30",
        "8:30",
        nowProvider("21:30")
    )
    assertThat(settings.isEnabled()).isFalse()
  }

  @Test fun isNotEnabled_earlierMinute() {
    val settings = DarkModeSettings(
        true,
        true,
        "22:30",
        "8:30",
        nowProvider("22:29")
    )
    assertThat(settings.isEnabled()).isFalse()
  }

  @Test fun isNotEnabled_laterMinute() {
    val settings = DarkModeSettings(
        true,
        true,
        "22:30",
        "8:30",
        nowProvider("8:31")
    )
    assertThat(settings.isEnabled()).isFalse()
  }

  @Test fun isNotEnabled_laterHour() {
    val settings = DarkModeSettings(
        true,
        true,
        "22:30",
        "8:30",
        nowProvider("9:30")
    )
    assertThat(settings.isEnabled()).isFalse()
  }

  @Test fun isEnabled_notAutomatic_on() {
    val settings = DarkModeSettings(
        true,
        false,
        "",
        "",
        nowProvider("8:30")
    )
    assertThat(settings.isEnabled()).isTrue()
  }

  @Test fun isEnabled_automatic_atStartTimeExactly() {
    val settings = DarkModeSettings(
        true,
        true,
        "22:30",
        "8:30",
        nowProvider(time = "22:30")
    )
    assertThat(settings.isEnabled()).isTrue()
  }

  @Test fun isEnabled_automatic_inRange() {
    val settings = DarkModeSettings(
        true,
        true,
        "22:30",
        "8:30",
        nowProvider(time = "1:30", nextDay = true)
    )
    assertThat(settings.isEnabled()).isTrue()
  }

  private fun nowProvider(
    time: String,
    nextDay: Boolean = false
  ): () -> Calendar = {
    calendarForTime(time = time.splitTime(), end = nextDay)
  }
}
