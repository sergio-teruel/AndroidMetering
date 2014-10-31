package com.ics.metering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class Options extends Activity implements OnClickListener{
	String session_id = "";
	ProgressDialog pDialog;

	//static ProgressDialog pDialog;
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Put your code here
		setContentView(R.layout.options);

		// Get and Send supply points layouts
		LinearLayout layGetSupply = (LinearLayout) findViewById(R.id.layGetSupplyPoint); 
		LinearLayout laySendSupply = (LinearLayout) findViewById(R.id.laySendSupplyPoint);
		LinearLayout laySendAttach = (LinearLayout) findViewById(R.id.laySendAttachment);
		LinearLayout laySetting = (LinearLayout) findViewById(R.id.laySetting);
		//LinearLayout layEmptySupply = (LinearLayout) findViewById(R.id.layEmptySupplyPoint);
		layGetSupply.setOnClickListener(this);
		laySendSupply.setOnClickListener(this);
		laySendAttach.setOnClickListener(this);
		laySetting.setOnClickListener(this);
		//layEmptySupply.setOnClickListener(this);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)	{
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == 1) {
			OpenERPJson openERPJson = new OpenERPJson(this);
			openERPJson.getDataFromOpenerp();
		}
	}	
	@Override
	public void onClick(View v) {
		Intent myIntent = null;
		// TODO Auto-generated method stub
    	OpenERPJson openERPjson = new OpenERPJson(this);
		switch (v.getId()) {
		case R.id.layGetSupplyPoint:
			if (!msgDialogReadingPending(R.string.txtGetSupplyPoint)) {
				//Comprobar si la tabla tmp_import_list contiene datos, si tiene datos los mostraremos
				if (AdaptadorDB.getCountTable(dbTableEnum.tmp_Import_Sel) > 0){
					myIntent = new Intent (this, ImportSelectionList.class);
				}else {
					myIntent = new Intent(this, GeneralFilter.class);
				}
				myIntent.putExtra("isRemote", true);
//				startActivity(myIntent);
				startActivityForResult(myIntent, 1);
				//this.finish();
			}
			break;
		case R.id.laySendSupplyPoint:
	    	openERPjson.sendReadingToOpenerp();
			break;
		case R.id.laySendAttachment:
	    	openERPjson.sendAttachmentToOpenerp();
			break;
		case R.id.laySetting:
			myIntent = new Intent(this,Settings.class);
			Toast.makeText(this, "Show more settings", Toast.LENGTH_SHORT).show();
			startActivity(myIntent);
			break;
//		case R.id.layEmptySupplyPoint:
			//Borramos acometidas de la base de datos
//			if (!msgDialogReadingPending(R.string.txtEmptySupplyPoints)) {
//				AdaptadorDB.emptyTables();
//				Toast.makeText(this, R.string.msg_empty_tables, Toast.LENGTH_SHORT).show();
//			}
//			break;
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
