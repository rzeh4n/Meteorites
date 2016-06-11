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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import rzeh4n.meteorite.data.CursorRecyclerViewAdapter;
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

    @BindView(R.id.list) RecyclerView mList;

    private CursorRecyclerViewAdapter mCursorRecyclerViewAdapter;
    private int mPosition = ListView.INVALID_POSITION;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_meteorite_list, container, false);
        ButterKnife.bind(this, root);
        mCursorRecyclerViewAdapter = new RecyclerViewAdapter(getActivity(), null);
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setAdapter(mCursorRecyclerViewAdapter);
        mList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        return root;
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
    public void onMessageEvent(ItemClickEvent event) {
        mPosition = event.position;
        //go to map activity
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_ID, event.meteoriteId);
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
        mCursorRecyclerViewAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mList.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorRecyclerViewAdapter.swapCursor(null);
    }

    public static class Meteorite {
        public final Long id;
        public final String name;
        public final Integer mass;

        public Meteorite(Long id, String name, Integer mass) {
            this.id = id;
            this.name = name;
            this.mass = mass;
        }

        public static Meteorite fromCursor(Cursor cursor) {
            return new Meteorite(cursor.getLong(CURSOR_COL_ID), cursor.getString(CURSOR_COL_NAME), cursor.getInt(CURSOR_COL_MASS));
        }
    }


    public static class RecyclerViewAdapter extends CursorRecyclerViewAdapter<RecyclerViewAdapter.ViewHolder> {

        public RecyclerViewAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private Long mId;
            private int mPosition;
            @BindView(R.id.name) TextView mName;
            @BindView(R.id.mass) TextView mMass;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                view.setOnClickListener(this);
            }

            public void bind(Meteorite meteorite, int position) {
                mId = meteorite.id;
                mPosition = position;
                mName.setText(meteorite.name);
                mMass.setText(Utils.formatMass(meteorite.mass));
            }

            @Override
            public void onClick(View v) {
                //Log.i(LOG_TAG, "clicked: " + mId);
                EventBus.getDefault().post(new ItemClickEvent(mId, mPosition));
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
            Meteorite meteorite = Meteorite.fromCursor(cursor);
            viewHolder.bind(meteorite, cursor.getPosition());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meteorite, parent, false);
            ViewHolder vh = new ViewHolder(itemView);
            return vh;
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

    public static class ItemClickEvent {
        public final Long meteoriteId;
        public final Integer position;

        public ItemClickEvent(Long meteoriteId, int position) {
            this.meteoriteId = meteoriteId;
            this.position = position;
        }
    }

}
