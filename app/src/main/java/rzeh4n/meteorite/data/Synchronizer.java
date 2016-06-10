package rzeh4n.meteorite.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rzeh4n.meteorite.R;

/**
 * Created by Martin Řehánek on 10.6.16.
 */
public class Synchronizer {

    public static final String LOG_TAG = Synchronizer.class.getSimpleName();

    private final Context mContext;
    private final Listener mListener;

    public Synchronizer(Context context, @NonNull Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public void run() {

        new AsyncTask<Void, Integer, Void>() {

            private boolean mError = false;
            private String mErrorMsg;

            @Override
            protected Void doInBackground(Void... params) {
                //clearDb();
                try {
                    synchronize();
                } catch (Throwable e) {
                    mError = true;
                    mErrorMsg = e.getMessage();
                }
                return null;
            }

            private void clearDb() {
                //delete all
                int deleted = mContext.getContentResolver().delete(MeteoriteContract.MeteoriteEntry.CONTENT_URI, null, null);
                Log.i(LOG_TAG, String.format("deleted %d records", deleted));
            }

            private void synchronize() throws IOException, JSONException {
                publishProgress(10);
                //https://data.nasa.gov/resource/y77d-th95.json?$where=year >= '2011-01-01T00:00:00.000'
                String url = "https://data.nasa.gov/resource/y77d-th95.json?%24where=year%20%3E%3D%20%272011-01-01T00%3A00%3A00.000%27";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .header("X-App-Token", mContext.getResources().getString(R.string.nasa_api_token))
                        .build();
                Response response = client.newCall(request).execute();
                publishProgress(30);
                String jsonStr = response.body().string();
                publishProgress(40);
                JSONArray array = new JSONArray(jsonStr);
                publishProgress(60);
                int size = array.length();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject meteorite = array.getJSONObject(i);
                    Long id = meteorite.getLong("id");
                    Integer mass = meteorite.getInt("mass");
                    String name = meteorite.getString("name");
                    JSONObject location = meteorite.getJSONObject("geolocation");
                    JSONArray coords = location.getJSONArray("coordinates");
                    //longitude first, latitude second,
                    //see https://dev.socrata.com/docs/datatypes/point.html#2.1
                    double longitude = coords.getDouble(0);
                    double latitude = coords.getDouble(1);
                    insertOrUpdate(id, name, mass, latitude, longitude);
                    if (i % 100 == 0) {
                        float ratio = (float) i / size;
                        publishProgress(60 + (int) (40 * ratio));
                    }
                }
                publishProgress(100);
            }

            private void insertOrUpdate(Long id, String name, Integer mass, double lat, double lon) {
                ContentResolver resolver = mContext.getContentResolver();
                Cursor selectCursor = resolver.query(MeteoriteContract.MeteoriteEntry.buildMeteoriteUri(id), new String[]{MeteoriteContract.MeteoriteEntry._ID}, null, null, null);
                boolean found = selectCursor.getCount() == 1;
                selectCursor.close();
                if (!found) {
                    selectCursor.close();
                    Uri uri = MeteoriteContract.MeteoriteEntry.CONTENT_URI;
                    resolver.insert(uri, buildContentValuesForInsert(id, name, mass, lat, lon));
                    //Log.d(LOG_TAG, "inserted: " + uri);
                } else {
                    selectCursor.close();
                    Uri uri = MeteoriteContract.MeteoriteEntry.buildMeteoriteUri(id);
                    resolver.update(uri, buildContentValuesForUpdate(name, mass, lat, lon), null, null);
                    //Log.d(LOG_TAG, "updated: " + uri);
                }
            }

            private ContentValues buildContentValuesForInsert(Long id, String name, Integer mass, double lat, double lon) {
                ContentValues values = new ContentValues();
                values.put(MeteoriteContract.MeteoriteEntry._ID, id);
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_NAME, name);
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_MASS, mass);
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LATITUDE, lat);
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LONGITUDE, lon);
                return values;
            }

            private ContentValues buildContentValuesForUpdate(String name, Integer mass, double lat, double lon) {
                ContentValues values = new ContentValues();
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_NAME, name);
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_MASS, mass);
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LATITUDE, lat);
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LONGITUDE, lon);
                return values;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                mListener.onProgress(values[0]);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                mListener.onError("synchronization task canceled");
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mError) {
                    mListener.onError(mErrorMsg);
                } else {
                    mListener.onFinished();
                    // TODO: 10.6.16 store into SharedProperties that was synchronized once
                }
            }

        }.execute();

    }


    public interface Listener {

        public void onFinished();

        public void onError(String message);

        public void onProgress(int percentage);


    }

}
