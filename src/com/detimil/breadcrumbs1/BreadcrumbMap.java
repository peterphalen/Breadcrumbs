package com.detimil.breadcrumbs1;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
	  double BREADCRUMB_LATITUDE;
	  double BREADCRUMB_LONGITUDE;
	  DatabaseHandler db = new DatabaseHandler(this);
	  HashMap<String, Integer> idMarkerMap = new HashMap<String, Integer>();
	    
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_breadcrumb_map);	      

	    //All breadcrumbs to list, and get the lat lang of most recent
    	List<Breadcrumb> breadcrumbs = db.getAllBreadcrumbs();
	    int breadcrumbsCount = breadcrumbs.size()-1;
	    BREADCRUMB_LATITUDE = ((breadcrumbs.get(breadcrumbsCount).getBreadcrumbLatitude())/1e6);
	    BREADCRUMB_LONGITUDE = ((breadcrumbs.get(breadcrumbsCount).getBreadcrumbLongitude())/1e6);
	    
	    //get MapFragment
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map1))
		        .getMap();
		    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

		//get markers for each breadcrumb
        for (Breadcrumb brd : breadcrumbs) {
			        Marker allbreadcrumblocations = map.addMarker(new MarkerOptions()
			          .position(new LatLng((brd.getBreadcrumbLatitude()/1e6), (brd.getBreadcrumbLongitude())/1e6))
			          .title(brd.getLabel())
			          .snippet("Click here to rename me!")
			          .icon(BitmapDescriptorFactory
			              .fromResource(R.drawable.red_dot)));
			        idMarkerMap.put(allbreadcrumblocations.getId(), brd.getId());

        }
        
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

        	@Override
        	public void onInfoWindowClick(Marker marker) {
            String markerId = marker.getId();
			int breadcrumbId = idMarkerMap.get(markerId);
			Intent intent = new Intent(getApplicationContext(), EditLabel.class);
			intent.putExtra("breadcrumbId", breadcrumbId);
	        startActivity(intent);
        	};
			});
   
        
			        
				    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(BREADCRUMB_LATITUDE, BREADCRUMB_LONGITUDE), 10));

				    // Zoom in, animating the camera.
				    map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
			    	
		  }
}	

