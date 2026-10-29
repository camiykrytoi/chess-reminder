package com.camiykrytoi.chessreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "chess_channel_high_priority";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Wake up device CPU so notification is handled reliably in Doze mode
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ChessReminder:AlarmWakeLock");
            wakeLock.acquire(15000); // 15 seconds
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

        // 2. Reschedule next week's occurrence automatically
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

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (soundUri == null) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "РЁР°С…РјР°С‚С‹ (Р“СЂРѕРјРєРёРµ РЅР°РїРѕРјРёРЅР°РЅРёСЏ)",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Р’Р°Р¶РЅС‹Рµ РЅР°РїРѕРјРёРЅР°РЅРёСЏ Рѕ С€Р°С…РјР°С‚Р°С… РІРѕ РІС‚РѕСЂРЅРёРє Рё РїСЏС‚РЅРёС†Сѓ");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 800, 300, 800, 300, 800});
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
            channel.setSound(soundUri, audioAttributes);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.setBypassDnd(true);
            nm.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentPi = PendingIntent.getActivity(context, notificationId, openIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(new long[]{0, 800, 300, 800, 300, 800})
            .setAutoCancel(true)
            .setContentIntent(contentPi);

        nm.notify(notificationId, builder.build());
    }
}