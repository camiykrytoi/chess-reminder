package com.camiykrytoi.chessreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "chess_channel_silent_v2";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ChessReminder:AlarmWakeLock");
            wakeLock.acquire(10000);
        }

        String title = intent.getStringExtra("EXTRA_TITLE");
        String text = intent.getStringExtra("EXTRA_TEXT");
        int reqCode = intent.getIntExtra("EXTRA_ID", 100);
        int dayOfWeek = intent.getIntExtra("EXTRA_DAY", -1);
        int hour = intent.getIntExtra("EXTRA_HOUR", -1);
        int minute = intent.getIntExtra("EXTRA_MINUTE", -1);

        if (title == null) title = "в™џпёЏ РџРѕСЂР° РЅР° С€Р°С…РјР°С‚С‹!";
        if (text == null) text = "РќР°РїРѕРјРёРЅР°РЅРёРµ РїРѕ СЂР°СЃРїРёСЃР°РЅРёСЋ.";

        showNotification(context, reqCode, title, text);

        if (dayOfWeek != -1 && hour != -1 && minute != -1) {
            AlarmScheduler.scheduleSlot(context, reqCode, dayOfWeek, hour, minute, title, text);
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            try {
                wakeLock.release();
            } catch (Exception ignored) {}
        }
    }

    public static void showNotification(Context context, int notificationId, String title, String text) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        // Channel strictly without sound and without vibration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "РЁР°С…РјР°С‚С‹ (РўРѕР»СЊРєРѕ СЃРѕРѕР±С‰РµРЅРёРµ)",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("РўРёС…РёРµ С‚РµРєСЃС‚РѕРІС‹Рµ РЅР°РїРѕРјРёРЅР°РЅРёСЏ Р±РµР· Р·РІСѓРєР° Рё РІРёР±СЂР°С†РёРё");
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null); // РџРѕР»РЅРѕСЃС‚СЊСЋ Р±РµР· Р·РІСѓРєР°
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentPi = PendingIntent.getActivity(context, notificationId, openIntent, flags);

        // Silent banner notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(null)      // Р‘Р•Р— Р—Р’РЈРљРђ
            .setVibrate(null)    // Р‘Р•Р— Р’РР‘Р РђР¦РР
            .setAutoCancel(true)
            .setContentIntent(contentPi);

        nm.notify(notificationId, builder.build());
    }
}