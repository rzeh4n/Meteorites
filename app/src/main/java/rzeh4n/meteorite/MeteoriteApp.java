package rzeh4n.meteorite;

import android.app.Application;

/**
 * Created by Martin Řehánek on 10.6.16.
 */
public class MeteoriteApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.scheduleAlarmForSynchronizationService(this);
    }

}
