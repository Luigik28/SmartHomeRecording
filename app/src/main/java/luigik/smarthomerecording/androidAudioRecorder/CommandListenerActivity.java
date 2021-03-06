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

import luigik.mediarecorderwave.Util;
import luigik.mediarecorderwave.Waves;
import luigik.smarthomerecording.Client.Server;
import luigik.smarthomerecording.R;

public class CommandListenerActivity extends AppCompatActivity {

    private static final String EXTRA_COLOR = "color";
    private static String filePath;
    private int color;
    private MediaRecorder recorder;
    private ImageButton buttonMic = null;
    private ImageButton buttonStop = null;
    private ProgressBar progressBar = null;

    private Waves waves = null;

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

        waves = new Waves(new Waves.Builder(this)
                .setLayersCount(1)
                .setWavesCount(6)
                .setWavesHeight(R.dimen.aar_wave_height)
                .setWavesFooterHeight(R.dimen.aar_footer_height)
                .setBubblesPerLayer(100)
                .setBubblesSize(R.dimen.aar_bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(Util.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build());

        contentLayout.addView(waves.getVisualizationView(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ((GLAudioVisualizationView) waves.getVisualizationView()).onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            ((GLAudioVisualizationView) waves.getVisualizationView()).onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        try {
            ((GLAudioVisualizationView) waves.getVisualizationView()).release();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        waves.start(recorder, 150);
    }

    public void stop(View view) {
        buttonStop.setVisibility(View.INVISIBLE);
        buttonStop.setClickable(false);
        waves.stop();
        recorder.stop();
        recorder.release();
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
