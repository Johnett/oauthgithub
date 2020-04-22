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

import android.app.Activity
import android.content.Context
import android.content.Intent

class Authentication {
  private var clientId: String? = null
  private var clientSecret: String? = null
  private var nextActivity: String? = null
  private var appContext: Activity? = null
  private var debug = false
  private var packageName: String? = null
  private var scopeList: ArrayList<String?>? = null
  private var clearBeforeLaunch = false

  fun isDebug(): Boolean {
    return debug
  }

  fun getScopeList(): ArrayList<String?>? {
    return scopeList
  }

  fun setScopeList(scopeList: ArrayList<String?>?) {
    this.scopeList = ArrayList()
    this.scopeList = scopeList
  }

  fun getPackageName(): String? {
    return packageName
  }

  fun setPackageName(packageName: String?) {
    this.packageName = packageName
  }

  fun setDebug(debug: Boolean) {
    this.debug = debug
  }

  fun Builder(): Authentication? {
    return Authentication()
  }

  fun withContext(activity: Activity?): Authentication? {
    setAppContext(activity)
    return this
  }

  fun withClientId(clientId: String?): Authentication? {
    setClientId(clientId)
    return this
  }

  fun withClientSecret(clientSecret: String?): Authentication? {
    setClientSecret(clientSecret)
    return this
  }

  fun nextActivity(activity: String?): Authentication? {
    setNextActivity(activity)
    return this
  }

  fun debug(active: Boolean): Authentication? {
    setDebug(active)
    return this
  }

  fun packageName(packageName: String?): Authentication? {
    setPackageName(packageName)
    return this
  }

  fun withScopeList(scopeList: ArrayList<String?>?): Authentication? {
    setScopeList(scopeList)
    return this
  }

  /**
   * Whether the app should clear all data (cookies and cache) before launching a new instance of
   * the webView
   *
   * @param clearBeforeLaunch true to clear data
   * @return An instance of this class
   */
  fun clearBeforeLaunch(clearBeforeLaunch: Boolean): Authentication? {
    this.clearBeforeLaunch = clearBeforeLaunch
    return this
  }

  fun setClientId(clientId: String?) {
    this.clientId = clientId
  }

  fun setClientSecret(clientSecret: String?) {
    this.clientSecret = clientSecret
  }

  fun getClientId(): String? {
    return clientId
  }

  fun getClientSecret(): String? {
    return clientSecret
  }

  fun getAppContext(): Context? {
    return appContext
  }

  fun setAppContext(appContext: Activity?) {
    this.appContext = appContext
  }

  fun getNextActivity(): String? {
    return nextActivity
  }

  fun setNextActivity(nextActivity: String?) {
    this.nextActivity = nextActivity
  }

  /**
   * This method will execute the instance created. The activity of login will be launched and
   * it will return a result after finishing its execution. The result will be one of the constants
   * hold in the class [ResultCode]
   * client_id, client_secret, package name and activity fully qualified are required
   */
  fun execute() {
    val scopeList = getScopeList()
    val githubId = getClientId()
    val githubSecret = getClientSecret()
    val hasScope = scopeList != null && scopeList.size > 0
    val intent = Intent(getAppContext(), AuthActivity::class.java)
    intent.putExtra("id", githubId)
    intent.putExtra("debug", isDebug())
    intent.putExtra("secret", githubSecret)
    intent.putExtra("package", getPackageName())
    intent.putExtra("activity", getNextActivity())
    intent.putExtra("clearData", clearBeforeLaunch)
    intent.putExtra("isScopeDefined", hasScope)
    if (hasScope) {
      intent.putStringArrayListExtra("scope_list", scopeList)
    }
    appContext!!.startActivityForResult(intent, REQUEST_CODE)
  }

  companion object {
    const val REQUEST_CODE = 1000
  }
}
