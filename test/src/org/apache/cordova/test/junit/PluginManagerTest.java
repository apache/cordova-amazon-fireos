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

package org.apache.cordova.test.junit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginManager;
import org.apache.cordova.test.CordovaWebViewTestActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class PluginManagerTest extends ActivityInstrumentationTestCase2<CordovaWebViewTestActivity> {
    // Plugin1 and PLugin2 return true, indicating the event is handled by them. Remaining plugins return false
    private static final String PLUGIN_PACKAGE = "org.apache.cordova.pluginApi.";
    private static final String PLUGIN1_SERVICE = "Plugin1";
    private static final String PLUGIN1_CLASS = PLUGIN_PACKAGE + PLUGIN1_SERVICE;
    private static final String PLUGIN2_SERVICE = "Plugin2";
    private static final String PLUGIN2_CLASS = PLUGIN_PACKAGE + PLUGIN2_SERVICE;
    private static final String PLUGIN3_SERVICE = "Plugin3";
    private static final String PLUGIN3_CLASS = PLUGIN_PACKAGE + PLUGIN3_SERVICE;
    private static final String PLUGIN4_SERVICE = "Plugin4";
    private static final String PLUGIN4_CLASS = PLUGIN_PACKAGE + PLUGIN4_SERVICE;
    private static final String PLUGIN5_SERVICE = "Plugin5";
    private static final String PLUGIN5_CLASS = PLUGIN_PACKAGE + PLUGIN5_SERVICE;
    private static final String TEST_URL = "file:///android_asset/www/plugins/%s.html";
    private static final String PLUGIN1_URL = String.format(TEST_URL, PLUGIN1_SERVICE);
    private static final String PLUGIN3_URL = String.format(TEST_URL, PLUGIN3_SERVICE);
    private static final String PLUGIN5_URL = String.format(TEST_URL, PLUGIN5_SERVICE);

    private CordovaWebViewTestActivity testActivity;
    private FrameLayout containerView;
    private LinearLayout innerContainer;
    private View testView;
    private PluginManager pluginMgr;
    private CordovaWebView mWebView;


    public PluginManagerTest() {
        super("org.apache.cordova.test.activities", CordovaWebViewTestActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testActivity = this.getActivity();
        containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
        innerContainer = (LinearLayout) containerView.getChildAt(0);
        testView = innerContainer.getChildAt(0);
        mWebView = (CordovaWebView) testView;
        pluginMgr = mWebView.pluginManager;

    }

    /**
     * Test the preconditions to verify view is not null
     */
    public void testPreconditions() {
        assertNotNull(innerContainer);
        assertNotNull(testView);
    }

    /**
     * Verify Plugin Manager is not null
     */
    public void testForPluginManager() {
        assertNotNull(pluginMgr);
        String className = pluginMgr.getClass().getSimpleName();
        assertEquals(0, className.compareTo("PluginManager"));
    }

    /**
     * Verify that Plugin1 which has higher priority will receive the event first and handle it. Verify that adding
     * plugin multiple times will update entries
     * 
     * @throws Throwable
     */
    public void testPluginManagerPriority() throws Throwable {
        addTestPlugin(PLUGIN4_SERVICE, PLUGIN4_CLASS, true, 1.5f);
        addTestPlugin(PLUGIN1_SERVICE, PLUGIN1_CLASS, true, -1.5f);
        addTestPlugin(PLUGIN4_SERVICE, PLUGIN4_CLASS, true, 1.5f);
        pluginMgr.postMessage("plugintest", "test");
        assertEquals(PLUGIN1_URL, getCurrentUrl());
    }

    /**
     * Verify that event propagates to the next plugin when event is not handled.
     * 
     * @throws Throwable
     */
    public void testPluginManagerPropagate() throws Throwable {
        addTestPlugin(PLUGIN2_SERVICE, PLUGIN2_CLASS, true, -1f);
        addTestPlugin(PLUGIN3_SERVICE, PLUGIN3_CLASS, true, -1f);
        pluginMgr.postMessage("plugintest", "test");
        // Sleep to make sure the message is propagated.
        Thread.sleep(1000);
        assertEquals(PLUGIN3_URL, getCurrentUrl());
    }

    /**
     * Verify that the event is not propagated when handled.
     * 
     * @throws Throwable
     */
    public void testPluginManagerHandle() throws Throwable {
        addTestPlugin(PLUGIN1_SERVICE, PLUGIN1_CLASS, true, -1f);
        addTestPlugin(PLUGIN3_SERVICE, PLUGIN3_CLASS, true, -1f);
        pluginMgr.postMessage("plugintest", "test");
        assertEquals(PLUGIN1_URL, getCurrentUrl());
    }

    /**
     * Verify that Plugin entries can be updated after init()
     * 
     * @throws Throwable
     */
    public void testPluginManagerUpdateAfterInit() throws Throwable {
        addTestPlugin(PLUGIN1_SERVICE, PLUGIN1_CLASS, true, -1.5f);
        addTestPlugin(PLUGIN4_SERVICE, PLUGIN4_CLASS, true, -1.5f);
        pluginMgr.postMessage("plugintest", "test");
        assertEquals(PLUGIN1_URL, getCurrentUrl());
        addTestPlugin(PLUGIN5_SERVICE, PLUGIN5_CLASS, true, -2.5f);        
        pluginMgr.postMessage("plugintest", "test");
        assertEquals(PLUGIN5_URL, getCurrentUrl());
    }

    /**
     * Test all plugins are added based on their priority.
     * 
     * @throws Exception
     */
    public void testPluginEntriesPriority() throws Exception {
        addTestPlugin(PLUGIN5_SERVICE, PLUGIN5_CLASS, true, -96f);
        addTestPlugin(PLUGIN1_SERVICE, PLUGIN1_CLASS, true, -100f);
        addTestPlugin(PLUGIN4_SERVICE, PLUGIN4_CLASS, true, -97f);
        addTestPlugin(PLUGIN2_SERVICE, PLUGIN2_CLASS, true, -99f);
        addTestPlugin(PLUGIN3_SERVICE, PLUGIN3_CLASS, true, -98f);
        
        Field entries = pluginMgr.getClass().getDeclaredField("entryMap");
        entries.setAccessible(true);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, PluginEntry> testEntries = (LinkedHashMap<String, PluginEntry>) entries.get(pluginMgr);
        List<PluginEntry> pluginList = new ArrayList<PluginEntry>();
        for (PluginEntry entry : testEntries.values())
            pluginList.add(entry);
        int entryIndex = 0;
        String[] serviceArray = { PLUGIN1_SERVICE, PLUGIN2_SERVICE, PLUGIN3_SERVICE, PLUGIN4_SERVICE, PLUGIN5_SERVICE };
        for (PluginEntry entry : pluginList) {
            if (entryIndex == 5)
                break;
            assertEquals(0, serviceArray[entryIndex].compareTo(entry.service));
            entryIndex++;
        }
    }

    /**
     * Create a plugin object and add it
     * 
     * @param service
     * @param cls
     * @param onload
     * @param priority
     */
    private void addTestPlugin(String service, String cls, boolean onload, final float priority) {
        PluginEntry pEntry = new PluginEntry(service, cls, onload, priority);
        pluginMgr.addService(pEntry);
        pluginMgr.getPlugin(service);
    }

    /**
     * Get the URL in the current WebView
     * 
     * @return URL
     * @throws Throwable
     */
    private String getCurrentUrl() throws Throwable {
        final Callable<String> awvUIThread = new Callable<String>() {
            @Override
            public String call() throws Exception {
                String currentURL = mWebView.getUrl();
                return currentURL;
            }
        };
        FutureTask<String> runnableTask = new FutureTask<String>(awvUIThread);
        // If AWV is not running on the UI Thread, a RunTimeException is thrown
        runTestOnUiThread(runnableTask);
        return runnableTask.get();
    }
}
