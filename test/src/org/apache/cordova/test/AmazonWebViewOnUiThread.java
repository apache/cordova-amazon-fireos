/*
 * Copyright (C) 2011 The Android Open Source Project
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

package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import android.os.Looper;
import android.test.InstrumentationTestCase;
import com.amazon.android.webkit.AmazonWebBackForwardList;
import com.amazon.android.webkit.AmazonWebHistoryItem;
import com.amazon.android.webkit.AmazonWebView;

import junit.framework.Assert;

/**
 * Many tests need to run WebView code in the UI thread. This class wraps a WebView so that calls are ensured to arrive
 * on the UI thread. All methods may be run on either the UI thread or test thread.
 */
public class AmazonWebViewOnUiThread {

    /**
     * The test that this class is being used in. Used for runTestOnUiThread.
     */
    private InstrumentationTestCase mTest;

    /**
     * The WebView that calls will be made on.
     */
    private AmazonWebView mWebView;

    /**
     * Initializes the webView with a WebViewClient, WebChromeClient, and PictureListener to prepare for
     * loadUrlAndWaitForCompletion. A new WebViewOnUiThread should be called during setUp so as to reinitialize between
     * calls.
     * 
     * @param test
     *            The test in which this is being run.
     * @param webView
     *            The webView that the methods should call.
     * @see loadUrlAndWaitForCompletion
     */
    public AmazonWebViewOnUiThread(InstrumentationTestCase test,
        CordovaWebView webView) {
        mTest = test;
        mWebView = webView;
    }

    public void loadUrl(final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(url);
            }
        });
    }

    public AmazonWebBackForwardList copyBackForwardList() {
        return getValue(new ValueGetter<AmazonWebBackForwardList>() {
            @Override
            public AmazonWebBackForwardList capture() {
                return mWebView.copyBackForwardList();
            }
        });
    }

    public void printBackForwardList() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AmazonWebBackForwardList currentList = copyBackForwardList();
                int currentSize = currentList.getSize();
                for (int i = 0; i < currentSize; ++i) {
                    AmazonWebHistoryItem item = currentList.getItemAtIndex(i);
                    String url = item.getUrl();
                    LOG.d("cordovaamzn", "The URL at index: " + Integer.toString(i)
                        + "is " + url);
                }
            }
        });
    }

    public String getUrl() {
        return getValue(new ValueGetter<String>() {
            @Override
            public String capture() {
                return mWebView.getUrl();
            }
        });
    }

    public boolean backHistory() {

        return getValue(new ValueGetter<Boolean>() {
            @Override
            public Boolean capture() {
                // Check webview first to see if there is a history
                // This is needed to support curPage#diffLink, since they are
                // added to appView's history, but not our history url array
                // (JQMobile behavior)
                if (mWebView.canGoBack()) {
                    printBackForwardList();
                    mWebView.goBack();

                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Helper for running code on the UI thread where an exception is a test failure. If this is already the UI thread
     * then it runs the code immediately.
     * 
     * @see runTestOnUiThread
     * @param r
     *            The code to run in the UI thread
     */
    public void runOnUiThread(Runnable r) {
        try {
            if (isUiThread()) {
                r.run();
            } else {
                mTest.runTestOnUiThread(r);
            }
        } catch (Throwable t) {
            Assert.fail("Unexpected error while running on UI thread: "
                + t.getMessage());
        }
    }

    private <T> T getValue(ValueGetter<T> getter) {
        runOnUiThread(getter);
        return getter.getValue();
    }

    private abstract class ValueGetter<T> implements Runnable {
        private T mValue;

        @Override
        public void run() {
            mValue = capture();
        }

        protected abstract T capture();

        public T getValue() {
            return mValue;
        }
    }

    /*
     * Returns true if the current thread is the UI thread based on the Looper.
     */
    private static boolean isUiThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }
}
