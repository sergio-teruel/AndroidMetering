package com.ics.metering;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class GeneralFilter extends Activity implements OnClickListener{
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	TextView txtFilterCity = null;
	TextView txtFilterBook =null;
	TextView txtFilterStreet = null;
	TextView txtFilterSupplyPoint = null;
	Boolean isRemote = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Put your code here
		setContentView(R.layout.general_filter);
		Bundle bundle = getIntent().getExtras();
		isRemote = bundle.getBoolean("isRemote", false);
		
		LinearLayout layCity = (LinearLayout) findViewById(R.id.layGeneralFilterCity);
		LinearLayout layBook = (LinearLayout) findViewById(R.id.layGeneralFilterBook);
		LinearLayout layStreet = (LinearLayout) findViewById(R.id.layGeneralFilterStreet);
		LinearLayout laySupply = (LinearLayout) findViewById(R.id.layGeneralFilterSupply);

		ImageView imgClearCity = (ImageView) findViewById(R.id.imgClearCity);
		ImageView imgClearBook = (ImageView) findViewById(R.id.imgClearBook);
		ImageView imgClearStreet = (ImageView) findViewById(R.id.imgClearStreet);
		ImageView imgClearSupply = (ImageView) findViewById(R.id.imgClearSupply);
		
		txtFilterCity = (TextView) findViewById(R.id.txtFilterCity);
		txtFilterBook = (TextView) findViewById(R.id.txtFilterBook);
		txtFilterStreet = (TextView) findViewById(R.id.txtFilterStreet);
		txtFilterSupplyPoint = (TextView) findViewById(R.id.txtFilterSupplyPoint);
		
		layCity.setOnClickListener(this);
		layBook.setOnClickListener(this);
		layStreet.setOnClickListener(this);
		laySupply.setOnClickListener(this);
		
		imgClearCity.setOnClickListener(this);
		imgClearBook.setOnClickListener(this);
		imgClearStreet.setOnClickListener(this);
		imgClearSupply.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)	{
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == 1) {
			setResult(resultCode);
			this.finish();
		}
		
	}		

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//Intent myIntent = null;
		boolean mostrarIntent = true;
		if (isRemote) AdaptadorDB.deleteTmpImportNoActive();		
		Intent myIntent = new Intent (this, GeneralFilterList.class);
    	myIntent.putExtra("isRemote", isRemote);
		switch (v.getId()) {
		case R.id.layGeneralFilterCity:
        	myIntent.putExtra("table", dbTableEnum.CITY);
        	break;
		case R.id.layGeneralFilterBook:
        	myIntent.putExtra("table", dbTableEnum.BOOK );
        	break;
		case R.id.layGeneralFilterStreet:
        	myIntent.putExtra("table", dbTableEnum.STREET);
        	break;
		case R.id.layGeneralFilterSupply:
        	myIntent.putExtra("table", dbTableEnum.READING); 
        	break;
		case R.id.imgClearCity:
			mostrarIntent = false;
			ICSUtil.clearPreferences(dbTableEnum.CITY.ordinal(), false, isRemote, this);
			txtFilterCity.setText("");
			break;
		case R.id.imgClearStreet:
			mostrarIntent = false;
			ICSUtil.clearPreferences(dbTableEnum.STREET.ordinal(), false, isRemote, this);
			txtFilterStreet.setText("");
			break;
		case R.id.imgClearBook:
			mostrarIntent = false;
			ICSUtil.clearPreferences(dbTableEnum.BOOK.ordinal(), false, isRemote, this);
			txtFilterBook.setText("");
			break;
		case R.id.imgClearSupply:
			mostrarIntent = false;
			ICSUtil.clearPreferences(dbTableEnum.READING.ordinal(), false, isRemote, this);
			txtFilterSupplyPoint.setText("");
			break;
		default:
			break;
		}
		if (mostrarIntent) {
			if ((txtFilterCity.getText().length()==0) && (v.getId() != R.id.layGeneralFilterCity)){
				Toast.makeText(this, "Seleccione municipio", Toast.LENGTH_SHORT).show();
			}else{
				startActivity(myIntent);
				//this.finish();
			}
		}
	}

	@Override
	public void onStart(){
		super.onStart();
		String keyCity = "city";
		String keyBook = "book";
		String keyStreet = "street";
		String keySupply = "supply_point";
		if (isRemote){
			keyCity = "import_" + keyCity;
			keyBook = "import_" + keyBook;
			keyStreet = "import_" + keyStreet;
			keySupply = "import_" + keySupply;
			//ICSUtil.clearImportPreferences(this);
		}
		txtFilterCity.setText(ICSUtil.readPreference(keyCity , this));
		txtFilterBook.setText(ICSUtil.readPreference(keyBook , this));
		txtFilterStreet.setText(ICSUtil.readPreference(keyStreet , this));
		txtFilterSupplyPoint.setText(ICSUtil.readPreference(keySupply , this));
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
	        	if (isRemote){
	        		AdaptadorDB.activeTmpImportSelected();
	        		if (txtFilterSupplyPoint.getText().length()>0){
	        			//ICSUtil.addImportPreference(dbTableEnum.READING, this);
	        		}else {
	        			if ((txtFilterBook.getText().length() + txtFilterStreet.getText().length())>0){
		        			//ICSUtil.addImportPreference(dbTableEnum.STREET, this);
		        			//ICSUtil.addImportPreference(dbTableEnum.BOOK, this);
	        			}else {
		        			if (txtFilterCity.getText().length()>0){
			        			//ICSUtil.addImportPreference(dbTableEnum.CITY, this);
		        			}
	        			}
	        		}
	        		Intent intent = new Intent(this,ImportSelectionList.class);
	        		startActivityForResult(intent, 2);
	        	}else{
		    		this.finish();	        	
	        	}
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}
}
