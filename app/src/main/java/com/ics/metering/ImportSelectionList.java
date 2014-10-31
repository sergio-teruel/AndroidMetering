package com.ics.metering;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class ImportSelectionList extends Activity {
	private myAdapter madapter = null;
	private static Cursor mcursor = null;
	ListView lvTmpImport = null;
	public static ProgressDialog pDialog;
	private static final int DIALOG_SUPPLY_PENDING = 1;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_selection_list);
		lvTmpImport = (ListView) findViewById(R.id.lvTmpImport);
		
		getDataSql();
		madapter = new myAdapter(this);
	
		lvTmpImport.setAdapter(madapter);
	}
	@Override
	protected void onStart() {
		super.onStart();
		getDataSql();
		madapter.notifyDataSetChanged();
	}
	
	private void getDataSql (){
		mcursor = AdaptadorDB.getCursorBuscador("", dbTableEnum.tmp_Import_Sel,  true, "table_id");
	}
	

	public static class myAdapter extends BaseAdapter {
		private Context myContext; 
		
		public myAdapter (Context ctx){
			myContext = ctx;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mcursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			mcursor.moveToPosition(position);
			return mcursor;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void notifyDataSetChanged()
		{
			super.notifyDataSetChanged();
			
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View myView = null;
			if (convertView == null) {
				LayoutInflater myInflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				myView = myInflater.inflate(R.layout.item_with_delete, null);
			} else {
				myView = convertView;
			}
			mcursor.moveToPosition(position);
			TextView txtItem = (TextView) myView.findViewById(R.id.txtItemWithDelete);
			txtItem.setText(mcursor.getString(mcursor.getColumnIndex("name")));
			
			
			ImageView imgDelete = (ImageView) myView.findViewById(R.id.imgDelete);
			imgDelete.setClickable(true);
			imgDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch (v.getId()) {
					case R.id.imgDelete:
						mcursor.moveToPosition(position);
						AdaptadorDB.removeTmpImportSelected(mcursor.getInt(mcursor.getColumnIndex("id")));
						mcursor.requery();
						notifyDataSetChanged();
						break;
					default:
						break;
					}
				}
			});
			return myView;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.import_selection_list, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)	{
		super.onActivityResult(requestCode, resultCode, intent);
		setResult(resultCode);
		finish();
	}	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {  
	        case R.id.mnu_plus:
	        	Intent intent = new Intent(this, GeneralFilter.class);
	        	intent.putExtra("isRemote", true);
	        	ICSUtil.clearImportPreferences(this);
	        	startActivityForResult(intent, 3);
	        	//this.finish();
	        	break;
	        case R.id.mnu_accept:
	        	//Añadiremos a la tabla READING segun las condiciones anteriores
	        	if (lvTmpImport.getCount() > 0){
					ICSUtil.clearImportPreferences(this);
					setResult(1);
					finish();
	        	}else{
	        		Toast.makeText(getApplicationContext(), R.string.msg_tmp_import_selection_no_elements, Toast.LENGTH_SHORT).show();
	        	}
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}
}
