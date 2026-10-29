package com.camiykrytoi.chessreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmScheduler {

    public static final int ID_TUE_10 = 101;
    public static final int ID_TUE_17 = 102;
    public static final int ID_FRI_10 = 103;
    public static final int ID_FRI_17 = 104;

    public static void scheduleAll(Context context) {
        scheduleSlot(context, ID_TUE_10, Calendar.TUESDAY, 10, 0, "в™џпёЏ Р’С‚РѕСЂРЅРёРє: СЃРµРіРѕРґРЅСЏ С€Р°С…РјР°С‚С‹!", "РќР°РїРѕРјРёРЅР°РЅРёРµ: СЃРµРіРѕРґРЅСЏ Р·Р°РЅСЏС‚РёСЏ. Р’С‹С…РѕРґ РёР· РґРѕРјР° РІ 17:30.");
        scheduleSlot(context, ID_TUE_17, Calendar.TUESDAY, 17, 0, "вЏ° 17:00 вЂ” РџРѕСЂР° РЅР° С€Р°С…РјР°С‚С‹!", "РЎРѕР±РёСЂР°Р№СЃСЏ! Р’С‹С…РѕРґ СЂРѕРІРЅРѕ РІ 17:30 (С‡РµСЂРµР· 30 РјРёРЅСѓС‚).");
        scheduleSlot(context, ID_FRI_10, Calendar.FRIDAY, 10, 0, "в™џпёЏ РџСЏС‚РЅРёС†Р°: СЃРµРіРѕРґРЅСЏ С€Р°С…РјР°С‚С‹!", "РќР°РїРѕРјРёРЅР°РЅРёРµ: СЃРµРіРѕРґРЅСЏ Р·Р°РЅСЏС‚РёСЏ. Р’С‹С…РѕРґ РёР· РґРѕРјР° РІ 17:30.");
        scheduleSlot(context, ID_FRI_17, Calendar.FRIDAY, 17, 0, "вЏ° 17:00 вЂ” РџРѕСЂР° РЅР° С€Р°С…РјР°С‚С‹!", "РЎРѕР±РёСЂР°Р№СЃСЏ! Р’С‹С…РѕРґ СЂРѕРІРЅРѕ РІ 17:30 (С‡РµСЂРµР· 30 РјРёРЅСѓС‚).");
    }

    public static void scheduleSlot(Context context, int reqCode, int dayOfWeek, int hour, int minute, String title, String text) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long now = System.currentTimeMillis();
        // If the calculated time has already passed this week, advance by 7 days
        if (c.getTimeInMillis() <= now) {
            c.add(Calendar.DAY_OF_YEAR, 7);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("EXTRA_ID", reqCode);
        intent.putExtra("EXTRA_TITLE", title);
        intent.putExtra("EXTRA_TEXT", text);
        intent.putExtra("EXTRA_DAY", dayOfWeek);
        intent.putExtra("EXTRA_HOUR", hour);
        intent.putExtra("EXTRA_MINUTE", minute);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pi = PendingIntent.getBroadcast(context, reqCode, intent, flags);

        // Bypass Doze mode and all background battery restrictions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        }
    }

    public static String getNextAlarmSummary() {
        Calendar next = null;
        int[][] slots = {
            {Calendar.TUESDAY, 10, 0},
            {Calendar.TUESDAY, 17, 0},
            {Calendar.FRIDAY, 10, 0},
            {Calendar.FRIDAY, 17, 0}
        };

        long now = System.currentTimeMillis();
        for (int[] slot : slots) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_WEEK, slot[0]);
            c.set(Calendar.HOUR_OF_DAY, slot[1]);
            c.set(Calendar.MINUTE, slot[2]);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() <= now) {
                c.add(Calendar.DAY_OF_YEAR, 7);
            }
            if (next == null || c.getTimeInMillis() < next.getTimeInMillis()) {
                next = c;
            }
        }

        if (next == null) return "РќР°РїРѕРјРёРЅР°РЅРёСЏ РЅРµ СѓСЃС‚Р°РЅРѕРІР»РµРЅС‹";
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM, РІ HH:mm", new Locale("ru"));
        return "РЎР»РµРґСѓСЋС‰РµРµ: " + sdf.format(next.getTime());
    }
}