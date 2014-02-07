package com.example.reminder.controllers;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

import android.app.Application;
import android.util.Log;

public class ReminderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d("TestApplication", "onCreate()");

        // output debug to LogCat, with tag LittleFluffyLocationLibrary
        LocationLibrary.showDebugOutput(true);

        // in most cases the following initialising code using defaults is probably sufficient:
        //
        // LocationLibrary.initialiseLibrary(getBaseContext(), "com.your.package.name");
        //
        // however for the purposes of the test app, we will request unrealistically frequent location broadcasts
        // every 1 minute, and force a location update if there hasn't been one for 2 minutes.
        //LocationLibrary.initialiseLibrary(getBaseContext(), 60 * 1000, 2 * 60 * 1000, "com.example.reminder.controllers");
        LocationLibrary.initialiseLibrary(getBaseContext(), 20 * 1000, 30 * 1000, "com.example.reminder.controllers");
    }
}
