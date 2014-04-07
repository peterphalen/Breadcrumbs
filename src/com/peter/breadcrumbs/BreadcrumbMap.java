package com.peter.breadcrumbs;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class BreadcrumbMap extends Activity {
	  private GoogleMap map;
	  double SHOW_THIS_LATITUDE;
	  double SHOW_THIS_LONGITUDE;
	  DatabaseHandler db;
	  List <Breadcrumb> breadcrumbs;
	  HashMap<String, Integer> idMarkerMap = new HashMap<String, Integer>();
	  //if this bool is set to true by an intent, zoom to the bounds of all markers
	  boolean ZOOM_TO_ALL_BREADCRUMBS = false;
	  LatLngBounds bounds;

	  
	  String DELETE_ALL_QUESTION_TEXT;
	  String OKAY_TEXT;
	  String CANCEL_TEXT;
	  String INFO_BOX_TEXT;
	  	    
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_breadcrumb_map);
	    

		  Resources res = getResources();
		  DELETE_ALL_QUESTION_TEXT = res.getString(R.string.delete_all_question);
		  OKAY_TEXT = res.getString(R.string.okay);
		  CANCEL_TEXT = res.getString(R.string.cancel);
		  INFO_BOX_TEXT = res.getString(R.string.info_box_instructions);
	    
	    //get MapFragment
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map1))
		        .getMap();
		    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		    map.setPadding(0, 0, 0, 60);
		    DatabaseHandler db = new DatabaseHandler(this);

		    breadcrumbs = db.getAllBreadcrumbs();
		    
			Bundle extras = getIntent().getExtras();
			int INT_SHOW_THIS_LATITUDE = extras.getInt("INT_SHOW_THIS_LATITUDE");
			int INT_SHOW_THIS_LONGITUDE = extras.getInt("INT_SHOW_THIS_LONGITUDE");
			boolean ZOOM_TO_ALL_BREADCRUMBS = extras.getBoolean("ZOOM_TO_ALL_BREADCRUMBS");

			
			SHOW_THIS_LATITUDE = INT_SHOW_THIS_LATITUDE/1e6;
			SHOW_THIS_LONGITUDE = INT_SHOW_THIS_LONGITUDE/1e6;
			
			//If the map has been generated and ZOOM_TO_ALL_BREADCRUMBS bool is false
			//show the latest breadcrumb, or if there's just one breadcrumb do the same
		    if((map != null && breadcrumbs != null && ZOOM_TO_ALL_BREADCRUMBS == false) || 
		    		( map != null && db.getBreadcrumbsCount() == 1 && ZOOM_TO_ALL_BREADCRUMBS == true )){
		    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

		    // Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
		    }
		    
			//If the map has been generated and there are no breadcrumbs in the db
			//show the latest location		    
		    if(map != null && db.getBreadcrumbsCount() == 0){
		    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

			    // Zoom in, animating the camera.
			    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
		    }
		    db.close();

	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.breadcrumb_map, menu);
		
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
        	AlertDialog.Builder delete_alertbox = new AlertDialog.Builder(BreadcrumbMap.this);
            db = new DatabaseHandler(getApplicationContext());

            // set the message to display
            delete_alertbox.setMessage(DELETE_ALL_QUESTION_TEXT);

            // set a positive/yes button and create a listener
            delete_alertbox.setPositiveButton(OKAY_TEXT, new DialogInterface.OnClickListener() {

                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {   

               if (db.getBreadcrumbsCount() > 0) {

              	db.deleteAllBreadcrumbs();
        	    map.clear();}
                }});
            
            // set a negative/no button and create a listener
            delete_alertbox.setNegativeButton(CANCEL_TEXT, new DialogInterface.OnClickListener() {

                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });

            

            if (db.getBreadcrumbsCount() > 0) {

            delete_alertbox.show();
            db.close();
            }
        	return true;
 
        default:
            return super.onOptionsItemSelected(item);
        }
    }    

	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Google analytics.
	    
	    }
	  
	
	@SuppressLint("NewApi")
	protected void onResume(){
		super.onResume();
	    map.clear();
	    DatabaseHandler db = new DatabaseHandler(this);

	    breadcrumbs = db.getAllBreadcrumbs();
	    final int breadcrumbCount = db.getBreadcrumbsCount();
	    
	    if (breadcrumbs != null) {
		//get markers for each breadcrumb
        for (Breadcrumb brd : breadcrumbs) {
			        Marker allbreadcrumblocations = map.addMarker(new MarkerOptions()
			          .position(new LatLng((brd.getBreadcrumbLatitude()/1e6), (brd.getBreadcrumbLongitude())/1e6))
			          .title(brd.getLabel())
			          .snippet(INFO_BOX_TEXT)
			          .icon(BitmapDescriptorFactory
			              .fromResource(R.drawable.red_dot)));
			        idMarkerMap.put(allbreadcrumblocations.getId(), brd.getId());
			          allbreadcrumblocations.showInfoWindow();
			          db.close();
			          
			    	  //If the map has been generated and ZOOM_TO_ALL_BREADCRUMBS bool is true
			  		// and there's more than one bcrumb set bounds to show all breadcrumbs
			  	    if(map != null && ZOOM_TO_ALL_BREADCRUMBS == true && breadcrumbCount > 1 ){
			  	    	LatLngBounds.Builder builder = new LatLngBounds.Builder();
			  	    	    builder.include(allbreadcrumblocations.getPosition());
			  	    	bounds = builder.build();
			  	    	}
			          
        }
        
        if( map != null && ZOOM_TO_ALL_BREADCRUMBS == true && breadcrumbCount > 1 ){
        
               map.fitBounds(bounds);
        }
     
        
        
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

        	@Override
        	public void onInfoWindowClick(Marker marker) {
            String markerId = marker.getId();
			int breadcrumbId = idMarkerMap.get(markerId);
			Intent intent = new Intent(getApplicationContext(), EditLabel.class);
			intent.putExtra("breadcrumbId", breadcrumbId);
	        startActivityForResult(intent, 0);
        				};
					}
        		);
	    	}
		  }
	
	  
	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Google analytics.
	  }
}	

