package com.detimil.breadcrumbs1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


	

@SuppressLint("NewApi")
public class CollectedBreadcrumbsActivity extends Activity  {

	ListView listview;
	DatabaseHandler db;
	SimpleCursorAdapter adapter; 
	Cursor cursor;
	
	private static final String TAG="CursorLoader";

	
	    
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collected_breadcrumbs);
        listview = (ListView) findViewById(android.R.id.list);

        
		
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
        Log.v(TAG,"ListAdapter is set");
        
    	
    this.listview.setOnItemClickListener(new OnItemClickListener() {

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
	
    	int _id = (int) id;
    	
    if (listview.getAdapter().getCount() > 0) {
    	
    	Breadcrumb breadcrumb = db.getBreadcrumb(_id);
    	Log.d("HelloListView", "You shortclicked Item: " + id + " at position:" + position);
    	Log.d("HelloListView", "Number of items in adapter:" + listview.getAdapter().getCount());
    	Log.d("HelloListView", "Number of items in database:" + db.getBreadcrumbsCount());

    	final Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "&daddr=" + breadcrumb.getBreadcrumbLatitude()/1e6 + "," + breadcrumb.getBreadcrumbLongitude()/1e6));
        startActivity(intent);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");}}});
    
    this.listview.setOnItemLongClickListener(new OnItemLongClickListener() {

    public boolean onItemLongClick(AdapterView<?> l, View v, final int position, long id) {
    	Log.d("HelloListView", "You long clicked Item: " + id + " at position:" + position);
    	Log.d("HelloListView", "Number of items in adapter:" + listview.getAdapter().getCount());
    	Log.d("HelloListView", "Number of items in database:" + db.getBreadcrumbsCount());
    	final int _id = (int) id;

    	
    	AlertDialog.Builder alertbox = new AlertDialog.Builder(CollectedBreadcrumbsActivity.this);
        
    	Breadcrumb breadcrumb = db.getBreadcrumb(_id);
    	String breadcrumbLabel = breadcrumb.getLabel();
    	alertbox.setMessage(breadcrumbLabel);
    	
    	Log.d("HelloListView", "You long clicked breadcrumb at position: " + position + " with SQL id :" + breadcrumb.getId());

    	
        // set a rename button and create a listener
        alertbox.setPositiveButton("Rename", new DialogInterface.OnClickListener() {

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
        alertbox.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
        	

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

}
