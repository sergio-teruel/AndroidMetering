package com.ics.metering;

import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import android.widget.Toast;


public final class OpenERPJson {
	private String session_id = ""; 
	private String address ="";
	private Context ctx = null;

	private DefaultHttpClient httpClient = new DefaultHttpClient();
	private HttpParams httpParams = httpClient.getParams();
	private int connectionTimeoutMillis = 5000;
	private int socketTimeoutMillis = 10000;

	private static ProgressDialog pDialog;
	
	public OpenERPJson(Context c){
		ctx = c;
		address = getServerAddress(ctx);
		session_id = loginOpenerp(ctx);
	}

	public String getSessionID(){
		return session_id;
	}
	public DefaultHttpClient getHttpClient(){
		return httpClient;
	}
	public String getAddress(){
		return address;
	}
	
	
	public static String getServerAddress(Context c){
		return "http://" + ICSUtil.readPreference("ServerAddress", c) + ":" + ICSUtil.readPreference("ServerPort", c);
	}
	
	public void setConnectionTimeOut(int time ){
		this.connectionTimeoutMillis  = time;
	}
	public void setSocketTimeOut(int time ){
		this.socketTimeoutMillis  = time;
	}
	
    
	private String loginOpenerp (Context ctx){
    	String URL = address + "/web/session/authenticate";
    	ThreadPolicy tp = ThreadPolicy.LAX;
    	StrictMode.setThreadPolicy(tp);
    	JSONObject jsonObjSend = new JSONObject();

    	try {
	    	JSONObject jsonParams = new JSONObject();
	    	jsonParams.put("base_location", address);
	    	jsonParams.put("login", ICSUtil.readPreference("ServerUser", ctx));
	    	jsonParams.put("password", ICSUtil.readPreference("ServerPassword", ctx));
	    	jsonParams.put("db", ICSUtil.readPreference("ServerDatabase", ctx));
	    	jsonObjSend.put("params", jsonParams);

	    	JSONObject header = new JSONObject();
	    	header.put("deviceType","Android"); // Device type
	    	header.put("deviceVersion","2.0"); // Device OS version
	    	header.put("language", "es-es");	// Language of the Android client
	    	jsonObjSend.put("header", header);
	    	// Output the JSON object we're sending to Logcat:
	    	Log.i("Sesion info Openerp", jsonObjSend.toString(2));
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	// Send the HttpPostRequest and receive a JSONObject in return 
    	HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMillis);
    	//HttpConnectionParams.setSoTimeout(httpParams, socketTimeoutMillis);
    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,httpClient);

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
    	}else {
    		session_id = "";
    	}
		return session_id;
    }
	
	private void pDialogInit(String message){
		pDialog = new ProgressDialog(ctx);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(message);
        pDialog.setCancelable(true);
	}
	
	public void sendReadingToOpenerp(){
        pDialogInit(ctx.getResources().getString(R.string.msg_procesing));
		new sendJSONReadingTask().execute(); 
	}
	
	private class sendJSONReadingTask extends AsyncTask<Object, Integer, Integer>{
    	int nReg = 0;
    	@Override
	    protected void onPreExecute() {
	        pDialog.setMax(100);
	        pDialog.setProgress(0);
	        pDialog.show();
	    }

	    @Override
		protected Integer doInBackground(Object... objectParams) {
	    	boolean error = false;
	    	String URL = address + "/web/dataset/call";
	    	
	    	if ( session_id.length()>0) {
		    	JSONArray  argsArray = new JSONArray(); 
		    	JSONObject jsonObjSend = new JSONObject();
		    	try {
			    	JSONObject jsonParams = new JSONObject();
			    	jsonObjSend.put("jsonrpc", "2.0");
			    	jsonParams.put("method", "import_android_reading");
			    	jsonParams.put("session_id", session_id);
			    	jsonParams.put("model", "ics.metering.reading");
			    	JSONArray arrayAux = new JSONArray();
			    	arrayAux = AdaptadorDB.arraySendReading();
			    	nReg = arrayAux.length(); // Para contar numero de registros
			    	argsArray.put(arrayAux);
			    	jsonParams.put("args", argsArray);
		    		jsonObjSend.put("params", jsonParams);
		    		// Output the JSON object we're sending to Logcat:
			    	Log.i("Send reading", jsonObjSend.toString(2));
		    	} catch (JSONException e) {
		    		e.printStackTrace();
		    		error = true;
		    	}
		    	// Send the HttpPostRequest and receive a JSONObject in return
		    	HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMillis);
		    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,httpClient);
		    	if ((!jsonObjRecv.has("error")) && (!error)){
		    		//OpenERP todo OK
		    		AdaptadorDB.deleteRegistro("Reading", "readed = 1", new String[]{});
		    	}
	    	}
			return nReg;
	    }
	    protected void onProgressUpdate(Integer... values) {
	        int progreso = values[0].intValue();
	        pDialog.setProgress(progreso);
	    }
	    
        protected void onPostExecute(Integer result) {
        	Toast.makeText(ctx, "Enviadas " + nReg + " lecturas", Toast.LENGTH_SHORT).show();
	        pDialog.dismiss();
	        //finish();
        }
    }
	
	public void getDataFromOpenerp(){
        pDialogInit("Procesando ...");
		new getDataFromOpenerpTask().execute();
	}
    private class getDataFromOpenerpTask extends AsyncTask<Object, Integer, Integer> {
		int nReg = 0;
		int totalReg = 0;
		boolean error = false;
		String TAG = "Get data from OpenERP";

    	@Override
	    protected void onPreExecute() {
	        pDialog.setMax(1000);
	        pDialog.setProgress(0);
	        pDialog.show();
	    }
        protected Integer doInBackground(Object... objectParams) {
        	final String URL = address + "/web/dataset/call";
        	JSONObject result = new JSONObject();
        	JSONArray arrayRecords = new JSONArray(); 
        	JSONObject jsonObjSend = new JSONObject();
        	try {
    	    	JSONObject jsonParams = new JSONObject();
    	    	jsonObjSend.put("jsonrpc", "2.0");
    	    	jsonParams.put("method", "get_openerp_data");
    	    	jsonParams.put("session_id", session_id);
    	    	jsonParams.put("model", "ics.metering.supply.point");
    	    	jsonParams.put("args", AdaptadorDB.arrayImportSelection());
        		jsonObjSend.put("params", jsonParams);

        		// Output the JSON object we're sending to Logcat:
    	    	Log.i(TAG, jsonObjSend.toString(2));
        	} catch (JSONException e) {
        		e.printStackTrace();
        	}
        	// Send the HttpPostRequest and receive a JSONObject in return
	    	HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMillis);
        	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend, httpClient);
        	ContentValues cv = new ContentValues();
        	Cursor cur;
        	String colName="", colAux = "";
        	String[] tables = new String[]{"City","Book","Street","Reading","Consumption","Issue","Issue_detail"};
        	try {
        		result = (JSONObject) jsonObjRecv.get("result");
        		totalReg = result.getInt("Total");
        		pDialog.setMax(totalReg);
        		if (!result.has("error")){
	                AdaptadorDB.emptyTables(); 
	    			JSONObject objOpenERP= new JSONObject();
	        		for (String table : tables) {
	    				if (result.has(table)){
	    					arrayRecords = new JSONArray(result.getString(table));
	    					if (arrayRecords.length() > 0){
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
		    									if (objOpenERP.getString(col) != "false"){
		    										if (objOpenERP.getString(col) == "true"){
		    											cv.put(col, 1);	
			    									}else{
			    										cv.put(col, objOpenERP.getString(col));
			    									}
		    									}
		    								}
		    							}
		    							AdaptadorDB.insertarRegistro(table, cv);
		    							if ((nReg % 10) == 0) publishProgress(nReg);
		    							nReg++;
		    							//publishProgress((int) ((nReg / (float) totalReg) * 100));
		    						}
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
            pDialog.setProgress(progress[0]);
            pDialog.setMessage(progress[0] +  " de " + totalReg);
        }

        protected void onPostExecute(Integer result) {
        	//Toast.makeText(ctx, "Importados " + nReg + " registros de " + totalReg, Toast.LENGTH_SHORT).show();
        	pDialog.dismiss();
        }
    }

	public void sendAttachmentToOpenerp(){
        pDialogInit(ctx.getResources().getString(R.string.msg_procesing));
		new sendJSONAttachmentTask().execute(); 
	}
    
	private class sendJSONAttachmentTask extends AsyncTask<Object, Integer, Integer>{
    	int nReg = 0;
    	@Override
	    protected void onPreExecute() {
	        pDialog.setMax(100);
	        pDialog.setProgress(0);
	        pDialog.show();
	    }

	    @Override
		protected Integer doInBackground(Object... objectParams) {
	    	boolean error = false;
	    	String URL = address + "/web/dataset/call";
	    	
	    	if ( session_id.length()>0) {
		    	JSONArray  argsArray = new JSONArray(); 
		    	JSONObject jsonObjSend = new JSONObject();
		    	try {
			    	JSONObject jsonParams = new JSONObject();
			    	jsonObjSend.put("jsonrpc", "2.0");
			    	jsonParams.put("method", "import_android_attachment");
			    	jsonParams.put("session_id", session_id);
			    	jsonParams.put("model", "ics.metering.supply.point");
			    	JSONArray arrayAux = new JSONArray();
			    	arrayAux = AdaptadorDB.arraySendAttachment(ctx);
			    	nReg = arrayAux.length(); // Para contar numero de registros
			    	argsArray.put(arrayAux);
			    	jsonParams.put("args", argsArray);
		    		jsonObjSend.put("params", jsonParams);
		    		// Output the JSON object we're sending to Logcat:
			    	Log.i("Send attachment", jsonObjSend.toString(2));
		    	} catch (JSONException e) {
		    		e.printStackTrace();
		    		error = true;
		    	}
		    	// Send the HttpPostRequest and receive a JSONObject in return
		    	HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMillis);
		    	JSONObject jsonObjRecv = HttpClient.SendHttpPost(URL, jsonObjSend,httpClient);
		    	if ((!jsonObjRecv.has("error")) && (!error)){
		    		//OpenERP todo OK
		    		//AdaptadorDB.deleteRegistro("Reading", "readed = 1", new String[]{});
		    	}
	    	}
			return nReg;
	    }
	    protected void onProgressUpdate(Integer... values) {
	        int progreso = values[0].intValue();
	        pDialog.setProgress(progreso);
	    }
	    
        protected void onPostExecute(Integer result) {
        	Toast.makeText(ctx, "Enviados " + nReg + " adjuntos", Toast.LENGTH_SHORT).show();
	        pDialog.dismiss();
	        //finish();
        }
    }
    
}
