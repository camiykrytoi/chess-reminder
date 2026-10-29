package com.camiykrytoi.chessreminder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvNextAlarm;
    private Button btnBatteryOpt;
    private Button btnExactAlarm;
    private Button btnReschedule;
    private Button btnTestAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvNextAlarm = findViewById(R.id.tvNextAlarm);
        btnBatteryOpt = findViewById(R.id.btnBatteryOpt);
        btnExactAlarm = findViewById(R.id.btnExactAlarm);
        btnReschedule = findViewById(R.id.btnReschedule);
        btnTestAlarm = findViewById(R.id.btnTestAlarm);

        // Check & request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Schedule alarms on first launch
        AlarmScheduler.scheduleAll(this);
        updateStatus();

        btnBatteryOpt.setOnClickListener(v -> requestBatteryOptimizationBypass());
        btnExactAlarm.setOnClickListener(v -> requestExactAlarmPermission());

        btnReschedule.setOnClickListener(v -> {
            AlarmScheduler.scheduleAll(this);
            updateStatus();
            Toast.makeText(this, "Р‘СѓРґРёР»СЊРЅРёРєРё СѓСЃРїРµС€РЅРѕ РѕР±РЅРѕРІР»РµРЅС‹!", Toast.LENGTH_SHORT).show();
        });

        btnTestAlarm.setOnClickListener(v -> {
            AlarmReceiver.showNotification(
                this,
                999,
                "в™џпёЏ РўР•РЎРў: РЁР°С…РјР°С‚С‹ СЂР°Р±РѕС‚Р°СЋС‚!",
                "РџСЂРѕРІРµСЂРєР° РіСЂРѕРјРєРѕСЃС‚Рё Рё РІРёР±СЂР°С†РёРё. РќР°РїРѕРјРёРЅР°РЅРёСЏ СЃСЂР°Р±РѕС‚Р°СЋС‚ РІРѕ Р’С‚ Рё РџС‚ РІ 10:00 Рё 17:00!"
            );
            Toast.makeText(this, "РўРµСЃС‚РѕРІРѕРµ СѓРІРµРґРѕРјР»РµРЅРёРµ РѕС‚РїСЂР°РІР»РµРЅРѕ!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        tvNextAlarm.setText(AlarmScheduler.getNextAlarmSummary());

        // Update Battery button state
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm.isIgnoringBatteryOptimizations(getPackageName())) {
                btnBatteryOpt.setText("вњ… Р‘Р°С‚Р°СЂРµСЏ: Р±РµР· РѕРіСЂР°РЅРёС‡РµРЅРёР№");
                btnBatteryOpt.setEnabled(false);
                btnBatteryOpt.setBackgroundColor(0xFF16A34A);
            } else {
                btnBatteryOpt.setText("вљЎ РћС‚РєР»СЋС‡РёС‚СЊ РѕРїС‚РёРјРёР·Р°С†РёСЋ Р±Р°С‚Р°СЂРµРё");
                btnBatteryOpt.setEnabled(true);
            }
        }

        // Update Exact Alarm button state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && am.canScheduleExactAlarms()) {
                btnExactAlarm.setText("вњ… РўРѕС‡РЅС‹Рµ Р±СѓРґРёР»СЊРЅРёРєРё: СЂР°Р·СЂРµС€РµРЅС‹");
                btnExactAlarm.setEnabled(false);
                btnExactAlarm.setBackgroundColor(0xFF16A34A);
            } else {
                btnExactAlarm.setText("вЏ° Р Р°Р·СЂРµС€РёС‚СЊ С‚РѕС‡РЅС‹Рµ Р±СѓРґРёР»СЊРЅРёРєРё");
                btnExactAlarm.setEnabled(true);
            }
        } else {
            btnExactAlarm.setVisibility(android.view.View.GONE);
        }
    }

    private void requestBatteryOptimizationBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
            }
        }
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception ignored) {}
        }
    }
}