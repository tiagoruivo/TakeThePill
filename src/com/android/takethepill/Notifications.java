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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Notifications extends Activity {

	private EditText mEmailText;
	private TextView mContactsNameText;
	private TextView mContactsTelText;
	private TextView mContactsEmailText;
	ArrayList<String> mPeopleList;

	private final int PICK_CONTACT = 1;
	
	private String name;
	private String tel;
	private String email;

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
		mContactsNameText = (TextView) findViewById(R.id.contacts_name);
		mContactsTelText = (TextView) findViewById(R.id.contacts_tel);
		mContactsEmailText = (TextView) findViewById(R.id.contacts_email);

		Button contactsButton= (Button) findViewById(R.id.check_contacts);
		
		Button emailButton = (Button) findViewById(R.id.send_email);
		
		Button callButton = (Button) findViewById(R.id.call);
		
		contactsButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});
		
		callButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Uri parsedPhoneNumber = Uri.parse("tel:"+mContactsTelText.getText().toString()); 
				Intent intent = new Intent(Intent.ACTION_CALL, parsedPhoneNumber);
				startActivity(intent);
			}
		});

		emailButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				if(! isValidEmail(mContactsEmailText.getText().toString())){
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.error_email, Toast.LENGTH_SHORT);
					toast.show();

				} else {
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_EMAIL  , new String[]{mContactsEmailText.getText().toString()});
					i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));
					i.putExtra(Intent.EXTRA_TEXT   , getString(R.string.body1) + " " + user + " " + getString(R.string.body2) + " "  + pill + " " + getString(R.string.body3) + " "  + hour);
					try {
						startActivity(Intent.createChooser(i, "Send mail..."));
					} catch (android.content.ActivityNotFoundException ex) {
						Toast.makeText(Notifications.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
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
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
	  super.onActivityResult(reqCode, resultCode, data);

	  switch (reqCode) {
	    case (PICK_CONTACT) :
	      if (resultCode == Activity.RESULT_OK) {
	        Uri contactData = data.getData();
	        System.out.println(contactData.toString());
	        Cursor c =  managedQuery(contactData, new String []{Contacts.DISPLAY_NAME, Contacts._ID}, null, null, null);
	        if (c.moveToFirst()) {
	          name = c.getString(0);
	          String id=c.getString(1);
	          System.out.println(id);
	          c.close();
	          
          Cursor c1 = getContentResolver().query(Data.CONTENT_URI,
                  new String[] {Data._ID, Phone.NUMBER},null,null, null);
	          if (c.moveToNext()){
	          tel = c1.getString(c1.getColumnIndex(Phone.NUMBER));
	          }
	          //email = c1.getString(2);
	          
	          c1.close();
	         
	          mContactsNameText.setText(name);
	          mContactsTelText.setText("tel:" + tel + ":");
	          mContactsEmailText.setText(email);
	        }
	      }
	      break;
	  }
	}

}
