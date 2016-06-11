package rzeh4n.meteorite;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import rzeh4n.meteorite.data.MeteoriteContract;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mActionBar;

    private int mMeteoritesCount = -1;
    private MapHelper mMapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mActionBar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(String.format("%d meteorites", getMeteoritesCount()));
        if (getResources().getBoolean(R.bool.multi_pane)) {
            mMapHelper = new MapHelper();
            mMapHelper.onCreate(this);
        }
    }

    private int getMeteoritesCount() {
        if (mMeteoritesCount != -1) {
            return mMeteoritesCount;
        } else {
            Cursor cursor = getContentResolver().query(MeteoriteContract.MeteoriteEntry.CONTENT_URI, new String[]{MeteoriteContract.MeteoriteEntry._ID}, null, null, null);
            mMeteoritesCount = cursor.getCount();
            cursor.close();
        }
        return mMeteoritesCount;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                // TODO: 11.6.16 dialog about app
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onMessageEvent(MeteoriteSelectedEvent event) {
        if (mMapHelper != null) { //replace map
            mMapHelper.setMeteoriteId(this, event.meteoriteId);
        } else { //go to map activity
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra(MapActivity.EXTRA_ID, event.meteoriteId);
            intent.putExtra(MapActivity.EXTRA_NAME, event.name);
            intent.putExtra(MapActivity.EXTRA_MASS, event.mass);
            startActivity(intent);
        }
    }


    public static class MeteoriteSelectedEvent {
        public final Long meteoriteId;
        public final String name;
        public final Integer mass;

        public MeteoriteSelectedEvent(Long meteoriteId, String name, Integer mass) {
            this.meteoriteId = meteoriteId;
            this.name = name;
            this.mass = mass;
        }
    }


}
