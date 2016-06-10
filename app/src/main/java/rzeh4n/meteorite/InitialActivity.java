package rzeh4n.meteorite;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import rzeh4n.meteorite.synchronization.Synchronizer;

public class InitialActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String LOG_TAG = InitialActivity.class.getSimpleName();

    @BindView(R.id.progresTitle) TextView mProgressTitle;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.progressPercentage) TextView mProgressPercentage;
    @BindView(R.id.btnRetry) Button mBtnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        ButterKnife.bind(this);
        mBtnRetry.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean synchronizedBefore = Utils.getPreferences(this).getBoolean(getResources().getString(R.string.pref_synchronized_before), false);
        long lastSynchronized = Utils.getPreferences(this).getLong(getResources().getString(R.string.pref_last_synchronized), 0);
        if (synchronizedBefore) {
            Log.i(LOG_TAG, "last synchronized: " + new Date(lastSynchronized).toString());
            startActivity(new Intent(InitialActivity.this, MainActivity.class));
            finish();
        } else {
            Log.i(LOG_TAG, "not synchronized yet");
            synchronize();
        }
    }

    private void synchronize() {
        // TODO: 10.6.16 Move synchronization into Fragment without view so that it survives orientation change
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            mProgressTitle.setText(getResources().getString(R.string.sync_title_no_network));
            mProgressBar.setProgress(0);
            mProgressPercentage.setText("0 %");
            mBtnRetry.setVisibility(View.VISIBLE);
            Snackbar.make(mProgressBar.getRootView(), R.string.sync_snackbar_enable_network, Snackbar.LENGTH_LONG)
                    .show();
        } else {//synchronize now
            mBtnRetry.setVisibility(View.INVISIBLE);
            mProgressTitle.setText(getResources().getString(R.string.sync_title_synchronizing));
            new Synchronizer(this, new Synchronizer.Listener() {
                @Override
                public void onFinished() {
                    startActivity(new Intent(InitialActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String message) {
                    Log.e(LOG_TAG, message);
                    mBtnRetry.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(0);
                    mProgressPercentage.setText("0 %");
                    mProgressTitle.setText(getResources().getString(R.string.sync_title_network_error));
                    Snackbar.make(mProgressBar.getRootView(), R.string.sync_title_network_error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.btn_retry, InitialActivity.this)
                            .show();
                }

                @Override
                public void onProgress(int percentage) {
                    mProgressBar.setIndeterminate(false);
                    //Log.i(LOG_TAG, "progress: " + percentage);
                    mProgressBar.setProgress(percentage);
                    mProgressPercentage.setText(String.format("%d %%", percentage));
                }
            }).run();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnRetry) {
            synchronize();
        } else { //must have been snackbar
            synchronize();
        }
    }

}
