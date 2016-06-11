package rzeh4n.meteorite;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import rzeh4n.meteorite.data.MeteoriteContract;

/**
 * Created by Martin Řehánek on 11.6.16.
 */
public class MapHelper implements OnMapReadyCallback {

    public static final String LOG_TAG = MapHelper.class.getSimpleName();

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

    private boolean dataSet = false;
    private String mName;
    private int mMass;
    private double mLatitude;
    private double mLongitude;
    private GoogleMap mMap;

    public void onCreate(AppCompatActivity activity) {
        mMap = null;
        SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean isReady() {
        return mMap != null;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        if (dataSet) {
            goToPosition(null);
        }
    }

    private void goToPosition(LatLng previousPosition) {
        mMap.clear();
        if (previousPosition != null) {
            Log.d(LOG_TAG, "zoom out");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(previousPosition, 1), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    goToNewPosition(true);
                }

                @Override
                public void onCancel() {
                    //nothing
                }
            });
        } else {
            goToNewPosition(false);
        }
    }

    private void goToNewPosition(final boolean withZoom) {
        final LatLng newPosition = new LatLng(mLatitude, mLongitude);
        MarkerOptions options = new MarkerOptions().position(newPosition)//
                .title(mName).visible(true).snippet(Utils.formatMass(mMass));
        mMap.addMarker(options);
        Log.d(LOG_TAG, "move");
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(newPosition)
                .build()), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if (withZoom) {
                    Log.d(LOG_TAG, "zoom in");
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 4));
                }
            }

            @Override
            public void onCancel() {
                //nothing
            }
        });
    }

    public void setMeteoriteId(Context context, long mId) {
        LatLng previousPosition = dataSet ? new LatLng(mLatitude, mLongitude) : null;
        Uri uri = MeteoriteContract.MeteoriteEntry.buildMeteoriteUri(mId);
        Cursor cursor = context.getContentResolver().query(uri, COLUMNS, null, null, null);
        if (cursor.isBeforeFirst()) {
            cursor.moveToFirst();
            mName = cursor.getString(CURSOR_COL_NAME);
            mMass = cursor.getInt(CURSOR_COL_MASS);
            mLatitude = cursor.getDouble(CURSOR_COL_LATITUDE);
            mLongitude = cursor.getDouble(CURSOR_COL_LONGITUDE);
        }
        cursor.close();
        dataSet = true;
        if (isReady()) {
            goToPosition(previousPosition);
        }
    }


}
