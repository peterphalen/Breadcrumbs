package com.detimil.breadcrumbs1;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

public class MainActivity extends FragmentActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
	
	
    // Global variable to hold the current location
    Location mCurrentLocation;
	
	private LocationClient mLocationClient;
	protected WifiManager wifi;
	LocationManager locationManager;
	MyCurrentLocationListener locationListener;
	private String bestProvider;
	private Location lastKnownLocation;
	private Location mostCurrentLocation;
	private double BREADCRUMB_LATITUDE;
	private double BREADCRUMB_LONGITUDE;
	
	private final static int
    CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	private static final String TAG = "MyActivity";
	
	// Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                    break;
                }
        }
     }
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = resultCode;
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
                return false;

            }
        }
		return true;
    }
    
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @SuppressWarnings("deprecation")
	@Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showDialog(connectionResult.getErrorCode());
        }
    }
	
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		/*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
		int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		  if(resp == ConnectionResult.SUCCESS){
		        mLocationClient = new LocationClient(this, this, this);
		  }
		  else{
		   Toast.makeText(this, "Google Play Service Error " + resp, Toast.LENGTH_LONG).show();}
	}
        
				
	
	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Google analytics.
	    
	 // Connect the client.
        mLocationClient.connect();
	  }
	
	protected void onResume(){
		super.onResume();
	  
				
		//Register locationListener to locationManager and start getting updates as to your current location

		lastKnownLocation = mLocationClient.getLastLocation();

		
		locationListener = new MyCurrentLocationListener();
		locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);


		
		//The following snippet says: if our locationListener hasn't had time to get our current 
		//location, find our lat and lang using our last known location. However, if we do have
		//an updated location by now, get our lat and lang from that. (The mostCurrentLocation
		//variable is defined in the LocationListener class below.)
		


	    if (mostCurrentLocation == null && lastKnownLocation != null){
	    	BREADCRUMB_LATITUDE = lastKnownLocation.getLatitude();
	    	BREADCRUMB_LONGITUDE = lastKnownLocation.getLongitude();
	    }
		
	
	}
	
	//When we get an updated location through our location listener, receive the updated 
	//location and call a method that tells us what to do with the location. When I get a new location
	//here I assign it to the variable mostCurrentLocation
	
	public class MyCurrentLocationListener implements LocationListener{ 
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
		

		// Check if any location has been found
	    if (mostCurrentLocation == null && lastKnownLocation == null){
	    	Toast.makeText(getApplicationContext(), "No location found yet\nPlease try again in just a sec",
					Toast.LENGTH_LONG).show();
	    }
	    else{
		    
	    
		// check if enabled and if not send user to the GSP settings
				// Better solution would be to display a toast suggesting they
				// go to the settings

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);

	    if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) && !wifi.isWifiEnabled()) {
	    	Toast.makeText(getApplicationContext(), "Improve accuracy by enabling WIFI and/or GPS",
					Toast.LENGTH_LONG).show();
	    }
		
	    Intent intent = new Intent(this, BreadcrumbMap.class);
        DatabaseHandler db = new DatabaseHandler(this);
        BREADCRUMB_LATITUDE = (BREADCRUMB_LATITUDE * 1e6);
        int lat = (int)BREADCRUMB_LATITUDE;
        BREADCRUMB_LONGITUDE = (BREADCRUMB_LONGITUDE * 1e6);
        int lng = (int)BREADCRUMB_LONGITUDE;
        String label = ("Breadcrumb " + (db.getBreadcrumbsCount()+1) );
	    db.addBreadcrumb(new Breadcrumb(lat, lng, label));
	    
		intent.putExtra("INT_SHOW_THIS_LATITUDE", lat);
		intent.putExtra("INT_SHOW_THIS_LONGITUDE", lng);
		db.close();
	    startActivity(intent);}
	}
	
	
	public void collectBreadcrumbs(View view) {
		DatabaseHandler db = new DatabaseHandler(this);
		if ( db.getBreadcrumbsCount() == 0 ) {
			Toast.makeText(getApplicationContext(), "You haven't dropped any breadcrumbs yet",
				    Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, CollectedBreadcrumbsActivity.class);
	        startActivity(intent);
		}
	else{
	    Intent intent = new Intent(this, CollectedBreadcrumbsActivity.class);
        startActivity(intent);}
		db.close();
	}
	
	public void breadcrumbMap(View view) {
        DatabaseHandler db = new DatabaseHandler(this);
     // Check if any location has been found and there are no breadcrumbs then do nothing with message
        
     // Check if any location has been found
	    if (mostCurrentLocation == null && lastKnownLocation == null){
	    	Toast.makeText(getApplicationContext(), "No location found yet\nPlease try again in just a sec",
					Toast.LENGTH_LONG).show();
	    }
	    else{	    	
	    	//If no breadcrumbs found but current location is found, send current location to the map and open it
		if ( db.getBreadcrumbsCount() == 0 ) {
			Toast.makeText(getApplicationContext(), "You haven't dropped any breadcrumbs yet",
				    Toast.LENGTH_LONG).show();}
			Intent intent = new Intent(this, BreadcrumbMap.class);
			
	        int current_lat = (int)(BREADCRUMB_LATITUDE * 1e6);
	        int current_lng = (int)(BREADCRUMB_LONGITUDE * 1e6);
			intent.putExtra("INT_SHOW_THIS_LATITUDE", current_lat);
			intent.putExtra("INT_SHOW_THIS_LONGITUDE", current_lng);
			startActivity(intent);
	    }
		db.close();
	}
	
	protected void onStop(){
		super.onStop();
		// Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        locationManager.removeUpdates(locationListener);
		if(locationManager==null){Log.d(TAG, "While pausing your locationManager is NULL");}else{Log.d(TAG, "Your locationManager is NOT NULL when your app pauses");};
	    EasyTracker.getInstance(this).activityStop(this);  // Google analytics.

	}
}