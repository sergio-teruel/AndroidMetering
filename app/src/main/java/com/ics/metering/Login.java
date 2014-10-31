package com.ics.metering;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Login extends Activity implements OnClickListener{
	private String userLogin = "";
	private String userPassword = "";
	private EditText txtPassword;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        //Obtenemos usuario anteriormente logeado y almacenado en preferences        
		//userLogin = ICSUtil.readPreference("userLogin", this);
        
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.btnLogin:
			//codigo para inicio del proceso del login
			//userLogin = txtUser.getText().toString();
			userPassword = txtPassword.getText().toString();
			try {
				checkLocalPin();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			break;

		default:
			break;
		}
	}
	
	public boolean checkLocalPin() throws JSONException{
		boolean res = false;
		// Existe el usuario, comprobamos el password
		if (ICSUtil.readPreference("pin", this).equals(userPassword) || userPassword.equals("")){
			//USUARIO OK CONTINUAMOS
			//ICSUtil.writePreference("userLogin", userLogin, this);//Guardamos el usuario para el proximo login 
			res = true;
			// Lanzamos la aplicacion
			Intent myIntent = new Intent(this, ListaContadores.class);
			this.finish();
			startActivity(myIntent);
		} else{
			Toast.makeText(this, "Usuario INCORRECTO", Toast.LENGTH_SHORT).show();
			res = false;
		}
				
		return res;
	}
	
	
	public void getUserPinOpenERP() throws JSONException{
		String session_id = "";
		session_id = MeteringJSON.loginOpenerp(this);
		if (session_id.length() > 0){
			JSONArray arrOpenERP = MeteringJSON.getJSONUsers(session_id, getApplicationContext());
			JSONObject objOpenERP = new JSONObject(arrOpenERP.getString(0));
			ICSUtil.writePreference("pin", objOpenERP.getString("pin_device"), this);
    		Toast.makeText(this,R.string.msg_import_users_ok, Toast.LENGTH_SHORT).show();
		} else {
			//Error al conectar a OpenERP
    		Toast.makeText(this,R.string.msg_connectdb_error, Toast.LENGTH_SHORT).show();
			//openSttings();
		}
	}
	
	public void openSttings(){
		Intent intent = new Intent(this, Settings.class);
		startActivity(intent);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_login, menu);
		return true;  
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {  
	        case R.id.mnu_options:  
	        	// put your code here
	        	openSttings();
	        	break;
	        case R.id.mnu_syncUsers:  
	        	// put your code here
				try {
					getUserPinOpenERP();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}
}
