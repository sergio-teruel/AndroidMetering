package com.ics.metering;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class ICSUtil extends Activity{
	//static SharedPreferences pref;
	public static String PREFKEYCITY = "city_id";
	public static String PREFKEYBOOK = "book_id";
	public static String PREFKEYSTREET = "street_id";
	public static String PREFKEYSUPPLY = "supply_point_id";
	public static String PREFKEYALLSUPPLY = "all_supply_point";
	public static String PREFKEYCITYNAME = "city";
	public static String PREFKEYBOOKNAME = "book";
	public static String PREFKEYSTREETNAME = "street";
	public static String PREFKEYSUPPLYNAME = "supply_point";
	//public static String PREFKEYALLSUPPLY = "all_supply_point";
	public static String PICTURES_DIR = "ics_metering";
	
    public static void writePreference (String key, String value, Context ctx){
		SharedPreferences pref = ctx.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor;
    	editor = pref.edit();
    	editor.putString(key, value);
    	editor.commit();
	}
    public static void writePreferenceBoolean (String key, Boolean value, Context ctx){
    	if (value == true) {
    		writePreference(key, "1", ctx);
    	} else{
    		writePreference(key, "0", ctx);
    	}
    }
    
    
	public static String readPreference (String key, Context ctx){
		SharedPreferences pref = ctx.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
		return pref.getString(key, ""); 
	}
	public static Boolean readPreferenceBoolean (String key, Context ctx){
		//Boolean x = (readPreference(key, ctx).equals("1"));
		return (readPreference(key, ctx).equals("1")); 
	}

	public static String sinCorchetes(String text){
		return text.replace("[", "").replace("]", "");
	}
	
	public static String wherePreferences (dbTableEnum table, Boolean isRemote, Context ctx){
		String pref = "";
		String importPref = "";
		String whereResult = "";
		String key ="";
		if (! isRemote) whereResult = "1=1";

		if (table.ordinal()>0){
			key = PREFKEYCITY;
			if (isRemote)  key = "import_" + PREFKEYCITY;
			pref = readPreference(key, ctx);
			if (pref != "") {
				if (isRemote){
					whereResult += "['city_id', 'in', [" + pref +"]],";
				}else {
					whereResult += " AND city_id in (" + pref +")";
				}
			}
		}
		if (table.ordinal()>1){
			key = PREFKEYBOOK;
			if (isRemote)  key = "import_" + PREFKEYBOOK;
			pref = readPreference(key, ctx);
			if (pref != "") {
				if (isRemote){
					whereResult += "['book_id', 'in', [" + pref +"]],";
				}else {
					whereResult += " AND book_id in (" + pref + ")";
				}
			}
		}
		if (table.ordinal()>2){
			key = PREFKEYSTREET;
			if (isRemote)  key = "import_" + PREFKEYSTREET;
			pref = readPreference(key, ctx);
			if (pref != "") {
				if (isRemote){
					whereResult += "['street_id', 'in', [" + pref +"]],";
				}else {
					whereResult += " AND street_id in (" + pref +")";
				}
			}
		}
		if (table.ordinal()>3){
			key = PREFKEYSUPPLY;
			if (isRemote)  key = "import_" + PREFKEYSUPPLY;
			pref = readPreference(key, ctx);
			if (pref != "") {
				if (isRemote){
					whereResult += "['id', 'in', [" + pref +"]],";
				}else {
					whereResult += " AND id in (" + pref +")";
				}
			}
			if (! readPreferenceBoolean(PREFKEYALLSUPPLY, ctx)){
				if (isRemote){
					//whereResult += "";
				}else {
					whereResult += " AND (readed = 0)";
				}
			}
		}
		// Añadimos una condicion para excluir los ya seleccionados cuando obtengo de openerp
		if (isRemote){
			String keys = AdaptadorDB.listKeysTmpImport(table);
			if (keys.length()>0){
				whereResult += "['id', 'not in', [" + keys +"]],";
			}
//			switch (table) {
//			case CITY:
//				pref =  readPreference("import_" + PREFKEYCITY, ctx);
//				if (pref != "") {
//					if (pref != "") whereResult += "['id', 'not in', [" + pref +"]],";
//				}
//				break;
//			case BOOK:
//				pref =  readPreference("import_" + PREFKEYBOOK, ctx);
//				if (pref != "") {
//					if (pref != "") whereResult += "['id', 'not in', [" + pref +"]],";
//				}
//				break;
//			case STREET:
//				pref =  readPreference("import_" + PREFKEYSTREET, ctx);
//				if (pref != "") {
//					if (pref != "") whereResult += "['id', 'not in', [" + pref +"]],";
//				}
//				break;
//			case READING:
//				pref =  readPreference("import_" + PREFKEYSUPPLY, ctx);
//				if (pref != "") {
//					if (pref != "") whereResult += "['id', 'not in', [" + pref +"]],";
//				}
//				break;
//	
//			default:
//				break;
//			}
		}
		return whereResult;
	}
	public static String removeDuplicate(String cad){
		Set<Integer> list = new HashSet<Integer>();
		String[] arrayList = cad.split(",");
		for (String id : arrayList) {
			list.add(Integer.parseInt(id.trim()));
		}
		return sinCorchetes(list.toString());
		
	}
	public static void addImportPreference(dbTableEnum table, Context ctx){
		String prefKey = "";
		switch (table) {
		case CITY :
			prefKey = PREFKEYCITY;
			break;
		case BOOK :
			prefKey = PREFKEYBOOK;
			break;
		case STREET :
			prefKey = PREFKEYSTREET;
			break;
		case READING :
			prefKey = PREFKEYSUPPLY;
			break;
		default:
			break;
		}
		String list_ids = readPreference("import_" + prefKey, ctx);
		if (list_ids.length()>0) list_ids += ",";
		list_ids += readPreference(prefKey, ctx);
		writePreference("import_" + prefKey, removeDuplicate(list_ids), ctx);
	}
	public static void addImportList(dbTableEnum table, Context ctx){
		
	}
	
	public static void clearImportPreferences(Context ctx){
		ICSUtil.writePreference("import_" + PREFKEYCITY, "", ctx);
		ICSUtil.writePreference("import_" + PREFKEYBOOK, "", ctx);
		ICSUtil.writePreference("import_" + PREFKEYSTREET, "", ctx);
		ICSUtil.writePreference("import_" + PREFKEYSUPPLY, "", ctx);

		ICSUtil.writePreference("import_" + PREFKEYCITYNAME, "", ctx);
		ICSUtil.writePreference("import_" + PREFKEYBOOKNAME, "", ctx);
		ICSUtil.writePreference("import_" + PREFKEYSTREETNAME, "", ctx);
		ICSUtil.writePreference("import_" + PREFKEYSUPPLYNAME, "", ctx);
	}

	public static void clearPreferences(int ordinal, boolean childOnly, boolean isRemote, Context ctx){
		String key = "";
		if (childOnly) ordinal++;
		if (ordinal<1) {
			key = "city";
			if (isRemote) key = "import_" + key;
			ICSUtil.writePreference(key +"_id", "", ctx);
			ICSUtil.writePreference(key, "", ctx);
		} 
		if (ordinal<2) {
			key = "book";
			if (isRemote) key = "import_" + key;
			ICSUtil.writePreference(key +"_id", "", ctx);
			ICSUtil.writePreference(key, "", ctx);
		} 
		if (ordinal<3) {
			key = "street";
			if (isRemote) key = "import_" + key;
			ICSUtil.writePreference(key +"_id", "", ctx);
			ICSUtil.writePreference(key, "", ctx);
		} 
		if (ordinal<4) {
			key = "supply_point";
			if (isRemote) key = "import_" + key;
			ICSUtil.writePreference(key +"_id", "", ctx);
			ICSUtil.writePreference(key, "", ctx);
		}
	}
	
	public static Bitmap scaleImage(int width, int height, String imagePath){
	    // Get the dimensions of the View
	    //int targetW = imageView.getWidth();
	    //int targetH = imageView.getHeight();
	    int targetW = width;
	    int targetH = height;

	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
//	    BitmapFactory.decodeFile(tFileList.get(position).toString(), bmOptions);
	    BitmapFactory.decodeFile(imagePath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;
		
	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
	  
	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;
	  
	    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
	    
		return bitmap;
	}
	public static Bitmap scaleImage(int width, int height, Bitmap bm){
	    // Get the dimensions of the View
	    //int targetW = imageView.getWidth();
	    //int targetH = imageView.getHeight();
	    int targetW = width;
	    int targetH = height;

	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
//	    BitmapFactory.decodeFile(tFileList.get(position).toString(), bmOptions);
	    //BitmapFactory.decodeStream(bm.);
	    
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;
		
	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
	  
	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;
	  
	    Bitmap bitmap = BitmapFactory.decodeFile("ss", bmOptions);
	    
		return bitmap;
	}
//	public static float roundTwoDecimals(float d) {
//        DecimalFormat df = new DecimalFormat("#.00");
//        return Float.valueOf(df.format(d));
//	}
	public static float roundTwoDecimals(float d) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Float.valueOf(df.format(d));
	}
}
