package com.android.takethepill;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PillViewActivity extends Activity {

	private TextView mUserText;
	private TextView mPillText;
	private TextView mDaysText;
	private ListView mTimeList;
	private Long mRowId;
	private PillsDbAdapter mDbHelper;
	
	private ArrayList<String> mArrayHours = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pill_view);
		setTitle(R.string.pill_view_title);

		//BBDD
		mDbHelper = new PillsDbAdapter(this);
		mDbHelper.open();

		mUserText = (TextView) findViewById(R.id.text_user);
		mPillText = (TextView) findViewById(R.id.text_pill);

		mDaysText = (TextView) findViewById(R.id.view_textDays);
		mTimeList = (ListView) findViewById(R.id.view_hourList);

		mRowId = (savedInstanceState == null) ? null :
			(Long) savedInstanceState.getSerializable(PillsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(PillsDbAdapter.KEY_ROWID) : null;
		}

		populateFields();
	}


	/**
	 * Rellena los campos de edicion de pil, solo si no es una nueva pill.
	 */
	private void populateFields() {

		if (mRowId != null) {
			//Cursor
			Cursor pillcursor = mDbHelper.fetchPill(mRowId);
			startManagingCursor(pillcursor);//Android lo gestiona

			//Fija el nombre del usuario
			mUserText.setText(pillcursor.getString(pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_USER)));
			//Fija el nombre de la pill
			mPillText.setText(pillcursor.getString(pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_PILL)));
			//Fija los dias de la semana
			mDaysText.setText(pillcursor.getString(pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_DAYS)));
			//Fija las horas de la pill
			hourStringToArray(pillcursor.getString(pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_HOUR)));
			updateList();
		}
	}

	/**
	 * Pasa de String a ArrayList las horas y lo guarda en el capo correspondiente
	 * @param hourString Horas a pasar al ArrayList
	 */
	private void hourStringToArray(String hourString){
		mArrayHours=new ArrayList<String>(Arrays.asList(hourString.split(" - ")));
	}
	/**
	 * Actualiza la lista de las horas	en su elemento visual.
	 */
	private void updateList(){
		mTimeList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mArrayHours));
	}
}
