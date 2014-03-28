package com.detimil.breadcrumbs1;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
	  	    
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_breadcrumb_map);
	    
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

			SHOW_THIS_LATITUDE = INT_SHOW_THIS_LATITUDE/1e6;
			SHOW_THIS_LONGITUDE = INT_SHOW_THIS_LONGITUDE/1e6;
		    if(map != null && db.getAllBreadcrumbs() != null ){
		    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 10));

		    // Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
		    }
		    
		    if(map != null && db.getAllBreadcrumbs() == null){
		    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(SHOW_THIS_LATITUDE, SHOW_THIS_LONGITUDE), 6));

			    // Zoom in, animating the camera.
			    map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		    }
		    db.close();

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
			          .snippet("Click this box for more!")
			          .icon(BitmapDescriptorFactory
			              .fromResource(R.drawable.red_dot)));
			        idMarkerMap.put(allbreadcrumblocations.getId(), brd.getId());
			          allbreadcrumblocations.showInfoWindow();
			          db.close();
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

