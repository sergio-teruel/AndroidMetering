package com.ics.metering;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity implements OnClickListener{

	TextView txtServerAddress = null;
	TextView txtServerPort = null;
	TextView txtServerUser = null;
	TextView txtServerPassword = null;
	TextView txtServerDatabase = null;
	Button	btnEmptyData = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Put your code here
		setContentView(R.layout.settings);
		
		txtServerAddress = (TextView) findViewById(R.id.txtServerAddress);
		txtServerPort = (TextView) findViewById(R.id.txtServerPort);
		txtServerUser = (TextView) findViewById(R.id.txtServerUser);
		txtServerPassword = (TextView) findViewById(R.id.txtServerPassword);
		txtServerDatabase = (TextView) findViewById(R.id.txtServerDatabase);
		btnEmptyData = (Button) findViewById(R.id.btnEmptyData);
		btnEmptyData.setOnClickListener(this);
		
		readValues();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_accept, menu);
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {  
	        case R.id.mnu_accept:
	        	//Toast.makeText(this, listIdSelected.toString(), Toast.LENGTH_SHORT).show();
	        	writeValues();
	    		this.finish();	        	
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}
	
	private void writeValues(){
		String a = "";
		a = txtServerAddress.getText().toString();
		ICSUtil.writePreference("ServerAddress", txtServerAddress.getText().toString(), this);
		ICSUtil.writePreference("ServerPort", txtServerPort.getText().toString(), this);
		ICSUtil.writePreference("ServerUser", txtServerUser.getText().toString(), this);
		ICSUtil.writePreference("ServerPassword", txtServerPassword.getText().toString(), this);
		ICSUtil.writePreference("ServerDatabase", txtServerDatabase.getText().toString(), this);
	}
	
	private void readValues (){
		txtServerAddress.setText(ICSUtil.readPreference("ServerAddress", this));
		txtServerPort.setText(ICSUtil.readPreference("ServerPort", this));
		txtServerUser.setText(ICSUtil.readPreference("ServerUser", this));
		txtServerPassword.setText(ICSUtil.readPreference("ServerPassword", this));
		txtServerDatabase.setText(ICSUtil.readPreference("ServerDatabase", this));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnEmptyData:
			//Borramos acometidas de la base de datos
			if (!msgDialogReadingPending(R.string.txtEmptySupplyPoints)) {
				AdaptadorDB.emptyTables();
				Toast.makeText(this, R.string.msg_empty_tables, Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
	}
	private boolean msgDialogReadingPending (int titleResource) {
		boolean result = false; 
		Cursor c = AdaptadorDB.getReading();
		if (c.getCount()>0) {
			result = true;
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(titleResource);
			alertDialogBuilder
			.setMessage(R.string.dialog_options_getsupply_message)
			.setCancelable(false)

//			.setPositiveButton(R.string.msg_yes,new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog,int id) {
//					//Enviamos lecturas a OpenERP
//					MeteringJSON.sendJSONReading(getApplicationContext());
//				}
//			  })
			.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
		}
		return result;
	}
	
	
}
