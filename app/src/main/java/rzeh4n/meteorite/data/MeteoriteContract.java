package rzeh4n.meteorite.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Martin Řehánek on 9.6.16.
 */
public class MeteoriteContract {

    public static final String CONTENT_AUTHORITY = "rzeh4n.meteorites";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_METEORITES = "meteorites";

    public static final class MeteoriteEntry implements BaseColumns {

        public static final String TABLE_NAME = "meteorite";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_YEAR = "year";

        public static final String COLUMN_MASS = "mass";

        public static final String COLUMN_LATITUDE = "lat";

        public static final String COLUMN_LONGTITUDE = "long";


        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_METEORITES).build();

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        public static Uri buildMeteoriteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
