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
import java.util.Arrays;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class PillEdit extends Activity {

	private EditText mUserText;
	private EditText mPillText;
	private Long mRowId;
	private PillsDbAdapter mDbHelper;
	//private TimePicker timePicker;

	private String mTimeString;
	private TextView mTimeText;
	private ArrayList<String> hours = new ArrayList();
	
	private int mHour;
	private int mMinute;
	static final int TIME_DIALOG_ID = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new PillsDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.pill_edit);
		setTitle(R.string.edit_pill);

		mUserText = (EditText) findViewById(R.id.user);
		mPillText = (EditText) findViewById(R.id.pill);

		mTimeText = (TextView) findViewById(R.id.textHour);
		Button confirmButton = (Button) findViewById(R.id.confirm);

		Button addTimeButton = (Button) findViewById(R.id.addtime);
		
		

		mRowId = (savedInstanceState == null) ? null :
			(Long) savedInstanceState.getSerializable(PillsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(PillsDbAdapter.KEY_ROWID)
					: null;
		}
		
		
		populateFields();

		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}

		});

		addTimeButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				//codigo para cuando se pulsa el boton: lanzar time spiner
				showDialog(TIME_DIALOG_ID);
			}

		});		
		final Calendar c = Calendar.getInstance();        
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);

	}

	private String hourArrayString(){
		String hourString="";
		for (int i=0; i<hours.size(); i++){
			hourString=hourString + hours.get(i);
			if (i<(hours.size()-1)) hourString=hourString+ " - ";
		}
		return hourString;
	}
	
	private void hourStringArray(String hourString){
		hours=new ArrayList(Arrays.asList(hourString.split(" - ")));
	}
	
	
	private void updateTime(int hourOfDay, int minute) {

		hours.add(new StringBuilder()
		.append(pad(hourOfDay)).append(":")
		.append(pad(minute)).toString());
		
		mTimeText.setText(hourArrayString());
	}
	
	

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	private void populateFields() {

		if (mRowId != null) {
			Cursor pillcursor = mDbHelper.fetchPill(mRowId);
			startManagingCursor(pillcursor);
			mUserText.setText(pillcursor.getString(
					pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_USER)));
			mPillText.setText(pillcursor.getString(
					pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_PILL)));
			hourStringArray(pillcursor.getString(
					pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_HOUR)));
			mTimeText.setText(hourArrayString());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(PillsDbAdapter.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private void saveState() {
		String user = mUserText.getText().toString();
		String pill = mPillText.getText().toString();
		String hour = mTimeText.getText().toString();

		if (mRowId == null) {
			long id = mDbHelper.createPill(user, pill, hour);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updatePill(mRowId, user, pill, hour);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this,
					mTimeSetListener, mHour, mMinute, true);            
		}
		return null;
	}
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mHour, mMinute);
			break;            
		}
	}  
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateTime(mHour, mMinute);
		}
	};

}
