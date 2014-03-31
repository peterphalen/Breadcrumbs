package com.peter.breadcrumbs;

public class Breadcrumb {
	private int _id;
	private int _latitude;
	private int _longitude;
	private String _label;

    // Empty constructor
    public Breadcrumb(){
	}	
    
    // constructor
	public Breadcrumb(int id, int latitude, int longitude, String label){
		this._id = id;
		this._latitude = latitude;
		this._longitude = longitude;
		this._label = label;
	}
	
	// constructor
	public Breadcrumb(int latitude, int longitude, String label){
		this._latitude = latitude;
		this._longitude = longitude;
		this._label = label;
	}
	
	// getting id
	public int getId() {
		return _id;
	}
	
	// setting id
	public void setId(int id) {
		this._id = id;
	}

	// getting latitude
	public int getBreadcrumbLatitude() {
		return _latitude;
	}

	// setting latitude
	public void setBreadcrumbLatitude(int latitude) {
		this._latitude = latitude;
	}
	
	// getting longitude
	public int getBreadcrumbLongitude() {
		return _longitude;
	}

	// setting longitude
	public void setBreadcrumbLongitude(int longitude) {
		this._longitude = longitude;
	}
	
	// getting label
	
	public String getLabel() {
		return _label;
	}
	
	// setting label
	
	public void setLabel(String label) {
		this._label = label;
	}
	
	

}