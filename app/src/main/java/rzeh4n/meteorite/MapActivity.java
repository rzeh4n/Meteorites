package rzeh4n.meteorite;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity/* implements OnMapReadyCallback*/ {

    private static final String LOG_TAG = MapActivity.class.getSimpleName();

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_MASS = "mass";


    @BindView(R.id.toolbar) Toolbar mActionBar;

    private long mId = -1;
    private MapHelper mMapHelper;

    private String mName;
    private int mMass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.multi_pane)) {
            finish();
        }
        mMapHelper = new MapHelper();
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        initData(savedInstanceState);
        setSupportActionBar(mActionBar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mName);
        getSupportActionBar().setSubtitle(Utils.formatMass(mMass));
        mMapHelper.onCreate(this);

    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mId = savedInstanceState.getLong(EXTRA_ID, -1);
            mName = savedInstanceState.getString(EXTRA_NAME, "");
            mMass = savedInstanceState.getInt(EXTRA_MASS, 0);
            //Log.i(LOG_TAG, "from saved state: " + mId);
        } else {
            Intent intent = getIntent();
            mId = intent.getLongExtra(EXTRA_ID, -1);
            mName = intent.getStringExtra(EXTRA_NAME);
            mMass = intent.getIntExtra(EXTRA_MASS, 0);
            //Log.i(LOG_TAG, "from intent: " + mId);
        }
        if (mId != -1) {
            mMapHelper.setMeteoriteId(this, mId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mId != -1) {
            outState.putLong(EXTRA_ID, mId);
            outState.putString(EXTRA_NAME, mName);
            outState.putInt(EXTRA_MASS, mMass);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //because of this: http://stackoverflow.com/questions/14462456/returning-from-an-activity-using-navigateupfromsametask/16147110#16147110
        //appcompat default behaviour of button up/back is that it creates the activity anew with savedInstanceState=null
        onBackPressed();
        return true;
    }
}
