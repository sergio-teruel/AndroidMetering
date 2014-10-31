package com.ics.metering;

import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import android.widget.Toast;

import com.ics.metering.AdaptadorDB.dbTableEnum;

public class MeteringJSON {
    protected static final String CONNECTOR_NAME = "OpenErpConnect";
	final static String TAG = "xxx";
	public static DefaultHttpClient client = new DefaultHttpClient();
	static HttpParams httpParams = client.getParams();
	static int connectionTimeoutMillis = 5000;
	static int socketTimeoutMillis = 10000;
	public static String DIRECCION = "";

    
	
    public static String loginOpenerp (Context ctx){
    	DIRECCION = "http://" + ICSUtil.readPreference("ServerAddress", ctx) + ":" + ICSUtil.readPreference("ServerPort", ctx);
    	String URL = DIRECCION + "/web/session/authenticate";
    	String session_id = "";

    	ThreadPolicy tp = ThreadPolicy.LAX;
    	StrictMode.setThreadPolicy(tp);
//        {"jsonrpc": "2.0",
//            "method": "call",
//            "params": {"session_id": "SID",
//                       "context": {},
//                       "arg1": "val1" },
//            "id": null}
    	JSONObject jsonObjSend = new JSONObject();

    	try {
    	
    	JSONObject jsonParams = new JSONObject();

    	jsonParams.put("base_location", DIRECCION);
    	jsonParams.put("login", ICSUtil.readPreference("ServerUser", ctx));
    	jsonParams.put("password", ICSUtil.readPreference("ServerPassword", ctx));
    	jsonParams.put("db", ICSUtil.readPreference("ServerDatabase", ctx));
    	jsonObjSend.put("params", jsonParams);
    		
    	// Add a nested JSONObject (e.g. for header information)
    	JSONObject header = new JSONObject();
    	header.put("deviceType","Android"); // Device type
    	header.put("deviceVersion","2.0"); // Device OS version
    	header.put("language", "es-es");	// Language of the Android client
    	jsonObjSend.put("header", header);

    	// Output the JSON object we're sending to Logcat:
    	Log.i(TAG, jsonObjSend.toString(2));

    	} catch (JSONException e) {
    	e.printStackTrace();
    	}

    	// Send the HttpPostRequest and receive a JSONObject in return 
    	HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMillis);
    	//HttpConnectionParams.setSoTimeout(httpParams, socketTimeoutMillis);
    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);

    	if (jsonObjRecv != null) {
	    	JSONObject result;
			try {
				result = (JSONObject) jsonObjRecv.get("result");
				//int uid = result.getInt("uid");
				String uid = result.getString("uid");
				if (uid.equals("false")){
					session_id="";
				} else {
					session_id = result.getString("session_id");
					ICSUtil.writePreference("uid", uid, ctx);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	    	/*
	    	* From here on do whatever you want with your JSONObject, e.g.
	    	* 1) Get the value for a key: jsonObjRecv.get("key");
	    	* 2) Get a nested JSONObject: jsonObjRecv.getJSONObject("key")
	    	* 3) Get a nested JSONArray: jsonObjRecv.getJSONArray("key")
	    	*/
    	}else {
    		session_id = "";
    	}
		return session_id;
    }

    
    public static JSONObject getSessionInfo (String session_id){
    	final String URL = DIRECCION + "/web/session/get_session_info";
    	JSONObject result = new JSONObject();
    	ThreadPolicy tp = ThreadPolicy.LAX;
    	StrictMode.setThreadPolicy(tp);
//        {"jsonrpc": "2.0",
//            "method": "call",
//            "params": {"session_id": "SID",
//                       "context": {},
//                       "arg1": "val1" },
//            "id": null}
    	JSONObject jsonObjSend = new JSONObject();
    	try {
	    	JSONObject jsonParams = new JSONObject();
	    	jsonObjSend.put("jsonrpc", "2.0");
	    	jsonObjSend.put("method", "call");
	    	jsonParams.put("session_id", session_id);
	    	JSONObject jsonContext = new JSONObject();
    		jsonObjSend.put("params", jsonParams);
	    		
	    	// Output the JSON object we're sending to Logcat:
	    	Log.i(TAG, jsonObjSend.toString(2));
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	// Send the HttpPostRequest and receive a JSONObject in return
    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);
    	
    	try {
			result = (JSONObject) jsonObjRecv.get("result");
			String pp = result.getString("pp");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result;
    }

    
    public static JSONArray getJSONBuscador (String session_id, String search, dbTableEnum table, Context ctx){
    	final String URL = DIRECCION + "/web/dataset/search_read";
    	JSONObject result = new JSONObject();
    	JSONArray array = null; 
    	ThreadPolicy tp = ThreadPolicy.LAX;
    	StrictMode.setThreadPolicy(tp);
//        {"jsonrpc": "2.0",
//            "method": "call",
//            "params": {"session_id": "SID",
//                       "context": {},
//                       "arg1": "val1" },
//            "id": null}
    	JSONObject jsonObjSend = new JSONObject();
    	
    	try {
	    	JSONObject jsonParams = new JSONObject();
	    	jsonObjSend.put("jsonrpc", "2.0");
	    	jsonObjSend.put("method", "call");
	    	jsonParams.put("session_id", session_id);
	    	jsonParams.put("model", modelOpenERP(table));
	    	String domain = "[['name','ilike','" + search + "']," + ICSUtil.wherePreferences(table, true, ctx);
	    	domain = domain.substring(0, domain.length()-1) + "]";
			jsonParams.put("domain", new JSONArray(domain));
	    	jsonParams.put("fields", new JSONArray("['name']"));
	    	//JSONObject jsonContext = new JSONObject();
    		jsonObjSend.put("params", jsonParams);
	    	// Output the JSON object we're sending to Logcat:
	    	Log.i(TAG, jsonObjSend.toString(2));
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	// Send the HttpPostRequest and receive a JSONObject in return
    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);
    	try {
			result = (JSONObject) jsonObjRecv.get("result");
			array = new JSONArray(result.getString("records"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return array;
    }
    
    public static JSONArray getJSONUsers (String session_id, Context ctx){
    	final String URL = DIRECCION + "/web/dataset/search_read";
    	JSONObject result = new JSONObject();
    	JSONArray array = null; 
    	ThreadPolicy tp = ThreadPolicy.LAX;
    	StrictMode.setThreadPolicy(tp);
    	JSONObject jsonObjSend = new JSONObject();
    	
    	try {
	    	JSONObject jsonParams = new JSONObject();
	    	jsonObjSend.put("jsonrpc", "2.0");
	    	jsonObjSend.put("method", "call");
	    	jsonParams.put("session_id", session_id);
	    	jsonParams.put("model", "ics.metering.user.pin");
	    	String domain = "[['user_id','=', "+ ICSUtil.readPreference("uid", ctx) +"]]" ;
			jsonParams.put("domain", new JSONArray(domain));
	    	jsonParams.put("fields", new JSONArray("['pin_device']"));
	    	//JSONObject jsonContext = new JSONObject();
    		jsonObjSend.put("params", jsonParams);
	    	// Output the JSON object we're sending to Logcat:
	    	Log.i(TAG, jsonObjSend.toString(2));
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	// Send the HttpPostRequest and receive a JSONObject in return
    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);
    	try {
			result = (JSONObject) jsonObjRecv.get("result");
			array = new JSONArray(result.getString("records"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return array;
    }

    
    static String modelOpenERP(dbTableEnum table){
        switch (table) {
		case CITY:
			return "ics.city";
		case BOOK:
			return "ics.metering.book";
		case STREET:
			return "ics.metering.street";
		case READING:
			return "ics.metering.supply.point";
		default:
			return "";
		}
    }
    
    
    
    public static void sessionInfo (String session_id){
    	//final String URL = DIRECCION + "/web/dataset/search_read";
    	final String URL = DIRECCION + "/web/session/get_session_info";
//dict: {'params': {'sort': '', 'domain': ['|', ['name', 'ilike', 'a'], ['login', 'ilike', 'a']], 'fields': ['name', 'login', 'lang', 'date'], 'session_id': '21001eb5037743f9b269d12885eeebaf', 'limit': 80, 'context': {'lang': 'es_ES', 'bin_size': True, 'tz': 'Europe/Brussels', 'uid': 1}, 'offset': 0, 'model': 'res.users'}, 'jsonrpc': '2.0', 'method': 'call', 'id': 'r44'}
//dict: {'params': {'sort': '', 'domain': ['|', ['name', 'ilike', 'a'], ['login', 'ilike', 'a']], 'fields': ['name', 'login', 'lang', 'date'], 'session_id': '21001eb5037743f9b269d12885eeebaf', 'limit': 80, 'context': {'lang': 'es_ES',                   'tz': 'Europe/Brussels', 'uid': 1}, 'offset': 0, 'model': 'res.users'}, 'jsonrpc': '2.0', 'id': 'r44', 'method': 'call'}    	
    	ThreadPolicy tp = ThreadPolicy.LAX;
    	StrictMode.setThreadPolicy(tp);
//        {"jsonrpc": "2.0",
//            "method": "call",
//            "params": {"session_id": "SID",
//                       "context": {},
//                       "arg1": "val1" },
//            "id": null}
    	JSONObject jsonObjSend = new JSONObject();
    	try {
	    	JSONObject jsonParams = new JSONObject();
	    	jsonObjSend.put("jsonrpc", "2.0");
	    	jsonObjSend.put("method", "call");
	    	//session_id = loginOpenerp();
	    	jsonParams.put("session_id", session_id);
	    	jsonParams.put("sort", "");
	    	jsonParams.put("domain", "['|', ['name', 'ilike', 'a'], ['login', 'ilike', 'a']]");
	    	jsonParams.put("fields", "['name', 'login', 'lang', 'date']");
	    	jsonParams.put("limit", 80);
	    	JSONObject jsonContext = new JSONObject();
	    	jsonContext.put("uid", 1);
	    	jsonContext.put("lang", "es_ES");
	    	jsonContext.put("tz", "Europe/Brussels");
	    	jsonParams.put("context", jsonContext);
	    	jsonParams.put("offset", 0);
	    	jsonParams.put("model", "res.users");
    		jsonObjSend.put("params", jsonParams);
	    	jsonObjSend.put("id", "r44");
	    		
	    	// Add a nested JSONObject (e.g. for header information)
//	    	JSONObject header = new JSONObject();
//	    	header.put("deviceType","Android"); // Device type
//	    	header.put("deviceVersion","2.0"); // Device OS version
//	    	header.put("language", "es-es");	// Language of the Android client
//	    	jsonObjSend.put("header", header);
	
	    	// Output the JSON object we're sending to Logcat:
	    	Log.i(TAG, jsonObjSend.toString(2));
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}

    	// Send the HttpPostRequest and receive a JSONObject in return
    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);
    	JSONObject result;
    	try {
			result = (JSONObject) jsonObjRecv.get("result");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void readOpenerp (String session_id){
    	final String URL = DIRECCION + "/web/dataset/search_read";
    	
    	ThreadPolicy tp = ThreadPolicy.LAX;
    	StrictMode.setThreadPolicy(tp);
//        {"jsonrpc": "2.0",
//            "method": "call",
//            "params": {"session_id": "SID",
//                       "context": {},
//                       "arg1": "val1" },
//            "id": null}
    	JSONObject jsonObjSend = new JSONObject();

    	try {
	    	JSONObject jsonParams = new JSONObject();
    		jsonObjSend.put("method", "call");
//	    	jsonObjSend.put("session_id", session_id);
//	    	jsonParams.put("context", "{}");
//	    	jsonParams.put("arg1", "ics_metering");
//	    	jsonObjSend.put("params", jsonParams);
	    	jsonObjSend.put("db", "sms");
	    	jsonParams.put("session_id", session_id);
	    	jsonParams.put("model", "res.partner");
    		jsonObjSend.put("params", jsonParams);
	    		
	    	// Add a nested JSONObject (e.g. for header information)
	    	JSONObject header = new JSONObject();
	    	header.put("deviceType","Android"); // Device type
	    	header.put("deviceVersion","2.0"); // Device OS version
	    	header.put("language", "es-es");	// Language of the Android client
	    	jsonObjSend.put("header", header);
	
	    	// Output the JSON object we're sending to Logcat:
	    	Log.i(TAG, jsonObjSend.toString(2));
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}

    	// Send the HttpPostRequest and receive a JSONObject in return
        
    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);

    	JSONObject result;
		try {
			result = (JSONObject) jsonObjRecv.get("result");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void fillTmpListItem(dbTableEnum table, Context ctx){
    	ContentValues cv = new ContentValues();
		JSONObject objOpenERP = new JSONObject();
		AdaptadorDB adaptadorDB = new AdaptadorDB(ctx);
		JSONArray arrOpenERP = MeteringJSON.getJSONBuscador(MeteringJSON.loginOpenerp(ctx), "", table, ctx);
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
		}
			
    }
    
    public static void sendJSONReading(Context ctx){
    	String session_id = loginOpenerp(ctx); 
    	boolean error = false;
    	final String URL = DIRECCION + "/web/dataset/call";
    	if (session_id.length()>0) {
	    	JSONArray  argsArray = new JSONArray(); 
	    	ThreadPolicy tp = ThreadPolicy.LAX;
	    	StrictMode.setThreadPolicy(tp);
	    	
	    	JSONObject jsonObjSend = new JSONObject();
	    	try {
		    	JSONObject jsonParams = new JSONObject();
		    	//JSONObject jsonArgs = new JSONObject();
		    	jsonObjSend.put("jsonrpc", "2.0");
		    	jsonParams.put("method", "import_android_reading");
		    	jsonParams.put("session_id", session_id);
		    	jsonParams.put("model", "ics.metering.reading");
		    	argsArray.put(AdaptadorDB.arraySendReading());
		    	jsonParams.put("args", argsArray);
	    		jsonObjSend.put("params", jsonParams);
	    		// Output the JSON object we're sending to Logcat:
		    	Log.i(TAG, jsonObjSend.toString(2));
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    		error = true;
	    	}
	    	// Send the HttpPostRequest and receive a JSONObject in return
	    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);
	    	if ((!jsonObjRecv.has("error")) && (!error)){
	    		//OpenERP todo OK
	    		AdaptadorDB.deleteRegistro("Reading", "readed = 1", new String[]{});
	    	}
    	}
    }
    public static class getJSONSupplyTask extends AsyncTask<Object, Integer, Integer> {
		int nReg = 0;
		int totalReg = 0;
		boolean error = false;
		Context ctx = null;
        @Override
	    protected void onPreExecute() {
        	//ImportSelectionList.pDialog.setMax(100);
        	//ImportSelectionList.pDialog.setProgress(0);
	        //ImportSelectionList.pDialog.show();
	    }

        protected Integer doInBackground(Object... objectParams) {
            
        	final String URL = DIRECCION + "/web/dataset/call";
        	JSONObject result = new JSONObject();
        	JSONArray arrayRecords = new JSONArray(); 
        	JSONObject jsonObjSend = new JSONObject();
        	ctx = (Context) objectParams[1];
        	try {
    	    	JSONObject jsonParams = new JSONObject();
    	    	jsonObjSend.put("jsonrpc", "2.0");
    	    	jsonParams.put("method", "get_openerp_data");
    	    	jsonParams.put("session_id", objectParams[0].toString()); //El id de sesion
    	    	jsonParams.put("model", "ics.metering.supply.point");
    	    	jsonParams.put("args", AdaptadorDB.arrayImportSelection());
        		jsonObjSend.put("params", jsonParams);

        		// Output the JSON object we're sending to Logcat:
    	    	Log.i(TAG, jsonObjSend.toString(2));
        	} catch (JSONException e) {
        		e.printStackTrace();
        	}
        	// Send the HttpPostRequest and receive a JSONObject in return
        	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,client);
        	ContentValues cv = new ContentValues();
        	Cursor cur;
        	String colName="", colAux = "";
        	String[] tables = new String[]{"City","Book","Street","Reading","Consumption"};
        	try {
        		result = (JSONObject) jsonObjRecv.get("result");
        		totalReg = result.getInt("Total");
        		if (!result.has("error")){
	                AdaptadorDB.emptyTables(); 
	    			JSONObject objOpenERP= new JSONObject();
	        		for (String table : tables) {
	    				if (result.has(table)){
	    					arrayRecords = new JSONArray(result.getString(table));
	    					cur = AdaptadorDB.pragmaTable(table);
	    					if (cur.moveToFirst()){
	    						objOpenERP = new JSONObject(arrayRecords.getString(0)); // solo la primera vez para ver los campos de las tablas
	    						ArrayList<String> columns = new ArrayList<String>();
	    						ArrayList<String> aditionalCol = new ArrayList<String>();
	    						// Buscamos los campos que existen tanto en la tabla como en el diccionario
	    						for (int j = 0; j < cur.getCount(); j++) {
	    							colName = cur.getString(1);	// 1 es la columna name
	    							if (objOpenERP.has(colName)){  
	    								columns.add(colName);
	    							}else{
	    								colAux = colName +"_id";
	    								if (objOpenERP.has(colAux)){  
	    									aditionalCol.add(colName);
	    								}
	    							}
	    							cur.moveToNext();
	    						}
	    						for (int i = 0; i < arrayRecords.length(); i++) {
	    							objOpenERP = new JSONObject(arrayRecords.getString(i));
	    							cv.clear();
	    							for (String col : columns) {
	    								if (col.contains("_id")){
	    									if (objOpenERP.getString(col) != "false"){  //La columna tiene valor distinto de false
	    										cv.put(col, objOpenERP.getJSONArray(col).getString(0)); //Ejemplo partner_id
	    										colAux = col.replace("_id", "");
	    										if (aditionalCol.contains(colAux)){ //Existe la columna en la tabla android
	    											cv.put(colAux, objOpenERP.getJSONArray(col).getString(1)); //Ejemplo partner
	    										}
	    									}
	    								}else{
	    									//CORREGIR ERROR DE DEVOLUCION DE FALSE EN ELEMENTOS NULOS
	    									cv.put(col, objOpenERP.getString(col));
	    								}
	    							}
	    							AdaptadorDB.insertarRegistro(table, cv);
	    							nReg++;
	    							//publishProgress((int) ((nReg / (float) totalReg) * 100));
	    						}
	    					}
	    				}
	    			}
        		}
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			error = true;
    		}
            if (!error){
            	
            }
            return nReg;
        }

        protected void onProgressUpdate(Integer... progress) {
            //ImportSelectionList.pDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(Integer result) {
        	Toast.makeText(ctx, "Importados " + nReg + " registros de " + totalReg, Toast.LENGTH_SHORT).show();
        	//ImportSelectionList.pDialog.s showDialog("Downloaded " + result + " bytes");
        }
    }
    
}
