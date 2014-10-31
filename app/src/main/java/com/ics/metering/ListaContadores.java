package com.ics.metering;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class ListaContadores extends ListActivity {
	private myAdapter madapter = null;
	private static Cursor cur = null;
	private SharedPreferences pref = null;
	private int readPendingSupply = 0;
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
		myAdaptadorDB.openDB();
		
		getDataSql();
		madapter = new myAdapter(this);
		setListAdapter(madapter);
		
		readPendingSupply = madapter.getCount();
		
		this.setTitle(readPendingSupply + ""); // + R.string.supply_title_activity);

		this.getListView().setOnItemLongClickListener( new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				cur.moveToPosition(arg2);
				//Toast.makeText(getApplicationContext(), cur.getString(cur.getColumnIndex("id")), Toast.LENGTH_SHORT).show();
				manualIssue();
				madapter.notifyDataSetChanged();
				// TODO Auto-generated method stub
				return true;
			}
		} );
		
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent myIntent = new Intent(this,input_reading.class);
		cur.moveToPosition(position);
		//myIntent.putExtra("last_value", cur.getString(cur.getColumnIndex("last_value")));
		myIntent.putExtra("name", cur.getString(cur.getColumnIndex("name")));
		myIntent.putExtra("id", cur.getInt(cur.getColumnIndex("id")));
		startActivity(myIntent);
	}


	@Override
	protected void onStart() {
		super.onStart();
		getDataSql();
		madapter.notifyDataSetChanged();
		this.setTitle(getString(R.string.supply_title_activity)+" " + AdaptadorDB.getTotalSupplyByReaded()+"");
	}
	
	private void getDataSql (){
		cur = AdaptadorDB.getCursorBuscador("", dbTableEnum.Reading, false,"");
	}
	

	public static class myAdapter extends BaseAdapter {
		private Context myContext; 
		
		public myAdapter (Context ctx){
			myContext = ctx;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cur.getCount();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			cur.moveToPosition(position);
			return cur;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View myView = null;
			if (convertView == null) {
				LayoutInflater myInflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				myView = myInflater.inflate(R.layout.item_lista_reading, null);
			} else {
				myView = convertView;
			}

			cur.moveToPosition(position);
			
			ImageView imgInterior = (ImageView) myView.findViewById(R.id.imgReadingInterior);
			ImageView imgReaded = (ImageView) myView.findViewById(R.id.imgReadingReaded);
			ImageView imgIssue = (ImageView) myView.findViewById(R.id.imgReadingIssue);
			
			if (cur.getInt(cur.getColumnIndex("interior")) == 0) {
				imgInterior.setVisibility(View.INVISIBLE);
			} else {
				imgInterior.setImageResource(R.drawable.ic_ics_reading_interior);
				imgInterior.setVisibility(View.VISIBLE);
			}
			if (cur.getInt(cur.getColumnIndex("readed")) == 1) {
				imgReaded.setImageResource(R.drawable.ic_ics_reading_ok);
				imgReaded.setVisibility(View.VISIBLE);
			} else {
				//imgReaded.setImageResource(R.drawable.circle_blue_dark);
				imgReaded.setVisibility(View.INVISIBLE);
			}
			int pp = cur.getInt(cur.getColumnIndex("issue_id"));
			
			if (cur.getInt(cur.getColumnIndex("issue_id")) > 0) {
				imgIssue.setImageResource(R.drawable.ic_ics_issue);
				imgIssue.setVisibility(View.VISIBLE);
			} else {
				imgIssue.setVisibility(View.INVISIBLE);
			}

			TextView txtSupply = (TextView) myView.findViewById(R.id.txtReadingSupply);
			TextView txtContract = (TextView) myView.findViewById(R.id.txtReadingContract);
			TextView txtMeter = (TextView) myView.findViewById(R.id.txtReadingMeter);
			TextView txtPartner = (TextView) myView.findViewById(R.id.txtReadingPartner);
			
			txtSupply.setText(cur.getString(cur.getColumnIndex("name")));
			txtContract.setText(cur.getString(cur.getColumnIndex("contract")));
			txtMeter.setText(cur.getString(cur.getColumnIndex("meter")));
			txtPartner.setText(cur.getString(cur.getColumnIndex("partner")));
			return myView;
		}
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
    	menu.findItem(R.id.mnu_all_supply_points).setChecked(ICSUtil.readPreferenceBoolean("all_supply_point", this));
	    return true;
	}	
	
	
	/** 
	 * Add menu items
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_principal, menu);
		return true;  
	}  
	 
	/** 
	 * Define menu action
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = null;
	    switch (item.getItemId()) {  
//	        case R.id.mnu_filtrar_acometidas:  
//	        	// put your code here
//	        	myIntent = new Intent(this,filtrar_acometidas.class);
//	        	startActivity(myIntent);
//	        	break;
//	        case R.id.mnu_cargar_datos:
//	        	myIntent = new Intent (this,get_supply_points.class);
//	        	startActivity(myIntent);
//	        	break;
//	        case R.id.mnu_datos_demo:
//	        	AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
//	        	myAdaptadorDB.CargaDatosDemo(this);
//	        	break;
//	        case R.id.mnu_busqueda:
//	        	myIntent = new Intent (this, GeneralFilterList.class);
//	        	myIntent.putExtra("table", "Street");
//	        	startActivity(myIntent);
//	        	break;
	        case R.id.mnu_general_filter:
	        	myIntent = new Intent (this,GeneralFilter.class);
	        	myIntent.putExtra("isRemote", false);
	        	startActivity(myIntent);
	        	break;
	        case R.id.mnu_options:
	        	myIntent = new Intent (this,Options.class);
	        	startActivity(myIntent);
	        	break;
	        case R.id.mnu_all_supply_points:
	        	//Grabar key preference
	        	ICSUtil.writePreferenceBoolean("all_supply_point", !item.isChecked(), this);
	        	item.setChecked(!item.isChecked());
	    		getDataSql();
	    		madapter.notifyDataSetChanged();
	        	//Toast.makeText(this, "Checked: " + ICSUtil.readPreference("all_supply_point", this), Toast.LENGTH_LONG).show();
	        	break;
	        case R.id.mnu_filter_gps:
	        	//Grabar key preference
	        	ICSUtil.writePreferenceBoolean("filter_gps", !item.isChecked(), this);
	        	item.setChecked(!item.isChecked());
	        	//Toast.makeText(this, "Checked: " + ICSUtil.readPreference("all_supply_point", this), Toast.LENGTH_LONG).show();
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}
    public void manualIssue(){
    	if (cur.getInt(cur.getColumnIndex("readed")) == 0){
	    	Cursor c = AdaptadorDB.getTable("Issue", "auto_eval = 0");
	    	c.moveToFirst();
	    	if (c.getCount() > 0){
				// Pantalla de seleccion de detalle de la incidencia
				Intent myIntent = new Intent(this, IssueReading.class);
				myIntent.putExtra("readingId", cur.getInt(cur.getColumnIndex("id")));
				myIntent.putExtra("issueId", c.getInt(c.getColumnIndex("id")));
				myIntent.putExtra("issue", c.getString(c.getColumnIndex("name")));
				myIntent.putExtra("manualIssue", true);
				startActivity(myIntent);
	    	}
    	}
    }

}

