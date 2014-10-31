package com.ics.metering;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;



public class AdaptadorDB extends SQLiteOpenHelper{
	/*CREATE TABLE "Lecturas" ("acometida_id" INTEGER PRIMARY KEY  NOT NULL  UNIQUE , "acometida" TEXT, "contador_id" INTEGER, "contador" TEXT, "fecha" DATETIME, "fecha_ul" DATETIME, "lectura_ultima" INTEGER, "lectura_actual" INTEGER, "incidencia_id" INTEGER, "incidencia_result" TEXT, "incidencia2_id" INTEGER, "incidencia2_result" TEXT, "interior" BOOL, "municipio_id" INTEGER, "libro_id" INTEGER, "calle_id" INTEGER, "leido" BOOL, "fecha_carga" DATETIME)
	 * 
	 * */
	public static enum dbTableEnum {
	    CITY,
	    BOOK,
	    STREET,
	    READING,
	    Reading,    // Este reading es para cuando queremos que se muestren solo las acometidas guardadas en preferencias
	    tmp_Import_Sel,
	    tmp_list_item,
	  }
	
	private static final String DATABASE_NAME = "ICS_Metering";
	private static final int DATABASE_VER = 57;
	private static final String TABLE_READING = "Reading";
	private static final String TABLE_CITY = "City";
	private static final String TABLE_STREET = "Street";
	public static final String TABLE_TMP_LIST_ITEM = "tmp_list_item";
	public static final String TABLE_TMP_IMPORT_SELECTED = "tmp_Import_Sel";
	public static final String TABLE_CONSUMPTION = "Consumption";
	private static SQLiteDatabase db;
	private String[] CREAR_BASEDATOS = new String[15];
	private static Context ctx; 
	
	int regcount =1000;
	int x=0;
	int y=1;
	int z=1;
	
	public AdaptadorDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VER);
		ctx = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
//		CREAR_BASEDATOS[0] = "CREATE TABLE 'Reading' ('acometida_id' INTEGER PRIMARY KEY  NOT NULL  UNIQUE , 'acometida' TEXT, 'contador_id' INTEGER, 'contador' TEXT, 'fecha' DATETIME, 'fecha_ul' DATETIME, 'lectura_ultima' INTEGER, 'lectura_actual' INTEGER, 'incidencia_id' INTEGER, 'incidencia_result' TEXT, 'incidencia2_id' INTEGER, 'incidencia2_result' TEXT, 'interior' BOOL, 'city_id' INTEGER, 'libro_id' INTEGER, 'street_id' INTEGER, 'leido' BOOL, 'fecha_carga' DATETIME);";
//		CREAR_BASEDATOS[0] = "CREATE TABLE 'Reading' ('id' INTEGER PRIMARY KEY  NOT NULL  UNIQUE , 'name' TEXT, 'contador_id' INTEGER, 'contador' TEXT, 'fecha' DATETIME, 'fecha_ul' DATETIME, 'lectura_ultima' INTEGER, 'lectura_actual' INTEGER, 'incidencia_id' INTEGER, 'incidencia_result' TEXT, 'incidencia2_id' INTEGER, 'incidencia2_result' TEXT, 'interior' BOOL, 'city_id' INTEGER, 'libro_id' INTEGER, 'street_id' INTEGER, 'leido' BOOL, 'fecha_carga' DATETIME, 'state' INTEGER);";
		CREAR_BASEDATOS[0] = "CREATE TABLE 'Reading' ('id' INTEGER PRIMARY KEY  NOT NULL  UNIQUE , " + //supply_point_id
				"'name' TEXT, " + //supply_point
				"'date' DATETIME, " +
				"'meter_id' INTEGER, " +
				"'meter' TEXT, " +
				"'value' INTEGER, " +
				"'contract_id' INTEGER, " +
				"'contract' TEXT, " +
				"'partner_id' INTEGER, " +
				"'partner' TEXT, " +
				//"'last_date' DATETIME, " +
				//"'last_value' INTEGER, " +
				//"'average_consumption' INTEGER, " + //daily average
				"'interior' BOOL DEFAULT 0, " +
				"'city_id' INTEGER, " +
				"'book_id' INTEGER, " +
				"'street_id' INTEGER, " +
				"'date_lock' DATETIME, " +
				"'user_id' INTEGER, " +
				"'supply_latitude' REAL, " +
				"'supply_longitude' REAL, " +
				"'supply_date_localization' DATE, " +
				"'comment' TEXT, " +
				"'readed' BOOL DEFAULT 0, " +
				"'extra_changed' BOOL DEFAULT 0, " +
				"'issue_id' INTEGER, " +
				"'issue_detail_id' INTEGER, " +
				"'issue_comment' TEXT, " +
				"'issue_reported_suscriber' BOOL DEFAULT 0, " +
				"'state' INTEGER  DEFAULT 0); "; 
