package com.android.takethepill;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class TakeThePill extends ListActivity {

	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;

	private PillsDbAdapter mDbHelper;

	//PREFS
	public static final String PREFS_NAME = "PrefsFile";
	public static final String EMAIL_KEY = "email";
	public static final String PHONE_KEY = "phone";
	private String mEmail, mPhone;

	/**
	 * Metodo al que se llama al crear la actividad
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restorePrefs();
		setContentView(R.layout.pills_list);
		mDbHelper = new PillsDbAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());
	}
	/**
	 * Rellena cada pills_row con informacion de la BBDD 
	 */
	private void fillData() {
		Cursor pillsCursor = mDbHelper.fetchAllPills(); //Coge todas las filas de la BBDD
		startManagingCursor(pillsCursor);//android lo gestiona automaticamente

		// Create an array to specify the fields we want to display in the list.
		String[] from = new String[]{
				PillsDbAdapter.KEY_USER,
				PillsDbAdapter.KEY_PILL, 
				PillsDbAdapter.KEY_DAYS,
				PillsDbAdapter.KEY_HOUR
		};

		// and an array of the fields we want to bind those fields to.
		int[] to = new int[]{R.id.user, R.id.pill, R.id.days, R.id.hour};

		// Now create a simple cursor adapter and set it to display.
		SimpleCursorAdapter pills = new SimpleCursorAdapter(this, R.layout.pills_row, pillsCursor, from, to);
		setListAdapter(pills);
	}

	/**
	 * Metodo que indica como crear el menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater(); 
		inflater.inflate(R.menu.main_menu, menu); 
		return true; 
	}

	/**
	 * Metodo que maneja el evento de seleccion de menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){ 
		case R.id.insert_id: 
			createPill();
			return true; 
		case R.id.prefs_id: 
			Intent i0 = new Intent(this, PreferencesActivity.class);
			startActivity(i0);
			return true;
		case R.id.help_id:
			Intent i1 = new Intent(this, HelpActivity.class);
			startActivity(i1);
			return true;
		case R.id.about_id:
			Intent i2 = new Intent(this, AboutActivity.class);
			startActivity(i2);
			return true;
		default: 
			return super.onOptionsItemSelected(item); 
		} 
	}

	/**
	 * Metodo para crear el menu contextual sobre elementos de la lista
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater(); 
		inflater.inflate(R.menu.context_menu, menu);
	}

	/**
	 * Metodo que gestiona la seleccion en menu contextual sobre elementos de la lista 
	 */	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final long id=info.id;
		switch(item.getItemId()) {
		case R.id.delete_id:

			AlertDialog.Builder adb=new AlertDialog.Builder(TakeThePill.this);
			adb.setTitle("Delete?");
			adb.setMessage("Are you sure?");
			adb.setNegativeButton("Cancel", null);
			adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {

				//Gestion del boton que confirma la eliminacion
				public void onClick(DialogInterface dialog, int which) {
					mDbHelper.deletePill(id);
					fillData();
				}});
			adb.show();			
			return true;
		case R.id.edit_id:			
			Intent i = new Intent(this, PillEdit.class);
			i.putExtra(PillsDbAdapter.KEY_ROWID, id);
			startActivityForResult(i, ACTIVITY_EDIT);			
			return true;
		}

		return super.onContextItemSelected(item);
	}
	/**
	 * Metodo que inicia la actividad de crear un nuevo item en la lista
	 */
	private void createPill() {
		Intent i = new Intent(this, PillEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}	

	/**
	 * Metodo que se llama cuando un item de la lista es seleccionado. 
	 * Lanza la actividad de  edicion.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i1 = new Intent(this, PillViewActivity.class);
		i1.putExtra(PillsDbAdapter.KEY_ROWID, id);
		startActivity(i1);

	}	

	/**
	 * Metodo por el que se vuelve desde la actividad de edición.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}


	private void restorePrefs(){
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mEmail= settings.getString(EMAIL_KEY, "empty");
		mPhone= settings.getString(PHONE_KEY, "0");
	}


}

