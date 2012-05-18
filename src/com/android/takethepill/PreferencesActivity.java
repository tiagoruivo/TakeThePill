package com.android.takethepill;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PreferencesActivity extends Activity {
	private TextView mEmailText;
	private TextView mPhoneText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		setTitle(R.string.preferences_title);

		mEmailText = (TextView) findViewById(R.id.email);
		mPhoneText = (TextView) findViewById(R.id.phone);
		Button confirmButton = (Button) findViewById(R.id.confirm_prefs);


		restorePrefs();
		//Listener para el boton de confirmar
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String email = mEmailText.getText().toString();
				String phone = mPhoneText.getText().toString();

				if(! isValidEmail(email)){
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.error_email, Toast.LENGTH_SHORT);
					toast.show();

				} else  if(! PhoneNumberUtils.isGlobalPhoneNumber(phone)){
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.error_phone, Toast.LENGTH_SHORT);
					toast.show();
				} else {
					setPrefs(email, phone);
					setResult(RESULT_OK);
					finish();
				}
			}

		});
	}

	private void setPrefs(String email, String phone){
		SharedPreferences settings = getSharedPreferences(TakeThePill.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();	

		editor.putString(TakeThePill.EMAIL_KEY, email);
		editor.putString(TakeThePill.PHONE_KEY, phone);

		editor.commit();

		Toast toast = Toast.makeText(getApplicationContext(),R.string.changed_prefs, Toast.LENGTH_SHORT);
		toast.show();

	}

	private void restorePrefs(){
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(TakeThePill.PREFS_NAME, 0);
		mEmailText.setText(settings.getString(TakeThePill.EMAIL_KEY, "empty"));
		mPhoneText.setText(settings.getString(TakeThePill.PHONE_KEY, "0"));
	}

	public final static boolean isValidEmail(CharSequence target) {
		try {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
		catch( NullPointerException exception ) {
			return false;
		}
	}

}
