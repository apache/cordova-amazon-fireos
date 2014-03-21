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
package org.apache.cordova;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.amazon.android.webkit.AmazonConsoleMessage;
import com.amazon.android.webkit.AmazonJsPromptResult;
import com.amazon.android.webkit.AmazonJsResult;
import com.amazon.android.webkit.AmazonValueCallback;
import com.amazon.android.webkit.AmazonWebChromeClient;
import com.amazon.android.webkit.AmazonWebStorage;
import com.amazon.android.webkit.AmazonWebView;
import com.amazon.android.webkit.AmazonGeolocationPermissions;
import com.amazon.android.webkit.AmazonMediaDeviceSettings;


import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * This class is the AmazonWebChromeClient that implements callbacks for our web view.
 * The kind of callbacks that happen here are on the chrome outside the document,
 * such as onCreateWindow(), onConsoleMessage(), onProgressChanged(), etc. Related
 * to but different than CordovaWebViewClient.
 *
 * @see <a href="http://developer.android.com/reference/android/webkit/WebChromeClient.html">WebChromeClient</a>
 * @see <a href="http://developer.android.com/guide/webapps/webview.html">WebView guide</a>
 * @see CordovaWebViewClient
 * @see CordovaWebView
 */
public class CordovaChromeClient extends AmazonWebChromeClient {

    public static final int FILECHOOSER_RESULTCODE = 5173;
    private static final String LOG_TAG = "CordovaChromeClient";
    private String TAG = "CordovaLog";

    /* Using a conservative database quota (used primarily for the stock Android back-end) */
    private static final long MAX_QUOTA = 100 * 1024 * 1024;
    
    protected CordovaInterface cordova;
    protected CordovaWebView appView;

    // the video progress view
    private View mVideoProgressView;
    
    // File Chooser
    public AmazonValueCallback<Uri> mUploadMessage;
    
    /**
     * Constructor.
     *
     * @param cordova
     */
    public CordovaChromeClient(CordovaInterface cordova) {
        this.cordova = cordova;
    }

    /**
     * Constructor.
     * 
     * @param ctx
     * @param app
     */
    public CordovaChromeClient(CordovaInterface ctx, CordovaWebView app) {
        this.cordova = ctx;
        this.appView = app;
    }

    /**
     * Constructor.
     * 
     * @param view
     */
    public void setWebView(CordovaWebView view) {
        this.appView = view;
    }

