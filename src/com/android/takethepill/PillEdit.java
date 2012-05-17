package com.android.takethepill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class PillEdit extends Activity {

	//Elementos visibles:
	private EditText mUserText;
	private EditText mPillText;
	private TextView mDaysText;
	private ListView mTimeList;

	//BBDD
	private Long mRowId;
	private PillsDbAdapter mDbHelper;

	//Time
	private int mHour;
	private int mMinute;
	private ArrayList<String> mArrayHours = new ArrayList<String>();

	//Days
	private boolean [] mArrayDays= new boolean[7];	

	//Dialogs
	private static final int TIME_DIALOG_ID = 0;
	private static final int DIALOG_MULTIPLE_CHOICE = 1;

	/**
	 * Metodo llamado al crear la actividad
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//BBDD
		mDbHelper = new PillsDbAdapter(this);
		mDbHelper.open();
		//Vistas
		setContentView(R.layout.pill_edit);
		setTitle(R.string.edit_pill);
		//Recuperamos elementos visuales
		mUserText = (EditText) findViewById(R.id.user);
		mPillText = (EditText) findViewById(R.id.pill);
		mDaysText = (TextView) findViewById(R.id.textDays);
		mTimeList = (ListView) findViewById(R.id.hourList);
		//Recuperamos botones
		Button confirmButton = (Button) findViewById(R.id.confirm);
		Button checkBox = (Button) findViewById(R.id.add_days);
		Button addTimeButton = (Button) findViewById(R.id.addtime);		

		/*
		 * Buscamos la fila de la pill a editar. 
		 * Si estamos creando una, mRowId valdra null y se recuperan extras del indent, 
		 * sino valdra el numero de fila de la pill a editar.
		 */	
		mRowId = (savedInstanceState == null) ? null :
			(Long) savedInstanceState.getSerializable(PillsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(PillsDbAdapter.KEY_ROWID) : null;
		}
		//Se rellenan los campos como corresponda	
		populateFields();

		//Listener para el boton de confirmar
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				if(mUserText.getText().length()==0){
					Toast toast1 = Toast.makeText(getApplicationContext(),R.string.error_user, Toast.LENGTH_SHORT);
					toast1.show();
				}else if(mPillText.getText().length()==0){
					Toast toast1 = Toast.makeText(getApplicationContext(),R.string.error_pill, Toast.LENGTH_SHORT);
					toast1.show();
				}else if(mDaysText.getText().toString().equals(getResources().getString(R.string.no_days))){
					Toast toast1 = Toast.makeText(getApplicationContext(),R.string.error_day, Toast.LENGTH_SHORT);
					toast1.show();
				}else if(mArrayHours.isEmpty()){
					Toast toast1 = Toast.makeText(getApplicationContext(),R.string.error_hour, Toast.LENGTH_SHORT);
					toast1.show();
				} else {
					//updateAlarms();
					setResult(RESULT_OK);
					finish();
				}
			}

		});

		//Listener para el boton de añadir horas
		addTimeButton.setOnClickListener(new View.OnClickListener() {

			final Calendar c = Calendar.getInstance();

			public void onClick(View view) {
				//codigo para cuando se pulsa el boton: lanza el time dialog
				mHour = c.get(Calendar.HOUR_OF_DAY);
				mMinute = c.get(Calendar.MINUTE);
				showDialog(TIME_DIALOG_ID);
			}

		});       		

		//Listener para el boton que muestra la seleccion de dias de la semana
		checkBox.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showDialog(DIALOG_MULTIPLE_CHOICE);
			}

		});

		//Listener para cuando se pulsa sobre un elemento de la lista de horas
		mTimeList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AlertDialog.Builder adb=new AlertDialog.Builder(PillEdit.this);
				adb.setTitle("Delete?");
				adb.setMessage("Are you sure you want to delete " + mArrayHours.get(arg2));
				final int positionToRemove = arg2;
				adb.setNegativeButton("Cancel", null);
				adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {

					//Gestion del boton que confirma la eliminacion
					public void onClick(DialogInterface dialog, int which) {
						mArrayHours.remove(positionToRemove);
						updateList();
					}});

				adb.show();
			}
		});		
	}


	/**
	 * Actualiza las alarmas tras confirmar los cambios.
	 */
	private void updateAlarms(){
		Intent intent = new Intent(PillEdit.this, RepeatingAlarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(PillEdit.this,
				0, intent, 0);

		// And cancel the alarm.
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(sender);

		// We want the alarm to go off 30 seconds from now.

		Calendar calendar = Calendar.getInstance();
		Calendar currentDay;
		int day=calendar.get(Calendar.DAY_OF_WEEK);
		for (int i=0; i<mArrayDays.length;i++){
			if ((day-1+i)==mArrayDays.length) day=day-7;
			if(mArrayDays[day-1+i]){
				String h;
				int hourOfDay;
				int min;

				for (int j=0; j<mArrayDays.length; i++){
					h=mArrayHours.get(j);
					hourOfDay= Integer.parseInt(h.split(":")[0]);
					min= Integer.parseInt(h.split(":")[1]);
					currentDay=Calendar.getInstance();
					currentDay.set(Calendar.HOUR_OF_DAY, hourOfDay);
					currentDay.set(Calendar.MINUTE, min);
					if (i==0 || currentDay.get(Calendar.HOUR_OF_DAY) <= Calendar.getInstance().get(Calendar.HOUR_OF_DAY) || currentDay.get(Calendar.MINUTE) <= Calendar.getInstance().get(Calendar.MINUTE))
						currentDay.add(Calendar.DAY_OF_YEAR, i+7);
					else
						currentDay.add(Calendar.DAY_OF_YEAR, i);
					long firstTime= currentDay.getTimeInMillis();
					am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							firstTime, 7*24*3600*1000, sender);
				}
			}
		}
	}

	/**
	 * Pasa de ArrayList a String las horas
	 * @return String cn las horas
	 */
	private String hourArrayToString(){
		String hourString="";
		for (int i=0; i<mArrayHours.size(); i++){
			hourString=hourString + mArrayHours.get(i);
			if (i<(mArrayHours.size()-1)) hourString=hourString+ " - ";
		}
		return hourString;
	}

	/**
	 * Pasa de String a ArrayList las horas y lo guarda en el capo correspondiente
	 * @param hourString Horas a pasar al ArrayList
	 */
	private void hourStringToArray(String hourString){
		mArrayHours=new ArrayList<String>(Arrays.asList(hourString.split(" - ")));
	}

	/**
	 * Pasa de String a Array los dias y fija a true los valores marcados del dialogo de dias.
	 * @param daysString
	 */
	private void daysStringToArray(String daysString){
		for (int i=0; i<mArrayDays.length; i++){
			mArrayDays[i]= (daysString.indexOf(getResources().getStringArray(R.array.select_dialog_day)[i])) !=-1;
		}
	}

	/**
	 * Actualiza los dias a partir del dialogo de seleccion de dias y 
	 * lo fija como texto en el elemento visual correspondiente.
	 */
	private void updateDays() {
		String stringDays="";
		for (int i=0; i<mArrayDays.length; i++){
			if(mArrayDays[i]){
				if (stringDays != "") stringDays= stringDays + " - ";
				stringDays= stringDays + getResources().getStringArray(R.array.select_dialog_day)[i];
			}
		}
		if (stringDays=="") mDaysText.setText(R.string.no_days);
		else mDaysText.setText(stringDays);
	}

	/**
	 * Actualiza la lista de las horas	en su elemento visual.
	 */
	private void updateList(){
		mTimeList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mArrayHours));
	}
	/**
	 * Añade al ArrayList de horas la hora dada por parametros
	 * @param hourOfDay Hora
	 * @param minute Minuto
	 */
	private void updateTime(int hourOfDay, int minute) {

		mArrayHours.add(
				new StringBuilder()
				.append(pad(hourOfDay)).append(":")
				.append(pad(minute)).toString());
		updateList();
	}

	/**
	 * Añade un cero a la izquierda del entero dato.
	 * @param c Entero a añadir digito
	 * @return String con cero a la izquierda + c.
	 */
	private static String pad(int c) {
		if (c >= 10) return String.valueOf(c);
		else return "0" + String.valueOf(c);
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
			daysStringToArray(mDaysText.getText().toString());
			hourStringToArray(pillcursor.getString(pillcursor.getColumnIndexOrThrow(PillsDbAdapter.KEY_HOUR)));
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
	/**
	 * Salva el estado antes de pasar a otra actividad
	 */
	private void saveState() {
		String user = mUserText.getText().toString();
		String pill = mPillText.getText().toString();
		String days = mDaysText.getText().toString();
		String hour = hourArrayToString();
		//TODO Hay que a–adir que no cree DB si hay nulo y sacarlo de confirm.
		if (mRowId == null) {
			long id = mDbHelper.createPill(user, pill, days, hour);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updatePill(mRowId, user, pill, days, hour);
		}
	}
	/**
	 * Gestiona los distintos dialogos que pueden crearse, segun su id
	 * @param id Id del dialogo.
	 */
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
			.setMultiChoiceItems(R.array.select_dialog_day,mArrayDays,
					new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {}})
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

	/**
	 * Se le llama antes de crear un dialogo
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mHour, mMinute);
			break;            
		}
	}  

	//Listener del TimePiker (atributo!!)
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateTime(mHour, mMinute);
		}
	};
}


//Añadir que no se han puesto horas aun.
