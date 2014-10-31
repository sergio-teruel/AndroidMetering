package com.ics.metering;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class IssueReading  extends Activity {
	boolean manualIssue;
	int issueId = 0;
	int readingId = 0;
	String issue = "";
	ListView listIssueDetail  = null;
	Cursor curIssueDetail;
	TextView txtIssueComment = null;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// TODO Put your code here
		setContentView(R.layout.issue_detail);
		listIssueDetail = (ListView) findViewById(R.id.lvIssueDetail);
		Bundle bundle = getIntent().getExtras();
		issueId = bundle.getInt("issueId");
		readingId = bundle.getInt("readingId");
		issue = bundle.getString("issue");
		manualIssue = bundle.getBoolean("manualIssue");

		setTitle(issue);
		
        curIssueDetail = AdaptadorDB.getRawQuery("SELECT id as _id,* FROM Issue_Detail WHERE issue_id = "+ issueId +" ORDER BY sequence");
        String[] fromIssuesDetail = new String[] {"name"};
        int[] toIssuesDetail = new int[] {android.R.id.text1};

        SimpleCursorAdapter mAdapterIssueDetail = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_checked, curIssueDetail, fromIssuesDetail, toIssuesDetail);
        listIssueDetail.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        startManagingCursor(curIssueDetail);
        listIssueDetail.setAdapter(mAdapterIssueDetail);
        
        txtIssueComment = (TextView) findViewById(R.id.txtIssueDetailComent);
	}
	
	public void saveIssueDetail(){
		ContentValues cv = new ContentValues();

		cv.put("issue_id", issueId);
		cv.put("issue_detail_id", curIssueDetail.getInt(curIssueDetail.getColumnIndex("id")));
		cv.put("issue_comment", txtIssueComment.getText().toString());
		if (manualIssue){
			cv.put("readed", 1);
			cv.put("date", new Date().toGMTString());
		}

		AdaptadorDB myAdaptadorDB = new AdaptadorDB(this);
		myAdaptadorDB.grabarLectura(cv, readingId);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_accept, menu);
		return true;  
	}  
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {  
	        case R.id.mnu_accept:
	        	if (listIssueDetail.getCheckedItemCount() > 0) {
	        		curIssueDetail.moveToPosition(listIssueDetail.getCheckedItemPosition());
	        		saveIssueDetail();
	        		finish();
	        	}else{
	        		msgDialog(R.string.msg_select_issue_detail_title);
	        	}
	        	break;
		  default:
			// put your code here	  
	    }  
	    return false;  
	}	
	private boolean msgDialog (int titleResource) {
		boolean result = false; 
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(titleResource);
		alertDialogBuilder
		.setMessage(R.string.msg_select_issue_detail)
		.setCancelable(false)

		.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
		return result;
	}
	
	@Override
	public void onBackPressed() {
		if (!manualIssue){
    		msgDialog(R.string.msg_select_issue_detail_title);
    	}else{
    		super.onBackPressed();
    	}
	}
}