//		CREAR_BASEDATOS[0] = "CREATE TABLE 'Reading' ('supply_point_id' INTEGER PRIMARY KEY  NOT NULL  UNIQUE , 'supply_point' TEXT, 'meter_id' INTEGER, 'meter' TEXT, 'date' DATETIME, 'date_last' DATETIME, 'lectura_ultima' INTEGER, 'lectura_actual' INTEGER, 'incidencia_id' INTEGER, 'incidencia_result' TEXT, 'incidencia2_id' INTEGER, 'incidencia2_result' TEXT, 'interior' BOOL, 'city_id' INTEGER, 'libro_id' INTEGER, 'street_id' INTEGER, 'leido' BOOL, 'fecha_carga' DATETIME);";
		CREAR_BASEDATOS[1] = "CREATE TABLE 'City' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' CHAR, 'code' INTEGER);";
		CREAR_BASEDATOS[2] = "CREATE TABLE 'Street' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' CHAR, 'city_id' integer);";
		CREAR_BASEDATOS[3] = "CREATE INDEX 'StreetKeycity_id' ON 'Street' ('city_id' ASC);";
		CREAR_BASEDATOS[4] = "CREATE TABLE 'Book' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' CHAR, 'city_id' INTEGER);";
		CREAR_BASEDATOS[5] = "CREATE INDEX 'BookKeycity_id' ON 'Book' ('city_id' ASC);";
		CREAR_BASEDATOS[6] = "CREATE TABLE 'Issue' ('id' INTEGER PRIMARY KEY  NOT NULL, 'name' CHAR, sequence INTEGER DEFAULT 0,condition TEXT, auto_eval BOOL DEFAULT 0);";
		CREAR_BASEDATOS[7] = "CREATE TABLE 'Issue_Detail' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' CHAR,sequence INTEGER DEFAULT 0, issue_id INTEGER, parent_id INTEGER DEFAULT 0);";
