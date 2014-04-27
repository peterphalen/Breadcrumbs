package com.peter.breadcrumbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
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

	private Location lastKnownLocation;
	private double BREADCRUMB_LATITUDE;
	private double BREADCRUMB_LONGITUDE;
	private int BREADCRUMB_LATITUDE_CONVERTED;
	private int BREADCRUMB_LONGITUDE_CONVERTED;
	String NO_LOCATION_FOUND_TEXT;
	String LOCATION_SERVICES_DISABLED_TEXT;
	String IMPROVE_ACCURACY_WARNING_TEXT;
	String NO_BREADCRUMBS_YET_WARNING_TEXT;
	String AUTO_GENERATED_BREADCRUMB_LABEL;
	  String CANCEL_TEXT;
	  String ENABLE_LOCATION_SERVICES_PROMPT;
	  String SETTINGS_TEXT;
	private DatabaseHandler db;
	private int breadcrumbCount;
	private RelativeLayout progressBarView;
	boolean GPSEnabled = true;

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
	      mLocationRequest.setInterval(100);
	   // Set the fastest update interval to 0 second
	      mLocationRequest.setFastestInterval(0);

			// Get the location manager	
					locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
					
			// Get string resources
					Resources res = getResources();
					NO_LOCATION_FOUND_TEXT = res.getString(R.string.no_location_found_text);
					IMPROVE_ACCURACY_WARNING_TEXT = res.getString(R.string.improve_accuracy_warning);
					NO_BREADCRUMBS_YET_WARNING_TEXT = res.getString(R.string.no_breadcrumbs_yet_warning);
					AUTO_GENERATED_BREADCRUMB_LABEL = res.getString(R.string.auto_generated_breadcrumb_label);	
					  CANCEL_TEXT = res.getString(R.string.cancel);
					LOCATION_SERVICES_DISABLED_TEXT = res.getString(R.string.location_services_disabled_text);	
					ENABLE_LOCATION_SERVICES_PROMPT = res.getString(R.string.enable_location_services_prompt);	
					SETTINGS_TEXT = res.getString(R.string.settings);
					 					
			}
	


	  @Override
	   public void onConnected(Bundle dataBundle) {
	      // Display the connection status
		
		  
		   // Get the current location's latitude & longitude
		      lastKnownLocation = mLocationClient.getLastLocation();


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
		     BREADCRUMB_LONGITUDE_CONVERTED = (int)BREADCRUMB_LONGITUDE;
 }
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
		      
		GPSEnabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );

		// Check if any location has been found
	    if ( lastKnownLocation == null){
	    	
			if(locationManager != null){
				//this locationManager update is needed because of a bug
				//affecting some Samsung phones. It's meant to 'kickstart' the locationListener
				  locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, 
						  new android.location.LocationListener() {
			                    @Override
			                    public void onStatusChanged(String provider, int status,
			                            Bundle extras) {
			                    }

			                    @Override
			                    public void onProviderEnabled(String provider) {
			                    }

			                    @Override
			                    public void onProviderDisabled(String provider) {
			                    }

			                    @Override
			                    public void onLocationChanged(final Location location) {
			                    }
			                }, null);}

	    	//if lastKnownLocation still null after 'kickstart' prompt location settings
	    	if ( !GPSEnabled ){
	    		
		    	//if location services are not enabled tell me about it
		    	EasyTracker.getInstance(this)
			    	.send(MapBuilder
			    		      .createEvent("Location not found",     // Event category (required)
			    	                   "GPS Not Enabled",  // Event action (required)
			    	                   "Drop Breadcrumb Button",   // Event label
			    	                   null)            // Event value
			    	      .build());
	    	//////////AlertDialog prompting location settings
		    AlertDialog.Builder NoLocationAlertDialog = new AlertDialog.Builder(MainActivity.this);

	        // Setting Dialog Title
		    NoLocationAlertDialog.setTitle(NO_LOCATION_FOUND_TEXT);

	        // Setting Dialog Message
		    NoLocationAlertDialog.setMessage(ENABLE_LOCATION_SERVICES_PROMPT);

	        // Setting Icon to Dialog
	        // alertDialog.setIcon(R.drawable.ic_launcher);

	        // Setting Positive "Yes" Button
		    NoLocationAlertDialog.setPositiveButton(SETTINGS_TEXT,
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {

	                        // Activity transfer to location settings
	                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                    }
	                });

	        // Setting Negative "NO" Button
		    NoLocationAlertDialog.setNegativeButton(CANCEL_TEXT,
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                        // Write your code here to invoke NO event
	                        dialog.cancel();
	                    }
	                });

	        // Showing Alert Message
		    NoLocationAlertDialog.show();
	    	//////////ENDAlertDialog prompting location settings
	    	}			    	
	    	//if location services ARE enabled tell me about it
	    	else{
	    		

	    		
	    		EasyTracker.getInstance(this)
		    	.send(MapBuilder
		    		      .createEvent("Location not found",     // Event category (required)
		    	                   "GPS *is* Enabled",  // Event action (required)
		    	                   "Drop Breadcrumb Button",   // Event label
		    	                   null)            // Event value
		    	      .build());
	    	

	    	//////////AlertDialog prompting location settings
		    AlertDialog.Builder NoLocationAlertDialog = new AlertDialog.Builder(MainActivity.this);

	        // Setting Dialog Title
		    NoLocationAlertDialog.setTitle(NO_LOCATION_FOUND_TEXT);

	        // Setting Dialog Message
		    NoLocationAlertDialog.setMessage(ENABLE_LOCATION_SERVICES_PROMPT);

	        // Setting Icon to Dialog
	        // alertDialog.setIcon(R.drawable.ic_launcher);

	        // Setting Positive "Yes" Button
		    NoLocationAlertDialog.setPositiveButton(SETTINGS_TEXT,
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {

	                        // Activity transfer to wifi settings
	                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                    }
	                });

	        // Setting Negative "NO" Button
		    NoLocationAlertDialog.setNegativeButton(CANCEL_TEXT,
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                        // Write your code here to invoke NO event
	                        dialog.cancel();
	                    }
	                });

	        // Showing Alert Message
		    NoLocationAlertDialog.show();
	    	//////////ENDAlertDialog prompting location settings
		    }

	    }
	    else{

	    	//If GPS not enabled but we have a locaiton ask user if they want to enable it
	    	//If they don't, drop a breadcrumb anyway
if ( !GPSEnabled ){
	    	
	    	//////////AlertDialog prompting location settings
	    AlertDialog.Builder GPSalertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
	    GPSalertDialog.setTitle(LOCATION_SERVICES_DISABLED_TEXT);

        // Setting Dialog Message
	    GPSalertDialog.setMessage(IMPROVE_ACCURACY_WARNING_TEXT);

        // Setting Icon to Dialog
        // alertDialog.setIcon(R.drawable.ic_launcher);

        // Setting Positive "Yes" Button
	    GPSalertDialog.setPositiveButton(SETTINGS_TEXT,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // Activity transfer to wifi settings
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });

        // Setting Negative "NO" Button
	    GPSalertDialog.setNegativeButton(CANCEL_TEXT,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Send your location as dropped crumb even without GPS enabled
                    	// TODO Change the confirm text here to make it clear that user will be dropping crumb? Ignore?
                    	
                	    Intent intent = new Intent(getApplicationContext(), BreadcrumbMap.class);

            	        //Auto generate breadcrumb label which will be "Breadcrumb "
            	        //In whatever language and its number in your database
            	        String label = (AUTO_GENERATED_BREADCRUMB_LABEL +" " + (breadcrumbCount+1) );
            		    db.addBreadcrumb(new Breadcrumb(BREADCRUMB_LATITUDE_CONVERTED, BREADCRUMB_LONGITUDE_CONVERTED, label));

            		    //Pass current location as an extra to map activity
            			intent.putExtra("INT_SHOW_THIS_LATITUDE", BREADCRUMB_LATITUDE_CONVERTED);
            			intent.putExtra("INT_SHOW_THIS_LONGITUDE", BREADCRUMB_LONGITUDE_CONVERTED);
            			intent.putExtra("DROP_BREADCRUMB_PRESSED", true);
            			intent.putExtra("THERE_ARE_BREADCRUMBS_ON_MAP", true);
            			db.close();
            	    	progressBarView.setVisibility(View.VISIBLE);

                	    startActivity(intent);

                        dialog.cancel();
                    }
                });

        // Showing Alert Message
	    GPSalertDialog.show();
    	//////////ENDAlertDialog prompting location settings
	    }else{
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
			intent.putExtra("THERE_ARE_BREADCRUMBS_ON_MAP", true);
			db.close();
	    	progressBarView.setVisibility(View.VISIBLE);
	    startActivity(intent);}
	    }
	    
	    
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
					GPSEnabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
					
					if(locationManager != null){
						//this locationManager update is needed because of a bug
						//affecting some Samsung phones. It's meant to 'kickstart' the locationListener
					  locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, 
							  new android.location.LocationListener() {
				                    @Override
				                    public void onStatusChanged(String provider, int status,
				                            Bundle extras) {
				                    }

				                    @Override
				                    public void onProviderEnabled(String provider) {
				                    }

				                    @Override
				                    public void onProviderDisabled(String provider) {
				                    }

				                    @Override
				                    public void onLocationChanged(final Location location) {
				                    }
				                }, null);}
					
					//if lastKnownLocation still null after 'kickstart' prompt location settings
			    	if ( !GPSEnabled ){
			    		

			    	//////////AlertDialog prompting location settings
				    AlertDialog.Builder NoLocationAlertDialog = new AlertDialog.Builder(MainActivity.this);

			        // Setting Dialog Title
				    NoLocationAlertDialog.setTitle(LOCATION_SERVICES_DISABLED_TEXT);

			        // Setting Dialog Message
				    NoLocationAlertDialog.setMessage(ENABLE_LOCATION_SERVICES_PROMPT);

			        // Setting Icon to Dialog
			        // alertDialog.setIcon(R.drawable.ic_launcher);

			        // Setting Positive "Yes" Button
				    NoLocationAlertDialog.setPositiveButton(SETTINGS_TEXT,
			                new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int which) {

			                        // Activity transfer to wifi settings
			                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			                    }
			                });

			        // Setting Negative "NO" Button
				    NoLocationAlertDialog.setNegativeButton(CANCEL_TEXT,
			                new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int which) {
			                        // Write your code here to invoke NO event
			                        dialog.cancel();
			                    }
			                });

			        // Showing Alert Message
				    NoLocationAlertDialog.show();
			    	//////////ENDAlertDialog prompting location settings
			    	}			    	
			    	//if location services ARE enabled tell me about it
			    	else{


			    	//////////AlertDialog prompting location settings
				    AlertDialog.Builder NoLocationAlertDialog = new AlertDialog.Builder(MainActivity.this);

			        // Setting Dialog Title
				    NoLocationAlertDialog.setTitle(NO_LOCATION_FOUND_TEXT);

			        // Setting Dialog Message
				    NoLocationAlertDialog.setMessage(ENABLE_LOCATION_SERVICES_PROMPT);

			        // Setting Icon to Dialog
			        // alertDialog.setIcon(R.drawable.ic_launcher);

			        // Setting Positive "Yes" Button
				    NoLocationAlertDialog.setPositiveButton(SETTINGS_TEXT,
			                new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int which) {

			                        // Activity transfer to wifi settings
			                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			                    }
			                });

			        // Setting Negative "NO" Button
				    NoLocationAlertDialog.setNegativeButton(CANCEL_TEXT,
			                new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int which) {
			                        // Write your code here to invoke NO event
			                        dialog.cancel();
			                    }
			                });

			        // Showing Alert Message
				    NoLocationAlertDialog.show();
			    	//////////ENDAlertDialog prompting location settings
				    }

			  
			  }else{

			Toast.makeText(getApplicationContext(), NO_BREADCRUMBS_YET_WARNING_TEXT,
				    Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, BreadcrumbMap.class);

			//put our lastknowlocation (times 1e6) as extra
			intent.putExtra("INT_SHOW_THIS_LATITUDE", BREADCRUMB_LATITUDE_CONVERTED);
			intent.putExtra("INT_SHOW_THIS_LONGITUDE", BREADCRUMB_LONGITUDE_CONVERTED);
			intent.putExtra("VIEW_MAP_PRESSED", true);

			progressBarView.setVisibility(View.VISIBLE);

			startActivity(intent);}
		}
		
		if (breadcrumbCount > 0) { //zoom to last breadcrumb location if there are breadcrumbs
			Intent intent = new Intent(this, BreadcrumbMap.class);
			intent.putExtra("THERE_ARE_BREADCRUMBS_ON_MAP", true);
			intent.putExtra("VIEW_MAP_PRESSED", true);

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