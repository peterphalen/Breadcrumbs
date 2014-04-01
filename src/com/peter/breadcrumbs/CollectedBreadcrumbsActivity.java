package com.peter.breadcrumbs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


	

@SuppressLint("NewApi")
public class CollectedBreadcrumbsActivity extends Activity  {

	ListView listview;
	DatabaseHandler db;	
	SimpleCursorAdapter adapter; 
	Cursor cursor;
	
	  String DELETE_ALL_QUESTION_TEXT;
	  String OKAY_TEXT;
	  String CANCEL_TEXT;
	  String DELETE_TEXT;
	  String OPTIONS_TEXT;
	
	    
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collected_breadcrumbs);
        listview = (ListView) findViewById(android.R.id.list);


        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder()
        .build();
        adView.loadAd(adRequest);
        
		  Resources res = getResources();
		  DELETE_ALL_QUESTION_TEXT = res.getString(R.string.delete_all_question);
		  OKAY_TEXT = res.getString(R.string.okay);
		  CANCEL_TEXT = res.getString(R.string.cancel);	
		  DELETE_TEXT = res.getString(R.string.delete_button_label);		
		  OPTIONS_TEXT = res.getString(R.string.options);		

    }
    
    @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Google analytics.
	  }
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.collected_breadcrumbs, menu);
		
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
        case R.id.delete_all:
            // Single menu item is selected do something
            // Ex: launching new activity/screen or show alert message

        	AlertDialog.Builder delete_alertbox = new AlertDialog.Builder(CollectedBreadcrumbsActivity.this);
            
            // set the message to display
            delete_alertbox.setMessage(DELETE_ALL_QUESTION_TEXT);

            // set a positive/yes button and create a listener
            delete_alertbox.setPositiveButton(OKAY_TEXT, new DialogInterface.OnClickListener() {

                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {   
               db = new DatabaseHandler(getApplicationContext());

               if (listview.getAdapter().getCount() > 0) {
              	db.deleteAllBreadcrumbs();
              	cursor = db.getAllLabels();
                adapter.changeCursor(cursor);}
                }});
            
            // set a negative/no button and create a listener
            delete_alertbox.setNegativeButton(CANCEL_TEXT, new DialogInterface.OnClickListener() {

                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            if (listview.getAdapter().getCount() > 0) {

            delete_alertbox.show();
            }
        	return true;
 
        default:
            return super.onOptionsItemSelected(item);
        }
    }    
    
    @Override
    protected void onResume() {
    	super.onResume();

    	db = new DatabaseHandler(this);

        cursor = db.getAllLabels();

        adapter = new SimpleCursorAdapter(this,
        		android.R.layout.simple_list_item_1,
        		cursor, 
        		new String[] { "label" },
        		new int[] { android.R.id.text1 }, 
        		0);
        listview.setAdapter(adapter);
                
    	
    this.listview.setOnItemClickListener(new OnItemClickListener() {

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
	
    	int _id = (int) id;
    	
    if (listview.getAdapter().getCount() > 0) {
    	
    	Breadcrumb breadcrumb = db.getBreadcrumb(_id);
    	
    	final Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "&daddr=" + breadcrumb.getBreadcrumbLatitude()/1e6 + "," + breadcrumb.getBreadcrumbLongitude()/1e6));
        startActivity(intent);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");}}});
    
    this.listview.setOnItemLongClickListener(new OnItemLongClickListener() {

    public boolean onItemLongClick(AdapterView<?> l, View v, final int position, long id) {
    	
    	final int _id = (int) id;

    	
    	AlertDialog.Builder alertbox = new AlertDialog.Builder(CollectedBreadcrumbsActivity.this);
        
    	Breadcrumb breadcrumb = db.getBreadcrumb(_id);
    	String breadcrumbLabel = breadcrumb.getLabel();
    	
    	alertbox.setMessage("'" + breadcrumbLabel +"'");
    	
        // set a rename button and create a listener
        alertbox.setPositiveButton(OPTIONS_TEXT, new DialogInterface.OnClickListener() {

            // open EditLabel activity and pass it the selected bcrumb ID to allow rename 
            public void onClick(DialogInterface arg0, int arg1) {
            	
                if (listview.getAdapter().getCount() > 0) {

            	Intent intent = new Intent(getApplicationContext(), EditLabel.class);
    			intent.putExtra("breadcrumbId", _id);
    	        startActivity(intent);
                }

            }
        });
    	
        // set a delete button and create a listener
        alertbox.setNegativeButton(DELETE_TEXT, new DialogInterface.OnClickListener() {
        	

            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {                
                if (listview.getAdapter().getCount() > 0) {
                	

                	Breadcrumb breadcrumb = db.getBreadcrumb(_id);
                    db.deleteBreadcrumb(breadcrumb);
                    cursor = db.getAllLabels();
                    adapter.changeCursor(cursor);
                    }
                
;            }
        });



        alertbox.show();

        return true;
        }
    });

}
    
    @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Google analytics.
	  }

}
