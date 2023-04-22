package com.example.noisereactor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class MainActivity extends AppCompatActivity {
    private static final int MICRO_PERMISSION_CODE = 200;
    private final int norm = 60;
    private LineChart chart;
    private NoiseMeter nMeter;
    private TextView tLevel;
    private TextView tMax;
    private TextView tNCount;
    private boolean isRunning;
    private Handler mHandler;
    private final String SAVED_MAX = "saved_max";
    private final String SAVED_N = "saved_n";
    private int nCount;
    private int maxLevel;

    private Runnable myRunnable = new Runnable() {
        public void run() {
            int db = nMeter.getNoiseLevel();
            addEntry(db);
            if (db < 20) tLevel.setTextColor(getResources().getColor(R.color.dark_green));
            else if (db < 40 ) tLevel.setTextColor(getResources().getColor(R.color.green));
            else if (db < 60 ) tLevel.setTextColor(getResources().getColor(R.color.yellow_green));
            else if (db < 80 ) tLevel.setTextColor(getResources().getColor(R.color.yellow));
            else if (db < 100 ) tLevel.setTextColor(getResources().getColor(R.color.orange));
            else if (db < 120 ) tLevel.setTextColor(getResources().getColor(R.color.red));
            else tLevel.setTextColor(getResources().getColor(R.color.dark_red));
            if (db > maxLevel) {
                maxLevel = db;
                tMax.setText("Макс. значение:  " + Integer.toString(maxLevel) + " db");
            }
            if (db > norm) {
                nCount++;
                tNCount.setText("Превышения 60 db:  " + Integer.toString(nCount) + " c");
            }
            tLevel.setText("Уровень шума:  " + Integer.toString(db) + " db");
            mHandler.postDelayed(myRunnable, 10);
        }
    };

    private void addEntry(int event) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if(set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), event), 0);
            chart.notifyDataSetChanged();
            chart.setVisibleXRange(0, 20);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Real Time");
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tLevel = findViewById(R.id.textViewNoiseLevel);
        tMax = findViewById(R.id.textViewMaxNoise);
        tNCount = findViewById(R.id.textViewNCount);
        nMeter = new NoiseMeter();
        mHandler = new Handler();
        chart = findViewById(R.id.chart);
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText("");

        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        XAxis xi = chart.getXAxis();
        xi.setDrawGridLines(false);
        xi.setAvoidFirstLastClipping(true);

        YAxis yiLeft = chart.getAxisLeft();
        xi.setDrawGridLines(false);
        yiLeft.setAxisMaximum(120);
        yiLeft.setAxisMinimum(0);
        xi.setDrawGridLines(true);

        YAxis yiRight = chart.getAxisRight();
        yiRight.setEnabled(false);

        loadInfo();
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONTROLS)) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(this, new String[]
                        {android.Manifest.permission.RECORD_AUDIO},MICRO_PERMISSION_CODE);
        }
    }

    public void buttonStartClick(View view) {
        if (!isRunning) {
            isRunning = true;
            nMeter.start();
            Toast.makeText(this, "Микрофон включен", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(myRunnable,  10);
        }
    }

    private void stopRecording() {
        if (isRunning) {
            isRunning = false;
            mHandler.removeCallbacks(myRunnable);
            nMeter.stop();
            Toast.makeText(this, "Микрофон выключен", Toast.LENGTH_SHORT).show();
            tLevel.setTextAppearance(this, androidx.core.R.style.TextAppearance_Compat_Notification_Info);
            tLevel.setTextSize(20);
            tLevel.setText("Уровень шума:  0 db");
        }
    }

    public void buttonStopClick(View view) {
        stopRecording();
        saveInfo();
    }

    public void buttonResetClick(View view) {
        stopRecording();
        nCount = 0;
        maxLevel = 0;
        tMax.setText("Макс. значение:  0 db");
        tNCount.setText("Превышения 60 db:  0 раз");
        saveInfo();
        chart.clearValues();
        chart.setVisibleXRange(0,0);
        chart.notifyDataSetChanged();
        Toast.makeText(this, "Сброс данных", Toast.LENGTH_SHORT).show();
    }

    private void saveInfo() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(SAVED_MAX, maxLevel);
        ed.putInt(SAVED_N, nCount);
        ed.apply();
    }

    private void loadInfo() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        maxLevel = sPref.getInt(SAVED_MAX, 0);
        tMax.setText("Макс. значение:  " + Integer.toString(maxLevel) + " db");
        nCount = sPref.getInt(SAVED_N, 0);
        tNCount.setText("Превышения 60 db:  " + Integer.toString(nCount) + " c");
    }
}