//		CREAR_BASEDATOS[8] = "CREATE TABLE 'Issue_Template_Line' ('id' INTEGER PRIMARY KEY  NOT NULL , template_id INTEGER, 'name' CHAR, issue_id INTEGER);";
//		CREAR_BASEDATOS[9] = "CREATE INDEX 'Issue_Template_LineKeytemplate_id' ON 'Issue_Template_Line' ('template_id' ASC);";
//		CREAR_BASEDATOS[10] = "CREATE TABLE 'Issue_Reading' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' CHAR, reading_id INTEGER, issue_id INTEGER, issue_line_id INTEGER);";
//		CREAR_BASEDATOS[11] = "CREATE INDEX 'Issue_ReadingKeyreading_id' ON 'Issue_Reading' ('reading_id' ASC);";
		CREAR_BASEDATOS[8] = "CREATE TABLE '"+ TABLE_TMP_IMPORT_SELECTED + "' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'table_id' INTEGER, 'key_id' INTEGER, 'name' CHAR, active BOOLEAN DEFAULT 0);";
		CREAR_BASEDATOS[9] = "CREATE INDEX 'tmp_Import_TableKey' ON '"+ TABLE_TMP_IMPORT_SELECTED + "' ('table_id', 'key_id' ASC);";
		CREAR_BASEDATOS[10] = "CREATE TABLE 'User' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' CHAR, 'password' CHAR);";
		CREAR_BASEDATOS[11] = "CREATE TABLE '"+ TABLE_TMP_LIST_ITEM + "' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' CHAR);";
		CREAR_BASEDATOS[12] = "CREATE TABLE 'Consumption' ('id' INTEGER PRIMARY KEY  NOT NULL , " +
				"'last_date' DATE, " +
				"'date' DATE, " +
				"'last_value' INTEGER, " +
				"'value' INTEGER, " +
				"'consumption' INTEGER, " +
				"'days' INTEGER, " +
				"'average' FLOAT, " +
				"'supply_point_id' INTEGER, " +
				"'meter_id' INTEGER, " +
				"'user_id' INTEGER, " +
				"'is_reported_invoice' BOOLEAN);";
        //		'state'
        //		'invoice_line_id'

		CREAR_BASEDATOS[13] = "CREATE VIEW View_check_issue AS " +
				"SELECT Reading.id, Reading.value, Reading.date, MAX(Consumption.value) AS last_value, (Consumption.date) AS last_date, " +
					"(Reading.value - MAX(Consumption.value)) / ((strftime('%J',Reading.date) - strftime('%J', MAX(Consumption.date))+0.01)) as average, " +
					"(MAX(Consumption.value) -  MIN(Consumption.value)) /  (( strftime('%J',MAX(Consumption.date)) -  strftime('%J',MIN(Consumption.date)))+0.01) AS last_average " +
				"FROM Reading LEFT JOIN Consumption ON Consumption.supply_point_id = Reading.id " +
				"GROUP BY Reading.id, Reading.value, Reading.date;";
		

		CREAR_BASEDATOS[14] = "CREATE TABLE ir_attachment (" +
				//"id INTEGER NOT NULL, " +
				//"create_uid INTEGER, " +
				//"create_date DATETIME, " +
				"write_date DATETIME, " +
				//"write_uid INTEGER, " +
				"description TEXT, " +
				"datas_fname TEXT, " +
				//"url TEXT, " +
				"res_model TEXT, " +
				//"company_id INTEGER, " +
				"res_name TEXT, " +
				"type TEXT NOT NULL, " +
				"res_id INTEGER, " +
				"file_size INTEGER, " +
				//"db_datas BLOB, " +
				"store_fname TEXT, " +
				"name TEXT NOT NULL, " +
				"file_type TEXT, " +
				"partner_id INTEGER);";
				//"user_id INTEGER, " +
				//"parent_id INTEGER);"; 
				//"index_content TEXT);"; 
		
		try {
			for (int i = 0; i < CREAR_BASEDATOS.length; i++) {
				db.execSQL(CREAR_BASEDATOS[i]);
			}
			//CargaDatosDemo();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.w("Pepe","Actualizando base de datos" + newVersion);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_READING);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_CITY);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_STREET);
		db.execSQL("DROP TABLE IF EXISTS Book");
		db.execSQL("DROP TABLE IF EXISTS Issue");
		db.execSQL("DROP TABLE IF EXISTS Issue_Detail");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TMP_LIST_ITEM );
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TMP_IMPORT_SELECTED );
		db.execSQL("DROP TABLE IF EXISTS User");
		db.execSQL("DROP TABLE IF EXISTS Consumption");
		db.execSQL("DROP VIEW IF EXISTS View_check_issue");
		db.execSQL("DROP TABLE IF EXISTS ir_attachment");
