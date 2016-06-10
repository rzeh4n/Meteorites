package rzeh4n.meteorite.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Martin Řehánek on 9.6.16.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "meteorites.db";
    private static final int VERSION_INITIAL = 1;
    private static final int VERSION = VERSION_INITIAL;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String statementCreateTableMeteorite =
                "CREATE TABLE " + MeteoriteContract.MeteoriteEntry.TABLE_NAME + " (" +
                        MeteoriteContract.MeteoriteEntry._ID + " INTEGER PRIMARY KEY," +
                        MeteoriteContract.MeteoriteEntry.COLUMN_NAME + " TEXT," +
                        MeteoriteContract.MeteoriteEntry.COLUMN_MASS + " INTEGER," +
                        MeteoriteContract.MeteoriteEntry.COLUMN_LATITUDE + " REAL," +
                        MeteoriteContract.MeteoriteEntry.COLUMN_LONGITUDE + " REAL" +
                        " );";
        String statementCreateIndexMeteoriteMass =
                "CREATE INDEX " + MeteoriteContract.MeteoriteEntry.TABLE_NAME + "_" + MeteoriteContract.MeteoriteEntry.COLUMN_MASS +
                        " ON " + MeteoriteContract.MeteoriteEntry.TABLE_NAME +
                        "(" + MeteoriteContract.MeteoriteEntry.COLUMN_MASS + ");";

        db.execSQL(statementCreateTableMeteorite);
        db.execSQL(statementCreateIndexMeteoriteMass);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //nothing yet
    }
}
