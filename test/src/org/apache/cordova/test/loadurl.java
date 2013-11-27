package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class loadurl extends CordovaActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.loadUrl(Config.getStartUrl());
	}

}
