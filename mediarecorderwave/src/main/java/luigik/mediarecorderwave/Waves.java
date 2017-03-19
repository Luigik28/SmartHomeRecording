package luigik.mediarecorderwave;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.view.View;

import com.cleveroad.audiovisualization.GLAudioVisualizationView;

import java.util.Timer;
import java.util.TimerTask;

public class Waves {

    private GLAudioVisualizationView visualizationView = null;
    private VisualizerHandler visualizerHandler = null;
    private Timer timer = null;

    private Waves() {
    }

    public Waves(GLAudioVisualizationView visualizationView) {
        this.visualizationView = visualizationView;
    }

    public void start(final MediaRecorder mediaRecorder, long period) {
        visualizerHandler = new VisualizerHandler();
        visualizationView.linkTo(visualizerHandler);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                visualizerHandler.onDataReceived((float) mediaRecorder.getMaxAmplitude());
            }
        }, 0, 250);

    }

    public void start(MediaRecorder mediaRecorder) {
        start(mediaRecorder, 250);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        visualizationView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
            visualizerHandler = null;
        }

    }


    public View getVisualizationView() {
        return visualizationView;
    }

    public static class Builder extends GLAudioVisualizationView.Builder {


        public Builder(@NonNull Context context) {
            super(context);
        }
    }
}
