package com.android.takethepill;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class PreferencesActivity extends Activity {
	private TextView mEmailText;
	private TextView mPhoneText;
	private CheckBox mAlarmsCheckbox;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		setTitle(R.string.preferences_title);

		mEmailText = (TextView) findViewById(R.id.email);
		mPhoneText = (TextView) findViewById(R.id.phone);
		mAlarmsCheckbox=(CheckBox) findViewById(R.id.checkBox_alarms);
		Button confirmButton = (Button) findViewById(R.id.confirm_prefs);


		restorePrefs();
		//Listener para el boton de confirmar
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String email = mEmailText.getText().toString();
				String phone = mPhoneText.getText().toString();
				boolean alarmsEnabled = mAlarmsCheckbox.isChecked();

				if(! isValidEmail(email)){
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.error_email, Toast.LENGTH_SHORT);
					toast.show();

				} else  if(! PhoneNumberUtils.isGlobalPhoneNumber(phone)){
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.error_phone, Toast.LENGTH_SHORT);
					toast.show();
				} else {
					setPrefs(email, phone, alarmsEnabled);
					setResult(RESULT_OK);
					finish();
				}
			}

		});
	}

	private void setPrefs(String email, String phone, boolean alarmsEnabled){
		SharedPreferences settings = getSharedPreferences(TakeThePill.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();	

		editor.putString(TakeThePill.EMAIL_KEY, email);
		editor.putString(TakeThePill.PHONE_KEY, phone);
		editor.putBoolean(TakeThePill.ALARMS_KEY, alarmsEnabled);

		editor.commit();

		Toast toast = Toast.makeText(getApplicationContext(),R.string.changed_prefs, Toast.LENGTH_SHORT);
		toast.show();

	}

	private void restorePrefs(){
		SharedPreferences settings = getSharedPreferences(TakeThePill.PREFS_NAME, 0);
		String emailHint = getResources().getString(R.string.hint_email);
		String phoneHint = getResources().getString(R.string.hint_phone);
		String email = settings.getString(TakeThePill.EMAIL_KEY, emailHint);
		String phone = settings.getString(TakeThePill.PHONE_KEY, phoneHint);
		if(!email.equals(emailHint)) mEmailText.setText(email);
		if(!phone.equals(phoneHint))mPhoneText.setText(phone);
		mAlarmsCheckbox.setChecked(settings.getBoolean(TakeThePill.ALARMS_KEY, true));
	}

	public final boolean isValidEmail(CharSequence target) {
		try {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
		catch( NullPointerException exception ) {
			return false;
		}
	}

}
