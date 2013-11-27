package org.apache.cordova.test.junit;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.test.AmazonWebViewOnUiThread;
import org.apache.cordova.test.loadurl;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CordovaTimeoutTest extends
		ActivityInstrumentationTestCase2<loadurl> {

	private int SLEEPTIME = 10000;
	private static final String ERROR_URL = "file:///android_asset/www/htmlnotfound/error.html";
	private static final String TEST_URL = "http://www.flickr.com/explore";

	loadurl testActivity;
	private FrameLayout containerView;
	private LinearLayout innerContainer;
	private CordovaWebView testView;

	private AmazonWebViewOnUiThread mUiThread;

	public CordovaTimeoutTest() {
		super("org.apache.cordova.test.actions", loadurl.class);
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

	/**
	 * Verify that timeout error occurs and a error page is loaded when set.
	 */
	public void testTimeOutErrorOccurs() {
		// Set the timeout value small so that error occurs
		this.getActivity().setStringProperty("loadUrlTimeoutValue", "500");
		this.getActivity().setStringProperty("errorUrl",
				"file:///android_asset/www/htmlnotfound/error.html");
		mUiThread.loadUrl(TEST_URL);
		sleep();
		String url = mUiThread.getUrl();
		assertNotNull(url);
		assertTrue(url.equals(ERROR_URL));
	}

	/**
	 * Verify that timeout does not occur for default timeout
	 */
	public void testTimeOutErrorDoesNotOccur() {
		// With default timeout= 20000ms verify error does not occur and
		// page loads
		this.getActivity().setStringProperty("errorUrl",
				"file:///android_asset/www/htmlnotfound/error.html");
		mUiThread.loadUrl(TEST_URL);
		sleep();
		String url = mUiThread.getUrl();
		assertNotNull(url);
		assertTrue(url.equals(TEST_URL));

	}

	/**
	 * Verify if a error splash screen is showing when the error page is not set
	 */
	public void testErrorDialogShows() {
		this.getActivity().setStringProperty("loadUrlTimeoutValue", "500");
		mUiThread.loadUrl(TEST_URL);
		sleep();
		String url = mUiThread.getUrl();
		assertNotNull(url);
		assertFalse(this.getActivity().hasWindowFocus());
	}

	/**
	 * Sleep to make sure URL loads completely
	 */
	private void sleep() {
		try {
			Thread.sleep(SLEEPTIME);
		} catch (InterruptedException e) {
			fail("Unexpected Timeout");
		}
	}

}