//		db.execSQL("DROP ALL TABLE");
		onCreate(db);
	}	
	
	public static void emptyTables(){
		db.execSQL("DELETE FROM " + TABLE_READING);
		db.execSQL("DELETE FROM " + TABLE_CITY);
		db.execSQL("DELETE FROM Street");
		db.execSQL("DELETE FROM Book");
		db.execSQL("DELETE FROM Issue");
		db.execSQL("DELETE FROM Issue_Detail");
		db.execSQL("DELETE FROM Consumption");
	}
	
	public void CargaDatosDemo (Context context){
		//SQLiteDatabase db = this.getWritableDatabase(); ---> esto esta mal  
		db = this.getWritableDatabase();
		emptyTables();
//		db.execSQL("INSERT INTO City (id,name,code) values (1,'Teruel',44000)");
//		db.execSQL("INSERT INTO City (id,name,code) values (2,'Calamocha',44002)");
//		db.execSQL("INSERT INTO City (id,name,code) values (3,'Bronchales',44003)");
//		db.execSQL("INSERT INTO City (id,name,code) values (4,'Villel',44045)");

		db.execSQL("INSERT INTO Issue (id, name, sequence, type, condition,template_id) values (1,'Absent', 10, 0, '', 0)");
		db.execSQL("INSERT INTO Issue (id, name, sequence, type, condition,template_id) values (2,'Same value', 20, 1, 'value=last_value', 1)");
		db.execSQL("INSERT INTO Issue (id, name, sequence, type, condition,template_id) values (3,'Breakdown', 30, 0, '', 2)");

		db.execSQL("INSERT INTO Issue_Template (id, name) values (1,'Plant Mismo valor')");
		db.execSQL("INSERT INTO Issue_Template (id, name) values (2,'Plant Roto')");

		db.execSQL("INSERT INTO Issue_Template_Line (id, template_id, name, issue_id) values (1, 1,'No habita', 0)");
		db.execSQL("INSERT INTO Issue_Template_Line (id, template_id, name, issue_id) values (2, 1,'Roto', 3)");

		db.execSQL("INSERT INTO Issue_Template_Line (id, template_id, name, issue_id) values (3, 2,'Manupulado', 0)");
		db.execSQL("INSERT INTO Issue_Template_Line (id, template_id, name, issue_id) values (4, 2,'Congelado', 0)");
		db.execSQL("INSERT INTO Issue_Template_Line (id, template_id, name, issue_id) values (5, 2,'Delincuencia', 0)");

		
		
		final ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(regcount);
		progressDialog.setCancelable(false);
		progressDialog.show();
		
        Thread t = new Thread(new Runnable(){
            public void run() {
                while(progressDialog.getProgress() < progressDialog.getMax())
                {
                	//progressDialog.incrementProgressBy(i-n);
                	progressDialog.setProgress(inserta());
                    try{Thread.sleep(50);}catch(Exception e){/* no-op */}
                }
                	progressDialog.dismiss();
            	}
	        });
        t.start();
	}

	public int inserta(){
		int r;
		Random rand=new Random();
		ContentValues cv = new ContentValues();
		ContentValues cvStreet = new ContentValues();
		ContentValues cvCity = new ContentValues();
		int city_id = 0;
		int street_id = 0;
		String[] names = new String[] {"Andres","Maria","Antonio", "Jose", "Raul"};
		String[] lastNames = new String[] {"Laparra","Agramunt","Perez", "Lopez", "Romero","Garcia"};
		String[] municipios= new String[] {"","Teruel","Calamocha","Bronchales", "Albarracín", "Estercuel","Villastar"};
		String[] calles = new String[] {"","Calle Mayor","Plaza Constitucion","Avd. Federico García Lorca", "Calle Santo Tomás", "Calle Vicente del Bosque","Plaza Tetuan"};
		String calle = "";

		while (y < municipios.length){
			cvCity.put("id", y); // anterior acometida_id
			cvCity.put("name", municipios[y]);
			cvCity.put("code", "4400" + y );
			db.insert("City", null, cvCity);
			z = 1;
			while (z < calles.length){
				cvStreet.put("id", y*10 + z); // anterior acometida_id
				cvStreet.put("name", calles[z]);
				cvStreet.put("city_id", y);
				db.insert("Street", null, cvStreet);
				z++;
			}
			y++;
		}
			
		while (x<regcount) {
/*			cv.put("interior", ListaContadores.boolRandom());
			cv.put("ean13", 998989);
			cv.put("street2", "Esto es la calle " + x);
			cv.put("partner_name", "Esto es el cliente numero: " + x);
			cv.put("last_value", 10000 + x);
			cv.put("value", 0);
			cv.put("state", "draft");
*/			
			x++;
			
			if (x % 100 == 0 ) {
				return x;
			}
			cv.put("id", x);
			r = rand.nextInt(2);
			cv.put("interior", r);
			//cv.put("acometida", "Esto es la acometida: " + x );
			city_id = rand.nextInt(municipios.length);
			if (city_id == 0) city_id =1; 
			cv.put("city_id", city_id);
			street_id = rand.nextInt(calles.length);
			if (street_id == 0) street_id =1; 
			cv.put("street_id", city_id * 10 + street_id);
			//calle = calles[rand.nextInt(calles.length-1)];
			cv.put("name", calles[street_id] + ", nº "+ rand.nextInt(100) );
			cv.put("meter_id", rand.nextInt(regcount));
			cv.put("meter", "CTRO0000" + x );
			cv.put("contract_id", rand.nextInt(regcount));
			cv.put("contract", "CR/2012/" + x);
			cv.put("partner_id", rand.nextInt(regcount));
			cv.put("partner", names[rand.nextInt(4)] +" "+lastNames[rand.nextInt(5)]+" "+lastNames[rand.nextInt(5)]);
			Date d = new Date();
			cv.put("date", (String) (DateFormat.format("yyyy-MM-dd hh:mm:ss", d)));
			// Select fecha, Datetime(fecha_ul/1000, 'unixepoch', 'localtime') from Reading
			//Dividimos entr 1000 porque las funciones de SQlite trabajan con segundos y GetTime devuelve milisegundos
			cv.put("last_date", d.getTime()/1000);
			r = rand.nextInt(20000);
			cv.put("last_value", r);
			cv.put("value", 0);
			cv.put("readed", 0);
			
			cv.put("state", rand.nextInt(2));
			
			db.insert(TABLE_READING, null, cv);

		}
		return regcount;
	}
	

	public static int boolRandom() {
		
		Random rand=new Random();
		int x=rand.nextInt(2);

		//x now has a random number, either 0 or 1.
		//Alternatively:
		//return ((Math.random()<0.5)?0:1) == 1;
		return x;
		//return ((x<0.5)?0:1) == 1;
	}
	
	public void openDB() {
		if (db == null){
			db = this.getWritableDatabase();
		}
	}

	public void closeDB() {
		if (db != null){
			db.close();
		}
	}

	public int grabarRegistro (String table, ContentValues values, String whereClause, String[] whereArgs) {
		openDB();
		return db.update(table, values, whereClause, whereArgs);
	}
	

	public static Long insertarRegistro (String table, ContentValues values) {
		//openDB();
		return db.insert(table, null, values);
	}
	public static int deleteRegistro (String table, String whereClause,String[] whereArgs) {
		//openDB();
		return (int) db.delete(table, whereClause, whereArgs);
		//return (int) db.insert(table, null, values);
	}

	public Boolean grabarLectura(ContentValues values, Integer id){
		grabarRegistro("Reading", values, "id=" + id , null);
		return true;
	}

