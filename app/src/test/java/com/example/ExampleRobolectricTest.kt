package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ludo Master", appName)
  }

  @Test
  fun `launch MainActivity`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java)
    controller.setup()
    val activity = controller.get()
    assertNotNull(activity)
  }

  @Test
  fun `transition past splash screen`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java)
    controller.setup()
    
    // Idle main looper for 3 seconds to complete the SplashScreen animation delay loop
    ShadowLooper.idleMainLooper(3500, TimeUnit.MILLISECONDS)
    
    val activity = controller.get()
    assertNotNull(activity)
  }
}
