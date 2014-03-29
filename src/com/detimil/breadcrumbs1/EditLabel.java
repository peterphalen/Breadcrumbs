package com.detimil.breadcrumbs1;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class EditLabel extends Activity {
	

	
	private DatabaseHandler db;
	int breadcrumbId;
	ShareActionProvider provider;
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_label);
		db = new DatabaseHandler(this);
		
		Bundle extras = getIntent().getExtras();
		breadcrumbId = extras.getInt("breadcrumbId");
		
    	EditText editText = (EditText) findViewById(R.id.breadcrumbLabelEditText);
    	String breadcrumbLabel = db.getBreadcrumb(breadcrumbId).getLabel();
    	editText.setHint(breadcrumbLabel);
        
    	if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
    		  // call something for API Level 11+
    		ActionBar actionBar = getActionBar();
    		actionBar.setDisplayHomeAsUpEnabled(true);}
    	
	}
	
	
	
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			    // Inflate the menu; this adds items to the action bar if it is present.
			    getMenuInflater().inflate(R.menu.edit_label, menu);
			    
			    
			    if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    		  // call something for API Level 14+
			 // Get the ActionProvider for later usage
			    provider = (ShareActionProvider) menu.findItem(R.id.menu_share)
			        .getActionProvider();
			    if (provider != null){
			    Intent intent = new Intent(Intent.ACTION_SEND);
			    if (db.getBreadcrumbsCount() > 0) {
			    Breadcrumb breadcrumb = db.getBreadcrumb(breadcrumbId);
			    int blat = breadcrumb.getBreadcrumbLatitude();
			    int blang = breadcrumb.getBreadcrumbLongitude();
			    Geocoder geocoder= new Geocoder(this, Locale.ENGLISH);
		         
		        try {
		               
		              //Place your latitude and longitude
		              List<Address> addresses = geocoder.getFromLocation(blat, blang, 1);
		              
		              if(addresses != null) {
		               
		            	  String address = addresses.get(0).getAddressLine(0);
		            	    String city = addresses.get(0).getAddressLine(1);
		            	    String postalCode = addresses.get(0).getPostalCode();
		            	    intent.setType("text/plain");
		    			    intent.putExtra(Intent.EXTRA_TEXT, address + " " + city + ", " + postalCode);
		    			    provider.setShareIntent(intent);
		    			    }
		              else {
		              Toast.makeText(getApplicationContext(),"Could not get address..! Generating latitude and longitude", Toast.LENGTH_LONG).show();

		 			    intent.setType("text/plain");
		 			    intent.putExtra(Intent.EXTRA_TEXT, blat + "," + blang);
					    provider.setShareIntent(intent);}
		              
		              }
		            	  
		         
		        catch (IOException e) {
		                 e.printStackTrace();
		                 Toast.makeText(getApplicationContext(),"Could not get address..! Generating lat and longitude", Toast.LENGTH_LONG).show();

		 			    intent.setType("text/plain");
		 			    intent.putExtra(Intent.EXTRA_TEXT, blat + "," + blang);
					    provider.setShareIntent(intent);}
			    	}
				}
			    }
			    
			    return true;
			    }
	
	
			
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; goto parent activity.
	            this.finish();
	            return true;
	        case R.id.menu_share:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@SuppressLint("NewApi")
	public void doShare() {
	    // populate the share intent with data
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
  		  // call something for API Level 14+
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    Breadcrumb breadcrumb = db.getBreadcrumb(breadcrumbId);
	    int blat = breadcrumb.getBreadcrumbLatitude();
	    int blang = breadcrumb.getBreadcrumbLongitude();
	    intent.setType("text/plain");
	    intent.putExtra(Intent.EXTRA_TEXT, blat + ", " + blang);
	    provider.setShareIntent(intent);}
	  } 
	
	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Google analytics.
	  }
	  
	
    public void navigateTo(View view) {
    Breadcrumb breadcrumb = db.getBreadcrumb(breadcrumbId);
	final Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "&daddr=" + breadcrumb.getBreadcrumbLatitude()/1e6 + "," + breadcrumb.getBreadcrumbLongitude()/1e6));
    startActivity(intent);
    intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
    }
	
    public void setBreadcrumbLabel(View view) {
    	// Store breadcrumb label in the database
    	EditText editText = (EditText) findViewById(R.id.breadcrumbLabelEditText);
    	String breadcrumbLabel = editText.getEditableText().toString();
    	if (breadcrumbLabel.matches("")) {
    	}else{
    	db.relabelBreadcrumb(breadcrumbId, breadcrumbLabel);}
    	
		finish();
    }
    
    public void deleteBreadcrumb(View view) {
    	// Delete breadcrumb label in the database
    	Breadcrumb breadcrumb = db.getBreadcrumb(breadcrumbId);
        db.deleteBreadcrumb(breadcrumb);
    	   
        finish();
        }
    
    public void cancel(View view) {
   	    finish();  
    }
    
    @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Google analytics.
	  }

}
