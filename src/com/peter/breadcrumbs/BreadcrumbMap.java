package com.peter.breadcrumbs;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


@SuppressLint("NewApi")
public class BreadcrumbMap extends FragmentActivity implements OnMapLongClickListener, OnMarkerDragListener{
	  private GoogleMap map;
	  double SHOW_THIS_LATITUDE;
	  double SHOW_THIS_LONGITUDE;
	  DatabaseHandler db;
	  List <Breadcrumb> breadcrumbs;
	  HashMap<String, Integer> idMarkerMap = new HashMap<String, Integer>();
	  SharedPreferences sharedpreferences;
	  
	  double clickedLatitude;
	  double clickedLongitude;
	  double draggedLatitude;
	  double draggedLongitude;
	  	  
	  private Menu menu;
	  String MAP_TYPE_KEY;
	  String MAP_TYPE_NORMAL;
	  String MAP_TYPE_HYBRID;
	  
	  String DELETE_ALL_QUESTION_TEXT;
	  String OKAY_TEXT;
	  String CANCEL_TEXT;
	  String INFO_BOX_TEXT;
	  String REQUEST_SAT_VIEW_TEXT;
	  String REQUEST_ROAD_VIEW_TEXT;
	  boolean VIEW_MAP_PRESSED_AND_BREADCRUMBS_STORED = false;
	  boolean VIEW_MAP_PRESSED_AND_BREADCRUMBS_NOT_STORED = false;
	  boolean DROP_BREADCRUMB_PRESSED = false;
	  Integer INT_SHOW_THIS_LATITUDE;
	  Integer INT_SHOW_THIS_LONGITUDE;
	  String AUTO_GENERATED_BREADCRUMB_LABEL;
	  int breadcrumbCount;
	  int CLICKED_BREADCRUMB_LATITUDE_INT;
	  int CLICKED_BREADCRUMB_LONGITUDE_INT;
	  
	  LatLng draggedMarkerPosition;

	  	    
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
		  REQUEST_SAT_VIEW_TEXT = res.getString(R.string.sat_view);
		  REQUEST_ROAD_VIEW_TEXT = res.getString(R.string.road_view);
		  AUTO_GENERATED_BREADCRUMB_LABEL = res.getString(R.string.auto_generated_breadcrumb_label);
		  
