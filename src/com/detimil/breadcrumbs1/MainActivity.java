package com.detimil.breadcrumbs1;

import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity {
	private LocationManager locationManager;
	private String bestProvider;
	private Location lastKnownLocation;
	private Location mostCurrentLocation;
	private double BREADCRUMB_LATITUDE;
	private double BREADCRUMB_LONGITUDE;
	
	private static final String TAG = "MyActivity";
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
				
		// Get the location manager	
				locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Determine the device's best location provider, without worrying about
		// costs like battery life or response time.
		
		// TODO Decide what criteria are most important to this app. Probably response time..
		
		// TODO If there is no provider enabled we have to prompt the user to enable one.
				
				Criteria criteria = new Criteria();
				bestProvider = locationManager.getBestProvider(criteria, false);

		// Get the last known location from the provider
				
				lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);
			}
		
	
	protected void onResume(){
		super.onResume();
	  
		//Register locationListener to locationManager and start getting updates as to your current location

		MyCurrentLoctionListener locationListener = new MyCurrentLoctionListener();
		locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);

		
		//The following snippet says: if our locationListener hasn't had time to get our current 
		//location, find our lat and lang using our last known location. However, if we do have
		//an updated location by now, get our lat and lang from that. (The mostCurrentLocation
		//variable is defined in the LocationListener class below.)
		
	    if (mostCurrentLocation == null){
	    	BREADCRUMB_LATITUDE = lastKnownLocation.getLatitude();
	    	BREADCRUMB_LONGITUDE = lastKnownLocation.getLongitude();
	    }
	
	}
	
	//When we get an updated location through our location listener, receive the updated 
	//location and call a method that tells us what to do with the location. When I get a new location
	//here I just assign it to the variable mostCurrentLocation
	
	public class MyCurrentLoctionListener implements LocationListener{ 
	    @Override
	    public void onLocationChanged(Location location) {
	    	mostCurrentLocation = location;
	    	
	    	BREADCRUMB_LATITUDE = mostCurrentLocation.getLatitude();
	    	BREADCRUMB_LONGITUDE = mostCurrentLocation.getLongitude();
	    	Log.d(TAG, mostCurrentLocation.getLatitude()+"");
	    }
	    @Override
	    public void onStatusChanged(String s, int i, Bundle bundle) {
	    }
	    @Override
	    public void onProviderEnabled(String s) {
	    }
	    @Override
	    public void onProviderDisabled(String s) {
	    }    
	}
	
	/** Called when the user clicks the dropBreadcrumb button */
	// This says that when we press the "Scatter Breadcrumbs" button
	// we send our most recent known latitude and longitude to the
	// DroppedCrumb activity as an extra.
	
	public void dropBreadcrumb(View view) {
	    Intent intent = new Intent(this, DroppedCrumb.class);
        DatabaseHandler db = new DatabaseHandler(this);
        BREADCRUMB_LATITUDE = (BREADCRUMB_LATITUDE * 1e6);
        int lat = (int)BREADCRUMB_LATITUDE;
        BREADCRUMB_LONGITUDE = (BREADCRUMB_LONGITUDE * 1e6);
        int lng = (int)BREADCRUMB_LONGITUDE;
	    db.addBreadcrumb(new Breadcrumb(lat, lng));
	    startActivity(intent);
	}
	
	public void collectBreadcrumbs(View view) {
	    Intent intent = new Intent(this, CollectedBreadcrumbsActivity.class);
        startActivity(intent);
	}
	
	protected void onPause(){
		super.onPause();
		if(locationManager==null){Log.d(TAG, "While pausing your locationManager is NULL");}else{Log.d(TAG, "Your locationManager is NOT NULL when your app pauses");};
	}
}