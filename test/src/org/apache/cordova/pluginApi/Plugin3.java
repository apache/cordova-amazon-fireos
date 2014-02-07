/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */
package org.apache.cordova.pluginApi;

import org.apache.cordova.CordovaPlugin;

import android.util.Log;

public class Plugin3 extends CordovaPlugin {
    private static final String TAG = "Plugin3";
    private static final String Plugin3_URL = "file:///android_asset/www/plugins/Plugin3.html";

    /**
     * Handles onMessage call back
     * 
     * @return null and allow event to propagate. {@inheritDoc}
     */
    @Override
    public Object onMessage(String id, Object data) {
        if (id.equals("plugintest")) {
            this.webView.loadUrl(Plugin3_URL);
            Log.e(TAG, "plugintest -> data: " + data.toString());
        }
        return null;
    }

}
