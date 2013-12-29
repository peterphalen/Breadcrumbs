package com.detimil.breadcrumbs1;


import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

@SuppressLint("NewApi")
public class DroppedCrumb extends Activity {
	  private GoogleMap map;
	  double BREADCRUMB_LATITUDE;
	  double BREADCRUMB_LONGITUDE;
	  
	  protected void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.activity_dropped_crumb);
		    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
		        .getMap();
	        DatabaseHandler db = new DatabaseHandler(this);
		    
		    List<Breadcrumb> breadcrumbs = db.getAllBreadcrumbs();
		    int breadcrumbsCount = breadcrumbs.size()-1;
		    BREADCRUMB_LATITUDE = ((breadcrumbs.get(breadcrumbsCount).getBreadcrumbLatitude())/1e6);
		    BREADCRUMB_LONGITUDE = ((breadcrumbs.get(breadcrumbsCount).getBreadcrumbLongitude())/1e6);

		    
	        for (Breadcrumb brd : breadcrumbs) {
				        Marker allbreadcrumblocations = map.addMarker(new MarkerOptions()
				          .position(new LatLng((brd.getBreadcrumbLatitude()/1e6), (brd.getBreadcrumbLongitude())/1e6))
				          .title("Breadcrumb")
				          .snippet("This location saved! Collect breadcrumbs to get back.")
				          .icon(BitmapDescriptorFactory
				              .fromResource(R.drawable.ic_launcher)));
	        }
				        
					    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(BREADCRUMB_LATITUDE, BREADCRUMB_LONGITUDE), 10));

					    // Zoom in, animating the camera.
					    map.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
				    	
			  }
			  
		}
