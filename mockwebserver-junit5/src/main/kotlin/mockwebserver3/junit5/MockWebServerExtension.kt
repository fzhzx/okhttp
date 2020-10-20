/*
 * Copyright (C) 2020 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mockwebserver3.junit5

import mockwebserver3.MockWebServer
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Runs MockWebServer for the duration of a single test method.
 *
 * In Java JUnit 5 tests (ie. tests annotated `@org.junit.jupiter.api.Test`), use this by defining
 * a field with the `@RegisterExtension` annotation.  When used with @ExtendWith, access the
 * server instance using the key MockWebServerExtension.MockWebServerKey.
 *
 * ```
 * @RegisterExtension public final MockWebServerExtension serverExtension = new MockWebServerExtension();
 * ```
 *
 * For Kotlin the `@JvmField` annotation is also necessary:
 *
 * ```
 * @JvmField @RegisterExtension val mockWebServer: MockWebServerExtension = MockWebServerExtension()
 * ```
 */
class MockWebServerExtension : BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback {
  val server: MockWebServer = MockWebServer()

  override fun beforeEach(context: ExtensionContext) {
    // Store MockWebServer in Global store in well defined location for use in other extensions
    context.getStore(ExtensionContext.Namespace.GLOBAL).put(MockWebServerKey, server)
  }

  override fun beforeTestExecution(context: ExtensionContext?) {
    try {
      server.start()
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  override fun afterEach(context: ExtensionContext) {
    try {
      server.shutdown()
    } catch (e: IOException) {
      logger.log(Level.WARNING, "MockWebServer shutdown failed", e)
    }
  }

  companion object {
    private val logger = Logger.getLogger(MockWebServerExtension::class.java.name)
    const val MockWebServerKey = "MockWebServer"
  }
}
