package com.peter.breadcrumbs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class MainActivity extends Activity implements 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		LocationListener{
	protected LocationManager locationManager;
    protected LocationClient mLocationClient;
    protected LocationRequest mLocationRequest;


	protected WifiManager wifi;
	private Location lastKnownLocation;
	private double BREADCRUMB_LATITUDE;
	private double BREADCRUMB_LONGITUDE;
	private int BREADCRUMB_LATITUDE_CONVERTED;
	private int BREADCRUMB_LONGITUDE_CONVERTED;
	String NO_LOCATION_WARNING_TEXT;
	String IMPROVE_ACCURACY_WARNING_TEXT;
	String NO_BREADCRUMBS_YET_WARNING_TEXT;
	String AUTO_GENERATED_BREADCRUMB_LABEL;
	private DatabaseHandler db;
	private int breadcrumbCount;

	private RelativeLayout progressBarView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		
		//get Spinner but make it invisible
		progressBarView = (RelativeLayout)findViewById(R.id.progressBarView);
		progressBarView.setVisibility(View.GONE);


	      mLocationClient = new LocationClient(this, this, this);
	      mLocationRequest = new LocationRequest();
	      
	   // Use high accuracy
	      mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	   // Set the update interval to 0 seconds
	      mLocationRequest.setInterval(0);
	   // Set the fastest update interval to 0 second
	      mLocationRequest.setFastestInterval(0);

			// Get the location manager	
					locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
					
			// Get string resources
					Resources res = getResources();
					NO_LOCATION_WARNING_TEXT = res.getString(R.string.no_location_found_warning);
					IMPROVE_ACCURACY_WARNING_TEXT = res.getString(R.string.improve_accuracy_warning);
					NO_BREADCRUMBS_YET_WARNING_TEXT = res.getString(R.string.no_breadcrumbs_yet_warning);
					AUTO_GENERATED_BREADCRUMB_LABEL = res.getString(R.string.auto_generated_breadcrumb_label);
				
					
			}
	

	  @Override
	   public void onConnected(Bundle dataBundle) {
	      // Display the connection status

		   // Get the current location's latitude & longitude
		      lastKnownLocation = mLocationClient.getLastLocation();
		      
		   //Request locaiton updates
		      mLocationClient.requestLocationUpdates(mLocationRequest, this);
		      
		      if (lastKnownLocation != null) {
				 // Get the current location's latitude & longitude
		    	BREADCRUMB_LATITUDE = lastKnownLocation.getLatitude();
		    	BREADCRUMB_LONGITUDE = lastKnownLocation.getLongitude();
		    		    	
		    	//Convert latitude and longitude to an integer
		    	//Requires multiplying by 1e6, which means you have to divide 
		    	//the number by 1e6 when you access it later
		        BREADCRUMB_LATITUDE = (BREADCRUMB_LATITUDE * 1e6);
		        BREADCRUMB_LATITUDE_CONVERTED = (int)BREADCRUMB_LATITUDE;
		        BREADCRUMB_LONGITUDE = (BREADCRUMB_LONGITUDE * 1e6);
		        BREADCRUMB_LONGITUDE_CONVERTED = (int)BREADCRUMB_LONGITUDE;
		        }
		    
	   }
	  
	  @Override
	  public void onLocationChanged(Location location) {
		// When new location is found, update lastKnownLocation var to it
		  lastKnownLocation = location;
		  if (lastKnownLocation != null){
			 // Get the current location's latitude & longitude
	    	BREADCRUMB_LATITUDE = lastKnownLocation.getLatitude();
	    	BREADCRUMB_LONGITUDE = lastKnownLocation.getLongitude();
	    	
	    	//Convert latitude and longitude to an integer
	    	//Requires multiplying by 1e6, which means you have to divide 
	    	//the number by 1e6 when you access it later
	    	 BREADCRUMB_LATITUDE = (BREADCRUMB_LATITUDE * 1e6);
		     BREADCRUMB_LATITUDE_CONVERTED = (int)BREADCRUMB_LATITUDE;
		     BREADCRUMB_LONGITUDE = (BREADCRUMB_LONGITUDE * 1e6);
		     BREADCRUMB_LONGITUDE_CONVERTED = (int)BREADCRUMB_LONGITUDE;}
		  	  }
	  
	   @Override
	   public void onDisconnected() {
		      // Display the error code on failure
			   int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
			   if (errorCode != ConnectionResult.SUCCESS) {
			     GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
			     }
	   }

	   @Override
	   public void onConnectionFailed(ConnectionResult connectionResult) {
	      // Display the error code on failure
		   int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		   if (errorCode != ConnectionResult.SUCCESS) {
		     GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
		     }
	   }

	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Google analytics.
	 // Connect the client.
	      mLocationClient.connect();

			db = new DatabaseHandler(this);
			breadcrumbCount = db.getBreadcrumbsCount();


	  }

	protected void onResume(){
		super.onResume();
	}

	


	/** Called when the user clicks the dropBreadcrumb button */
	// This says that when we press the "Scatter Breadcrumbs" button
	// we send our most recent known latitude and longitude to the
	// DroppedCrumb activity as an extra.

	public void dropBreadcrumb(View view) {
		      
		// Check if any location has been found
	    if ( lastKnownLocation == null){
	    	Toast.makeText(getApplicationContext(), NO_LOCATION_WARNING_TEXT,
					Toast.LENGTH_LONG).show();
	    }
	    else{
	    	

		// check if Wifi or GPS enabled and if not send user to the GSP settings

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);

		//If both GPS and Wifi are off, continue with method but warn user with toast
	    if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) && !wifi.isWifiEnabled()) {
	    	Toast.makeText(getApplicationContext(), IMPROVE_ACCURACY_WARNING_TEXT,
					Toast.LENGTH_LONG).show();
	    }

	    //open breadcrumb map
	    Intent intent = new Intent(this, BreadcrumbMap.class);

        //Auto generate breadcrumb label which will be "Breadcrumb "
        //In whatever language and its number in your database
        String label = (AUTO_GENERATED_BREADCRUMB_LABEL +" " + (breadcrumbCount+1) );
	    db.addBreadcrumb(new Breadcrumb(BREADCRUMB_LATITUDE_CONVERTED, BREADCRUMB_LONGITUDE_CONVERTED, label));

	    //Pass current location as an extra to map activity
		intent.putExtra("INT_SHOW_THIS_LATITUDE", BREADCRUMB_LATITUDE_CONVERTED);
		intent.putExtra("INT_SHOW_THIS_LONGITUDE", BREADCRUMB_LONGITUDE_CONVERTED);
		intent.putExtra("DROP_BREADCRUMB_PRESSED", true);
		db.close();
    	progressBarView.setVisibility(View.VISIBLE);

		
	    startActivity(intent);}
	}


	public void collectBreadcrumbs(View view) {
		progressBarView.setVisibility(View.VISIBLE);

		//Open collection activity. If there are no breadcrumbs show toast warning as well
		if ( breadcrumbCount == 0 ) {
			Intent intent = new Intent(this, CollectedBreadcrumbsActivity.class);

			Toast.makeText(getApplicationContext(), NO_BREADCRUMBS_YET_WARNING_TEXT,
				    Toast.LENGTH_LONG).show();

	        startActivity(intent);
		}
	else{
	    Intent intent = new Intent(this, CollectedBreadcrumbsActivity.class);
        startActivity(intent);}
		db.close();
	}

	public void breadcrumbMap(View view) {

		
     // Check if any location has been found and there are no breadcrumbs then do nothing with message
        
	    	//If no breadcrumbs are in database but current location is found, 
	    	//send current location to the map and open it
		if ( breadcrumbCount == 0 ) {
			
			  if ( lastKnownLocation == null){
			    	Toast.makeText(getApplicationContext(), NO_LOCATION_WARNING_TEXT,
							Toast.LENGTH_LONG).show();
			    }else{

			Toast.makeText(getApplicationContext(), NO_BREADCRUMBS_YET_WARNING_TEXT,
				    Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, BreadcrumbMap.class);

			//put our lastknowlocation (times 1e6) as extra
			intent.putExtra("INT_SHOW_THIS_LATITUDE", BREADCRUMB_LATITUDE_CONVERTED);
			intent.putExtra("INT_SHOW_THIS_LONGITUDE", BREADCRUMB_LONGITUDE_CONVERTED);
			intent.putExtra("VIEW_MAP_PRESSED_AND_BREADCRUMBS_NOT_STORED", true);
			progressBarView.setVisibility(View.VISIBLE);

			startActivity(intent);}
		}
		if (breadcrumbCount > 0) { //zoom to last breadcrumb location if there are breadcrumbs
			Intent intent = new Intent(this, BreadcrumbMap.class);

			intent.putExtra("VIEW_MAP_PRESSED_AND_BREADCRUMBS_STORED", true);
			progressBarView.setVisibility(View.VISIBLE);

			startActivity(intent);
		}

	    }
	

	protected void onStop(){
		super.onStop();
		db.close();
        /*
         * Remove location updates for a listener.
         * The current Activity is the listener, so
         * the argument is "this".
         */
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
    }
    /*
     * After disconnect() is called, the client is
     * considered "dead".
     */
       mLocationClient.disconnect();
       
       progressBarView.setVisibility(View.GONE);
       EasyTracker.getInstance(this).activityStop(this);  // Google analytics.

	}
}