package rzeh4n.meteorite.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import rzeh4n.meteorite.R;
import rzeh4n.meteorite.Utils;

/**
 * Created by Martin Řehánek on 12.6.16.
 */
public class BootReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(LOG_TAG, "scheduling alarm");
            Utils.scheduleAlarmForSynchronizationService(context);
            SharedPreferences.Editor editor = Utils.getPreferences(context).edit();
            editor.putBoolean(context.getResources().getString(R.string.pref_synchronization_scheduled), true);
            editor.commit();
        }
    }
}
