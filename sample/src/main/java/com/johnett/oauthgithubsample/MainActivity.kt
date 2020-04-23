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
package com.johnett.oauthgithubsample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.johnett.oauthgithub.Authentication

class MainActivity : AppCompatActivity() {
  private val tokenView by lazy { findViewById<TextView>(R.id.tvToken) }
  private val loginButton by lazy { findViewById<Button>(R.id.btLogin) }
  val GITHUB_ID: String = BuildConfig.GITHUB_ID
  val GITHUB_SECRET: String = BuildConfig.GITHUB_SECRET

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    loginButton.onClickDebounced {
      Authentication()
        .Builder()
        ?.withClientId(GITHUB_ID)
        ?.withClientSecret(GITHUB_SECRET)
        ?.withContext(this)
        ?.packageName("com.johnett.oauthgithubsample")
        ?.nextActivity("com.johnett.oauthgithubsample.UserActivity")
        ?.debug(true)
        ?.execute()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    Log.d("test_case_running", "$requestCode /$resultCode")
    super.onActivityResult(requestCode, resultCode, data)
  }
}
