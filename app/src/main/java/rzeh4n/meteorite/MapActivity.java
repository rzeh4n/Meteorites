package rzeh4n.meteorite;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import rzeh4n.meteorite.data.MeteoriteContract;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_ID = "id";
    private static final String LOG_TAG = MapActivity.class.getSimpleName();

    private static final String[] COLUMNS = {
            MeteoriteContract.MeteoriteEntry.COLUMN_NAME,
            MeteoriteContract.MeteoriteEntry.COLUMN_MASS,
            MeteoriteContract.MeteoriteEntry.COLUMN_LATITUDE,
            MeteoriteContract.MeteoriteEntry.COLUMN_LONGITUDE,
    };

    private static final int CURSOR_COL_NAME = 0;
    private static final int CURSOR_COL_MASS = 1;
    private static final int CURSOR_COL_LATITUDE = 2;
    private static final int CURSOR_COL_LONGITUDE = 3;

    private GoogleMap mMap;
    private long mId = -1;
    private String mName;
    private int mMass;
    private float mLatitude;
    private float mLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initData(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mId = savedInstanceState.getLong(EXTRA_ID, -1);
            Log.i(LOG_TAG, "from saved state: " + mId);
        } else {
            mId = getIntent().getLongExtra(EXTRA_ID, -1);
            Log.i(LOG_TAG, "from intent: " + mId);
        }
        if (mId != -1) {
            Uri uri = MeteoriteContract.MeteoriteEntry.buildMeteoriteUri(mId);
            Cursor cursor = getContentResolver().query(uri, COLUMNS, null, null, null);
            if (cursor.isBeforeFirst()) {
                cursor.moveToFirst();
                mName = cursor.getString(CURSOR_COL_NAME);
                mMass = cursor.getInt(CURSOR_COL_MASS);
                mLatitude = cursor.getFloat(CURSOR_COL_LATITUDE);
                mLongitude = cursor.getFloat(CURSOR_COL_LONGITUDE);
            }
            cursor.close();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mId != -1) {
            outState.putLong(EXTRA_ID, mId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        LatLng position = new LatLng(mLatitude, mLongitude);
        MarkerOptions options = new MarkerOptions().position(position)//
                .title(mName).visible(true).snippet(Utils.formatMass(mMass));
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        zoomAfterWhile(googleMap, position, 1000, 4);
    }

    private void zoomAfterWhile(final GoogleMap map, final LatLng position, final int delayMs, final int zoomLevel) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(position)
                        .zoom(zoomLevel)
                        .build()));
            }
        }.execute();

    }
}
