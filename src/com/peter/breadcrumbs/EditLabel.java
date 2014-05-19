package com.peter.breadcrumbs;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ShareActionProvider;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class EditLabel extends Activity {
		
	private DatabaseHandler db;
	int breadcrumbId;
	ShareActionProvider provider;

	double breadcrumbLatitude;
	double breadcrumbLongitude;
	String breadcrumbLabel;
	String DIRECTIONS_SNIPPET;
	Breadcrumb breadcrumb;
	Tracker tracker;

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_label);
		db = new DatabaseHandler(this);
		
		 // Get tracker.
        tracker = ((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);
        
		Bundle extras = getIntent().getExtras();
		breadcrumbId = extras.getInt("breadcrumbId");
		
    	breadcrumbLabel = db.getBreadcrumb(breadcrumbId).getLabel();
        
    	breadcrumb = db.getBreadcrumb(breadcrumbId);
	    breadcrumbLatitude = breadcrumb.getBreadcrumbLatitude()/1E6;
	    breadcrumbLongitude = breadcrumb.getBreadcrumbLongitude()/1E6;
    	
    	if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
    		  // call something for API Level 11+
    		ActionBar actionBar = getActionBar();
    		actionBar.setDisplayHomeAsUpEnabled(true);
    		actionBar.setTitle(breadcrumbLabel);
    		}
    	Resources res = getResources();
    	DIRECTIONS_SNIPPET = res.getString(R.string.share_directions_snippet);
    	
	}

	
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			    // Inflate the menu; this adds items to the action bar if it is present.
			    getMenuInflater().inflate(R.menu.edit_label, menu);
			    
			    
			    if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    		  // call something for API Level 14+
			 // Get the ActionProvider for later usage, share directions-snippet plus google map link in text form
			    provider = (ShareActionProvider) menu.findItem(R.id.menu_share)
			        .getActionProvider();
			    if (provider != null){
			    Intent intent = new Intent(Intent.ACTION_SEND);
	    		tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Breadcrumb Shared")
                .setAction("Someone clicked share button")
                .build());
	 			    intent.setType("text/plain");
	 			    intent.putExtra(Intent.EXTRA_TEXT, DIRECTIONS_SNIPPET + " " + breadcrumbLabel + ": http://maps.google.com/maps?" + "&daddr=" + breadcrumbLatitude + "," + breadcrumbLongitude);
				    provider.setShareIntent(intent);
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
	
	  @Override
	  public void onStart() {
	    super.onStart();
	    //Get an Analytics tracker to report app starts & uncaught exceptions etc.
	    GoogleAnalytics.getInstance(this).reportActivityStart(this);
	    
	  }
	  
    public void navigateTo(View view) {
	final Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "&daddr=" + breadcrumbLatitude + "," + breadcrumbLongitude));
    startActivity(intent);
    intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
    }
	
    public void setBreadcrumbLabel(View view) {
    	// Store editText-entered breadcrumb label in the database
    	EditText editText = (EditText) findViewById(R.id.breadcrumbLabelEditText);
    	String breadcrumbLabel = editText.getEditableText().toString();
    	if (breadcrumbLabel.matches("")) {
    	}else{
    	db.relabelBreadcrumb(breadcrumbId, breadcrumbLabel);}
    	Intent returnIntent = new Intent();
    	 returnIntent.putExtra("EDIT_BREADCRUMB_ID",breadcrumbId);
    	 setResult(RESULT_OK,returnIntent);
		finish();
    }
    
    public void deleteBreadcrumb(View view) {
    	// Delete breadcrumb label in the database
    	Breadcrumb breadcrumb = db.getBreadcrumb(breadcrumbId);
        db.deleteBreadcrumb(breadcrumb);
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent); 
        finish();
        }
    
    public void cancel(View view) {
    	Intent returnIntent = new Intent();
   	 returnIntent.putExtra("EDIT_BREADCRUMB_ID",breadcrumbId);
   	 setResult(RESULT_OK,returnIntent);
   	 finish();  
    }
    
    @Override
	  public void onStop() {
	    super.onStop();
	  //Stop the analytics tracking
	    GoogleAnalytics.getInstance(this).reportActivityStop(this);	  
	    
    }

}
