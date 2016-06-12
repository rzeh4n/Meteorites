package rzeh4n.meteorite.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Martin Řehánek on 10.6.16.
 */
public class SynchronizationAlarmReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = SynchronizationAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "starting synchronization service");
        Intent serviceIntent = new Intent(context, SynchronizationService.class);
        context.startService(serviceIntent);
    }
}
