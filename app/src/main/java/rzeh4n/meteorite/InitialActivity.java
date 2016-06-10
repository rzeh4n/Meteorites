package rzeh4n.meteorite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import rzeh4n.meteorite.data.Synchronizer;

public class InitialActivity extends AppCompatActivity {

    public static final String LOG_TAG = InitialActivity.class.getSimpleName();


    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.progressText) TextView mProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        boolean initializeNow = true;
        if (initializeNow) {
            new Synchronizer(this, new Synchronizer.Listener() {
                @Override
                public void onFinished() {
                    startActivity(new Intent(InitialActivity.this, MainActivity.class));
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(InitialActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onProgress(int percentage) {
                    mProgressBar.setIndeterminate(false);
                    //Log.i(LOG_TAG, "progress: " + percentage);
                    mProgressBar.setProgress(percentage);
                    mProgressText.setText(String.format("%d %%", percentage));
                }
            }).run();
        } else {
            Log.i(LOG_TAG, "database initialized already");
            startActivity(new Intent(InitialActivity.this, MainActivity.class));
        }
    }

}
