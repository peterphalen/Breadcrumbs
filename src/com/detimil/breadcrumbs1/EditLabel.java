package com.detimil.breadcrumbs1;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class EditLabel extends Activity {
	

	
	private DatabaseHandler db;
	private int breadcrumbId;

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
    	
        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
        .build();
        adView.loadAd(adRequest);
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_label, menu);
		
		return false;
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
    	
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);    
        finish();}
    
    public void cancel(View view) {
   	    finish();  
    }

}
