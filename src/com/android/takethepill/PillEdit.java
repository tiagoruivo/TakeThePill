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
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PillEdit extends Activity {

    private EditText mUserText;
    private EditText mPillText;
    private EditText mHourText;
    private Long mRowId;
    private PillsDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new PillsDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.pill_edit);
        setTitle(R.string.edit_pill);

        mUserText = (EditText) findViewById(R.id.user);
        mPillText = (EditText) findViewById(R.id.pill);
        mHourText = (EditText) findViewById(R.id.hour);

        
        Button confirmButton = (Button) findViewById(R.id.confirm);

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
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchPill(mRowId);
            startManagingCursor(note);
            mUserText.setText(note.getString(
                    note.getColumnIndexOrThrow(PillsDbAdapter.KEY_USER)));
            mPillText.setText(note.getString(
                    note.getColumnIndexOrThrow(PillsDbAdapter.KEY_PILL)));
            mHourText.setText(note.getString(
                    note.getColumnIndexOrThrow(PillsDbAdapter.KEY_HOUR)));
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
        String hour = mHourText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createPill(user, pill, hour);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updatePill(mRowId, user, pill, hour);
        }
    }

}
