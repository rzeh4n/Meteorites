package rzeh4n.meteorite;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by Martin Řehánek on 10.6.16.
 */
public class MeteoriteApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        boolean scheduled = Utils.getPreferences(this).getBoolean(getResources().getString(R.string.pref_synchronization_scheduled), false);
        if (!scheduled) {
            Utils.scheduleAlarmForSynchronizationService(this);
            SharedPreferences.Editor editor = Utils.getPreferences(this).edit();
            editor.putBoolean(getResources().getString(R.string.pref_synchronization_scheduled), true);
            editor.commit();
        }
    }

}
