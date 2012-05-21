package com.android.takethepill;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class PillViewActivity extends Activity {

	private TextView mUserText;
	private TextView mPillText;
	private TextView mDaysText;
	private TextView mTimeText;
	private Long mRowId;
	private PillsDbAdapter mDbHelper;

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
		mTimeText = (TextView) findViewById(R.id.view_textHours);

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
			mTimeText.setText(pillcursor.getString(pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_HOUR)));
		
		}
	}
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	}

}
