package com.detimil.breadcrumbs1;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
	

public class CollectedBreadcrumbsActivity extends Activity {

	private ListView listview;
	private DatabaseHandler db;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_collected_breadcrumbs);

        db = new DatabaseHandler(this);
        List<Breadcrumb> breadcrumbs = db.getAllBreadcrumbs();
        
        //Convert breadcrumbs list to array. Unneeded? Commented out...
        //String[] array = breadcrumbs.toArray(new String[breadcrumbs.size()]);
        
        listview = (ListView) findViewById(R.id.list);
        ArrayAdapter<Breadcrumb> adapter = new ArrayAdapter<Breadcrumb>(this,
                android.R.layout.simple_list_item_1, breadcrumbs);
            listview.setAdapter(adapter);
	TextView emptyText = (TextView)findViewById(android.R.id.empty);
            listview.setEmptyView(emptyText);

    this.listview.setOnItemClickListener(new OnItemClickListener() {

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {

	ArrayAdapter<Breadcrumb> adapter = (ArrayAdapter<Breadcrumb>) listview.getAdapter();
    if (listview.getAdapter().getCount() > 0) {
    	Breadcrumb breadcrumb = (Breadcrumb) listview.getAdapter().getItem(position);

    	final Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "&daddr=" + breadcrumb.getBreadcrumbLatitude()/1e6 + "," + breadcrumb.getBreadcrumbLongitude()/1e6));
        startActivity(intent);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");}}});
    
    this.listview.setOnItemLongClickListener(new OnItemLongClickListener() {

    public boolean onItemLongClick(AdapterView<?> l, View v, final int position, long id) {
    	Log.d("HelloListView", "You clicked Item: " + id + " at position:" + position);
    	Log.d("HelloListView", "Number of items in adapter:" + listview.getAdapter().getCount());
    	
    	AlertDialog.Builder alertbox = new AlertDialog.Builder(CollectedBreadcrumbsActivity.this);
        
        // set the message to display
        alertbox.setMessage("Delete Breadcrumb?");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {                
            	@SuppressWarnings("unchecked")
        		ArrayAdapter<Breadcrumb> adapter = (ArrayAdapter<Breadcrumb>) listview.getAdapter();
                if (listview.getAdapter().getCount() > 0) {
                	Breadcrumb breadcrumb = (Breadcrumb) listview.getAdapter().getItem(position);
                    db.deleteBreadcrumb(breadcrumb);
                    adapter.remove(breadcrumb);
                    }
                adapter.notifyDataSetChanged();
                
                
                
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });

        alertbox.show();

        return true;
        }
    });
}
}
