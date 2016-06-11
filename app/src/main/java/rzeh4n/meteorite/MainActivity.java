package rzeh4n.meteorite;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import rzeh4n.meteorite.data.MeteoriteContract;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mAppbar;
    private int mMeteoritesCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mAppbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle(String.format("%d meteorites", getMeteoritesCount()));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
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

}
