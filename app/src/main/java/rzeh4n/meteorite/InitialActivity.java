package rzeh4n.meteorite;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Random;

import rzeh4n.meteorite.data.MeteoriteContract;

public class InitialActivity extends AppCompatActivity {

    public static final String LOG_TAG = InitialActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        initData();
    }

    private void initData() {
        //just for testing, won't bother with worker thread here
        boolean initializeNow = true;
        if (initializeNow) {
            Log.i(LOG_TAG, "initializing database");
            //delete all
            int deleted = getContentResolver().delete(MeteoriteContract.MeteoriteEntry.CONTENT_URI, null, null);
            Log.i(LOG_TAG, String.format("deleted %d records", deleted));
            //insert fake data
            insert(1l, "Flyweight Fry", 1);
            insert(2l, "Big Ben", 1000);
            insert(3l, "Typical Tony", 500);
            Random rand = new Random();
            for (int i = 1; i <= 20; i++) {
                insert((long) 5 + i, "Random Randy " + i, rand.nextInt(1000) + 1);
            }
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Log.i(LOG_TAG, "database initialized already");
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void insert(Long id, String name, Integer mass) {
        ContentValues contentValues = buildContentValues(id, name, mass);
        Uri insertedUri = getContentResolver().insert(MeteoriteContract.MeteoriteEntry.CONTENT_URI, contentValues);
        //Log.d(LOG_TAG, "inserted: " + ContentUris.parseId(insertedUri));
        Log.d(LOG_TAG, "inserted: " + insertedUri);
    }


    private ContentValues buildContentValues(Long id, String name, Integer mass) {
        ContentValues values = new ContentValues();
        values.put(MeteoriteContract.MeteoriteEntry._ID, id);
        values.put(MeteoriteContract.MeteoriteEntry.COLUMN_NAME, name);
        values.put(MeteoriteContract.MeteoriteEntry.COLUMN_MASS, mass);
        return values;
    }

    public static class Meteorite {
        private final String name;
        private final long id;
        private final int weight;

        public Meteorite(String name, long id, int weight) {
            this.name = name;
            this.id = id;
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }

        public int getWeight() {
            return weight;
        }
    }
}
