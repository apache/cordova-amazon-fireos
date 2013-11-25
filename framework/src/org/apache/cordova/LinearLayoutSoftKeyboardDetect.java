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

import org.apache.cordova.LOG;

import android.content.Context;
//import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

/**
 * This class is used to detect when the soft keyboard is shown and hidden in the web view.
 */
public class LinearLayoutSoftKeyboardDetect extends LinearLayout {

    private static final String TAG = "SoftKeyboardDetect";

    // Kindle Fire devices have a Soft Key Bar with Home, Back and Search buttons that is displayed by default if the
    // application is not set to full screen mode. As a result,
    // In portrait mode - Contanier's Height = Screen Height - Status Bar Height - Soft Key Bar Height, Container's
    // Width = Screen Width
    // In landscape mode - Contanier's Height = Screen Height - Status Bar Height, Contanier's Width = Screen Width -
    // Soft Key Bar Width
    //
    // Screen width and height for 3rd generation Kindle Fire devices are listed below
    // Kindle Fire HDX 8.9" Screen Height = 2560 Screen Width = 1600
    // [In portrait mode] SoftKeyBar Height = 122 (4.76% of Screen Height)
    // [In landscape mode] SoftKeyBar Width = 122 (4.76% of Screen Width)
    // Kindle Fire HDX 7" Screen Height = 1920 Screen Width = 1200
    // [In portrait mode] SoftKeyBar Height = 117 (6.3% of Screen Height)
    // [In landscape mode] SoftKeyBar Width = 117 (6.3% of Screen Width)
    // Kindle Fire HD 7" Screen Height = 1280 Screen Width = 800
    // [In portrait mode] SoftKeyBar Height = 78 (6.09% of Screen Height)
    // [In landscape mode] SoftKeyBar Width = 78 (6.09% of Screen Width)
    //
    // Show/Hide keyboard events
    // Should be fired when the height of the container changes by a value equal to the height of the keyboard.
    // Our implementation - These events will be fired when the change in the height of the container is more than 7% of
    // the screen height. This threshold value was chosen based on the height of the Soft Key Bar in the 3rd generation
    // devices. This change has been added so that the show/hide keyboard events are not wrongly triggered when the soft
    // key bar shows up.
    //
    // Orientation change
    // [Landscape to Portrait] Screen Height should be equal to Container Width
    // [Portrait to Landscape] Screen Height should be equal to Container Width + Soft Key Bar Width
    // Our Implementation - Orientation change occurs when the difference between screen height and container width is
    // less than 7% of the screen height. This threshold value was chosen based on the height of the Soft Key Bar in the
    // 3rd generation devices.
   
    private static final int PERCENTAGE_CHANGE_THRESHOLD = 7; 
    
    private int oldHeight = 0;  // Need to save the old height as not to send redundant events
    private int oldWidth = 0; // Need to save old width for orientation change
    private int screenWidth = 0;
    private int screenHeight = 0;
    private CordovaActivity app = null;

    public LinearLayoutSoftKeyboardDetect(Context context, int width, int height) {
        super(context);
        screenWidth = width;
        screenHeight = height;
        app = (CordovaActivity) context;
    }

    @Override
    /**
     * Start listening to new measurement events.  Fire events when the height
     * gets smaller fire a show keyboard event and when height gets bigger fire
     * a hide keyboard event.
     *
     * Note: We are using app.postMessage so that this is more compatible with the API
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        LOG.v(TAG, "We are in our onMeasure method");

        // Get the current height of the visible part of the screen.
        // This height will not included the status bar.\
        int width, height;

        height = MeasureSpec.getSize(heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        LOG.v(TAG, "Old Height = %d", oldHeight);
        LOG.v(TAG, "Height = %d", height);
        LOG.v(TAG, "Old Width = %d", oldWidth);
        LOG.v(TAG, "Width = %d", width);
        
        // If the oldHeight = 0 then this is the first measure event as the app starts up.
        // If oldHeight == height then we got a measurement change that doesn't affect us.
        if (oldHeight == 0 || oldHeight == height) {
            LOG.d(TAG, "Ignore this event");
        }
        // Account for orientation change and ignore this event/Fire orientation change
        else if (hasOrientationChanged(width)) {
            int tmp_var = screenHeight;
            screenHeight = screenWidth;
            screenWidth = tmp_var;
            LOG.v(TAG, "Orientation Change");
            
        }
        // If the height has gotten bigger then we will assume the soft keyboard has
        // gone away.
        else if (height > oldHeight) {
            if (app != null && hasKeyboardEventOccurred(height) ) {   
                LOG.v(TAG, "Fired Hide Keyboard Event") ;
                app.appView.sendJavascript("cordova.fireDocumentEvent('hidekeyboard');");
            }
        }
        // If the height has gotten smaller then we will assume the soft keyboard has 
        // been displayed.
        else if (height < oldHeight) {
            if (app != null && hasKeyboardEventOccurred(height)  ) {  
                LOG.v(TAG, "Fired Show Keyboard Event") ;
                app.appView.sendJavascript("cordova.fireDocumentEvent('showkeyboard');");
            }
        }

        // Update the old height for the next event
        oldHeight = height;
        oldWidth = width;
    }

    /**
     * Function that returns true if orientation has changed
     * 
     * @param width
     *            The width of the container
     * @return
     */
    private boolean hasOrientationChanged(int width) {
        //Calculate difference between screen height and container as a percentage of screen height
        double percentageChange = (double) (Math.abs(screenHeight - width) * 100) / screenHeight;
        return percentageChange < PERCENTAGE_CHANGE_THRESHOLD;
    }

    /**
     * Function that returns true if the keyboard show/hide event has occurred.
     * 
     * @param height
     *            The height of the container
     * @return
     */
    private boolean hasKeyboardEventOccurred(int height) {
        //Calculate percentage change in container height as a percentage of screen height
        double percentageChange = (double) (Math.abs(oldHeight - height) * 100) / screenHeight; 
        return percentageChange > PERCENTAGE_CHANGE_THRESHOLD; 
    }
}
