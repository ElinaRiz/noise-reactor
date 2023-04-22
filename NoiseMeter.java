package com.example.noisereactor;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class NoiseMeter {
    private AudioRecord aRecorder = null;
    int minSize;

    public void start() {
        if (aRecorder == null) {
            try {
                minSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10;
                aRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
                aRecorder.startRecording();
            } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (aRecorder != null) {
            aRecorder.stop();
            aRecorder.release();
            aRecorder = null;
        }
    }

    public int getNoiseLevel() {
        int max = 0;
        if (aRecorder != null) {
            short[] buffer = new short[minSize];
            aRecorder.read(buffer, 0, minSize);
            for (short s : buffer) {
                if (Math.abs(s) > max)
                    max = Math.abs(s);
            }
        }
        return (int) Math.round(20 * Math.log10(max / 2700.0)) + 65;
    }
}