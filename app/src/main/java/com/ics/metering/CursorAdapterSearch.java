package com.ics.metering;




import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;



public class CursorAdapterSearch extends CursorAdapter {

	   private AdaptadorDB db;
		
	 
	   public CursorAdapterSearch(Context context, Cursor c) {
	     super(context, c);
	     db = new AdaptadorDB(context);
	   }

	   @Override
	   public void bindView(View view, Context arg1, Cursor cursor) {
	     String item = createItem(cursor);
	     ((TextView ) view).setText(item);  
	   }

	   @Override
	   public View newView(Context context, Cursor cursor, ViewGroup parent) {
	     final LayoutInflater inflater = LayoutInflater.from(context);
	     final TextView view = (TextView) inflater.inflate(R.layout.item_search, parent, false);
	        
	     String item = createItem(cursor);
	     view.setText(item);
	        
	     return view;
	   }

	   @Override
	   public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
	     Cursor currentCursor = null;
	         
	     if (getFilterQueryProvider() != null) {
	        return getFilterQueryProvider().runQuery(constraint);
	     }
	         
	     String args = "";
	         
	     if (constraint != null) {
	        args = constraint.toString();       
	     }
	  
	     currentCursor = AdaptadorDB.getCursorBuscador(args,null, false,"");
	  
	     return currentCursor;
	   }
	 
	   private String createItem(Cursor cursor){
	     String item = cursor.getString(1);
	     return item;
	   }
	}
