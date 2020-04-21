/**
 * Designed and developed by Johnett Mathew (@Johnett)
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
package com.johnett.oauthgithub

import android.content.Context
import androidx.annotation.VisibleForTesting

/** @author Johnett Mathew (@Johnett) */
class Greeter(
  @VisibleForTesting var context: Context?
) {
  /** Greets someone with the given [name]. */
  fun greet(name: String): String {
    return context?.resources?.getString(R.string.hello_x, name).orEmpty()
  }

  /** Releases the [context]. */
  fun dispose() {
    context = null
  }
}