    /**
     * Tell the client to display a javascript alert dialog.
     *
     * @param view
     * @param url
     * @param message
     * @param result
     */
    @Override
    public boolean onJsAlert(AmazonWebView view, String url, String message, final AmazonJsResult result) {
        Object returnVal = processJsCallback(url, message, null, result, "onJsAlert");
        if (returnVal != null && returnVal instanceof Boolean && (Boolean) returnVal) {
            return true;
        }
        AlertDialog.Builder dlg = new AlertDialog.Builder(this.cordova.getActivity());
        dlg.setMessage(message);
        dlg.setTitle("Alert");
        //Don't let alerts break the back button
        dlg.setCancelable(true);
        dlg.setPositiveButton(android.R.string.ok,
                new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        dlg.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //DO NOTHING
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    result.confirm();
                    return false;
                }
                else
                    return true;
            }
        });
        dlg.show();
        return true;
    }

    /**
     * Tell the client to display a confirm dialog to the user.
     *
     * @param view
     * @param url
     * @param message
     * @param result
     */
    @Override
    public boolean onJsConfirm(AmazonWebView view, String url, String message, final AmazonJsResult result) {
        Object returnVal = processJsCallback(url, message, null, result, "onJsCofirm");
        if (returnVal != null && returnVal instanceof Boolean && (Boolean) returnVal) {
            return true;
        }
        AlertDialog.Builder dlg = new AlertDialog.Builder(this.cordova.getActivity());
        dlg.setMessage(message);
        dlg.setTitle("Confirm");
        dlg.setCancelable(true);
        dlg.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        dlg.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
        dlg.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //DO NOTHING
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    result.cancel();
                    return false;
                }
                else
                    return true;
            }
        });
        dlg.show();
        return true;
    }

    
    /**
     * Tell the client to display a prompt dialog to the user.
     * If the client returns true, AmazonWebView will assume that the client will
     * handle the prompt dialog and call the appropriate AmazonJsPromptResult method.
     *
     * Since we are hacking prompts for our own purposes, we should not be using them for
     * this purpose, perhaps we should hack console.log to do this instead!
     *
     * @param view
     * @param url
     * @param message
     * @param defaultValue
     * @param result
     */
    @Override
    public boolean onJsPrompt(AmazonWebView view, String url, String message, String defaultValue, AmazonJsPromptResult result) {

        // Security check to make sure any requests are coming from the page initially
        // loaded in webview and not another loaded in an iframe.
        boolean reqOk = false;
        if (url.startsWith("file://") || Config.isUrlWhiteListed(url)) {
            reqOk = true;
        }

        // Calling PluginManager.exec() to call a native service using 
        // prompt(this.stringify(args), "gap:"+this.stringify([service, action, callbackId, true]));
        if (reqOk && defaultValue != null && defaultValue.length() > 3 && defaultValue.substring(0, 4).equals("gap:")) {
            JSONArray array;
            try {
                array = new JSONArray(defaultValue.substring(4));
                String service = array.getString(0);
                String action = array.getString(1);
                String callbackId = array.getString(2);
                String r = this.appView.exposedJsApi.exec(service, action, callbackId, message);
                result.confirm(r == null ? "" : r);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Sets the native->JS bridge mode. 
        else if (reqOk && defaultValue != null && defaultValue.equals("gap_bridge_mode:")) {
        	try {
                this.appView.exposedJsApi.setNativeToJsBridgeMode(Integer.parseInt(message));
                result.confirm("");
        	} catch (NumberFormatException e){
                result.confirm("");
                e.printStackTrace();
        	}
        }

        // Polling for JavaScript messages 
        else if (reqOk && defaultValue != null && defaultValue.equals("gap_poll:")) {
            String r = this.appView.exposedJsApi.retrieveJsMessages("1".equals(message));
            result.confirm(r == null ? "" : r);
        }

        // Do NO-OP so older code doesn't display dialog
        else if (defaultValue != null && defaultValue.equals("gap_init:")) {
            result.confirm("OK");
        }

        // Show dialog
        else {       	
            Object returnVal = processJsCallback(url, message, defaultValue, result, "onJsPrompt");
            if (returnVal != null && returnVal instanceof Boolean && (Boolean) returnVal) {
                return true;
            }
            final AmazonJsPromptResult res = result;
            AlertDialog.Builder dlg = new AlertDialog.Builder(this.cordova.getActivity());
            dlg.setMessage(message);
            final EditText input = new EditText(this.cordova.getActivity());
            if (defaultValue != null) {
                input.setText(defaultValue);
            }
            dlg.setView(input);
            dlg.setCancelable(false);
            dlg.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String usertext = input.getText().toString();
                            res.confirm(usertext);
                        }
                    });
            dlg.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            res.cancel();
                        }
                    });
            dlg.show();
        }
        return true;
    }

    /**
     * Handle database quota exceeded notification.
     */
    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
            long totalUsedQuota, AmazonWebStorage.QuotaUpdater quotaUpdater)
    {
        LOG.d(TAG, "onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", estimatedSize, currentQuota, totalUsedQuota);
        quotaUpdater.updateQuota(MAX_QUOTA);
    }

    // console.log in api level 7: http://developer.android.com/guide/developing/debug-tasks.html
    // Expect this to not compile in a future Android release!
    @SuppressWarnings("deprecation")
    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID)
    {
        //This is only for Android 2.1
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.ECLAIR_MR1)
        {
            LOG.d(TAG, "%s: Line %d : %s", sourceID, lineNumber, message);
            super.onConsoleMessage(message, lineNumber, sourceID);
        }
    }

    @TargetApi(8)
    @Override
    public boolean onConsoleMessage(AmazonConsoleMessage consoleMessage)
    {
        if (consoleMessage.message() != null)
            LOG.d(TAG, "%s: Line %d : %s" , consoleMessage.sourceId() , consoleMessage.lineNumber(), consoleMessage.message());
         return super.onConsoleMessage(consoleMessage);
    }

    
    /**
     * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified
     * origin.
     * <p>
     * Note- This prompt is displayed when web content from the specified origin is attempting to use the Geolocation
     * API
     * <ul>
     * <li>1. getCurrentPosition(PositionCallback successCallback, PositionErrorCallback errorCallback, optional
     * PositionOptions options)</li>
     * <li>2. watchPosition(PositionCallback successCallback, PositionErrorCallback errorCallback, optional
     * PositionOptions options)</li>
     * </ul>
     * 
     * @param origin
     * @param callback
     */
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, AmazonGeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }
    
    // API level 7 is required for this, see if we could lower this using something else
    @Override
    public void onShowCustomView(View view, AmazonWebChromeClient.CustomViewCallback callback) {
        this.appView.showCustomView(view, callback);
    }

	@Override
	public void onHideCustomView() {
    	this.appView.hideCustomView();
	}
    
    @Override
    /**
     * Ask the host application for a custom progress view to show while
     * a <video> is loading.
     * @return View The progress view.
     */
    public View getVideoLoadingProgressView() {

	    if (mVideoProgressView == null) {	        
	    	// Create a new Loading view programmatically.
	    	
	    	// create the linear layout
	    	LinearLayout layout = new LinearLayout(this.appView.getContext());
	        layout.setOrientation(LinearLayout.VERTICAL);
	        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
	        layout.setLayoutParams(layoutParams);
	        // the proress bar
	        ProgressBar bar = new ProgressBar(this.appView.getContext());
	        LinearLayout.LayoutParams barLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	        barLayoutParams.gravity = Gravity.CENTER;
	        bar.setLayoutParams(barLayoutParams);   
	        layout.addView(bar);
	        
	        mVideoProgressView = layout;
	    }
    return mVideoProgressView; 
    }
    
    public void openFileChooser(AmazonValueCallback<Uri> uploadMsg) {
        this.openFileChooser(uploadMsg, "*/*");
    }

    public void openFileChooser( AmazonValueCallback<Uri> uploadMsg, String acceptType ) {
        this.openFileChooser(uploadMsg, acceptType, null);
    }
    
    public void openFileChooser(AmazonValueCallback<Uri> uploadMsg, String acceptType, String capture)
    {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        this.cordova.getActivity().startActivityForResult(Intent.createChooser(i, "File Browser"),
                FILECHOOSER_RESULTCODE);
    }
    
    public AmazonValueCallback<Uri> getValueCallback() {
        return this.mUploadMessage;
    }
    
    /**
     * Notify the host application that media access is denied.
     * <p>
     * Note- getUserMedia() JS API is currently not supported by AmazonWebView
     * 
     * @param origin
     *            The origin of the web content attempting to use the media device request api
     * @param callback
     *            The callback to use to set the permission state for the origin
     */
    @Override
    public void onMediaDevicePermissionsShowPrompt(String origin, AmazonMediaDeviceSettings.Callback callback) {
        // Currently, media access should always be denied
        callback.invoke(false, true);
    }
    
    /**
     * This method will give chance for plugin to handle onJsAlert, onJsConfirm, onJsPrompt callback
     * 
     * @param url
     * @param message
     * @param defaultValue
     * @param result
     * @param messageType
     * @return
     */
    private Object processJsCallback(String url, String message, String defaultValue, Object result, String messageType) {
        if (appView.pluginManager != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("url", url);
                data.put("message", message);
                if (messageType.equals("onJsPrompt")) {
                    data.put("defaultValue", defaultValue);
                }
                data.put("result", result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return appView.pluginManager.postMessage(messageType, data);
            
        }
        return null;
    }
}
