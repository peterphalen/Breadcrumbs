package com.peter.breadcrumbs;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class BreadcrumbMap extends Activity {
	  private GoogleMap map;
	  double SHOW_THIS_LATITUDE;
	  double SHOW_THIS_LONGITUDE;
	  DatabaseHandler db;
	  List <Breadcrumb> breadcrumbs;
	  HashMap<String, Integer> idMarkerMap = new HashMap<String, Integer>();
	  SharedPreferences sharedpreferences;
	  
	  
	  private Menu menu;
	  String MAP_TYPE_KEY;
	  String MAP_TYPE_NORMAL;
	  String MAP_TYPE_HYBRID;
	  
	  String DELETE_ALL_QUESTION_TEXT;
	  String OKAY_TEXT;
	  String CANCEL_TEXT;
	  String INFO_BOX_TEXT;
	  String SAT_VIEW_TEXT;
	  String ROAD_VIEW_TEXT;
	  	    
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_breadcrumb_map);

	    //get String resources
		  Resources res = getResources();
		  DELETE_ALL_QUESTION_TEXT = res.getString(R.string.delete_all_question);
		  OKAY_TEXT = res.getString(R.string.okay);
		  CANCEL_TEXT = res.getString(R.string.cancel);
		  INFO_BOX_TEXT = res.getString(R.string.info_box_instructions);
		  SAT_VIEW_TEXT = res.getString(R.string.sat_view);
		  ROAD_VIEW_TEXT = res.getString(R.string.road_view);
  
	    
	    //get MapFragment
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map1))
		        .getMap();
	    
	    //get Shared prefs
	    sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

	    	
	    	DatabaseHandler db = new DatabaseHandler(this);

		    int breadcrumbCount = db.getBreadcrumbsCount();
		    
		    
		    //get lat/lang pair to zoom to
			Bundle extras = getIntent().getExtras();
			int INT_SHOW_THIS_LATITUDE = extras.getInt("INT_SHOW_THIS_LATITUDE");
			int INT_SHOW_THIS_LONGITUDE = extras.getInt("INT_SHOW_THIS_LONGITUDE");

			
			SHOW_THIS_LATITUDE = INT_SHOW_THIS_LATITUDE/1e6;
			SHOW_THIS_LONGITUDE = INT_SHOW_THIS_LONGITUDE/1e6;

		    if(map != null){
		    	String prefValue = sharedpreferences.getString(MAP_TYPE_KEY, MAP_TYPE_NORMAL);
		    	int mapType;
		    	if ("MAP_TYPE_HYBRID".equals(prefValue)) {
		    	    mapType = GoogleMap.MAP_TYPE_HYBRID;
		    	} else {
		    	    mapType = GoogleMap.MAP_TYPE_NORMAL;
		    	}
		    	map.setMapType(mapType);
			    map.setPadding(0, 0, 0, 60);
		    }
		    
			//if map is null and there are breadcurmbs zoom to the latest breadcrumb
		    if(map != null && breadcrumbCount > 0 ){
		    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

		    // Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
		    }
		    
			//If the map has been generated and there are no breadcrumbs in the db
			//show the latest location of the user at a lower zoom		    
		    if(map != null && breadcrumbCount == 0){
		    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

			    // Zoom in, animating the camera.
			    map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
		    }
		    db.close();

	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.breadcrumb_map, menu);
        MenuItem MapMenuItem = menu.findItem(R.id.mapType);
        if(map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
        	MapMenuItem.setTitle(SAT_VIEW_TEXT);
        }
        if(map.getMapType() == GoogleMap.MAP_TYPE_HYBRID){
        	MapMenuItem.setTitle(ROAD_VIEW_TEXT);
        }		this.menu = menu;
		return true; //return true because you want the delete all optio
	}

	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

		//this script gives you the delete all optiosmenu option
        switch (item.getItemId())
        {
        case R.id.mapType:
            MenuItem mapMenuTitle = menu.findItem(R.id.mapType);
        	if (map.getMapType() == GoogleMap.MAP_TYPE_HYBRID){
        	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(MAP_TYPE_KEY, "MAP_TYPE_NORMAL");
            mapMenuTitle.setTitle(SAT_VIEW_TEXT);
            editor.apply(); 
            
            }
        	if (map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
        		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(MAP_TYPE_KEY, "MAP_TYPE_HYBRID");
                editor.apply(); 
                mapMenuTitle.setTitle(ROAD_VIEW_TEXT);
        	}
            return true;
            
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
  
            //only show the "are you sure you want to delete?" alertbox
            //if there are breadcrumbs in the database
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
	    
	    if (breadcrumbs != null) {
		//get markers for each breadcrumb
        for (Breadcrumb brd : breadcrumbs) {
			        Marker allbreadcrumblocations = map.addMarker(new MarkerOptions()
			          .position(new LatLng((brd.getBreadcrumbLatitude()/1e6), (brd.getBreadcrumbLongitude())/1e6))
			          .title(brd.getLabel())
			          .snippet(INFO_BOX_TEXT)
			          .icon(BitmapDescriptorFactory
			              .fromResource(R.drawable.red_dot)));
			        // take all the marker ids and put them in a hashmap 
			        //that maps them to the associated breadcrumb id they mark
			        idMarkerMap.put(allbreadcrumblocations.getId(), brd.getId());
			          allbreadcrumblocations.showInfoWindow();
			          db.close();

			  	    	}
			          
        }
     
        
        
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
        		//when you click an infowindow it references the hashmap
        		//to get the breadcrumb id, opens EditLabel activity
        		//and sends the breadcrumb id to the activity as an extra
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
		  
	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Google analytics.
	  }
}	

