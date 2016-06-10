package rzeh4n.meteorite.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Martin Řehánek on 9.6.16.
 */
public class MeteoriteProvider extends ContentProvider {

    public static final String LOG_TAG = MeteoriteProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    //content:rzeh4n.meteorites/meteorites
    private static final int METEORITES = 100;
    //content:rzeh4n.meteorites/meteorites/[meteorite_id]
    private static final int METEORITE_BY_ID = 101;

    private DbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MeteoriteContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, String.format("%s", MeteoriteContract.PATH_METEORITES), METEORITES);
        matcher.addURI(authority, String.format("%s/#", MeteoriteContract.PATH_METEORITES), METEORITE_BY_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            // /meteorites
            case METEORITES: {
                cursor = mDbHelper.getReadableDatabase().query(
                        MeteoriteContract.MeteoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // /meteorites/#
            case METEORITE_BY_ID: {
                long verbId = extractMeteoriteId(uri);
                cursor = mDbHelper.getReadableDatabase().query(
                        MeteoriteContract.MeteoriteEntry.TABLE_NAME,
                        projection,
                        MeteoriteContract.MeteoriteEntry._ID + "=?",
                        new String[]{String.valueOf(verbId)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException(uri.toString());

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case METEORITES:
                return MeteoriteContract.MeteoriteEntry.CONTENT_LIST_TYPE;
            case METEORITE_BY_ID:
                return MeteoriteContract.MeteoriteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case METEORITES: {
                long _id = mDbHelper.getWritableDatabase().insert(MeteoriteContract.MeteoriteEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    Uri insertedUri = MeteoriteContract.MeteoriteEntry.buildMeteoriteUri(_id);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return insertedUri;
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
            }
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        switch (sUriMatcher.match(uri)) {
            case METEORITES: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                int inserted = 0;
                db.beginTransaction();
                try {
                    for (ContentValues values : valuesArray) {
                        db.insert(MeteoriteContract.MeteoriteEntry.TABLE_NAME, null, values);
                        inserted++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    return inserted;
                }
            }
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //only deletion of all meteorites implemented
        switch (sUriMatcher.match(uri)) {
            // /meteorites
            case METEORITES: {
                return mDbHelper.getReadableDatabase().delete(MeteoriteContract.MeteoriteEntry.TABLE_NAME, null, null);
            }
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            // /meteorites/#
            case METEORITE_BY_ID: {
                //ignoring user defined selection
                selection = MeteoriteContract.MeteoriteEntry._ID + "=?";
                selectionArgs = new String[]{extractMeteoriteId(uri).toString()};
                return mDbHelper.getReadableDatabase().update(MeteoriteContract.MeteoriteEntry.TABLE_NAME, values, selection, selectionArgs);
            }
            default:
                throw new UnsupportedOperationException(uri.toString());
        }
    }

    private Long extractMeteoriteId(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(1));
    }
}
