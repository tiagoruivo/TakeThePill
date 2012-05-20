/*
 * Copyright (C) 2008 Google Inc.
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

package com.android.takethepill;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Notifications extends Activity {

	private EditText mEmailText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		final String user=extras.getString("user");
		final String pill=extras.getString("pill");
		final String hour=extras.getString("hour");

		setContentView(R.layout.notifications);
		setTitle(R.string.title);

		mEmailText = (EditText) findViewById(R.id.email);

		Button emailButton = (Button) findViewById(R.id.send_email);

		Button callButton = (Button) findViewById(R.id.call);

		Button emailMeButton = (Button) findViewById(R.id.send_me_email);

		callButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_CALL);
				startActivity(intent);
			}
		});

		emailButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				if(! isValidEmail(mEmailText.getText().toString())){
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.error_email, Toast.LENGTH_SHORT);
					toast.show();

				} else {
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_EMAIL  , new String[]{mEmailText.getText().toString()});
					i.putExtra(Intent.EXTRA_SUBJECT, R.string.subject);
					i.putExtra(Intent.EXTRA_TEXT   , getString(R.string.body1) + " " + user + " " + getString(R.string.body2) + " "  + pill + " " + getString(R.string.body3) + " "  + hour);
					try {
						startActivity(Intent.createChooser(i, "Send mail..."));
					} catch (android.content.ActivityNotFoundException ex) {
						Toast.makeText(Notifications.this, getResources().getString(R.string.error_no_mail_clients), Toast.LENGTH_LONG).show();
					}
				}
			}

		});

		emailMeButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {				
				SharedPreferences settings = getSharedPreferences(TakeThePill.PREFS_NAME, 0);

				String myEmail = settings.getString(TakeThePill.EMAIL_KEY, "empty");

				if(myEmail.equals("empty")){
					Toast.makeText(Notifications.this, getResources().getString(R.string.error_no_mail_preferences), Toast.LENGTH_LONG).show();
				} else {
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_EMAIL  , new String[]{myEmail});
					i.putExtra(Intent.EXTRA_SUBJECT, R.string.subject);
					i.putExtra(Intent.EXTRA_TEXT   , getString(R.string.body1) + " " + user + " " + getString(R.string.body2) + " "  + pill + " " + getString(R.string.body3) + " "  + hour);
					try {
						startActivity(Intent.createChooser(i, "Send mail..."));
					} catch (android.content.ActivityNotFoundException ex) {
						Toast.makeText(Notifications.this, getResources().getString(R.string.error_no_mail_clients), Toast.LENGTH_SHORT).show();
					}
				}

			}
		});
	}

	public final boolean isValidEmail(CharSequence target) {
		try {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
		catch( NullPointerException exception ) {
			return false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void saveState() {
	}

}
