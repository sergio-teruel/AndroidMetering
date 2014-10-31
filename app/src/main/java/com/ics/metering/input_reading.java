package com.ics.metering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.ics.metering.AdaptadorDB.dbTableEnum;



public class input_reading extends Activity implements OnClickListener{
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	private static int TAKE_PICTURE = 1;
	private static int SELECT_PICTURE = 2;
	
	private static String JPEG_FILE_PREFIX = "";
	private static String JPEG_FILE_SUFFIX = ".jpg";
	private static int TRY_ISSUE = 2; // Numero de veces que el usuario ha de rellenar el valor antes de issue
	
	MediaScannerConnection scannerConn= null;
	String mCurrentPhotoPath ="";
	TextView txtInputValue;
	int itemId;
	int value = 0;
	int previousValue = 0;
	String name = "";
	int tryNumber = 0;
	
	LocationManager milocManager = null;
	LocationListener milocListener  = null;

	private static Cursor cur = null;
	Cursor curConsum = null;
	AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
	private XYPlot mySimpleXYPlot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Put your code here
		setContentView(R.layout.input_reading);

		Bundle bundle = getIntent().getExtras();
		this.setTitle(bundle.getString("name"));
		itemId = bundle.getInt("id",0);

		txtInputValue = (TextView) findViewById(R.id.new_reading_value);

		cargarDatos();
		value = cur.getInt(cur.getColumnIndex("value"));
		previousValue = value;
		 
		if (value != 0) {
			txtInputValue.setText(Integer.toString(value));
		}

