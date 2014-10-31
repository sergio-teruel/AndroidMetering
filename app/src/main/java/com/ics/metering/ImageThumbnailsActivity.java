package com.ics.metering;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;



public class ImageThumbnailsActivity extends Activity {
      /** Called when the activity is first created. */
      GridView imagegrid;
      
      @Override
      public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.image_thumbnails);
            Intent i = getIntent();
            // Selected image id
            String supply_id_path = i.getExtras().getString("supply_id_path");
            init_grid_images(supply_id_path);
      }

	private void init_grid_images(final String path){
    	  final ImageGridAdapter myImageGridAdapter = new ImageGridAdapter(this,path);
    	  if (myImageGridAdapter.getCount() > 0 ) {
	    	  imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
	          imagegrid.setAdapter(myImageGridAdapter);
	          imagegrid.setOnItemClickListener(new OnItemClickListener() {
	              @Override
	              public void onItemClick(AdapterView<?> parent, View v,
	                      int position, long id) {
	   
//	                  // Sending image id to FullScreenActivity
//	                  Intent i = new Intent(getApplicationContext(), ViewImage.class);
//	                  // passing array index
//	                  i.putExtra("id", position);
//	                  i.putExtra("path", path);
//	                  startActivity(i);
	            	  
	            	  Intent intent = new Intent ();
	            	  intent.setAction(Intent.ACTION_VIEW);
	            	  File file = new File(myImageGridAdapter.getItem(position).toString()); 
	            	  Uri uri = Uri.fromFile(file);
	            	  intent.setDataAndType(uri, "image/*");
	            	  startActivity(intent);
	            	  
	              }
	          });          
    	  }else{
    		  //No hay imagenes disponibles
/*    		  TextView txtNoImages = new TextView(this);
    		  txtNoImages.setText(R.string.noImages);
    		  this.addContentView(txtNoImages, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    		   
*/    	  
    		  Toast.makeText(this, R.string.noImages, Toast.LENGTH_SHORT).show();
    	  }
      }
	public class ImageGridAdapter extends BaseAdapter{
		static final int IMAGE_WIDTH = 160; 
		static final int IMAGE_HEIGHT = 160; 
		private Context mContext;
		List<String> tFileList;
		ByteArrayOutputStream baos;
		Bitmap bm;
		File f;
		File[] files;
		  
		public void getImagesArray (String path){
			ReadSDCard(path);
		  }
		  
		private List<String> ReadSDCard(String path) {
		  tFileList = new ArrayList<String>();
		
		  // It have to be matched with the directory in SDCard
		  f = new File(path);// Here you take your specific folder//
		  files = f.listFiles(new FilenameFilter() {
		      @Override
		      public boolean accept(File dir, String name) {
		          return ((name.endsWith(".jpg")) || (name.endsWith(".png")));
		      }
		  });
		  if (files != null){
			  for (int i = 0; i < files.length; i++) {
			      //It's assumed that all file in the path are in supported type
			      File file = files[i];
			      tFileList.add(file.getPath());
			  }
		  }
		  return tFileList;
		}
		  // Constructor
		public ImageGridAdapter(Context c, String imagesPath){
		    mContext = c;
		    if (imagesPath.length() > 0){
		  	  getImagesArray(imagesPath); 
		    }
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
		//return mThumbIds.length;
			return tFileList.size();
		}
		
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return files[position];
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			//ImageView imageView = new ImageView(mContext.getApplicationContext());
				ImageView imageView;
				if (convertView == null) {
					imageView = new ImageView(mContext);
				}else{
					imageView = (ImageView) convertView;
				}
			    Bitmap bm = ICSUtil.scaleImage(IMAGE_WIDTH, IMAGE_HEIGHT, tFileList.get(position).toString());
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(new GridView.LayoutParams(IMAGE_WIDTH, IMAGE_HEIGHT));
				imageView.setImageBitmap(bm);
				return imageView;			
			}
	}
	
}

