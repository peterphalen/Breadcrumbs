package com.peter.breadcrumbs;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class MyApplication extends Application {

//Logging TAG
private static final String TAG = "Breadcrumbs";

public enum TrackerName {
APP_TRACKER, // Tracker used only in this app.
}

HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

public MyApplication() {
super();
}

synchronized Tracker getTracker(TrackerName trackerId) {
if (!mTrackers.containsKey(trackerId)) {

GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
Tracker t = analytics.newTracker(R.xml.app_tracker);

mTrackers.put(trackerId, t);
 
}
return mTrackers.get(trackerId);
}
}