//	public Boolean grabarPosicionGPS(ContentValues values, Integer id){
//		grabarRegistro("Reading", values, "id=" + id , null);
//		return true;
//	}
	
//	public Cursor leerMunicipios (){
//		AbrirDB();
//		String sql = "Select id, id AS _id, name, code from City";
//		return db.rawQuery(sql, null);
//	}
//
//	public Cursor leerCalles(){
//		AbrirDB();
//		return db.query("Street", new String[]{"*", "id AS _id, name as item"}, null, null, null, null, null);
//	}
//
//	public Cursor leerCalles(String city_id){
//		AbrirDB();
//		return db.query("Street", new String[]{"*", "id AS _id"}, "city_id=?", new String[]{city_id}, null, null, null);
//	}
//
//	public Cursor leerCalles(String city_id, String name){
//		AbrirDB();
//		return db.query("Street", new String[]{"*", "id AS _id"}, "(city_id=?) AND (name like %?%)", new String[]{city_id, name}, null, null, null);
//	}
//
//	public Cursor leerAcometidas(String id, String column){
//		AbrirDB();
//		return db.query("Reading", new String[]{"*", "acometida_id AS _id"}, column + "=?", new String[]{id}, null, null, null);
//	}
	
	public static Cursor getCursorBuscador(String textSearch, dbTableEnum tableSearch, boolean isRemote, String order){
		textSearch = textSearch.replace("'", "''");
	    String[] fields = new String[]{"*" ,"id  AS _id", "name AS item"};
	    String where = "";
	    String[] selectionArgs = new String[]{};
	    String orderBy ="id";
	    String table = tableSearch.toString();
	    
	    if (order.length() > 0)	orderBy = order;
    	
	    if (isRemote){
	    	if (!(table == TABLE_TMP_IMPORT_SELECTED)){
		    	table = TABLE_TMP_LIST_ITEM;
	    	}
    	}else{
    		where = ICSUtil.wherePreferences(tableSearch, false, ctx);
    		
    	}
    	
	    if (textSearch.length()>0){
		    selectionArgs = new String[]{"%" + textSearch +"%"};			
	    	//where = ICSUtil.wherePreferences(tableSearch, false, ctx);
			if (where.length()>0) {
				where +=" AND "; 
			}
	    	where += "name LIKE ?";
	    }//else{
		    //selectionArgs = new String[]{};			
	    	//where = ICSUtil.wherePreferences(tableSearch, false, ctx);
	    //}
   		return db.query(table, fields, where, selectionArgs, "", "", orderBy);
	}	


	public static Cursor getCursorById(int id, dbTableEnum tableSearch){
		String[] fields = new String[]{"*"};
		return db.query(tableSearch.toString(), fields, "id = " + id, new String[]{}, "", "", "");
	}	

	public static Cursor getLocalUser(String userName){
		String[] fields = new String[]{"*"};
		return db.query("User", fields, "name = '" + userName + "'", new String[]{}, "", "", "");
	}	
	public void addUserOpenERP(ContentValues values){
		insertarRegistro("User", values);
	}
	// Añadimos a la tabla temporal antes de filtar datos de Openerp
	public void addTmpListItem(ContentValues values){
		Long r = insertarRegistro("tmp_list_item", values);
	}
	public void emptyTable(String table){
		openDB();
		db.execSQL("DELETE FROM " +table.toString());
	}
	
	public void emptyTableTmpListItem(){
		emptyTable(TABLE_TMP_LIST_ITEM);
	}
	public void addTmpImportSelected(ContentValues values){
		Long r = insertarRegistro(TABLE_TMP_IMPORT_SELECTED, values);
	}
	public static void removeTmpImportSelected(Integer id){
		String whereClause = "id = ?";
		String[] whereArgs ={Integer.toString(id)};
		//Ojo si la base de datos no esta abierta----
		int r = db.delete(TABLE_TMP_IMPORT_SELECTED, whereClause, whereArgs);
	}
	public static int getCountTable(dbTableEnum table){
		Cursor tmpCursor;
		tmpCursor = getCursorBuscador("", table, true, "");
		return tmpCursor.getCount();
	}
	
	public static void deleteTmpImportNoActive(){
		String sql = "DELETE FROM tmp_Import_Sel WHERE active=0";
		db.execSQL(sql);
	}

	public static void activeTmpImportSelected(){
		String sql = "DELETE FROM tmp_Import_Sel " +
				"WHERE active=0 AND table_id < (SELECT max(table_id) FROM tmp_Import_Sel WHERE active=0)";
		db.execSQL(sql);
		sql = "UPDATE tmp_Import_Sel SET active=1 WHERE active=0";				
		db.execSQL(sql);
	}

	public static String listKeysTmpImport(dbTableEnum table){
		String listKeys = "";
		Cursor cur = db.query(TABLE_TMP_IMPORT_SELECTED, new String[]{"key_id"}, "active=1 AND table_id=" + table.ordinal(), new String[]{},"","","");
		cur.moveToFirst();
		for (int i = 0; i < cur.getCount(); i++) {
			listKeys += cur.getString(0);
			if (i < cur.getCount()-1) listKeys += ",";
			cur.moveToNext();
		}
		return listKeys;
	}

	
	public static JSONArray arrayImportSelection(){
		JSONArray array = new JSONArray();
		JSONArray arrayCont = new JSONArray();
		String list = "";
		String strObject="[{";
		
		strObject += "'city_id':[" + listKeysTmpImport(dbTableEnum.CITY) + "],";
		strObject += "'book_id':[" + listKeysTmpImport(dbTableEnum.BOOK) + "],";
		strObject += "'street_id':[" + listKeysTmpImport(dbTableEnum.STREET) + "],";
		strObject += "'id':[" + listKeysTmpImport(dbTableEnum.READING) + "]";
		strObject += "}]";

		//strObject = "'city_id':[" + list + "]," + "'book_id':[" + list + "]," + "'street_id':[" + list + "],";
		try {
			arrayCont = new JSONArray(strObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//arrayCont.put(strObject);
		
		return arrayCont;
	}
	
	
	public static JSONArray arrayImportSelectionxx(){
		JSONArray array = new JSONArray();
		JSONObject objAux = new JSONObject();
		Cursor cur = db.query(TABLE_TMP_IMPORT_SELECTED, new String[]{"id','table_id','key_id','name'"}, "active=1", new String[]{},"","","table_id, key_id");
		final int COLTABLE=1;
		cur.moveToFirst();
		int tableId = cur.getInt(COLTABLE);
		for (int i = 0; i < cur.getCount(); i++) {
			if (tableId!=cur.getInt(COLTABLE)){
				
			}
			cur.moveToNext();
		}
		switch (cur.getInt(COLTABLE)) {
		case 0:		//City
			//array.put(new JSONObject("{'city_id':" + listKeysTmpImport(dbTableEnum.CITY) + ""));
			break;

		case 1:		//Book
			
			break;

		case 2:		//Street
			
			break;

		case 3:		//Supply
			
			break;

		default:
			break;
		}
		return null;
	}
	public static Cursor pragmaTable(String tableName){
		return db.rawQuery("PRAGMA table_info(" +tableName+ ")", new String[]{});
	}
	
	public Cursor getSupplyConsumption(int supply_id){
		return db.query(TABLE_CONSUMPTION, new String[]{"id AS _id","*"}, "supply_point_id = " + supply_id, new String[]{},"","","date");
	}
	public static Cursor getReading(){
		return db.query(TABLE_READING, new String[]{"*"}, "readed = 1", new String[]{},"","","");
	}
	public static Cursor getTable(String table, String selection){
		return db.query(table, new String[]{"*"},  selection, new String[]{},"","","");
	}
	
	public static Cursor getRawQuery(String sql){
		return db.rawQuery(sql, new String[]{});
	}

	public static JSONArray arraySendReading(){
    	JSONObject jsonObj = new JSONObject();
    	JSONObject jsonExtra = new JSONObject();
    	JSONArray arrayRecords = new JSONArray(); 
		//reading en estado leido
		Cursor c = getReading();
    	if (c.getCount()>0) {
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
		    	jsonObj = new JSONObject();
				try {
					jsonObj.put("supply_point_id", c.getInt(c.getColumnIndex("id")));
					jsonObj.put("date", c.getString(c.getColumnIndex("date")));
					jsonObj.put("meter_id", c.getInt(c.getColumnIndex("meter_id")));
					jsonObj.put("value", c.getInt(c.getColumnIndex("value")));
					jsonObj.put("user_id", c.getInt(c.getColumnIndex("user_id"))); //de preferences??
					jsonObj.put("contract_id", c.getInt(c.getColumnIndex("contract_id")));
					jsonObj.put("issue_id", c.getInt(c.getColumnIndex("issue_id")));
					jsonObj.put("issue_detail_id", c.getInt(c.getColumnIndex("issue_detail_id")));
					jsonObj.put("issue_comment", c.getString(c.getColumnIndex("issue_comment")));
					//jsonObj.put("extra_changed", c.getInt(c.getColumnIndex("extra_changed")));
					if (c.getInt(c.getColumnIndex("extra_changed"))>0){
						jsonExtra.put("supply_latitude", c.getString(c.getColumnIndex("supply_latitude")));
						jsonExtra.put("supply_longitude", c.getString(c.getColumnIndex("supply_longitude")));
						jsonExtra.put("supply_date_localization", c.getString(c.getColumnIndex("supply_date_localization")));
						jsonObj.put("extra_changed", jsonExtra);
					}
					arrayRecords.put(jsonObj);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				c.moveToNext();
			}
		}
		return arrayRecords;
	}

	public static Cursor getLocalAttachment(Context ctx){
		String lastSync =  ICSUtil.readPreference("lastAttachmentSync", ctx);
		if (lastSync.length()==0) lastSync="2000-01-01 00:00:00";
		return db.query("ir_attachment", new String[]{"*"}, "write_date > '" + lastSync +"'", new String[]{}, "", "", "write_date");
	}

	public static JSONArray arraySendAttachment(Context ctx){
    	JSONObject jsonObj = new JSONObject();
    	JSONObject jsonExtra = new JSONObject();
    	JSONArray arrayRecords = new JSONArray(); 
		//reading en estado leido
		Cursor c = getLocalAttachment(ctx);
		int colCount = c.getColumnCount();
    	if (c.getCount()>0) {
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
		    	jsonObj = new JSONObject();
				try {
					for (int nCol = 0; nCol < colCount; nCol++){
						jsonObj.put(c.getColumnName(nCol), c.getString(nCol));
					}
//	                File f = new File (Environment.getExternalStorageDirectory() +"/"+ ICSUtil.PICTURES_DIR + "/" + c.getString(c.getColumnIndex("res_id"))+ "/" + c.getString(c.getColumnIndex("datas_fname")));
	                String mCurrentPhotoPath = Environment.getExternalStorageDirectory() +"/"+ ICSUtil.PICTURES_DIR + "/" + c.getString(c.getColumnIndex("res_id"))+ "/" + c.getString(c.getColumnIndex("datas_fname"));
	                Bitmap mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
	            
                    ByteArrayOutputStream output = new ByteArrayOutputStream();  
	                mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, output); //bm is the bitmap object   
	                byte[] bytes = output.toByteArray();
	                //byte[] base64Image = Base64.encode(bytes, Base64.);
	                
					jsonObj.put("db_datas", Base64.encodeToString(bytes, Base64.NO_WRAP));
//					jsonObj.put("db_datas", mImageBitmap.toString());
					//jsonObj.put("supply_point_id", c.getInt(c.getColumnIndex("id")));
					arrayRecords.put(jsonObj);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				c.moveToNext();
			}
		}
		return arrayRecords;
	}

	public static String getTotalSupplyByReaded(){
		String sql ="SELECT readed, COUNT(id) as counter FROM Reading GROUP BY readed";
		Cursor cur = db.rawQuery(sql, new String[]{});
		cur.moveToFirst();
		int total = 0;
		int readed = 0;
		for (int i = 0; i < cur.getCount(); i++) {
			if (cur.getInt(cur.getColumnIndex("readed")) == 1){
				readed = cur.getInt(cur.getColumnIndex("counter"));
			}
			total = total + cur.getInt(cur.getColumnIndex("counter"));
			cur.moveToNext();
		}
		return readed + " (" +total+") ";
	}
}
