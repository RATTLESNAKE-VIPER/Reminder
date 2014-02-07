/*
 * Copyright (C) 2011 Wglxy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reminder.controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * This is the activity for feature 4 in the dashboard application.
 * It displays some text and provides a way to get back to the home activity.
 *
 */

public class SettingsViewController extends DashboardActivity 
{

private EditText etSnoozeTime;
private EditText etMinRadius;
private EditText etMaxRadius;

/**
 * onCreate
 *
 * Called when the activity is first created. 
 * This is where you should do all of your normal static set up: create views, bind data to lists, etc. 
 * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
 * 
 * Always followed by onStart().
 *
 * @param savedInstanceState Bundle
 */

protected void onCreate(Bundle savedInstanceState) 
{
    super.onCreate(savedInstanceState);
    setContentView (R.layout.controller_settings);
    setTitleFromActivityLabel (R.id.title_text);
    
    etSnoozeTime = (EditText) findViewById(R.id.snooze_time_value);
    etMinRadius = (EditText) findViewById(R.id.min_radius_value);
    etMaxRadius = (EditText) findViewById(R.id.max_radius_value);
    this.populateFields();
}

private void populateFields() {
	
}

//listeners---------------------
public void onClickSave (View v) {
	
}
    
} // end class
