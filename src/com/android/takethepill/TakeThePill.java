package com.android.takethepill;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class TakeThePill extends ListActivity {

	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int PREFS_ID = Menu.FIRST + 2;
	private static final int HELP_ID = Menu.FIRST + 3;
	private static final int ABOUT_ID = Menu.FIRST + 4;

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
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		menu.add(0, PREFS_ID, 1, R.string.menu_prefs);
		menu.add(0, HELP_ID, 2, R.string.menu_help);
		menu.add(0, ABOUT_ID, 3, R.string.menu_about);
		return true;
	}

	/**
	 * Metodo que maneja el evento de seleccion de menu
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case INSERT_ID:
			createPill();
			return true;
		case PREFS_ID:
			//TODO Activity para cambiar las preferencias
			setPrefs("mr.pujo@gmail.com","650928719");
			return true;

		case HELP_ID:
			//TODO
			return true;
		case ABOUT_ID:
			//TODO 
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * Metodo para crear el menu contextual sobre elementos de la lista
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	/**
	 * Metodo que gestiona la seleccion en menu contextual sobre elementos de la lista 
	 */	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mDbHelper.deletePill(info.id);
			fillData();
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
		Intent i = new Intent(this, PillEdit.class);
		i.putExtra(PillsDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_EDIT);
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

	private void setPrefs(String email, String phone){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();	

		editor.putString(EMAIL_KEY, email);
		editor.putString(PHONE_KEY, phone);

		editor.commit();

		Toast toast = Toast.makeText(getApplicationContext(),R.string.changed_prefs, Toast.LENGTH_SHORT);
		toast.show();

	}
}

