package rzeh4n.meteorite;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rzeh4n.meteorite.data.MeteoriteContract;

/**
 * Created by Martin Řehánek on 9.6.16.
 */
public class MeteoriteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MeteoriteListFragment.class.getSimpleName();

    private static final String[] COLUMNS = {
            MeteoriteContract.MeteoriteEntry.TABLE_NAME + "." + MeteoriteContract.MeteoriteEntry._ID,
            MeteoriteContract.MeteoriteEntry.COLUMN_NAME,
            MeteoriteContract.MeteoriteEntry.COLUMN_MASS
    };

    private static final int CURSOR_COL_ID = 0;
    private static final int CURSOR_COL_NAME = 1;
    private static final int CURSOR_COL_MASS = 2;

    private static final String STATE_KEY_SELECTED_POSITION = "selected_position";
    private static final int LOADER = 0;

    @BindView(R.id.list)
    ListView mListView;

    private ListAdapter mListAdapter;
    private int mPosition = ListView.INVALID_POSITION;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_meteorite_list, container, false);
        ButterKnife.bind(this, root);
        mListAdapter = new ListAdapter(getActivity(), null, 0);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                Log.i(LOG_TAG, "clicked: " + id);
                openMapActivity(id);
            }
        });
        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.i(LOG_TAG, "selected: " + id);
                mPosition = position;
                openMapActivity(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return root;
    }

    private void openMapActivity(long id) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_ID, id);
        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MeteoriteContract.MeteoriteEntry.COLUMN_MASS + " DESC";
        return new CursorLoader(getActivity(),
                MeteoriteContract.MeteoriteEntry.CONTENT_URI,
                COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListAdapter.swapCursor(null);
    }

    public static class ListAdapter extends CursorAdapter {

        public ListAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_meteorite, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.name)).setText(cursor.getString(CURSOR_COL_NAME));
            ((TextView) view.findViewById(R.id.mass)).setText(String.format("%d g", cursor.getInt(CURSOR_COL_MASS)));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_KEY_SELECTED_POSITION, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

}
