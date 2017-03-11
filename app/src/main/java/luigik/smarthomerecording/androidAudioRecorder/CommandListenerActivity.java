package luigik.smarthomerecording.androidAudioRecorder;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.cleveroad.audiovisualization.GLAudioVisualizationView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import luigik.smarthomerecording.Client.Server;
import luigik.smarthomerecording.R;

public class CommandListenerActivity extends AppCompatActivity {

    private static String filePath;
    private int color;

    private MediaRecorder recorder;

    private static final String EXTRA_COLOR = "color";

    private GLAudioVisualizationView visualizerView = null;
    private VisualizerHandler visualizerHandler = null;
    private ImageButton buttonMic = null;
    private ImageButton buttonStop = null;
    private ProgressBar progressBar = null;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_listner);
        filePath = getExternalCacheDir() + "/audio_command.3gp";
        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(EXTRA_COLOR);
        } else {
            color = getIntent().getIntExtra(EXTRA_COLOR, Color.RED);
        }
        //KEEP SCREEN ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(Util.getDarkerColor(color)));
        }

        RelativeLayout contentLayout = (RelativeLayout) findViewById(R.id.contentLayout);
        contentLayout.setBackgroundColor(Util.getDarkerColor(color));
        buttonMic = (ImageButton) findViewById(R.id.buttonMic);
        buttonStop = (ImageButton) findViewById(R.id.buttonStop);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        buttonStop.setVisibility(View.INVISIBLE);
        buttonStop.setClickable(false);

        if (Util.isBrightColor(color)) {
            buttonMic.setColorFilter(Color.BLACK);
            buttonStop.setColorFilter(Color.BLACK);
        }

        visualizerView = new GLAudioVisualizationView.Builder(this)
                .setLayersCount(1)
                .setWavesCount(6)
                .setWavesHeight(R.dimen.aar_wave_height)
                .setWavesFooterHeight(R.dimen.aar_footer_height)
                .setBubblesPerLayer(100)
                .setBubblesSize(R.dimen.aar_bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(Util.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build();

        contentLayout.addView(visualizerView, 0);

        timer = new Timer();

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            visualizerView.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            visualizerView.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        try {
            visualizerView.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_COLOR, color);
        super.onSaveInstanceState(outState);
    }

    public void listen(View view) {
        buttonMic.setVisibility(View.INVISIBLE);
        buttonStop.setVisibility(View.VISIBLE);
        buttonMic.setClickable(false);
        buttonStop.setClickable(true);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(filePath);
        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        visualizerHandler = new VisualizerHandler();
        visualizerView.linkTo(visualizerHandler);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                visualizerHandler.onDataReceived((float) recorder.getMaxAmplitude());
            }
        }, 0, 250);
    }

    public void stop(View view) {
        buttonStop.setVisibility(View.INVISIBLE);
        buttonStop.setClickable(false);
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        recorder.stop();
        recorder.release();
        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }
        progressBar.setVisibility(View.VISIBLE);
        new AsyncSend().execute();
    }


    private class AsyncSend extends AsyncTask<Object, Object, Object> {
        protected Object doInBackground(Object[] params) {
            try {
                new Server("192.168.1.106", 8080).sendFile(filePath);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            CommandListenerActivity.this.setResult(RESULT_OK);
            CommandListenerActivity.this.finish();
        }
    }
}
