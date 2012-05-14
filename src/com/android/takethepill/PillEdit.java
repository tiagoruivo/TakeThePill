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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class PillEdit extends Activity {

	private EditText mUserText;
	private EditText mPillText;
	private Long mRowId;
	private PillsDbAdapter mDbHelper;
	private TextView mDaysText;
	private ArrayList<String> hours = new ArrayList();
	private boolean [] arrayDays= new boolean[7];
	private ListView mTimeList;
	
	private int mHour;
	private int mMinute;
	private static final int TIME_DIALOG_ID = 0;
	private static final int DIALOG_MULTIPLE_CHOICE = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new PillsDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.pill_edit);
		setTitle(R.string.edit_pill);

		mUserText = (EditText) findViewById(R.id.user);
		mPillText = (EditText) findViewById(R.id.pill);
		mDaysText = (TextView) findViewById(R.id.textDays);
		mTimeList = (ListView) findViewById(R.id.hourList);
		
		Button confirmButton = (Button) findViewById(R.id.confirm);
		Button checkBox = (Button) findViewById(R.id.add_days);
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
		
		/* Display a list of checkboxes */
        checkBox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_MULTIPLE_CHOICE);
            }
        });

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
	
	private void daysStringArray(String daysString){
		for (int i=0; i<arrayDays.length; i++){
			arrayDays[i]= (daysString.indexOf(getResources().getStringArray(R.array.select_dialog_day)[i])) !=-1;
		}
	}
	
	private void updateDays() {
		String stringDays="";
		for (int i=0; i<arrayDays.length; i++){
		if(arrayDays[i]){
			if (stringDays != "")
				stringDays= stringDays + " - ";
			stringDays= stringDays + getResources().getStringArray(R.array.select_dialog_day)[i];
			}
		}
		mDaysText.setText(stringDays);
	}
	
	private void updateList(){
		mTimeList.setAdapter(new ArrayAdapter<String>(this,
			      android.R.layout.simple_list_item_1, hours));
	}
	
	private void updateTime(int hourOfDay, int minute) {

		hours.add(new StringBuilder()
		.append(pad(hourOfDay)).append(":")
		.append(pad(minute)).toString());
		updateList();
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
			mDaysText.setText(pillcursor.getString(
					pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_DAYS)));
			daysStringArray(mDaysText.getText().toString());
			hourStringArray(pillcursor.getString(
					pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_HOUR)));
			updateList();
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
		String days = mDaysText.getText().toString();
		String hour = hourArrayString();

		if (mRowId == null) {
			long id = mDbHelper.createPill(user, pill, days, hour);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updatePill(mRowId, user, pill, days, hour);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this,
					mTimeSetListener, mHour, mMinute, true);
		case DIALOG_MULTIPLE_CHOICE:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_popup_reminder)
                .setTitle(R.string.alert_dialog_multi_choice)
                .setMultiChoiceItems(R.array.select_dialog_day,arrayDays,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton,
                                    boolean isChecked) {
                            	if(arrayDays[whichButton])
                            		arrayDays[whichButton] = true;
                            	else
                            		arrayDays[whichButton] = false;
                            }
                        })
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        updateDays(); 
                    }
                })
               .create();
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
