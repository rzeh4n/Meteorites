package rzeh4n.meteorite;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Random;

import rzeh4n.meteorite.synchronization.SynchronizationAlarmReceiver;

/**
 * Created by Martin Řehánek on 10.6.16.
 */
public class Utils {

    private static final String PREFERENCES_NAME = "meteorite";

    public static String formatMass(int massGrams) {
        if (massGrams < 1000) {
            return String.format("%d g", massGrams);
        } else if (massGrams % 1000 == 0) {
            return String.format("%d kg", massGrams / 1000);
        } else {
            return String.format("%.3f kg", massGrams / 1000f);
        }
    }

    public static void scheduleAlarmForSynchronizationService(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SynchronizationAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        Random rand = new Random();
        int dayInMillis = 1000 * 60 * 60 * 24;
        int trigger = rand.nextInt(dayInMillis);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, trigger, dayInMillis, alarmIntent);
    }


    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

}
