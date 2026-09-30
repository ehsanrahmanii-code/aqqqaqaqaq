package com.arsam.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs the ARSAM Flask engine in a background thread under a foreground service
 * so MIUI/Android 15 is less likely to kill it immediately.
 */
public class EngineService extends Service {
    public static final String CHANNEL_ID = "arsam_engine";
    public static final int NOTIF_ID = 5101;
    public static final String ACTION_STARTED = "com.arsam.app.ENGINE_STARTED";
    public static final String ACTION_FAILED = "com.arsam.app.ENGINE_FAILED";
    public static final String EXTRA_ERROR = "error";
    public static final String EXTRA_PORT = "port";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannel();
        startForeground(NOTIF_ID, buildNotification("ARSAM در حال اجرا…"));
        if (started.compareAndSet(false, true)) {
            executor.execute(this::bootEngine);
        }
        return START_STICKY;
    }

    private void bootEngine() {
        try {
            Python py = Python.getInstance();
            PyObject mod = py.getModule("arsam_boot");
            // start_server blocks (Flask) — OK on this worker thread
            mod.callAttr("start_server", 8080, "127.0.0.1");
        } catch (Throwable t) {
            Intent fail = new Intent(ACTION_FAILED);
            fail.setPackage(getPackageName());
            fail.putExtra(EXTRA_ERROR, String.valueOf(t.getMessage()));
            sendBroadcast(fail);
            stopSelf();
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "ARSAM Engine", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("موتور تحلیل ARSAM");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String text) {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ARSAM")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        started.set(false);
        executor.shutdownNow();
        super.onDestroy();
    }
}
