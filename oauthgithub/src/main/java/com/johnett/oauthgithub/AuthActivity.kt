package com.johnett.oauthgithub

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


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
    scopeAppendToUrl = "";

    val intent: Intent = intent

    if (intent.extras != null) {
      CLIENT_ID = intent.getStringExtra("id")!!
      PACKAGE = intent.getStringExtra("package")!!
      CLIENT_SECRET = intent.getStringExtra("secret")!!
      ACTIVITY_NAME = intent.getStringExtra("activity")!!
      debug = intent.getBooleanExtra("debug", false);
      isScopeDefined = intent.getBooleanExtra("isScopeDefined", false);
      clearDataBeforeLaunch = intent.getBooleanExtra("clearData", false);
    } else {
      Log.d(TAG, "intent extras null");
      finish();
    }

    var urlLoad = "$GITHUB_URL?client_id=$CLIENT_ID";

    if (isScopeDefined) {
      scopeList = intent.getStringArrayListExtra("scope_list")
      scopeAppendToUrl = getCsvFromList(scopeList as java.util.ArrayList<String>)
      urlLoad += "&scope=$scopeAppendToUrl"
    }

    if (debug) {
      Log.d(
        TAG, "intent received is "
            + "\n-client id: " + CLIENT_ID
            + "\n-secret:" + CLIENT_SECRET
            + "\n-activity: " + ACTIVITY_NAME
            + "\n-Package: " + PACKAGE
      );
      Log.d(TAG, "onCreate: Scope request are : $scopeAppendToUrl")
    }

    if (clearDataBeforeLaunch) {
      clearDataBeforeLaunch()
    }

    webView = findViewById(R.id.webView)

    if (webView == null) {
      return
    }

    webView!!.settings.javaScriptEnabled = true;
    webView!!.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        try {
          if (url != null) {
            if (!url.contains("?code=")) return false
          };

          CODE = url?.substring(url.lastIndexOf("?code=") + 1)!!
          val tokenCode: List<String> = CODE.split("=")
          val tokenFetchedIs: String = tokenCode[1]
          val cleanToken: List<String> = tokenFetchedIs.split("&");

          fetchOauthTokenWithCode(cleanToken[0]);

          if (debug) {
            Log.d(TAG, "code fetched is: $CODE");
            Log.d(TAG, "code token: " + tokenCode[1]);
            Log.d(TAG, "token cleaned is: " + cleanToken[0]);
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
        return false;
      }
    }


    webView!!.loadUrl(urlLoad)
  }

  fun clearDataBeforeLaunch() {
    val cookieManager: CookieManager = CookieManager.getInstance();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cookieManager.removeAllCookies {
        // a callback which is executed when the cookies have been removed
        @Override
        fun onReceiveValue(aBoolean: Boolean) {
          Log.d(TAG, "Cookie removed: $aBoolean");
        }
      };
    } else {
      //noinspection deprecation
      cookieManager.removeAllCookie();
    }
  }

  fun fetchOauthTokenWithCode(code: String) {
    val client = OkHttpClient()
    val url: HttpUrl.Builder = GITHUB_OAUTH.toHttpUrlOrNull()!!.newBuilder()
    url.addQueryParameter("client_id", CLIENT_ID);
    url.addQueryParameter("client_secret", CLIENT_SECRET);
    url.addQueryParameter("code", code);

    val urlOauth: String = url.build().toString();

    val request: Request = Request.Builder()
      .header("Accept", "application/json")
      .url(urlOauth)
      .build();

    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        if (debug) {
          Log.d(TAG, "IOException: " + e.message);
        }

        finishThisActivity(ERROR)
      }

      override fun onResponse(call: Call, response: Response) {
        response.use {
          if (!response.isSuccessful) throw IOException("Unexpected code $response")

          if (response.isSuccessful) {
            val jsonData: String = response.body.toString()

            if (debug) {
              Log.d(TAG, "response is: $jsonData");
            }

            try {
              val jsonObject: JSONObject = JSONObject(jsonData);
              val authToken: String = jsonObject.getString("access_token");

              storeToSharedPreference(authToken);

              if (debug) {
                Log.d(TAG, "token is: $authToken");
              }

            } catch (exp: JSONException) {
              if (debug) {
                Log.d(TAG, "json exception: " + exp.message)
              }
            }

          } else {
            if (debug) {
              Log.d(TAG, "onResponse: not success: " + response.message)
            }
          }

          finishThisActivity(SUCCESS)
        }
      }
    })
  }

  // Allow web view to go back a page.
  override fun onBackPressed() {
    if (webView!!.canGoBack()) {
      webView!!.goBack();
    } else {
      super.onBackPressed();
    }
  }

  fun storeToSharedPreference(authToken: String) {
    val prefs: SharedPreferences = getSharedPreferences("github_prefs", MODE_PRIVATE);
    val edit: SharedPreferences.Editor = prefs.edit();

    edit.putString("oauth_token", authToken);
    edit.apply();
  }

  /**
   * Finish this activity and returns the result
   *
   * @param resultCode one of the constants from the class ResultCode
   */
  fun finishThisActivity(resultCode: Int) {
    setResult(resultCode);
    finish();
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
