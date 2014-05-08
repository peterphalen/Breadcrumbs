package com.peter.breadcrumbs;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
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

	  HashMap<String, Integer> GetBreadcrumbFromMarkerMap = new HashMap<String, Integer>();
	  double clickedLatitude;
	  double clickedLongitude;
	  double draggedLatitude;
	  double draggedLongitude;
	  	  
	  private Menu menu;
	  String MAP_TYPE_KEY;
	  
	  String DELETE_ALL_QUESTION_TEXT;
	  String OKAY_TEXT;
	  String CANCEL_TEXT;
	  String INFO_BOX_TEXT;
	  String REQUEST_SAT_VIEW_TEXT;
	  String REQUEST_ROAD_VIEW_TEXT;
	  boolean VIEW_MAP_PRESSED = false;
	  boolean DROP_BREADCRUMB_PRESSED = false;
	  Integer INT_SHOW_THIS_LATITUDE;
	  Integer INT_SHOW_THIS_LONGITUDE;
	  String AUTO_GENERATED_BREADCRUMB_LABEL;
	  int breadcrumbCount;
	  int CLICKED_BREADCRUMB_LATITUDE_INT;
	  int CLICKED_BREADCRUMB_LONGITUDE_INT;
	  boolean THERE_ARE_BREADCRUMBS_ON_MAP;
	  int editBreadcrumbId = -8;
	  int thisMarkerBreadcrumbId = -9;
	  String markerId = null;
	  Tracker tracker;
	  LatLng draggedMarkerPosition;
	  private AdView adView;
      int mapTypeNormal = GoogleMap.MAP_TYPE_NORMAL;
      int mapTypeHybrid = GoogleMap.MAP_TYPE_HYBRID;
      int mapType = mapTypeNormal;

      private Marker lastMarker = null;
      
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_breadcrumb_map);
        		  
	    //get MapFragment
	    map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1))
		        .getMap();
		  tracker = ((MyApplication) getApplication()).getTracker(
  				  MyApplication.TrackerName.APP_TRACKER);
	    //get String resources
		  Resources res = getResources();
		  DELETE_ALL_QUESTION_TEXT = res.getString(R.string.delete_all_question);
		  OKAY_TEXT = res.getString(R.string.okay);
		  CANCEL_TEXT = res.getString(R.string.cancel);
		  INFO_BOX_TEXT = res.getString(R.string.info_box_instructions);
		  REQUEST_SAT_VIEW_TEXT = res.getString(R.string.sat_view);
		  REQUEST_ROAD_VIEW_TEXT = res.getString(R.string.road_view);
		  AUTO_GENERATED_BREADCRUMB_LABEL = res.getString(R.string.auto_generated_breadcrumb_label);
		    
		  
		    //get lat/lang pair to zoom to
			Bundle extras = getIntent().getExtras();
			INT_SHOW_THIS_LATITUDE = extras.getInt("INT_SHOW_THIS_LATITUDE");
			INT_SHOW_THIS_LONGITUDE = extras.getInt("INT_SHOW_THIS_LONGITUDE");
			VIEW_MAP_PRESSED = extras.getBoolean("VIEW_MAP_PRESSED");
			THERE_ARE_BREADCRUMBS_ON_MAP = extras.getBoolean("THERE_ARE_BREADCRUMBS_ON_MAP");
			DROP_BREADCRUMB_PRESSED = extras.getBoolean("DROP_BREADCRUMB_PRESSED");
			
			// Look up the AdView as a resource and load a request.
			 	        adView = (AdView)this.findViewById(R.id.adView2);
			 			AdRequest adRequest = new AdRequest.Builder()
			  	        .build();
			 	        adView.loadAd(adRequest);


			  		 adView.setAdListener(new AdListener() {
					        @Override
					        public void onAdOpened() {
					    		tracker.send(new HitBuilders.EventBuilder()
					                .setCategory("Ad Click")
					                .setAction("Map Activity")
					                .build());}
					        
					    });
			  	        
		    if(map != null){
		    	map.setMapType(mapType);
			    map.setPadding(0, 0, 0, 70);
		    }
		    
		    if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
	    		  // call something for API Level 11+
	    		ActionBar actionBar = getActionBar();
	    		actionBar.setDisplayHomeAsUpEnabled(true);}
		    

	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
		// Check map type and allow user to click to the alternative
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.breadcrumb_map, menu);
        MenuItem MapMenuItem = menu.findItem(R.id.mapType);
        if(mapType == mapTypeNormal){
        	MapMenuItem.setTitle(REQUEST_SAT_VIEW_TEXT);
        }
        if(mapType == mapTypeHybrid){
        	MapMenuItem.setTitle(REQUEST_ROAD_VIEW_TEXT);
        }		this.menu = menu;
 	   	return super.onCreateOptionsMenu(menu);
	}

	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		//this script gives you the maptypes and delete all menu options
        MenuItem mapMenuTitle = menu.findItem(R.id.mapType);
        switch (item.getItemId())
        {
        case R.id.mapType:
        	if (map.getMapType() == mapTypeHybrid){
        	mapType = mapTypeNormal;
    
            mapMenuTitle.setTitle(REQUEST_SAT_VIEW_TEXT);
            }
        	if (map.getMapType() == mapTypeNormal){
        		mapType = mapTypeHybrid;
                mapMenuTitle.setTitle(REQUEST_ROAD_VIEW_TEXT);
        	}
        	map.setMapType(mapType);

            return true;

        case R.id.delete_all:
            // Single menu item is selected do something
            // Ex: launching new activity/screen or show alert message
        	AlertDialog.Builder delete_alertbox = new AlertDialog.Builder(BreadcrumbMap.this);

            // set the message to display
            delete_alertbox.setMessage(DELETE_ALL_QUESTION_TEXT);

            // set a positive/yes button and create a listener
            delete_alertbox.setPositiveButton(OKAY_TEXT, new DialogInterface.OnClickListener() {

            	
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {   

               if (THERE_ARE_BREADCRUMBS_ON_MAP == true) {
            	db = new DatabaseHandler(getApplicationContext());

              	db.deleteAllBreadcrumbs();
              	THERE_ARE_BREADCRUMBS_ON_MAP = false;
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
            if (THERE_ARE_BREADCRUMBS_ON_MAP == true) {

            delete_alertbox.show();
            }
        	return true;
 
        default:
            return super.onOptionsItemSelected(item);

        }
        
    }    

	  @Override
	  public void onStart() {
	    super.onStart();
        db = new DatabaseHandler(this);
        breadcrumbCount = db.getBreadcrumbsCount();
        if (breadcrumbCount == 0){
			THERE_ARE_BREADCRUMBS_ON_MAP = false;
        }else{
			THERE_ARE_BREADCRUMBS_ON_MAP = true;
			if (map != null && VIEW_MAP_PRESSED == true){
				map.setMyLocationEnabled(true);
			}
        }
	    //Get an Analytics tracker to report app starts & uncaught exceptions etc.
	    GoogleAnalytics.getInstance(this).reportActivityStart(this);	    
	    }
	  
	
	@SuppressLint("NewApi")
	protected void onResume(){
		super.onResume();
		
		if (map != null){
	    map.clear();
	    if (db == null){
	        db = new DatabaseHandler(this);
	    }
	    breadcrumbs = db.getAllBreadcrumbs();
	    
	    if (breadcrumbs != null) {
		//get markers for each breadcrumb
		    Marker allbreadcrumblocations = null;

        for (Breadcrumb brd : breadcrumbs) {
			        allbreadcrumblocations = map.addMarker(new MarkerOptions()
			          .position(new LatLng((brd.getBreadcrumbLatitude()/1E6), (brd.getBreadcrumbLongitude())/1E6))
			          .title(brd.getLabel())
			          .snippet(INFO_BOX_TEXT)
			          .draggable(true)
			          .icon(BitmapDescriptorFactory
			              .fromResource(R.drawable.red_dot)));
			        
			        //Set up hashmap to associate markers with breadcrumbs
			        String allbreadcrumblocationsID = allbreadcrumblocations.getId();
			        thisMarkerBreadcrumbId = brd.getId();
			        GetBreadcrumbFromMarkerMap.put(allbreadcrumblocationsID, thisMarkerBreadcrumbId);
			        
			        //If editBreadcrumbId is not set by onactivityresult then just show all the info windows
			        if (editBreadcrumbId == -8){
					       allbreadcrumblocations.showInfoWindow();
			        }else{
				        //if the marker we're on matches the editBreadcrumbId then show it's info window
			        	if (thisMarkerBreadcrumbId == editBreadcrumbId){
						       allbreadcrumblocations.showInfoWindow();
			        	}
			        }
		
			        }
        


	    if( VIEW_MAP_PRESSED == true && THERE_ARE_BREADCRUMBS_ON_MAP == true){  
	    		//if view map was just pressed and there are breadcrumbs zoom to latest one
	    	Breadcrumb LatestBreadcrumb = breadcrumbs.get(breadcrumbCount-1);
	    	SHOW_THIS_LATITUDE = ((LatestBreadcrumb.getBreadcrumbLatitude())/1E6);
		    SHOW_THIS_LONGITUDE = ((LatestBreadcrumb.getBreadcrumbLongitude())/1E6);
	    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));
	    	
	    	// Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);

	    }
	    else{
	    	
			if (INT_SHOW_THIS_LATITUDE != null && INT_SHOW_THIS_LONGITUDE != null){
				SHOW_THIS_LATITUDE = INT_SHOW_THIS_LATITUDE/1E6;
				SHOW_THIS_LONGITUDE = INT_SHOW_THIS_LONGITUDE/1E6;
	    	
		//if there are breadcurmbs on map and Drop a Breadcrumb was pressed, zoom to the latest breadcrumb
	    if( DROP_BREADCRUMB_PRESSED == true && THERE_ARE_BREADCRUMBS_ON_MAP == true){
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

    	
	    // Zoom in, animating the camera.
	    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
	    }
	    
		//If there are no breadcrumbs in the db amd View Map was pressed to get here
		//show the latest location of the user at a lower zoom		    
	    if( VIEW_MAP_PRESSED == true && THERE_ARE_BREADCRUMBS_ON_MAP == false){
	    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

		    // Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
		    }
	    	}
	    }
	    }
        
     
        
        
		//when you click an infowindow it references the hashmap
		//to get the breadcrumb id, opens EditLabel activity
		//and sends the breadcrumb id to the activity as an extra
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
        	@Override
        	public void onInfoWindowClick(Marker marker) { 
                markerId = marker.getId();
      			int breadcrumbId = GetBreadcrumbFromMarkerMap.get(markerId);
      			Intent intent = new Intent(getApplicationContext(), EditLabel.class);
      			intent.putExtra("breadcrumbId", breadcrumbId);
    			editBreadcrumbId = breadcrumbId;
      	        startActivityForResult(intent, 1);     				};
					}
        		);
	    	
	
    map.setOnMapLongClickListener(this);
    map.setOnMarkerDragListener(this);
    


    // When you click a marker, clicking it again opens the associated Breadcrumb
    map.setOnMarkerClickListener(new OnMarkerClickListener() {
    	@Override
    	public boolean onMarkerClick(Marker marker) {
    		
    		if (lastMarker == null){
    			lastMarker = marker;
    		}else{
            if (marker.getId().equals(lastMarker.getId()) ) {
            	lastMarker = marker;
                    markerId = marker.getId();
        			int breadcrumbId = GetBreadcrumbFromMarkerMap.get(markerId);
        			Intent intent = new Intent(getApplicationContext(), EditLabel.class);
        			editBreadcrumbId = breadcrumbId;
        			intent.putExtra("breadcrumbId", breadcrumbId);
        	        startActivityForResult(intent, 1);
            	}else{
            		lastMarker = marker;
            	}
            }
            
            
    		return false;
    	};
    }
    );
    
		}
		
    adView.resume();
    
	}
	
	// When you finish dragging the marker, update the SQlite database
	// with the new location
	@Override
	public void onMarkerDragEnd(Marker marker) {
		//get marker ID via hashmap
        String draggedMarkerId = marker.getId();

		int draggedBreadcrumbId = GetBreadcrumbFromMarkerMap.get(draggedMarkerId);
		//get marker end position and adjust breadcrumb accordingly
		draggedMarkerPosition = marker.getPosition();
		draggedLatitude = draggedMarkerPosition.latitude;
		draggedLongitude = draggedMarkerPosition.longitude;
		int DRAGGED_BREADCRUMB_LATITUDE_INT = (int)(draggedLatitude * 1E6);
        int DRAGGED_BREADCRUMB_LONGITUDE_INT = (int)(draggedLongitude * 1E6);
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
        CLICKED_BREADCRUMB_LATITUDE_INT = (int)(clickedLatitude * 1E6);
        CLICKED_BREADCRUMB_LONGITUDE_INT = (int)(clickedLongitude * 1E6);
        db.addBreadcrumb(new Breadcrumb(CLICKED_BREADCRUMB_LATITUDE_INT, CLICKED_BREADCRUMB_LONGITUDE_INT, label));
    	THERE_ARE_BREADCRUMBS_ON_MAP = true;

        map.clear();
        
   	 breadcrumbs = db.getAllBreadcrumbs();

	    if (breadcrumbs != null) {
		//get markers for each breadcrumb
        for (Breadcrumb brd : breadcrumbs) {
			        Marker allbreadcrumblocations = map.addMarker(new MarkerOptions()
			          .position(new LatLng((brd.getBreadcrumbLatitude()/1E6), (brd.getBreadcrumbLongitude())/1E6))
			          .title(brd.getLabel())
			          .snippet(INFO_BOX_TEXT)
			          .draggable(true)
			          .icon(BitmapDescriptorFactory
			              .fromResource(R.drawable.red_dot)));
			        
			        ///////Set up hashmap to hold marker ids and breacdrumb ids
			        String allbreadcrumblocationsID = allbreadcrumblocations.getId();
			        thisMarkerBreadcrumbId = brd.getId();
			        GetBreadcrumbFromMarkerMap.put(allbreadcrumblocationsID, thisMarkerBreadcrumbId);
			        
					       allbreadcrumblocations.showInfoWindow();

			          		}

        }
    }
    
    
    //Give me back the breadcrumbId I sent you
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
            	Bundle extras = data.getExtras();
            	editBreadcrumbId = extras.getInt("EDIT_BREADCRUMB_ID");            	
                // Do something with the contact here (bigger example below)
            }
        }
    }
    
	  @Override
	  public void onPause() {
		    adView.pause();

	    super.onPause();
	    
	  }



	@Override
	public void onMarkerDrag(Marker arg0) {		
	}


	@Override
	public void onMarkerDragStart(Marker marker) {
		marker.showInfoWindow();
		
	}

	
	  @Override
	  public void onStop() {
	    
	    VIEW_MAP_PRESSED = false;
		DROP_BREADCRUMB_PRESSED = false;
      db.close();

    //Stop the analytics tracking
	    GoogleAnalytics.getInstance(this).reportActivityStop(this);	  	    
	    super.onStop();

	  }
		
	}	

