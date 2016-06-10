package rzeh4n.meteorite.synchronization;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rzeh4n.meteorite.R;
import rzeh4n.meteorite.Utils;
import rzeh4n.meteorite.data.MeteoriteContract;

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
                clearDb();
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
                List<Meteorite> buffer = new ArrayList<Meteorite>(100);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject meteorite = array.getJSONObject(i);
                    Long id = meteorite.getLong("id");
                    Integer mass = meteorite.getInt("mass");
                    String name = meteorite.getString("name");
                    JSONObject location = meteorite.getJSONObject("geolocation");
                    JSONArray coords = location.getJSONArray("coordinates");
                    //longitude first, latitude second,
                    //see https://dev.socrata.com/docs/datatypes/point.html#2.1
                    Double longitude = coords.getDouble(0);
                    Double latitude = coords.getDouble(1);
                    buffer.add(new Meteorite(id, name, mass, latitude, longitude));
                    if (i % 100 == 0) {
                        bulkInsertOrUpdate(buffer);
                        float ratio = (float) i / size;
                        publishProgress(60 + (int) (40 * ratio));
                        buffer = new ArrayList<>(100);
                    }
                }
                if (!buffer.isEmpty()) {
                    bulkInsertOrUpdate(buffer);
                }
                publishProgress(100);
            }

            private void bulkInsertOrUpdate(List<Meteorite> buffer) {
                List<ContentValues> toBeInserted = new ArrayList<>();
                ContentResolver resolver = mContext.getContentResolver();
                for (Meteorite meteorite : buffer) {
                    Cursor selectCursor = resolver.query(MeteoriteContract.MeteoriteEntry.buildMeteoriteUri(meteorite.getId()),
                            new String[]{MeteoriteContract.MeteoriteEntry._ID}, null, null, null);
                    boolean found = selectCursor.getCount() == 1;
                    selectCursor.close();
                    if (!found) {
                        toBeInserted.add(buildContentValuesForInsert(meteorite));
                        //Log.d(LOG_TAG, "to be inserted: " + uri);
                    } else {
                        Uri uri = MeteoriteContract.MeteoriteEntry.buildMeteoriteUri(meteorite.getId());
                        resolver.update(uri, buildContentValuesForUpdate(meteorite), null, null);
                        //Log.d(LOG_TAG, "updated: " + uri);
                    }
                }
                //bulk insert
                ContentValues[] values = new ContentValues[toBeInserted.size()];
                values = toBeInserted.toArray(values);
                int inserted = resolver.bulkInsert(MeteoriteContract.MeteoriteEntry.CONTENT_URI, values);
                Log.d(LOG_TAG, "inserted: " + inserted);
            }

            private ContentValues buildContentValuesForInsert(Meteorite meteorite) {
                ContentValues values = new ContentValues();
                values.put(MeteoriteContract.MeteoriteEntry._ID, meteorite.getId());
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_NAME, meteorite.getName());
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_MASS, meteorite.getMass());
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LATITUDE, meteorite.getLat());
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LONGITUDE, meteorite.getLon());
                return values;
            }

            private ContentValues buildContentValuesForUpdate(Meteorite meteorite) {
                ContentValues values = new ContentValues();
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_NAME, meteorite.getName());
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_MASS, meteorite.getMass());
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LATITUDE, meteorite.getLat());
                values.put(MeteoriteContract.MeteoriteEntry.COLUMN_LONGITUDE, meteorite.getLon());
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
                    SharedPreferences.Editor editor = Utils.getPreferences(mContext).edit();
                    Resources resources = mContext.getResources();
                    editor.putBoolean(resources.getString(R.string.pref_synchronized_before), true);
                    editor.putLong(resources.getString(R.string.pref_last_synchronized), System.currentTimeMillis());
                    editor.commit();
                    mListener.onFinished();
                }
            }

        }.execute();

    }


    public interface Listener {

        public void onFinished();

        public void onError(String message);

        public void onProgress(int percentage);


    }

    static class Meteorite {
        private final Long id;
        private final String name;
        private final Integer mass;
        private final Double lat;
        private final Double lon;

        Meteorite(Long id, String name, Integer mass, Double lat, Double lon) {
            this.id = id;
            this.name = name;
            this.mass = mass;
            this.lat = lat;
            this.lon = lon;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Integer getMass() {
            return mass;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLon() {
            return lon;
        }
    }

}
