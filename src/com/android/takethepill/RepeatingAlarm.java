package com.android.takethepill;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class RepeatingAlarm extends BroadcastReceiver{
	String ns = Context.NOTIFICATION_SERVICE;
	NotificationManager mNotificationManager;
	private static final int HELLO_ID = 1;


	@Override
	public void onReceive(Context context, Intent intent){

		mNotificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.ic_alert;
		CharSequence tickerText = TakeThePill.getAppName();
		Bundle extras = intent.getExtras();
		String user=extras.getString("user");
		String pill=extras.getString("pill");
		Calendar calendar= Calendar.getInstance();
		String hour=calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
		System.out.println(hour);
		Notification notification = new Notification(icon, tickerText, 0);

		CharSequence contentTitle =  TakeThePill.getAppName();
		CharSequence contentText = user + " - " + pill;
		Intent notificationIntent = new Intent(context, Notifications.class);
		notificationIntent.putExtra("user", user);
		notificationIntent.putExtra("pill", pill);
		notificationIntent.putExtra("hour", hour);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.defaults |= Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
		notification.flags |=Notification.FLAG_AUTO_CANCEL;
		notification.flags |=Notification.FLAG_INSISTENT;
		
		long[] vibrate = {0,500,500,500};
		notification.vibrate = vibrate;


		if(TakeThePill.getAlarmsEnabled()) mNotificationManager.notify(HELLO_ID, notification);

	}
}

