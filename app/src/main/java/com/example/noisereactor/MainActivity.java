package com.example.noisereactor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private int norm;
    private String info_norm;
    private LineChart chart;
    private NoiseMeter nMeter;
    private TextView textViewNoiseLevel;
    private TextView textViewMaxNoise;
    private TextView textViewNoiseCount;
    private Handler mHandler;
    private final String SAVED_MAX = "saved_max";
    private final String SAVED_N = "saved_n";
    private int nCount;
    private int maxLevel;

    private MyCustomAdapter customAdapter;
    private ListView list;

    private final Runnable myRunnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            int db = nMeter.getNoiseLevel();
            addEntry(db);
            if (db < 20) textViewNoiseLevel.setTextColor(getResources().getColor(R.color.dark_green));
            else if (db < 40 ) textViewNoiseLevel.setTextColor(getResources().getColor(R.color.green));
            else if (db < 60 ) textViewNoiseLevel.setTextColor(getResources().getColor(R.color.yellow_green));
            else if (db < 80 ) textViewNoiseLevel.setTextColor(getResources().getColor(R.color.yellow));
            else if (db < 100 ) textViewNoiseLevel.setTextColor(getResources().getColor(R.color.orange));
            else if (db < 120 ) textViewNoiseLevel.setTextColor(getResources().getColor(R.color.red));
            else textViewNoiseLevel.setTextColor(getResources().getColor(R.color.dark_red));
            if (db > maxLevel) {
                maxLevel = db;
                textViewMaxNoise.setText("Макс. значение:  " + maxLevel + " db");
            }
            if (db > norm) {
                nCount++;
                textViewNoiseCount.setText("Превышения " + norm + " db:  " + nCount + " c");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                customAdapter.addItem(new Cell(currentDateandTime, Integer.toString(db), info_norm));
            }
            textViewNoiseLevel.setText("Уровень шума:  " + db + " db");
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
            chart.setVisibleXRange(0, 50);
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        textViewNoiseLevel = findViewById(R.id.textViewNoiseLevel);
        textViewMaxNoise = findViewById(R.id.textViewMaxNoise);
        textViewNoiseCount = findViewById(R.id.textViewNoiseCount);
        nMeter = new NoiseMeter();
        mHandler = new Handler();
        chart = findViewById(R.id.lineChart);
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText("");

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
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

        norm = 60;
        info_norm = "not defined       ";
        maxLevel = 0;
        nCount = 0;
        loadInfo();
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONTROLS)) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(this, new String[]
                        {android.Manifest.permission.RECORD_AUDIO},200);
        }

        list = (ListView) findViewById(R.id.tableInvoices);
        customAdapter = new MyCustomAdapter(this);
        list.setAdapter(customAdapter);
    }

    public void buttonSettingClick(View view) {
        if(!nMeter.isRunning()) {
            final String[] textNorm = {
                    "Палаты больниц днём (35 db - макс.50)",
                    "Палаты больниц ночью (25 db - макс.40)",
                    "Кабинеты врачей (35 db - макс.50)",
                    "Учебные помещения (40 db - макс.55)",
                    "Жилые комнаты днём (40 db - макс.55)",
                    "Жилые комнаты ночью (30 db - макс.45)",
                    "Номера гостиниц днём (45 db - макс.60)",
                    "Номера гостиниц ночью (35 db - макс.50)",
                    "Заведения общественного питания (55 db - макс.70)",
                    "Торговые залы (60 db - макс.75)",
                    "Территории, прилегающие к зданиям больниц днём (45 db - макс.60)",
                    "Территории, прилегающие к зданиям больниц ночью (35 db - макс.50)",
                    "Территории, прилегающие к жилым домам днём (55 db - макс.70)",
                    "Территории, прилегающие к жилым домам ночью (45 db - макс.60)",
                    "Территории, прилегающие к гостиницам днём (60 db - макс.75)",
                    "Территории, прилегающие к гостиницам ночью (50 db - макс.65)",
                    "Площадка отдыха на территории больниц (35 db - макс.50)",
                    "Площадки отдыха на территории микрорайонов 45 db - макс.60)",
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Выберите помещение")
                    .setSingleChoiceItems(textNorm, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0 :
                                case 7:
                                case 2:
                                case 11:
                                case 16:
                                    norm = 35;
                                    info_norm = "35 db - макс.50";
                                    break;
                                case 1 :
                                    norm = 25;
                                    info_norm = "25 db - макс.40";
                                    break;
                                case 3:
                                case 4:
                                    norm = 40;
                                    info_norm = "40 db - макс.55";
                                    break;
                                case 5:
                                    norm = 30;
                                    info_norm = "30 db - макс.45";
                                    break;
                                case 6:
                                case 10:
                                case 13:
                                case 17:
                                    norm = 45;
                                    info_norm = "45 db - макс.60";
                                    break;
                                case 8:
                                case 12:
                                    norm = 55;
                                    info_norm = "55 db - макс.70";
                                    break;
                                case 9:
                                case 14:
                                    norm = 60;
                                    info_norm = "60 db - макс.75";
                                    break;
                                case 15:
                                    norm = 50;
                                    info_norm = "50 db - макс.65";
                                    break;
                            }
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            textViewNoiseCount.setText("Превышения " + norm + " db:  " + nCount + " c");
                        }
                    })
                    .setNegativeButton("По-умолчанию", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            norm = 60;
                            info_norm = "not defined       ";
                            textViewNoiseCount.setText("Превышения " + norm + " db:  " + nCount + " c");
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else Toast.makeText(this, "Остановите запись", Toast.LENGTH_SHORT).show();
    }

    public void buttonStartClick(View view) {
        if (!nMeter.isRunning()) {
            nMeter.start();
            Toast.makeText(this, "Микрофон включен", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(myRunnable,  10);
        }
    }

    @SuppressLint("SetTextI18n")
    private void stopRecording() {
        if (nMeter.isRunning()) {
            mHandler.removeCallbacks(myRunnable);
            nMeter.stop();
            Toast.makeText(this, "Микрофон выключен", Toast.LENGTH_SHORT).show();
            textViewNoiseLevel.setTextAppearance(this, androidx.core.R.style.TextAppearance_Compat_Notification_Info);
            textViewNoiseLevel.setTextSize(20);
            textViewNoiseLevel.setText("Уровень шума:  0 db");
        }
    }

    public void buttonStopClick(View view) {
        stopRecording();
        saveInfo();
    }

    @SuppressLint("SetTextI18n")
    public void buttonResetClick(View view) {
        if(!nMeter.isRunning()){
            norm = 60;
            info_norm = "not defined       ";
            nCount = 0;
            maxLevel = 0;
            textViewMaxNoise.setText("Макс. значение:  0 db");
            textViewNoiseCount.setText("Превышения " + norm + " db:  0 с");
            saveInfo();
            chart.clearValues();
            chart.setVisibleXRange(0,0);
            chart.notifyDataSetChanged();
            customAdapter.clear();
            Toast.makeText(this, "Сброс данных", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "Остановите запись", Toast.LENGTH_SHORT).show();
    }

    private void saveInfo() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(SAVED_MAX, maxLevel);
        ed.putInt(SAVED_N, nCount);
        ed.apply();
    }

    @SuppressLint("SetTextI18n")
    private void loadInfo() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        maxLevel = sPref.getInt(SAVED_MAX, 0);
        textViewMaxNoise.setText("Макс. значение:  " + maxLevel + " db");
        nCount = sPref.getInt(SAVED_N, 0);
        textViewNoiseCount.setText("Превышения " + norm + " db:  " + nCount + " c");
    }
}