	    //get MapFragment
	    map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1))
		        .getMap();
	    	    	    
	    //get Shared prefs
	    sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

	    	
        db = new DatabaseHandler(this);

		    //get lat/lang pair to zoom to
			Bundle extras = getIntent().getExtras();
			INT_SHOW_THIS_LATITUDE = extras.getInt("INT_SHOW_THIS_LATITUDE");
			INT_SHOW_THIS_LONGITUDE = extras.getInt("INT_SHOW_THIS_LONGITUDE");
			VIEW_MAP_PRESSED_AND_BREADCRUMBS_STORED = extras.getBoolean("VIEW_MAP_PRESSED_AND_BREADCRUMBS_STORED");
			VIEW_MAP_PRESSED_AND_BREADCRUMBS_NOT_STORED = extras.getBoolean("VIEW_MAP_PRESSED_AND_BREADCRUMBS_NOT_STORED");
			DROP_BREADCRUMB_PRESSED = extras.getBoolean("DROP_BREADCRUMB_PRESSED");
			
			
	        // Look up the AdView as a resource and load a request.
	        AdView adView = (AdView)this.findViewById(R.id.adView2);
	        AdRequest adRequest = new AdRequest.Builder()
	        .build();
	        adView.loadAd(adRequest);

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
		    

            breadcrumbCount = db.getBreadcrumbsCount();

	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.breadcrumb_map, menu);
        MenuItem MapMenuItem = menu.findItem(R.id.mapType);
        if(map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
        	MapMenuItem.setTitle(REQUEST_SAT_VIEW_TEXT);
        }
        if(map.getMapType() == GoogleMap.MAP_TYPE_HYBRID){
        	MapMenuItem.setTitle(REQUEST_ROAD_VIEW_TEXT);
        }		this.menu = menu;
		return true; //return true because you want the delete all optio
	}

	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        MenuItem mapMenuTitle = menu.findItem(R.id.mapType);
    	int mapType = GoogleMap.MAP_TYPE_NORMAL; 

		//this script gives you the delete all optiosmenu option
        switch (item.getItemId())
        {
        case R.id.mapType:
        	if (map.getMapType() == GoogleMap.MAP_TYPE_HYBRID){
        	mapType = GoogleMap.MAP_TYPE_NORMAL;
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(MAP_TYPE_KEY, "MAP_TYPE_NORMAL");
            mapMenuTitle.setTitle(REQUEST_SAT_VIEW_TEXT);
            editor.apply(); 
            }
        	if (map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
        		mapType = GoogleMap.MAP_TYPE_HYBRID;
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(MAP_TYPE_KEY, "MAP_TYPE_HYBRID");
                mapMenuTitle.setTitle(REQUEST_ROAD_VIEW_TEXT);
                editor.apply(); 

        	}
        	map.setMapType(mapType);
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

               if (breadcrumbCount > 0) {

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
            if (breadcrumbCount > 0) {

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

	    breadcrumbs = db.getAllBreadcrumbs();
	    
	    if (breadcrumbs != null) {
		//get markers for each breadcrumb
        for (Breadcrumb brd : breadcrumbs) {
			        Marker allbreadcrumblocations = map.addMarker(new MarkerOptions()
			          .position(new LatLng((brd.getBreadcrumbLatitude()/1e6), (brd.getBreadcrumbLongitude())/1e6))
			          .title(brd.getLabel())
			          .snippet(INFO_BOX_TEXT)
			          .draggable(true)
			          .icon(BitmapDescriptorFactory
			              .fromResource(R.drawable.red_dot)));
			        // take all the marker ids and put them in a hashmap 
			        //that maps them to the associated breadcrumb id they mark
			        idMarkerMap.put(allbreadcrumblocations.getId(), brd.getId());
			          allbreadcrumblocations.showInfoWindow();
			          
			  	    	}

		if (INT_SHOW_THIS_LATITUDE != null && INT_SHOW_THIS_LONGITUDE != null){
		SHOW_THIS_LATITUDE = INT_SHOW_THIS_LATITUDE/1e6;
		SHOW_THIS_LONGITUDE = INT_SHOW_THIS_LONGITUDE/1e6;

	    if(map != null && VIEW_MAP_PRESSED_AND_BREADCRUMBS_STORED == true){  
	    		//if view map was just pressed and there are breadcrumbs zoom to latest one
	    	SHOW_THIS_LATITUDE = ((breadcrumbs.get(breadcrumbCount).getBreadcrumbLatitude())/1e6);
		    SHOW_THIS_LONGITUDE = ((breadcrumbs.get(breadcrumbCount).getBreadcrumbLongitude())/1e6);
	    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

		    // Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
	    }
	    else{
		//if map is null and there are breadcurmbs zoom to the latest breadcrumb
	    if(map != null && DROP_BREADCRUMB_PRESSED == true ){
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

	    // Zoom in, animating the camera.
	    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
	    }
	    
		//If the map has been generated and there are no breadcrumbs in the db
		//show the latest location of the user at a lower zoom		    
	    if(map != null && VIEW_MAP_PRESSED_AND_BREADCRUMBS_NOT_STORED == true){
	    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

		    // Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
		    }
	    	}
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
	        startActivity(intent);
        				};
					}
        		);
	    	
	
    map.setOnMapLongClickListener(this);
    map.setOnMarkerDragListener(this);
    
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub
		//get marker ID via hashmap
        String draggedMarkerId = marker.getId();
		int draggedBreadcrumbId = idMarkerMap.get(draggedMarkerId);
		//get marker end position and adjust breadcrumb accordingly
		draggedMarkerPosition = marker.getPosition();
		draggedLatitude = draggedMarkerPosition.latitude;
		draggedLongitude = draggedMarkerPosition.longitude;
		int DRAGGED_BREADCRUMB_LATITUDE_INT = (int)(draggedLatitude * 1e6);
        int DRAGGED_BREADCRUMB_LONGITUDE_INT = (int)(draggedLongitude * 1e6);
        db.newBreadcrumbPosition(draggedBreadcrumbId, DRAGGED_BREADCRUMB_LATITUDE_INT, DRAGGED_BREADCRUMB_LONGITUDE_INT);
	}
	
	
	//longclicking map adds a breadcrumb to it
    @Override
    public void onMapLongClick(LatLng point) {

        breadcrumbCount = db.getBreadcrumbsCount();
        //Auto generate breadcrumb label which will be "Breadcrumb "
        //In whatever language and its number in your database
        String label = (AUTO_GENERATED_BREADCRUMB_LABEL +" " + (breadcrumbCount+1) );
        clickedLatitude = point.latitude;
        clickedLongitude = point.longitude;
        CLICKED_BREADCRUMB_LATITUDE_INT = (int)(clickedLatitude * 1e6);
        CLICKED_BREADCRUMB_LONGITUDE_INT = (int)(clickedLongitude * 1e6);
        db.addBreadcrumb(new Breadcrumb(CLICKED_BREADCRUMB_LATITUDE_INT, CLICKED_BREADCRUMB_LONGITUDE_INT, label));
        
        map.clear();
	    
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
			          		}
        }
    }
	
	  @Override
	  public void onStop() {
	    super.onStop();
	    
	    VIEW_MAP_PRESSED_AND_BREADCRUMBS_STORED = false;
		VIEW_MAP_PRESSED_AND_BREADCRUMBS_NOT_STORED = false;
		DROP_BREADCRUMB_PRESSED = false;
        db.close();

	    EasyTracker.getInstance(this).activityStop(this);  // Google analytics.
	  }


	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Why is this method necessary?
		
	}


	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Why is this method necessary?
		
	}



		
	}	