		rellenarCampos();
		cargarGrafica(curConsum);
	}		
	void cargarDatos(){
		myAdaptadorDB.openDB();
		cur = AdaptadorDB.getCursorById(itemId, dbTableEnum.Reading);
		cur.moveToFirst();
		
		curConsum =  myAdaptadorDB.getSupplyConsumption(cur.getInt(cur.getColumnIndex("id")));
	}

	//void rellenaCampos(int[] controles, String[] columnas){
	void rellenarCampos(){
		int[] controles = new int[]{R.id.txtDateLocalization, R.id.txtLatitude, R.id.txtLongitude, R.id.txtComment};
		String[] columnas = new String[]{"supply_date_localization", "supply_latitude", "supply_longitude", "comment"};
		asingLayoutData(controles, columnas, cur);
		
		
		if (curConsum.moveToLast()){
			controles = new int[]{R.id.txtLastDate, R.id.txtLastValue, R.id.txtAverage};
			columnas = new String[]{"date", "value", "average"};
			asingLayoutData(controles, columnas, curConsum);
		}
	}
	
	void asingLayoutData(int[] controles, String[] columnas, Cursor c){
		int i = 0;
		int col = 0;
		TextView[] txt = new TextView[controles.length];
		for (int control : controles) {
			txt[i]= (TextView) findViewById(control);
			col = c.getColumnIndex(columnas[i]);
//			if (c.getType(col) == Cursor.FIELD_TYPE_NULL){
//				txt[i].setText("");
//			}else{
//				txt[i].setText(c.getString(col));
//			}
			switch (c.getType(col)) {
			case Cursor.FIELD_TYPE_NULL:
				txt[i].setText("");
				break;
			case Cursor.FIELD_TYPE_FLOAT:
		        DecimalFormat df = new DecimalFormat("0.##");
				txt[i].setText(df.format(c.getDouble(col)));
				//txt[i].setText(ICSUtil.roundTwoDecimals(c.getFloat(col))+"");
				break;
			default:
				txt[i].setText(c.getString(col));
				break;
			}
			i++;
		}

	}
    private void saveReading(){
		ContentValues cv = new ContentValues();
		cv.put("value", txtInputValue.getText().toString());
		Date d = new Date();
		//cv.put("value", value);
		if (txtInputValue.getText().length()>0){
			cv.put("readed", 1);
			cv.put("state", 1);
			//cv.put("date", (String) (DateFormat.format("yyyy-MM-dd hh:mm:ss", d)));
			//cv.put("date", d.toLocaleString());
			cv.put("date", d.toGMTString());
			cv.put("user_id", ICSUtil.readPreference("uid", this));
		}else {
			cv.put("readed", 0);
			cv.put("state", 1);
		}
		AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
		myAdaptadorDB.grabarLectura(cv, itemId);
    }
	

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
//		switch (v.getId()) {
//		case R.id.btnaccept_new_reading_value:
//			ContentValues cv = new ContentValues();
//			cv.put("value", txtInputValue.getText().toString());
//			AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
//			myAdaptadorDB.grabarLectura(cv, itemId);
//			finish();
//			break;
//
//		default:
//			Toast myToast = Toast.makeText(this, "hola rer", Toast.LENGTH_SHORT);
//			myToast.show();
//			break;
//		}
	}
	
	
	
	/** 
	 * Add menu items
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_input_reading, menu);
		return true;  
	}  
	/** 
	 * Define menu action
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
	    switch (item.getItemId()) {  
	        case R.id.mnu_accept:
	        	if (txtInputValue.getText().length()>0){
		        	saveReading();
		        	if (checkIssue() == 0){
		        		finish();
		        	}
	    		}
	        	break;
	        case R.id.mnu_take_photo:
       			int code = TAKE_PICTURE;
       			try {
					dispatchTakePictureIntent(code);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	break;
	        case R.id.mnu_show_gallery:
				intent = new Intent (this, ImageThumbnailsActivity.class);
	        	intent.putExtra("supply_id_path", Environment.getExternalStorageDirectory() +"/"+ ICSUtil.PICTURES_DIR + "/" + getAlbumName());
	        	startActivity(intent);
	        	
	        	break;
	        case R.id.mnu_show_map:
	        	Float longitude = cur.getFloat(cur.getColumnIndex("supply_longitude"));
	        	Float latitude = cur.getFloat(cur.getColumnIndex("supply_latitude"));
	        	intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude + ","+ longitude));
	        	startActivity(intent);
	    		//milocManager.removeUpdates(milocListener);
	        	break;
	        case R.id.mnu_save_localization:
	    		milocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    		milocListener = new MiLocationListener();
	    		milocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, milocListener);
	    		break;
	        case R.id.mnu_manual_issue:
	        	manualIssue();
	    		break;
	        case R.id.mnu_delete_reading:
	        	if (deleteReading()) {
		        	Toast.makeText(this, "Se ha borrado la lectura actual", Toast.LENGTH_SHORT).show();
		        	finish();
	        	}
	    		break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}

//    @Override 
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    	/**
//    	 * Se revisa si la imagen viene de la c�mara (TAKE_PICTURE) o de la galer�a (SELECT_PICTURE)
//    	 */
//    	if (requestCode == TAKE_PICTURE) {
//    		/**
//    		 * Si se reciben datos en el intent tenemos una vista previa (thumbnail)
//    		 */
//    		if (data != null) {
//    			handleSmallCameraPhoto(data);
//    			
//    			/**
//    			 * En el caso de una vista previa, obtenemos el extra �data� del intent y 
//    			 * lo mostramos en el ImageView
//    			 */
////    			if (data.hasExtra("data")) { 
////    				ImageView iv = (ImageView)findViewById(R.id.imgView);
////    				iv.setImageBitmap((Bitmap) data.getParcelableExtra("data"));
////    			}
//    		/**
//    		 * De lo contrario es una imagen completa
//    		 */    			
//    		} else {
//    			
//    			/**
//    			 * A partir del nombre del archivo ya definido lo buscamos y creamos el bitmap
//    			 * para el ImageView
//    			 */
////    			ImageView iv = (ImageView)findViewById(R.id.imgView);
////    			iv.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
//    			/**
//    			 * Para guardar la imagen en la galer�a, utilizamos una conexi�n a un MediaScanner
//    			 */
//    			new MediaScannerConnectionClient() {
//    				private MediaScannerConnection msc = null; {
//    					msc = new MediaScannerConnection(getApplicationContext(), this); msc.connect();
//    				}
//    				public void onMediaScannerConnected() { 
//    					msc.scanFile(mCurrentPhotoPath, "images/*");
//    				}
//    				public void onScanCompleted(String mCurrentPhotoPath, Uri contentUri) { 
//    					msc.disconnect();
//    				} 
//    			};
//    			
//    		}
//    	/**
//    	 * Recibimos el URI de la imagen y construimos un Bitmap a partir de un stream de Bytes
//    	 */
//    	} else if (requestCode == SELECT_PICTURE){
//    		Uri selectedImage = data.getData();
//    		InputStream is;
//    		try {
//    			is = getContentResolver().openInputStream(selectedImage);
//    	    	BufferedInputStream bis = new BufferedInputStream(is);
//    	    	Bitmap bitmap = BitmapFactory.decodeStream(bis);            
//    	    	ImageView iv = (ImageView)findViewById(R.id.imgView);
//    	    	iv.setImageBitmap(bitmap);						
//    		} catch (FileNotFoundException e) {}
//    	}
//    }

    public class MiLocationListener implements LocationListener{

		@Override
		public void onLocationChanged(Location loc) {
	
		    loc.getLatitude();
		    loc.getLongitude();
		    Date d=new Date(); 

		    ContentValues cvPosition = new ContentValues();
		    cvPosition.put("supply_latitude", loc.getLatitude());
		    cvPosition.put("supply_longitude", loc.getLongitude());
		    cvPosition.put("supply_date_localization", (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", d));
		    cvPosition.put("extra_changed", 1);
		    myAdaptadorDB.grabarLectura(cvPosition, itemId);

    		milocManager.removeUpdates(milocListener);

    		String coordenadas =  "Mis coordenadas son: " + "Latitud = " + loc.getLatitude() + "Longitud = " + loc.getLongitude();
		    Toast.makeText( getApplicationContext(), coordenadas, Toast.LENGTH_LONG).show();
		    //Toast.makeText( getApplicationContext(), cvPosition.toString(), Toast.LENGTH_LONG).show();
		    
		    cargarDatos();
		    rellenarCampos();
    		
    		//onCreate(null);
	    }

		public void onProviderDisabled(String provider)
	    {
	    	Toast.makeText( getApplicationContext(),"Gps Desactivado",Toast.LENGTH_SHORT ).show();

	    }
	    public void onProviderEnabled(String provider)
	    {
	    	Toast.makeText( getApplicationContext(),"Gps Activo",Toast.LENGTH_SHORT ).show();
	    }
	    public void onStatusChanged(String provider, int status, Bundle extras){
	    	Toast.makeText( getApplicationContext(),"Gps Cambio estado",Toast.LENGTH_SHORT ).show();
	    }
    }    

    void cargarGraficax(Cursor c){
        // Inicializamos el objeto XYPlot búscandolo desde el layout:
        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
 
        // Creamos dos arrays de prueba. En el caso real debemos reemplazar
        // estos datos por los que realmente queremos mostrar
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {500, 0, 0, 0, 0, 0};
 
        SimpleXYSeries consumo = null;
 
        ArrayList<Integer> valores = new ArrayList<Integer>();
        ArrayList<Integer> fechas = new ArrayList<Integer>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    	Date fecha = null;
        
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
            	//consumo.addLast(c.getInt(c.getColumnIndex("consumption")), c.getInt(c.getColumnIndex("date")));
            	valores.add(c.getInt(c.getColumnIndex("consumption")));
				try {
					fecha = df.parse(c.getString(c.getColumnIndex("date")));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	fechas.add((int) (fecha.getTime()/86400000));
    			c.moveToNext();
    		}
        }
        XYSeries pr1 = new SimpleXYSeries( fechas,valores,"Titulo");
        
        // Añadimos Línea Número UNO:
        XYSeries series1 = new SimpleXYSeries(valores,  // Array de datos
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Sólo valores verticales
                "Series1"); // Nombre de la primera serie
        
//        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),  // Array de datos
//                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Sólo valores verticales
//                "Series1"); // Nombre de la primera serie

        // Repetimos para la segunda serie
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers
), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Series2");
 
        // Modificamos los colores de la primera serie
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 200, 0),                   // Color de la línea
                Color.rgb(0, 100, 0),                   // Color del punto
                Color.rgb(150, 190, 150));              // Relleno
 
        // Una vez definida la serie (datos y estilo), la añadimos al panel
        mySimpleXYPlot.addSeries(series1, series1Format);
 
        // Repetimos para la segunda serie
        mySimpleXYPlot.addSeries(series2, new LineAndPointFormatter
(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), Color.rgb(150, 150, 190)));    	
    }
    
    void cargarGrafica(final Cursor c){
	    // Inicializamos el objeto XYPlot búscandolo desde el layout:
	    mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	
	    // Creamos dos arrays de prueba. En el caso real debemos reemplazar
	    // estos datos por los que realmente queremos mostrar
	    Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
	    Number[] series2Numbers = {4, 6, 3, 8, 2, 10};
	    Number[] series3Numbers = {2, 8, 14, 25, 47, 90};
	
	    // Añadimos Línea Número UNO:
	    XYSeries series1 = new SimpleXYSeries(
	            Arrays.asList(series1Numbers),  // Array de datos
	            SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, // Sólo valores verticales
	            "Series1"); // Nombre de la primera serie

	    Vector<Double> vector=new Vector<Double>();
        for (double x=0.0;x<Math.PI*5;x+=Math.PI/20){
            vector.add(x);
            vector.add(Math.sin(x));
        }
        series1 = new SimpleXYSeries(vector,  // Array de datos
	            SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, 
	            "Series1"); // Nombre de la primera serie);

//	    series1 = new SimpleXYSeries(
//	            Arrays.asList(series3Numbers),  // Array de datos
//	            Arrays.asList(series1Numbers),
//	            "Series1"); // Nombre de la primera serie

	    // Repetimos para la segunda serie
	    XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers
	    		), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Series2");
	
	    // Modificamos los colores de la primera serie
	    LineAndPointFormatter series1Format = new LineAndPointFormatter(
	            Color.rgb(0, 200, 0),                   // Color de la línea
	            Color.rgb(0, 100, 0),                   // Color del punto
	            Color.rgb(150, 190, 150));              // Relleno
	
	    // Una vez definida la serie (datos y estilo), la añadimos al panel
	    mySimpleXYPlot.addSeries(series1, series1Format);
	
	    // Repetimos para la segunda serie
	  //  mySimpleXYPlot.addSeries(series2, new LineAndPointFormatter
	  //  		(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), Color.rgb(150, 150, 190)));
    }

    private String getAlbumName(){
    	File storageDir = new File (Environment.getExternalStorageDirectory() +"/"+ ICSUtil.PICTURES_DIR + "/" + itemId);
    	if (storageDir.exists() == false){
    		storageDir.mkdirs();
    	}
    	return ((Integer) itemId).toString();
    }

    private void dispatchTakePictureIntent(int actionCode) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + JPEG_FILE_SUFFIX;
        File f = new File (Environment.getExternalStorageDirectory() +"/"+ ICSUtil.PICTURES_DIR + "/" + getAlbumName()+ "/" + imageFileName);
        mCurrentPhotoPath = f.getAbsolutePath();
        ICSUtil.writePreference("mCurrentPhotoPath", mCurrentPhotoPath, this);
        //FileOutputStream out = new FileOutputStream(mCurrentPhotoPath);
    	
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
    	startActivityForResult(takePictureIntent, actionCode);
    }    
    
    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == TAKE_PICTURE) {
    		mCurrentPhotoPath = ICSUtil.readPreference("mCurrentPhotoPath", this);
            Bitmap mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
	        double factor = (double)mImageBitmap.getHeight()/(double)mImageBitmap.getWidth();
			try {
		        mImageBitmap = Bitmap.createScaledBitmap(mImageBitmap, 1600, (int) (1600 * factor), true);
				FileOutputStream out = new FileOutputStream(mCurrentPhotoPath);
	            mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
	            addAtachment(new File(mCurrentPhotoPath));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    public void graficaFecha(){
    	
    }
    public int checkIssue(){
    	value = Integer.parseInt(txtInputValue.getText().toString());
    	Cursor c = AdaptadorDB.getTable("Issue", "auto_eval = 1");
    	c.moveToFirst();
    	String sql = "SELECT * FROM View_check_issue WHERE (id= " + itemId +") AND ";
    	for (int i = 0; i < c.getCount(); i++) {
    		Cursor curCheck = AdaptadorDB.getRawQuery(sql + c.getString(c.getColumnIndex("condition")));
    		if (curCheck.getCount() > 0) {
    			tryNumber ++;
    			if (tryNumber >= TRY_ISSUE){
    				// Pantalla de seleccion de detalle de la incidencia
    				Intent myIntent = new Intent(this, IssueReading.class);
    				myIntent.putExtra("readingId", itemId);
    				myIntent.putExtra("issueId", c.getInt(c.getColumnIndex("id")));
    				myIntent.putExtra("issue", c.getString(c.getColumnIndex("name")));
    				startActivity(myIntent);
    				finish();
    			}else{
    				txtInputValue.setText("");
    				Toast.makeText(getApplicationContext(), c.getString(c.getColumnIndex("name")) + " " + c.getString(c.getColumnIndex("condition")) + R.string.msg_try_issue_alert, Toast.LENGTH_SHORT).show();
    				return 1;
    			}
    		}else{
    			if (cur.getInt(cur.getColumnIndex("issue_id"))>0){
    				deleteIssue();
    			}
    		}
    		c.moveToNext();
		}
		return 0;
    }

    private boolean deleteIssue() {
		// TODO Auto-generated method stub
    	ContentValues cv = new ContentValues();
    	cv.put("issue_id", "");
    	cv.put("issue_detail_id", "");
    	cv.put("issue_comment", "");
		AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
		return  myAdaptadorDB.grabarLectura(cv, itemId);
	}
    
	public void onBackPressed(){
    	if (tryNumber > 0) deleteReading();
    	super.onBackPressed();
    }

    public boolean deleteReading(){
    	ContentValues cv = new ContentValues();
    	cv.put("value", "");
    	cv.put("issue_id", "");
    	cv.put("issue_detail_id", "");
    	cv.put("issue_comment", "");
    	cv.put("readed", "0");
		AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
		return  myAdaptadorDB.grabarLectura(cv, itemId);
    }
    
    public int manualIssue(){
    	Cursor c = AdaptadorDB.getTable("Issue", "auto_eval = 0");
    	c.moveToFirst();
    	if (c.getCount() > 0){
			// Pantalla de seleccion de detalle de la incidencia
			Intent myIntent = new Intent(this, IssueReading.class);
			myIntent.putExtra("readingId", itemId);
			myIntent.putExtra("issueId", c.getInt(c.getColumnIndex("id")));
			myIntent.putExtra("issue", c.getString(c.getColumnIndex("name")));
			myIntent.putExtra("manualIssue", false);
			startActivity(myIntent);
    	}
		return itemId;
    }

    private void addAtachment(File f){
    	ContentValues cv = new ContentValues();
    	String resModel = "ics.metering.supply.point";
    	String resName = this.getTitle().toString();
    	//cv.put("id", 0);
		//cv.put("create_uid", 0);
		Date d = new Date();
		//cv.put("date", (String) (DateFormat.format("yyyy-MM-dd hh:mm:ss", d)));
    	//cv.put("create_date", d.toGMTString());
    	//cv.put("write_date", d.toGMTString());
    	//cv.put("create_date", (String) (DateFormat.format("yyyy-MM-dd hh:mm:ss", d)));
    	cv.put("write_date", (String) (DateFormat.format("yyyy-MM-dd hh:mm:ss", d)));

    	//cv.put("write_uid", 0);
    	//cv.put("description", "");
    	cv.put("datas_fname", f.getName());
		//"url TEXT, " +
		cv.put("res_model", resModel);
		//"company_id INTEGER, " +
		cv.put("res_name", resName); 
		cv.put("type","binary");
		cv.put("res_id", itemId); // Id del supply point
		cv.put("file_size",f.length());
		//"db_datas BLOB, " +
		//cv.put("store_fname TEXT, " +
		cv.put("name", f.getName());
		cv.put("file_type", "image/jpg");
		cv.put("partner_id", cur.getInt(cur.getColumnIndex("partner_id")));
		//cv.put("user_id", 0);
		//cv.put("parent_id INTEGER);"; 
		//"index_content TEXT);"; 
		AdaptadorDB.insertarRegistro("ir_attachment", cv);
	}
 }
