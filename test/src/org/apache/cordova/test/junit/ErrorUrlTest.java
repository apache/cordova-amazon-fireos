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
import org.apache.cordova.test.errorurl;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ErrorUrlTest extends ActivityInstrumentationTestCase2<errorurl> {

	private int TIMEOUT = 1000;
	errorurl testActivity;
	private FrameLayout containerView;
	private LinearLayout innerContainer;
	private CordovaWebView testView;

	private AmazonWebViewOnUiThread mUiThread;

	public ErrorUrlTest() {
		super("org.apache.cordova.test", errorurl.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		testActivity = this.getActivity();
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

	// This will fail for AWV because the current url on a wrong page will
	// return the URL used to load
	public void testUrl() {
		mUiThread.loadUrl("file:///android_asset/www/htmlnotfound/index.html");
		sleep();
		String good_url = "file:///android_asset/www/error.html";
		String url = mUiThread.getUrl();
		assertNotNull(url);
		assertTrue(url.equals(good_url));
	}

	private void sleep() {
		try {
			Thread.sleep(TIMEOUT);
		} catch (InterruptedException e) {
			fail("Unexpected Timeout");
		}
	}

}
