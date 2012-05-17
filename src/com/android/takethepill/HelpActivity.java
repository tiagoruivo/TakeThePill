package com.android.takethepill;

import android.app.Activity;
import android.os.Bundle;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		setTitle(R.string.help_title);
	}
	
}
