package org.apache.cordova.test.junit;

/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.test.backbuttonmultipage;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class BackButtonMultiPageTest extends
		ActivityInstrumentationTestCase2<backbuttonmultipage> {

	private int TIMEOUT = 1000;
	backbuttonmultipage testActivity;
	private FrameLayout containerView;
	private LinearLayout innerContainer;
	private CordovaWebView testView;

	private AmazonWebViewOnUiThread mUiThread;

	public BackButtonMultiPageTest() {
		super("org.apache.cordova.test", backbuttonmultipage.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		testActivity = this.getActivity();
		// Sleep to make sure main page is properly loaded
		sleep();
		containerView = (FrameLayout) testActivity
				.findViewById(android.R.id.content);
		innerContainer = (LinearLayout) containerView.getChildAt(0);
		testView = (CordovaWebView) innerContainer.getChildAt(0);
		mUiThread = new AmazonWebViewOnUiThread(this, testView);
	}

	public void testPreconditions() {
		assertNotNull(innerContainer);
		assertNotNull(testView);
	}

	public void testViaHref() {
		testView.sendJavascript("window.location = 'sample2.html';");
		sleep();
		String url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		testView.sendJavascript("window.location = 'sample3.html';");
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample3.html"));
		boolean didGoBack = mUiThread.backHistory();
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		assertTrue(didGoBack);
		didGoBack = mUiThread.backHistory();
		sleep();
		url = mUiThread.getUrl();		
		assertTrue(url.endsWith("index.html"));
		assertTrue(didGoBack);
	}

	public void testViaLoadUrl() {
		testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
		sleep();
		String url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample3.html"));
		boolean didGoBack = mUiThread.backHistory();
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		assertTrue(didGoBack);
		didGoBack = mUiThread.backHistory();
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("index.html"));
		assertTrue(didGoBack);
	}

	public void testViaBackButtonOnView() {
		testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
		sleep();
		String url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample3.html"));
		BaseInputConnection viewConnection = new BaseInputConnection(testView,
				true);
		KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_BACK);
		KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP,
				KeyEvent.KEYCODE_BACK);
		viewConnection.sendKeyEvent(backDown);
		viewConnection.sendKeyEvent(backUp);
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		viewConnection.sendKeyEvent(backDown);
		viewConnection.sendKeyEvent(backUp);
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("index.html"));
	}

	public void testViaBackButtonOnLayout() {
		testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
		sleep();
		String url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample3.html"));
		BaseInputConnection viewConnection = new BaseInputConnection(
				containerView, true);
		KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_BACK);
		KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP,
				KeyEvent.KEYCODE_BACK);
		viewConnection.sendKeyEvent(backDown);
		viewConnection.sendKeyEvent(backUp);
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("sample2.html"));
		viewConnection.sendKeyEvent(backDown);
		viewConnection.sendKeyEvent(backUp);
		sleep();
		url = mUiThread.getUrl();
		assertTrue(url.endsWith("index.html"));
	}

	private void sleep() {
		try {
			Thread.sleep(TIMEOUT);
		} catch (InterruptedException e) {
			fail("Unexpected Timeout");
		}
	}

}
