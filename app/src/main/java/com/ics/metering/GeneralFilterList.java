package com.ics.metering;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class GeneralFilterList extends Activity implements OnItemClickListener {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	EditText txtSearch;
	ListView lv = null;
	private static Cursor mCursor = null;
	static JSONObject objOpenERP = null;
	static JSONArray arrOpenERP = null;
	CheckAdapter mAdapter;
	CheckAdapter chAdapter = null;
	dbTableEnum table = null ;
	private SharedPreferences pref = null;
	private SharedPreferences.Editor editor = null;
	private Set<Integer> listIdSelected = new HashSet<Integer>();
	private Set<Integer> listIdSelectedPosiciones = new HashSet<Integer>();
	String session_id = "";
	Boolean isRemote = false;
	ProgressDialog pDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Put your code here
		setContentView(R.layout.general_filter_list);
		Bundle bundle = getIntent().getExtras();
		table = (dbTableEnum) bundle.getSerializable("table");
		isRemote = bundle.getBoolean("isRemote", false);
		//dbTableEnum currentTable = dbTableEnum.valueOf(bundle.getString("table")); 
		pref = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
		editor = pref.edit();
		
		txtSearch = (EditText) findViewById(R.id.txtGeneralFilterList);
		lv = (ListView) findViewById(R.id.lvGeneralFilterList);
		lv.setOnItemClickListener(this);
		 
		txtSearch.selectAll();
		if (isRemote){
			pDialog = new ProgressDialog(this);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        pDialog.setMessage("Procesando...");
	        pDialog.setCancelable(true);
	        pDialog.setMax(100);

	        //ICSUtil.clearImportPreferences(this);
			//MeteringJSON.fillTmpListItem(table, this);
	       new fillTmpListItemTask().execute(table);
			// Cuando acaba la tarea de carga de datos de OpenERP he continuar con la carga de la pantalla
	       	// desde onPostExecute
	       
		}else{
			cargaPantalla();
		}
	}

	public void cargaPantalla(){
		mCursor = AdaptadorDB.getCursorBuscador("",table, isRemote,"");
			
		startManagingCursor(mCursor);

		switch (table) {
		case CITY:
	        String[] fromC = new String[] { "id", "name"};
	        int[] toC = new int[] { R.id.CheckListItem, R.id.text1};
	        //int[] toC = new int[] { android.R.id.text1, R.id.text1, R.id.txtcode};
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromC, toC);
			break;
		case BOOK:
	        String[] fromB = new String[] { "id", "name"};
	        int[] toB = new int[] { R.id.CheckListItem, R.id.text1};
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromB, toB);
			break;
		case STREET:
	        String[] fromS = new String[] { "id", "name"};
	        int[] toS = new int[] { R.id.CheckListItem, R.id.text1};
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromS, toS);
			break;
		case READING:
	        String[] fromR = new String[] { "id", "name"};
	        int[] toR = new int[] { R.id.CheckListItem, R.id.text1};
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromR, toR);
			break;
		default:
			break;
		}
        lv.setAdapter(mAdapter);

        TextWatcher textWatcher = new TextWatcher() {
		    @Override
		    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		    @Override
		    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		    @Override
		    public void afterTextChanged(Editable editable) {
		       //here, after we introduced something in the EditText we get the string from it
		    	//String p = editable.toString();
				mCursor = AdaptadorDB.getCursorBuscador(editable.toString(), table, isRemote,"");
		    	mAdapter.notifyDataSetChanged();
		        //mAdapter.changeCursor(mCursor);
		    }
		};
		txtSearch.addTextChangedListener(textWatcher);
	}

	private void saveSelection(boolean oneItemOnly){
		String keyId ="";
		String keyName ="";
		String multiText="";
		switch (table) {
		case CITY:
			keyId = "city_id";
			keyName = "city";
			break;
		case BOOK:
			keyId = "book_id";
			keyName = "book";
			break;
		case STREET:
			keyId = "street_id";
			keyName = "street";
			break;
		case READING:
			keyId = "supply_point_id";
			keyName = "supply_point";
			break;
		default:
			break;
		}
		
		if (isRemote) {
			keyId = "import_" + keyId;
			keyName = "import_" + keyName;
		}
		if (listIdSelected.size() < 2) {
			ICSUtil.writePreference(keyId, mCursor.getString(mCursor.getColumnIndex("id")), this);
		} else {
			ICSUtil.writePreference(keyId, ICSUtil.sinCorchetes(listIdSelected.toString()), this);
			multiText = " y (" + listIdSelected.size() + ") mas";
		}
		ICSUtil.writePreference(keyName, mCursor.getString(mCursor.getColumnIndex("name")) + multiText, this);
		//+1 por limpiar las preferencias inferiores sin elimnar la preferencia de la tabla que estamos cambiando
		ICSUtil.clearPreferences(table.ordinal(), true, isRemote, this);
		if (isRemote) {
			if (listIdSelectedPosiciones.size() == 0) saveSelectionTmpImport(table.ordinal());
			for(Integer setElement:listIdSelectedPosiciones) {
	        	mCursor.moveToPosition(Integer.parseInt(setElement.toString()));
	        	saveSelectionTmpImport(table.ordinal());
	        }
			
		}
//		}else{
//			
//			//Añadir a la tabla tmp_import_selected
//			if (oneItemOnly){
//				//No se ha marcado ninguno
//				saveSelectionTmpImport(table.ordinal());
//			}else{
//
//				for(Integer setElement:listIdSelectedPosiciones) {
//		        	mCursor.moveToPosition(Integer.parseInt(setElement.toString()));
//		        	saveSelectionTmpImport(table.ordinal());
//			        }
//				//Para pulsacion sobre un elemento
//			}
//		}
	}
	private void saveSelectionTmpImport(int table_id){
		AdaptadorDB adaptadorDB = new AdaptadorDB(this);
		ContentValues cv = new ContentValues();

		cv.put("table_id", table.ordinal());
		cv.put("key_id", mCursor.getString(mCursor.getColumnIndex("id")));
		cv.put("name", mCursor.getString(mCursor.getColumnIndex("name")));
		cv.put("active", "0");
		
		adaptadorDB.addTmpImportSelected(cv);
	}	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.lvGeneralFilterList:
			saveSelection(true);
			//if (isRemote) showImportSelectionList();
			this.finish();
			//Toast.makeText(this, mCursor.getString(mCursor.getColumnIndex("name")) , Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}
	 public class Item {
		 private String id;
		 private String description;

		 public Item(String pID, String pDesc) {
		 id = pID;
		 description = pDesc;
		 }

		 public long getID() {
		 return Integer.valueOf(id);
		 }

		 public String getDescription() {
		 return description;
		 }
	 } 
	
	 public static class ViewHolder {
		 public CheckBox chkItem;
		 }
	
	private class CheckAdapter extends BaseAdapter {
		//private ArrayList<Item> items = new ArrayList<Item>();
		private LayoutInflater inflater;
		private boolean[] itemSelection;
		Context myContext = null;
		String[] from = null;
		int[] to = null;
		int layoutItem = 0;
		public TextView[] textArray;

		public CheckAdapter(Context ctx, int layoutItem, Cursor cur, String[] from, int[] to) {
			//El primer view que viene es un check y el resto lo que se quiera
			myContext = ctx;
			this.from = from;
			this.to = to;
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.itemSelection = new boolean[getCount()];
			this.textArray = new TextView[to.length]; 
			this.layoutItem = layoutItem;
		}
		 
		@Override
		public int getCount() {
			return mCursor.getCount();
		}

		 
		 @Override
		 public String getItem(int position) {
			 mCursor.moveToPosition(position);
			 return  mCursor.getString(mCursor.getColumnIndex("name"));
		 }

		 @Override
		 public long getItemId(int position) {
			 mCursor.moveToPosition(position);
			 return  mCursor.getLong(mCursor.getColumnIndex("id"));
		 }	

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				LayoutInflater myInflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = myInflater.inflate(layoutItem, null);
			} 

			final ViewHolder holder = new ViewHolder();
			//holder.chkItem = (CheckBox)convertView.findViewById(R.id.CheckListItem);
			for (int i = 0; i < to.length; i++) {
				if (i == 0) {
					holder.chkItem = (CheckBox)convertView.findViewById(to[i]);
				} else {
					textArray[i] = (TextView) convertView.findViewById(to[i]);
				}
			}
			mCursor.moveToPosition(position);
			
			holder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					itemSelection[position] = holder.chkItem.isChecked();
					mCursor.moveToPosition(position);
					if (holder.chkItem.isChecked()) {
						listIdSelected.add(mCursor.getInt(mCursor.getColumnIndex("id")));
						listIdSelectedPosiciones.add(position);
					} else {
						listIdSelected.remove(mCursor.getInt(mCursor.getColumnIndex("id")));
						listIdSelectedPosiciones.remove(position);
					}
				}
			});
			
			for (int i = 1; i < from.length; i++) {
				textArray[i].setText(mCursor.getString(mCursor.getColumnIndex(from[i])));
			}
			holder.chkItem.setChecked(itemSelection[position]);
			convertView.setTag(holder);
			return convertView;
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_general_filter_list, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {  
	        case R.id.mnu_filter_accept:
	        	if (!listIdSelected.isEmpty()){
	        		saveSelection(false);
		        	//if (isRemote) showImportSelectionList();
		    		this.finish();
	        	}else{
	        		Toast.makeText(getApplicationContext(), R.string.msg_GeneralFilterList_no_elements , Toast.LENGTH_SHORT).show();
	        	}
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}
	
	public void showImportSelectionList(){
		Intent intent = new Intent(this, ImportSelectionList.class);
		startActivity(intent);
	}
	
	private class fillTmpListItemTask extends AsyncTask<dbTableEnum, Integer, Boolean>{
	    @Override
	    protected void onPreExecute() {
	        pDialog.setMax(100);
	        pDialog.setProgress(0);
	        pDialog.show();
	    }

	    protected void onProgressUpdate(Integer... values) {
	        int progreso = values[0].intValue();
	        pDialog.setProgress(progreso);
	    }

		@Override
		protected Boolean doInBackground(dbTableEnum... params) {
			// TODO Auto-generated method stub
	    	ContentValues cv = new ContentValues();
			JSONObject objOpenERP = new JSONObject();
			AdaptadorDB adaptadorDB = new AdaptadorDB(GeneralFilterList.this);
			JSONArray arrOpenERP = MeteringJSON.getJSONBuscador(MeteringJSON.loginOpenerp(GeneralFilterList.this), "", 
					params[0], GeneralFilterList.this);
			adaptadorDB.emptyTableTmpListItem();
			for (int i = 0; i < arrOpenERP.length(); i++) {
				try {
					objOpenERP = new JSONObject(arrOpenERP.getString(i));
					cv.put("id", objOpenERP.getInt("id"));
					cv.put("name", objOpenERP.getString("name"));
					adaptadorDB.addTmpListItem(cv);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            publishProgress(i*10);
	            if(isCancelled()) break;
			}
	        return true;
		}

		protected void onPostExecute(Boolean result) {
			cargaPantalla();
	        pDialog.dismiss();
	    }

		@Override
	    protected void onCancelled() {
			Toast.makeText(GeneralFilterList.this, R.string.msg_cancel, Toast.LENGTH_SHORT).show();
	    }
	}
}