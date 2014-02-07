package com.example.reminder.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.reminder.controllers.R;

public class NotifierHelper {
    private static final int NOTIFY_1 = 0x1001;
    //public static void sendNotification(Activity caller, Class<?> activityToLaunch, String title, String msg, int numberOfEvents, boolean flashLed, boolean vibrate) {    
    public static void sendNotification(Context caller, Class<?> activityToLaunch, String title, String msg, int numberOfEvents, boolean flashLed, boolean vibrate, Bundle extras) {    
        NotificationManager notifier = (NotificationManager) caller.getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notify = new Notification(R.drawable.icon, "", System.currentTimeMillis());

        notify.icon = R.drawable.icon;
        notify.tickerText = title;
        notify.when = System.currentTimeMillis();
        notify.number = numberOfEvents;
        notify.flags |= Notification.FLAG_AUTO_CANCEL;

        if (flashLed) {
        // add lights
            notify.flags |= Notification.FLAG_SHOW_LIGHTS;
            notify.ledARGB = Color.CYAN;
            notify.ledOnMS = 500;
            notify.ledOffMS = 500;
        }

        if (vibrate) {
            notify.vibrate = new long[] {100, 200, 200, 200, 200, 200, 1000, 200, 200, 200, 1000, 200};
        }

        Intent toLaunch = new Intent(caller, activityToLaunch);
        toLaunch.putExtras(extras);
        toLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent intentBack = PendingIntent.getActivity(caller, 0, toLaunch, PendingIntent.FLAG_UPDATE_CURRENT); // 3 => PendingIntent.FLAG_UPDATE_CURRENT, 0

        notify.setLatestEventInfo(caller, title, msg, intentBack);
        notifier.notify(NOTIFY_1, notify);
    }
}

