package rzeh4n.meteorite.synchronization;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SynchronizationService extends Service {

    public static final String LOG_TAG = SynchronizationService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not a binding service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronize();
        return START_NOT_STICKY;
    }

    private void synchronize() {
        Log.i(LOG_TAG, "synchronizing");
        new Synchronizer(getApplicationContext(), new Synchronizer.Listener() {
            @Override
            public void onFinished() {
                Log.d(LOG_TAG, "finished");
                stopSelf();
            }

            @Override
            public void onError(String message) {
                Log.e(LOG_TAG, "error: " + message);
                stopSelf();
            }

            @Override
            public void onProgress(int percentage) {
                //nothing
            }
        }).run();
    }

}
