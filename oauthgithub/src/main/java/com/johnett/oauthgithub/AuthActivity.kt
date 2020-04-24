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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

class AuthActivity : AppCompatActivity() {

  var scopeAppendToUrl = ""
  private var scopeList: List<String>? = null
  private var webView: WebView? = null
  private var clearDataBeforeLaunch = false
  private var isScopeDefined = false
  private var debug = false

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_auth)

    scopeList = ArrayList()
    scopeAppendToUrl = ""

    val intent: Intent = intent

    if (intent.extras != null) {
      CLIENT_ID = intent.getStringExtra("id")!!
      PACKAGE = intent.getStringExtra("package")!!
      CLIENT_SECRET = intent.getStringExtra("secret")!!
      ACTIVITY_NAME = intent.getStringExtra("activity")!!
      debug = intent.getBooleanExtra("debug", false)
      isScopeDefined = intent.getBooleanExtra("isScopeDefined", false)
      clearDataBeforeLaunch = intent.getBooleanExtra("clearData", false)
    } else {
      Log.d(TAG, "intent extras null")
      finish()
    }

    var urlLoad = "$GITHUB_URL?client_id=$CLIENT_ID"

    if (isScopeDefined) {
      scopeList = intent.getStringArrayListExtra("scope_list")
      scopeAppendToUrl = getCsvFromList(scopeList as java.util.ArrayList<String>)
      urlLoad += "&scope=$scopeAppendToUrl"
    }

    if (debug) {
      Log.d(
        TAG, "intent received is " +
            "\n-client id: " + CLIENT_ID +
            "\n-secret:" + CLIENT_SECRET +
            "\n-activity: " + ACTIVITY_NAME +
            "\n-Package: " + PACKAGE
      )
      Log.d(TAG, "onCreate: Scope request are : $scopeAppendToUrl")
    }

    if (clearDataBeforeLaunch) {
      clearDataBeforeLaunch()
    }

    webView = findViewById(R.id.webView)

    if (webView == null) {
      return
    }

    webView!!.settings.javaScriptEnabled = true
    webView!!.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        try {
          if (url != null) {
            if (!url.contains("?code=")) return false
          }

          CODE = url?.substring(url.lastIndexOf("?code=") + 1)!!
          val tokenCode: List<String> = CODE.split("=")
          val tokenFetchedIs: String = tokenCode[1]
          val cleanToken: List<String> = tokenFetchedIs.split("&")

          fetchOauthTokenWithCode(cleanToken[0])

          if (debug) {
            Log.d(TAG, "code fetched is: $CODE")
            Log.d(TAG, "code token: " + tokenCode[1])
            Log.d(TAG, "token cleaned is: " + cleanToken[0])
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
        return false
      }
    }

    webView!!.loadUrl(urlLoad)
  }

  fun clearDataBeforeLaunch() {
    val cookieManager: CookieManager = CookieManager.getInstance()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cookieManager.removeAllCookies {
        // a callback which is executed when the cookies have been removed
        @Override
        fun onReceiveValue(aBoolean: Boolean) {
          Log.d(TAG, "Cookie removed: $aBoolean")
        }
      }
    } else {
      //noinspection deprecation
      cookieManager.removeAllCookie()
    }
  }

  fun fetchOauthTokenWithCode(code: String) {
    val client = OkHttpClient()
    val url: HttpUrl.Builder = GITHUB_OAUTH.toHttpUrlOrNull()!!.newBuilder()
    url.addQueryParameter("client_id", CLIENT_ID)
    url.addQueryParameter("client_secret", CLIENT_SECRET)
    url.addQueryParameter("code", code)

    val urlOauth: String = url.build().toString()

    val request: Request = Request.Builder()
      .header("Accept", "application/json")
      .url(urlOauth)
      .build()

    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        if (debug) {
          Log.d(TAG, "IOException: " + e.message)
        }

        finishThisActivity(ERROR)
      }

      override fun onResponse(call: Call, response: Response) {
        response.use {
          if (!response.isSuccessful) throw IOException("Unexpected code $response")

          if (response.isSuccessful) {
            val jsonData: String = response.body!!.string()

            if (debug) {
              Log.d(TAG, "response is: $jsonData")
            }

            try {
              Log.d("this_is_a_test", jsonData)
              val jsonObject = JSONObject(jsonData)
              val authToken = jsonObject.getString("access_token")

              storeToSharedPreference(authToken)
              finishThisActivity(SUCCESS)
              if (debug) {
                Log.d(TAG, "token is: $authToken")
              }
            } catch (exp: JSONException) {
              if (debug) {
                Log.d(TAG, "json exception: " + exp.message)
                finishThisActivity(ERROR)
              }
            }
          } else {
            if (debug) {
              Log.d(TAG, "onResponse: not success: " + response.message)
              finishThisActivity(ERROR)
            }
          }
        }
      }
    })
  }

  // Allow web view to go back a page.
  override fun onBackPressed() {
    if (webView!!.canGoBack()) {
      webView!!.goBack()
    } else {
      super.onBackPressed()
    }
  }

  fun storeToSharedPreference(authToken: String) {
    val prefs: SharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE)
    val edit: SharedPreferences.Editor = prefs.edit()

    edit.putString("token", authToken)
    edit.apply()
  }

  /**
   * Finish this activity and returns the result
   *
   * @param resultCode one of the constants from the class ResultCode
   */
  fun finishThisActivity(resultCode: Int) {
    setResult(resultCode)
    if (resultCode == 1) {
      val intent = Intent()
      intent.setClassName(PACKAGE, ACTIVITY_NAME)
      startActivity(intent)
    }
    finish()
  }

  /**
   * Generate a comma separated list of scopes out of the
   *
   * @param scopeList list of scopes as defined
   * @return comma separated list of scopes
   */
  fun getCsvFromList(scopeList: List<String>): String {
    var csvString = ""

    scopeList.forEach { scope ->
      if (csvString != "") {
        csvString += ","
      }

      csvString += scope
    }

    return csvString
  }

  companion object {
    const val SUCCESS = 1
    const val ERROR = 2
    const val GITHUB_URL = "https://github.com/login/oauth/authorize"
    const val GITHUB_OAUTH = "https://github.com/login/oauth/access_token"
    private var CODE = ""
    var PACKAGE = ""
    var CLIENT_ID = ""
    var CLIENT_SECRET = ""
    var ACTIVITY_NAME = ""
    const val TAG = "github-oauth"
  }
}
