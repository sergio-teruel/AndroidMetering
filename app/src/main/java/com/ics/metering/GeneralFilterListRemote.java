package com.ics.metering;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class GeneralFilterListRemote extends Activity implements OnItemClickListener {
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
	String session_id = "";
	Boolean isRemote = false;
	
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
			mCursor = null;
			session_id = MeteringJSON.loginOpenerp(this);
			arrOpenERP = MeteringJSON.getJSONBuscador(session_id, "", table, getApplicationContext());
		}else{
			mCursor = AdaptadorDB.getCursorBuscador("",table, false,"");
			startManagingCursor(mCursor);
		}
		

        switch (table) {
		case CITY:
	        String[] fromC = new String[] { "id", "name", "id"};
	        int[] toC = new int[] { R.id.CheckListItem, R.id.text1, R.id.txtcode};
	        //int[] toC = new int[] { android.R.id.text1, R.id.text1, R.id.txtcode};
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromC, toC);
			break;
		case BOOK:
	        String[] fromB = new String[] { "id", "name", "id" };
	        int[] toB = new int[] { R.id.CheckListItem, R.id.text1, R.id.txtcode };
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromB, toB);
			break;
		case STREET:
	        String[] fromS = new String[] { "id", "name", "id" };
	        int[] toS = new int[] { R.id.CheckListItem, R.id.text1, R.id.txtcode };
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromS, toS);
			break;
		case READING:
	        String[] fromR = new String[] { "id", "name", "id" };
	        int[] toR = new int[] { R.id.CheckListItem, R.id.text1, R.id.txtcode };
	        mAdapter = new CheckAdapter(this, R.layout.list_item_street, mCursor, fromR, toR);
			break;
		default:
			break;
		}

		
//		if (table.equals("Street")) {
//	        String[] from = new String[] { "id", "name", "id" };
//	        int[] to = new int[] { android.R.id.text1, R.id.text1, R.id.txtcode };
//	        mAdapter = new SimpleCursorAdapter(this, R.layout.list_item_street, mCursor, from, to);
//		} else {
//
//		}

//		if (mCursor != null) {
        //mAdapter.changeCursor(mCursor);
        //chAdapter.changeCursor(mCursor);
