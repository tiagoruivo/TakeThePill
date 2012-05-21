package com.android.takethepill;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Notifications extends Activity {

	private TextView mContactsNameText;
	private EditText mTelText;
	private EditText mEmailText;
	ArrayList<String> mPeopleList;

	private final int PICK_CONTACT = 1;

	private String mName;
	private String mTel;
	private String mEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		final String user=extras.getString("user");
		final String pill=extras.getString("pill");
		final String hour=extras.getString("hour");

		setContentView(R.layout.notifications);
		setTitle(R.string.title);

		mEmailText = (EditText) findViewById(R.id.email_notif);
		mContactsNameText = (TextView) findViewById(R.id.contacts_name);
		mTelText = (EditText) findViewById(R.id.phone_notif);

		Button contactsButton= (Button) findViewById(R.id.check_contacts);

		ImageButton emailButton = (ImageButton) findViewById(R.id.send_email);

		ImageButton callButton = (ImageButton) findViewById(R.id.call);
		
		Button emailMeButton = (Button) findViewById(R.id.send_me_email);

		contactsButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});		

		callButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String phone = mTelText.getText().toString();
				if(! PhoneNumberUtils.isGlobalPhoneNumber(phone)){
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.error_phone, Toast.LENGTH_SHORT);
					toast.show();
				} else {
					Uri parsedPhoneNumber = Uri.parse("tel:"+phone); 
					Intent intent = new Intent(Intent.ACTION_CALL, parsedPhoneNumber);
					startActivity(intent);
				}
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
					i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));
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
					i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));
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
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case PICK_CONTACT:
			mName=null;
			mEmail=null;
			mTel=null;
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c =  managedQuery(contactData, new String []{Contacts.DISPLAY_NAME, Contacts._ID}, null, null, null);
				if (c.moveToFirst()) {
					mName = c.getString(0);
					String id=c.getString(1);
					c.close();

					Cursor c1 = getContentResolver().query(
							Phone.CONTENT_URI,
							new String[] {Phone.NUMBER},Data.CONTACT_ID + "=?",
							new String[] {String.valueOf(id)},
							null);
					if(c1.moveToFirst()) mTel = c1.getString(0);
					c1.close();

					Cursor c2 = getContentResolver().query(
							Email.CONTENT_URI,
							new String[] {Email.DATA},Data.CONTACT_ID + "=?",
							new String[] {String.valueOf(id)},
							null);

					if(c2.moveToFirst()) mEmail = c2.getString(0);
					c1.close(); 
				}				
			}
			if(mName==null){				
				mContactsNameText.setText(getResources().getString(R.string.warn_someone));
			} else {
				mContactsNameText.setText(getResources().getString(R.string.warn)+" "+mName);
			}
			mTelText.setText(mTel);
			mEmailText.setText(mEmail);
			break;
		}
	}

}