//		}
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
				if (isRemote){
					arrOpenERP = MeteringJSON.getJSONBuscador(session_id, editable.toString(), table, getApplicationContext());
				}else{
					mCursor = AdaptadorDB.getCursorBuscador(editable.toString(), table, false,"");
				}
		    	mAdapter.notifyDataSetChanged();
		        //mAdapter.changeCursor(mCursor);
		    }
		};
		txtSearch.addTextChangedListener(textWatcher);
	}


	private void saveSelection(){
		//private void saveSelection(Boolean multi){
		//String ids = "";
		String keyId ="";
		String keyName ="";
		String multiText="";
		switch (table) {
		case CITY:
			//ICSUtil.writePreference("city_id", mCursor.getString(mCursor.getColumnIndex("id")), this);
			//ICSUtil.writePreference("city", mCursor.getString(mCursor.getColumnIndex("name")), this);
			keyId = "city_id";
			keyName = "city";
			break;
		case BOOK:
			//ICSUtil.writePreference("book_id", mCursor.getString(mCursor.getColumnIndex("id")), this);
			//ICSUtil.writePreference("book", mCursor.getString(mCursor.getColumnIndex("name")), this);
			keyId = "book_id";
			keyName = "book";
			break;
		case STREET:
			//ICSUtil.writePreference("street_id", mCursor.getString(mCursor.getColumnIndex("id")), this);
			//ICSUtil.writePreference("street", mCursor.getString(mCursor.getColumnIndex("name")), this);
			keyId = "street_id";
			keyName = "street";
			break;
		case READING:
//			ICSUtil.writePreference("supply_point_id", mCursor.getString(mCursor.getColumnIndex("id")), this);
//			ICSUtil.writePreference("supply_point", mCursor.getString(mCursor.getColumnIndex("name")), this);
			keyId = "supply_point_id";
			keyName = "supply_point";
//			if (multi == true) {
//				ids = listIdSelected.toString();
//				ICSUtil.writePreference("supply_point", mCursor.getString(mCursor.getColumnIndex("name")) + " y otros sdf dsfsdf sd fsd fsdfsdf sdf sdf sd fsd fsd fsd fsd fsd fsd fsd fsd fsd f(" + listIdSelected.size()+ ") mas", this);
//			} else {
//				ids = mCursor.getString(mCursor.getColumnIndex("id"));
//				ICSUtil.writePreference("supply_point", mCursor.getString(mCursor.getColumnIndex("name")), this);
//			}
//			ICSUtil.writePreference("supply_point_id", ids, this);
			break;
		default:
			break;
		}
		if (isRemote){
			try {
				if (listIdSelected.size() < 2) {
					ICSUtil.writePreference(keyId, objOpenERP.getString("id"), this);
				} else {
					ICSUtil.writePreference(keyId, ICSUtil.sinCorchetes(listIdSelected.toString()), this);
					multiText = " y (" + listIdSelected.size() + ") mas";
				}
				ICSUtil.writePreference(keyName, objOpenERP.getString("name") + multiText, this);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {
			if (listIdSelected.size() < 2) {
				ICSUtil.writePreference(keyId, mCursor.getString(mCursor.getColumnIndex("id")), this);
			} else {
				ICSUtil.writePreference(keyId, ICSUtil.sinCorchetes(listIdSelected.toString()), this);
				multiText = " y (" + listIdSelected.size() + ") mas";
			}
			ICSUtil.writePreference(keyName, mCursor.getString(mCursor.getColumnIndex("name")) + multiText, this);
		}
		clearPreferences(table.ordinal());
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.lvGeneralFilterList:
			saveSelection();
			this.finish();
			//Toast.makeText(this, mCursor.getString(mCursor.getColumnIndex("name")) , Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}
	public void clearPreferences(int ordinal){
		if (ordinal<1) {
			ICSUtil.writePreference("book_id", "", this);
			ICSUtil.writePreference("book", "", this);
		} 
		if (ordinal<2) {
			ICSUtil.writePreference("street_id", "", this);
			ICSUtil.writePreference("street", "", this);
		} 
		if (ordinal<3) {
			ICSUtil.writePreference("supply_point_id", "", this);
			ICSUtil.writePreference("supply_point", "", this);
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
		 
//		public void addItem(final Item item) {
//			items.add(item);
//			notifyDataSetChanged();
//		}
		 
		@Override
		public int getCount() {
			if (isRemote){
				return arrOpenERP.length();
			}else{
				return mCursor.getCount();
			}
		}
		 
		 @Override
		 public String getItem(int position) {
			 if (isRemote){
				 try {
						objOpenERP = new JSONObject(arrOpenERP.getString(position));
						return objOpenERP.getString("name");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "Error";
					}
			 }else{
				 mCursor.moveToPosition(position);
				 return  mCursor.getString(mCursor.getColumnIndex("name"));
			 }
		 }

		 @Override
		 public long getItemId(int position) {
			 if (isRemote){
				 try {
						objOpenERP = new JSONObject(arrOpenERP.getString(position));
						return objOpenERP.getLong("id");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return 0;
					}
			 }else{
				 mCursor.moveToPosition(position);
				 return  mCursor.getLong(mCursor.getColumnIndex("id"));
			 }
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
			//holder.chkItem = (CheckBox)convertView.findViewById(to[0]);
			//holder.text1 = (TextView) convertView.findViewById(R.id.text1);
			//holder.txtCode = (TextView) convertView.findViewById(R.id.txtcode);
			
			 if (isRemote){
				 try {
						objOpenERP = new JSONObject(arrOpenERP.getString(position));
						holder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener(){
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								itemSelection[position] = holder.chkItem.isChecked();
								try {
									objOpenERP = new JSONObject(arrOpenERP.getString(position));
									if (holder.chkItem.isChecked()) {
										listIdSelected.add(objOpenERP.getInt("id"));
									} else {
										listIdSelected.remove(objOpenERP.getInt("id"));
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
						for (int i = 1; i < from.length; i++) {
							textArray[i].setText(objOpenERP.getString(from[i]));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			 }else{
				mCursor.moveToPosition(position);
				
				holder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						itemSelection[position] = holder.chkItem.isChecked();
						mCursor.moveToPosition(position);
						if (holder.chkItem.isChecked()) {
							listIdSelected.add(mCursor.getInt(mCursor.getColumnIndex("id")));
						} else {
							listIdSelected.remove(mCursor.getInt(mCursor.getColumnIndex("id")));
						}
					}
				});
				
				for (int i = 1; i < from.length; i++) {
					textArray[i].setText(mCursor.getString(mCursor.getColumnIndex(from[i])));
				}
			 }
			//holder.text1.setText(mCursor.getString(mCursor.getColumnIndex("name")));
			//holder.txtCode.setText(mCursor.getString(mCursor.getColumnIndex("id")));
			holder.chkItem.setChecked(itemSelection[position]);
			convertView.setTag(holder);
			//holder.chkItem.setText(getItem(position));
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
	        	//Toast.makeText(this, listIdSelected.toString(), Toast.LENGTH_SHORT).show();
	        	saveSelection();
	        	if (isRemote){
	        		Intent intent = new Intent(this, ImportSelectionList.class);
	        		startActivity(intent);
	        	}
	    		this.finish();	        	
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}
//	static JSONObject objOpenERP(int position){
//		 try {
//				objOpenERP = new JSONObject(arrOpenERP.getString(position));
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		 return objOpenERP;
//	}
}