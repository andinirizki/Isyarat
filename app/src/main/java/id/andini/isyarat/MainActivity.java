package id.andini.isyarat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private FrameLayout rootLayout;
    private TextToSpeech tts;

    private int score = 120;
    private int lives = 3;
    private int level = 0;

    private final int QUESTIONS_PER_LEVEL = 3;

    private boolean bigText = false;
    private boolean contrastMode = false;
    private boolean soundOn = false;
    private boolean answerLocked = false;
    private boolean settingsOpen = false;

    private boolean reminderOn = false;
    private int reminderHour = 19;
    private int reminderMinute = 0;

    private static final String CHANNEL_ID = "pengingat_belajar_channel";
    private static final int REMINDER_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_CODE = 2001;

    private static final String PREF_NAME = "pengingat_belajar_pref";
    private static final String KEY_REMINDER_ON = "reminder_on";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final String KEY_NOTE_TEXT = "note_text";

    private final int PINK = Color.parseColor("#F24F89");
    private final int PURPLE = Color.parseColor("#8D55D8");
    private final int BLUE = Color.parseColor("#45AEEA");
    private final int DARK = Color.parseColor("#233044");
    private final int GREEN = Color.parseColor("#42C7A5");
    private final int ORANGE = Color.parseColor("#FF9A57");

    private final String[] gameMateri = {
            "Halo",
            "Terima kasih",
            "Maaf",
            "Tolong",
            "Iya",
            "Makan",
            "Minum",
            "Sekolah",
            "Senang"
    };

    private final int[] gameGambarMateri = {
            R.drawable.img_halo,
            R.drawable.img_terima_kasih,
            R.drawable.img_maaf,
            R.drawable.img_tolong,
            R.drawable.img_iya,
            R.drawable.img_makan,
            R.drawable.img_minum,
            R.drawable.img_sekolah,
            R.drawable.img_senang
    };

    private final String[][] gamePilihan = {
            {"Halo", "Terima Kasih", "Maaf", "Tolong"},
            {"Halo", "Terima Kasih", "Maaf", "Tolong"},
            {"Maaf", "Iya", "Tidak", "Halo"},

            {"Tolong", "Maaf", "Terima Kasih", "Tidak"},
            {"Tidak", "Halo", "Iya", "Tolong"},
            {"Makan", "Minum", "Sekolah", "Senang"},

            {"Minum", "Makan", "Halo", "Maaf"},
            {"Rumah", "Sekolah", "Teman", "Senang"},
            {"Sedih", "Iya", "Senang", "Tolong"}
    };

    private final String[] kamusKata = {
            "Aku Mau Makan",
            "Aku Mau Minum",
            "Aku Mau Sekolah",
            "Aku Senang",
            "Halo",
            "Terima kasih",
            "Maaf",
            "Tolong",
            "Iya"
    };

    private final String[] kamusKeterangan = {
            "Digunakan untuk menyatakan kegiatan makan.",
            "Digunakan untuk menyatakan kegiatan minum.",
            "Digunakan untuk menunjukkan ingin bersekolah.",
            "Digunakan untuk menunjukkan perasaan bahagia.",
            "Digunakan untuk menyapa seseorang.",
            "Digunakan untuk mengucapkan rasa terima kasih.",
            "Digunakan saat meminta maaf.",
            "Digunakan saat meminta bantuan.",
            "Digunakan untuk menyatakan setuju."
    };

    private final int[] kamusGambar = {
            R.drawable.img_makan,
            R.drawable.img_minum,
            R.drawable.img_sekolah,
            R.drawable.img_senang,
            R.drawable.img_halo,
            R.drawable.img_terima_kasih,
            R.drawable.img_maaf,
            R.drawable.img_tolong,
            R.drawable.img_iya
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        makeFullScreen();

        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);

        loadReminderSettings();
        createNotificationChannel();
        requestNotificationPermission();

        if (reminderOn) {
            scheduleLearningReminder(reminderHour, reminderMinute);
        }

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("id", "ID"));
            }
        });

        showHome();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            makeFullScreen();
        }
    }

    private void makeFullScreen() {
        Window window = getWindow();

        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(params);
        }

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);

            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        }
    }

    private int getDisplayLevel() {
        return (level / QUESTIONS_PER_LEVEL) + 1;
    }

    private int getQuestionNumberInLevel() {
        return (level % QUESTIONS_PER_LEVEL) + 1;
    }

    private int getTotalQuestions() {
        return gameMateri.length;
    }

    private void resetGame() {
        score = 120;
        lives = 3;
        level = 0;
    }

    private void showHome() {
        LinearLayout content = createHomeScreen();

        content.addView(space(8));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo_belajar);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(350), dp(235));
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoParams.setMargins(0, 0, 0, dp(6));
        content.addView(logo, logoParams);

        TextView desc = text(
                "Game edukasi inklusif untuk belajar isyarat\ndengan cara yang menyenangkan",
                16,
                DARK,
                Typeface.BOLD
        );
        desc.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams descParams = matchWrap();
        descParams.setMargins(0, dp(2), 0, dp(14));
        content.addView(desc, descParams);

        TextView title = text("♡  Ayo Mulai Belajar!  ♡", 27, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleMenuParams = matchWrap();
        titleMenuParams.setMargins(0, 0, 0, dp(6));
        content.addView(title, titleMenuParams);

        TextView sub = text(
                "Kenali gerakan tangan, tebak arti isyarat,\nkumpulkan skor, dan naik level.",
                16,
                DARK,
                Typeface.NORMAL
        );
        sub.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subParams = matchWrap();
        subParams.setMargins(0, 0, 0, dp(16));
        content.addView(sub, subParams);

        content.addView(button("▶   Mulai Belajar   ❯", PINK, Color.parseColor("#FF6F9B"), v -> {
            speak("Mulai belajar bahasa isyarat");
            showLearn();
        }));

        content.addView(space(10));

        content.addView(button("✦   Main Game Tebak Isyarat   ❯", Color.parseColor("#B579E8"), PURPLE, v -> {
            speak("Main game tebak isyarat sampai level tiga");
            resetGame();
            showGame();
        }));

        content.addView(space(10));

        content.addView(button("📖   Buka Kamus Isyarat   ❯", Color.parseColor("#63C5F0"), BLUE, v -> {
            speak("Buka kamus isyarat");
            showDictionary();
        }));

        addSettingButtons(content);
        addFooter(content);
    }

    private void addSettingButtons(LinearLayout content) {
        content.addView(space(10));

        content.addView(button(
                settingsOpen ? "⚙   Tutup Pengaturan" : "⚙   Pengaturan",
                Color.parseColor("#A46BE8"),
                PURPLE,
                v -> {
                    settingsOpen = !settingsOpen;
                    speak("Pengaturan aplikasi");
                    showHome();
                }
        ));

        if (!settingsOpen) return;

        content.addView(space(10));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER);

        row1.addView(settingIconButton(
                soundOn ? "♪" : "×",
                soundOn ? "Suara\nAktif" : "Suara\nNonaktif",
                soundOn ? BLUE : Color.parseColor("#A8A8A8"),
                soundOn ? Color.parseColor("#E1F6FF") : Color.parseColor("#F2F2F2"),
                v -> {
                    soundOn = !soundOn;

                    if (soundOn) {
                        Toast.makeText(MainActivity.this, "Suara diaktifkan", Toast.LENGTH_SHORT).show();
                        speak("Suara diaktifkan");
                    } else {
                        Toast.makeText(MainActivity.this, "Suara dinonaktifkan", Toast.LENGTH_SHORT).show();
                    }

                    showHome();
                }
        ), settingRowParams());

        row1.addView(settingIconButton(
                "◐",
                contrastMode ? "Kontras\nAktif" : "Kontras\nNonaktif",
                contrastMode ? PURPLE : ORANGE,
                contrastMode ? Color.parseColor("#EFE2FF") : Color.parseColor("#FFF0E5"),
                v -> {
                    contrastMode = !contrastMode;
                    speak("Mode kontras diubah");
                    showHome();
                }
        ), settingRowParams());

        row1.addView(settingIconButton(
                "A",
                bigText ? "Tulisan\nBesar" : "Tulisan\nNormal",
                bigText ? PINK : GREEN,
                bigText ? Color.parseColor("#FFE4EF") : Color.parseColor("#E3FFF7"),
                v -> {
                    bigText = !bigText;
                    speak("Mode tulisan diubah");
                    showHome();
                }
        ), settingRowParams());

        content.addView(row1);

        content.addView(space(8));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER);

        row2.addView(settingIconButton(
                "⏰",
                reminderOn ? "Pengingat\n" + formatTime(reminderHour, reminderMinute) : "Atur\nPengingat",
                reminderOn ? ORANGE : Color.parseColor("#A8A8A8"),
                reminderOn ? Color.parseColor("#FFF0E5") : Color.parseColor("#F2F2F2"),
                v -> showReminderDialog()
        ), settingRowParams());

        row2.addView(settingIconButton(
                "📝",
                "Catatan\nBelajar",
                PINK,
                Color.parseColor("#FFE4EF"),
                v -> {
                    speak("Buka catatan belajar");
                    showNotes();
                }
        ), settingRowParams());

        content.addView(row2);
    }

    private LinearLayout.LayoutParams settingRowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(78), 1);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private LinearLayout settingIconButton(String iconText, String label, int iconColor, int bgColor, View.OnClickListener listener) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(4), dp(6), dp(4), dp(6));
        box.setBackground(roundStroke(bgColor, 18, Color.WHITE, 2));
        box.setOnClickListener(listener);
        addElevation(box, 0);

        TextView icon = text(iconText, 18, Color.WHITE, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(ovalBg(iconColor));

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(32), dp(32));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        box.addView(icon, iconParams);

        TextView text = text(label, 11, iconColor, Typeface.BOLD);
        text.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams textParams = matchWrap();
        textParams.setMargins(0, dp(3), 0, 0);
        box.addView(text, textParams);

        return box;
    }

    private void showReminderDialog() {
        final TimePicker timePicker = new TimePicker(this);
        timePicker.setIs24HourView(true);
        timePicker.setHour(reminderHour);
        timePicker.setMinute(reminderMinute);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atur Waktu Pengingat Belajar");
        builder.setView(timePicker);

        builder.setPositiveButton("Save", (dialog, which) -> {
            reminderHour = timePicker.getHour();
            reminderMinute = timePicker.getMinute();

            reminderOn = true;
            saveReminderSettings();
            scheduleLearningReminder(reminderHour, reminderMinute);

            Toast.makeText(
                    MainActivity.this,
                    "Pengingat belajar disimpan: " + formatTime(reminderHour, reminderMinute),
                    Toast.LENGTH_SHORT
            ).show();

            speak("Pengingat belajar disimpan");
            showHome();
        });

        builder.setNegativeButton("Batal", null);

        builder.setNeutralButton("Matikan", (dialog, which) -> {
            reminderOn = false;
            saveReminderSettings();
            cancelLearningReminder();

            Toast.makeText(
                    MainActivity.this,
                    "Pengingat belajar dimatikan",
                    Toast.LENGTH_SHORT
            ).show();

            speak("Pengingat belajar dimatikan");
            showHome();
        });

        builder.show();
    }

    private void scheduleLearningReminder(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    private void cancelLearningReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    private void saveReminderSettings() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_REMINDER_ON, reminderOn);
        editor.putInt(KEY_REMINDER_HOUR, reminderHour);
        editor.putInt(KEY_REMINDER_MINUTE, reminderMinute);
        editor.apply();
    }

    private void loadReminderSettings() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        reminderOn = preferences.getBoolean(KEY_REMINDER_ON, false);
        reminderHour = preferences.getInt(KEY_REMINDER_HOUR, 19);
        reminderMinute = preferences.getInt(KEY_REMINDER_MINUTE, 0);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat Belajar",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifikasi pengingat belajar bahasa isyarat");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin notifikasi diaktifkan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifikasi tidak akan muncul jika izin ditolak", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class ReminderReceiver extends BroadcastReceiver {

        private static final String CHANNEL_ID = "pengingat_belajar_channel";
        private static final int NOTIFICATION_ID = 1001;
        private static final int REMINDER_REQUEST_CODE = 1001;

        @Override
        public void onReceive(Context context, Intent intent) {
            int hour = intent.getIntExtra("hour", 19);
            int minute = intent.getIntExtra("minute", 0);

            createNotificationChannel(context);
            showNotification(context);
            scheduleNextReminder(context, hour, minute);
        }

        @SuppressLint("MissingPermission")
        private void showNotification(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            Intent openAppIntent = new Intent(context, MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(context, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(context);
                builder.setPriority(Notification.PRIORITY_HIGH);
                builder.setDefaults(Notification.DEFAULT_ALL);
            }

            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Pengingat Belajar")
                    .setContentText("Ayo belajar bahasa isyarat sekarang.")
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentIntent(openAppPendingIntent)
                    .setColor(Color.parseColor("#F24F89"));

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }

        private void scheduleNextReminder(Context context, int hour, int minute) {
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("hour", hour);
            intent.putExtra("minute", minute);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REMINDER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            }
        }

        private void createNotificationChannel(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Pengingat Belajar",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.setDescription("Notifikasi pengingat belajar bahasa isyarat");
                channel.enableVibration(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                channel.setShowBadge(true);

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }
    }

    private void showNotes() {
        LinearLayout content = createScreen();

        content.addView(topBack());

        TextView title = text("Catatan Belajar", 33, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.setMargins(0, dp(18), 0, dp(10));
        content.addView(title, titleParams);

        TextView subtitle = text(
                "Tulis catatan penting agar mudah dipelajari lagi.",
                16,
                DARK,
                Typeface.BOLD
        );
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.setMargins(0, 0, 0, dp(18));
        content.addView(subtitle, subtitleParams);

        LinearLayout card = card();
        card.setPadding(dp(20), dp(22), dp(20), dp(22));

        TextView label = text("Catatan Saya", 22, PURPLE, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams labelParams = matchWrap();
        labelParams.setMargins(0, 0, 0, dp(12));
        card.addView(label, labelParams);

        EditText noteInput = new EditText(this);
        noteInput.setText(getSavedNote());
        noteInput.setHint("Contoh: Kata isyarat yang sulit diingat adalah...");
        noteInput.setTextSize(bigText ? 20 : 16);
        noteInput.setTextColor(contrastMode ? Color.BLACK : DARK);
        noteInput.setHintTextColor(Color.parseColor("#9A8BAE"));
        noteInput.setGravity(Gravity.TOP | Gravity.START);
        noteInput.setMinLines(8);
        noteInput.setPadding(dp(14), dp(14), dp(14), dp(14));
        noteInput.setBackground(roundStroke(Color.WHITE, 20, Color.parseColor("#F0DDF8"), 2));

        LinearLayout.LayoutParams inputParams = matchWrap();
        inputParams.height = dp(230);
        inputParams.setMargins(0, 0, 0, dp(14));
        card.addView(noteInput, inputParams);

        noteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveNote(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        card.addView(button("Simpan Catatan", PINK, PINK, v -> {
            saveNote(noteInput.getText().toString());

            Toast.makeText(
                    MainActivity.this,
                    "Catatan berhasil disimpan",
                    Toast.LENGTH_SHORT
            ).show();

            speak("Catatan berhasil disimpan");
        }));

        card.addView(space(10));

        card.addView(outlineButton("Hapus Catatan", PURPLE, v -> {
            noteInput.setText("");
            saveNote("");

            Toast.makeText(
                    MainActivity.this,
                    "Catatan berhasil dihapus",
                    Toast.LENGTH_SHORT
            ).show();

            speak("Catatan berhasil dihapus");
        }));

        content.addView(card);
        addFooter(content);
    }

    private void saveNote(String note) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_NOTE_TEXT, note);
        editor.apply();
    }

    private String getSavedNote() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return preferences.getString(KEY_NOTE_TEXT, "");
    }

    private void showLearn() {
        LinearLayout content = createScreen();

        content.addView(topBack());

        TextView title = text("Mulai Belajar Isyarat", 31, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.setMargins(0, dp(18), 0, dp(20));
        content.addView(title, titleParams);

        LinearLayout card = card();
        card.setPadding(dp(22), dp(26), dp(22), dp(26));

        TextView materiTitle = text("Mengenal Penyakit Telinga", 25, PURPLE, Typeface.BOLD);
        materiTitle.setGravity(Gravity.CENTER);
        card.addView(materiTitle);

        addDiseaseItem(
                card,
                "Tinitus",
                "Tinitus adalah kondisi ketika telinga terasa mendengar bunyi berdenging, berdengung, atau berdesis, padahal tidak ada suara dari luar.",
                R.drawable.tinitus
        );

        addDiseaseItem(
                card,
                "Otitis Eksterna",
                "Otitis eksterna adalah peradangan pada saluran telinga bagian luar. Kondisi ini dapat menyebabkan telinga terasa sakit, merah, gatal, atau bengkak.",
                R.drawable.otitis_eksterna
        );

        addDiseaseItem(
                card,
                "Gendang Telinga Pecah",
                "Gendang telinga pecah adalah kondisi ketika selaput gendang telinga mengalami robekan atau lubang. Kondisi ini dapat menyebabkan nyeri, gangguan pendengaran, atau keluar cairan dari telinga.",
                R.drawable.gendang_telinga_pecah
        );

        addDiseaseItem(
                card,
                "Vertigo",
                "Vertigo adalah kondisi ketika seseorang merasa lingkungan di sekitarnya berputar atau tubuh terasa tidak seimbang. Kondisi ini dapat terjadi karena gangguan pada sistem keseimbangan di telinga bagian dalam.",
                R.drawable.vertigo
        );

        addDiseaseItem(
                card,
                "Otitis Interna",
                "Otitis interna adalah peradangan atau infeksi pada telinga bagian dalam. Kondisi ini dapat menyebabkan pusing, gangguan keseimbangan, telinga berdengung, nyeri, atau penurunan pendengaran.",
                R.drawable.otitis_interna
        );

        card.addView(button("Dengarkan Panduan", PINK, PINK, v ->
                speak("Belajar mengenal penyakit telinga seperti tinitus, otitis eksterna, gendang telinga pecah, vertigo, dan otitis interna.")
        ));

        card.addView(space(14));

        card.addView(button("Mulai Game", Color.parseColor("#B579E8"), PURPLE, v -> {
            resetGame();
            showGame();
        }));

        content.addView(card);
        addFooter(content);
    }

    private void addDiseaseItem(LinearLayout parent, final String title, final String description, int imageRes) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(16), dp(16), dp(16), dp(16));
        item.setBackground(roundStroke(Color.parseColor("#FFF7FB"), 22, Color.parseColor("#FFD3E4"), 2));
        addElevation(item, 0);

        TextView name = text(title, 23, PINK, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        item.addView(name);

        ImageView image = new ImageView(this);
        image.setImageResource(imageRes);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setBackground(roundStroke(Color.WHITE, 20, Color.parseColor("#F0DDF8"), 2));
        image.setPadding(dp(10), dp(10), dp(10), dp(10));

        LinearLayout.LayoutParams imageParams = matchWrap();
        imageParams.height = dp(190);
        imageParams.setMargins(0, dp(12), 0, dp(12));
        item.addView(image, imageParams);

        TextView desc = text(description, 16, DARK, Typeface.NORMAL);
        desc.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams descParams = matchWrap();
        descParams.setMargins(0, 0, 0, dp(12));
        item.addView(desc, descParams);

        item.addView(button("Dengarkan " + title, Color.parseColor("#B579E8"), PURPLE, v ->
                speak(title + ". " + description)
        ));

        LinearLayout.LayoutParams itemParams = matchWrap();
        itemParams.setMargins(0, dp(8), 0, dp(14));
        parent.addView(item, itemParams);
    }

    private void showGame() {
        answerLocked = false;

        LinearLayout content = createGameScreen();

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = smallCircleButton("←", PINK);
        back.setTextSize(25);
        back.setOnClickListener(v -> showHome());
        top.addView(back);

        View empty = new View(this);
        top.addView(empty, new LinearLayout.LayoutParams(0, dp(46), 1));

        top.addView(smallChip("★ " + score));
        top.addView(spaceHorizontal(8));
        top.addView(smallChip("♥ " + lives));

        content.addView(top);

        TextView title = text("✨ Ayo Tebak Isyarat! ✨", 29, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.setMargins(0, dp(14), 0, dp(14));
        content.addView(title, titleParams);

        LinearLayout progressCard = card();
        progressCard.setOrientation(LinearLayout.HORIZONTAL);
        progressCard.setGravity(Gravity.CENTER_VERTICAL);
        progressCard.setPadding(dp(14), dp(10), dp(14), dp(10));

        TextView levelBox = text("Level " + getDisplayLevel(), 16, Color.WHITE, Typeface.BOLD);
        levelBox.setGravity(Gravity.CENTER);
        levelBox.setBackground(roundGradient(Color.parseColor("#B579E8"), PURPLE, 22));
        progressCard.addView(levelBox, new LinearLayout.LayoutParams(dp(105), dp(44)));

        FrameLayout track = new FrameLayout(this);
        track.setBackground(roundBg(Color.parseColor("#EBD8F6"), 18));

        int trackWidth = dp(140);
        int fillWidth = (int) (trackWidth * ((level + 1) / (float) getTotalQuestions()));

        View fill = new View(this);
        fill.setBackground(roundGradient(Color.parseColor("#C081EC"), PURPLE, 18));
        track.addView(fill, new FrameLayout.LayoutParams(fillWidth, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(trackWidth, dp(14));
        trackParams.setMargins(dp(14), 0, 0, 0);
        progressCard.addView(track, trackParams);

        TextView progressText = text("  " + getQuestionNumberInLevel() + "/3", 15, PURPLE, Typeface.BOLD);
        progressText.setGravity(Gravity.CENTER);
        progressCard.addView(progressText, new LinearLayout.LayoutParams(dp(56), dp(44)));

        content.addView(progressCard);

        LinearLayout gameCard = card();
        gameCard.setPadding(dp(17), dp(20), dp(17), dp(20));

        LinearLayout.LayoutParams gameParams = matchWrap();
        gameParams.setMargins(0, dp(14), 0, dp(8));

        TextView question = text("♡  Gerakan ini artinya apa?  ♡", 24, PURPLE, Typeface.BOLD);
        question.setGravity(Gravity.CENTER);
        gameCard.addView(question);

        FrameLayout imageBox = new FrameLayout(this);
        imageBox.setBackground(roundStroke(Color.WHITE, 26, Color.parseColor("#F0DDF8"), 2));
        imageBox.setPadding(dp(14), dp(14), dp(14), dp(14));

        ImageView gestureImage = new ImageView(this);
        gestureImage.setImageResource(gameGambarMateri[level]);
        gestureImage.setAdjustViewBounds(true);
        gestureImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        FrameLayout.LayoutParams gestureParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        gestureParams.gravity = Gravity.CENTER;

        imageBox.addView(gestureImage, gestureParams);

        LinearLayout.LayoutParams imageParams = matchWrap();
        imageParams.height = dp(215);
        imageParams.setMargins(0, dp(16), 0, dp(14));
        gameCard.addView(imageBox, imageParams);

        TextView pilih = text("💗  Pilih jawaban yang benar  💗", 15, Color.parseColor("#7A6F91"), Typeface.BOLD);
        pilih.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams pilihParams = matchWrap();
        pilihParams.setMargins(0, 0, 0, dp(10));
        gameCard.addView(pilih, pilihParams);

        String[] option = gamePilihan[level];

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        row1.addView(answerButton("A   " + option[0], option[0]), optionParams());
        row1.addView(answerButton("B   " + option[1], option[1]), optionParams());

        row2.addView(answerButton("C   " + option[2], option[2]), optionParams());
        row2.addView(answerButton("D   " + option[3], option[3]), optionParams());

        gameCard.addView(row1);
        gameCard.addView(row2);

        content.addView(gameCard, gameParams);
        addFooter(content);
    }

    private void showSuccess() {
        LinearLayout content = createSuccessScreen();

        TextView title = text("Yay!\nJawaban Benar!", 27, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.setMargins(0, dp(4), 0, dp(10));
        content.addView(title, titleParams);

        LinearLayout card = card();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView jawabannya = text("Jawabannya adalah", 18, DARK, Typeface.BOLD);
        jawabannya.setGravity(Gravity.CENTER);
        card.addView(jawabannya);

        TextView answer = text(gameMateri[level], 24, PINK, Typeface.BOLD);
        answer.setGravity(Gravity.CENTER);
        answer.setBackground(roundStroke(Color.parseColor("#FFE3EE"), 26, Color.parseColor("#FFD0E0"), 2));

        LinearLayout.LayoutParams answerParams = new LinearLayout.LayoutParams(dp(270), dp(58));
        answerParams.gravity = Gravity.CENTER_HORIZONTAL;
        answerParams.setMargins(0, dp(10), 0, dp(12));
        card.addView(answer, answerParams);

        ImageView successImage = new ImageView(this);
        successImage.setImageResource(gameGambarMateri[level]);
        successImage.setAdjustViewBounds(true);
        successImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        successImage.setBackground(roundStroke(Color.WHITE, 20, Color.parseColor("#F0DDF8"), 2));
        successImage.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams successImageParams = matchWrap();
        successImageParams.height = dp(150);
        successImageParams.setMargins(0, 0, 0, dp(12));
        card.addView(successImage, successImageParams);

        TextView reward = text("Kamu mendapatkan\n+ 10 Poin dan + 1 Bintang", 18, PURPLE, Typeface.BOLD);
        reward.setGravity(Gravity.CENTER);
        reward.setBackground(roundBg(Color.parseColor("#F2E1FF"), 18));
        reward.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams rewardParams = matchWrap();
        rewardParams.setMargins(0, 0, 0, dp(14));
        card.addView(reward, rewardParams);

        String nextLabel = level < getTotalQuestions() - 1 ? "Lanjut" : "Selesai Game";

        card.addView(button(nextLabel, PINK, PINK, v -> {
            if (level < getTotalQuestions() - 1) {
                level++;
                showGame();
            } else {
                showGameComplete();
            }
        }));

        card.addView(space(10));

        card.addView(outlineButton("Ulangi Soal Ini", PURPLE, v -> showGame()));

        content.addView(card);
        addFooter(content);
    }

    private void showGameComplete() {
        LinearLayout content = createScreen();

        content.addView(space(60));

        LinearLayout card = card();
        card.setPadding(dp(24), dp(38), dp(24), dp(38));

        TextView title = text("Selamat!\nGame Selesai", 38, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        card.addView(title);

        TextView info = text(
                "Kamu berhasil menyelesaikan\nLevel 1 sampai Level 3.\n\nSkor akhir: " + score,
                23,
                DARK,
                Typeface.BOLD
        );
        info.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams infoParams = matchWrap();
        infoParams.setMargins(0, dp(22), 0, dp(32));
        card.addView(info, infoParams);

        card.addView(button("Main Lagi", PINK, PINK, v -> {
            resetGame();
            showGame();
        }));

        card.addView(space(16));

        card.addView(outlineButton("Kembali ke Beranda", PURPLE, v -> showHome()));

        content.addView(card);
        addFooter(content);
    }

    private void showGameOver() {
        LinearLayout content = createScreen();

        content.addView(space(70));

        LinearLayout card = card();
        card.setPadding(dp(24), dp(38), dp(24), dp(38));

        TextView over = text("Game Over", 40, PINK, Typeface.BOLD);
        over.setGravity(Gravity.CENTER);
        card.addView(over);

        TextView info = text("Nyawa kamu habis.\nSkor akhir: " + score, 23, DARK, Typeface.BOLD);
        info.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams infoParams = matchWrap();
        infoParams.setMargins(0, dp(22), 0, dp(32));
        card.addView(info, infoParams);

        card.addView(button("Ulang Game", PINK, PINK, v -> {
            resetGame();
            showGame();
        }));

        card.addView(space(16));

        card.addView(outlineButton("Kembali ke Beranda", PURPLE, v -> showHome()));

        content.addView(card);
        addFooter(content);
    }

    private void showDictionary() {
        LinearLayout content = createScreen();

        content.addView(topBack());

        TextView title = text("Kamus Isyarat", 33, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.setMargins(0, dp(18), 0, dp(10));
        content.addView(title, titleParams);

        TextView subtitle = text("Daftar kata bahasa isyarat yang memiliki gambar", 17, DARK, Typeface.BOLD);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.setMargins(0, 0, 0, dp(18));
        content.addView(subtitle, subtitleParams);

        for (int i = 0; i < kamusKata.length; i++) {
            addDictionaryItem(content, kamusKata[i], kamusKeterangan[i], kamusGambar[i]);
        }

        addFooter(content);
    }

    private void checkAnswer(String selected) {
        String correct = gameMateri[level];

        if (selected.equalsIgnoreCase(correct)) {
            score += 10;
            speak("Jawaban benar. Kamu mendapatkan sepuluh poin.");
            showSuccess();
        } else {
            lives--;
            score = Math.max(0, score - 5);

            vibrate();
            speak("Jawaban salah. Nyawa berkurang.");
            Toast.makeText(this, "Jawaban salah. Nyawa berkurang.", Toast.LENGTH_SHORT).show();

            if (lives <= 0) {
                showGameOver();
            } else {
                showGame();
            }
        }
    }

    private LinearLayout createHomeScreen() {
        rootLayout.removeAllViews();
        makeFullScreen();

        if (contrastMode) {
            rootLayout.setBackgroundColor(Color.WHITE);
        } else {
            rootLayout.setBackgroundColor(Color.TRANSPARENT);

            ImageView bg = new ImageView(this);
            bg.setImageResource(R.drawable.bg_awan);
            bg.setScaleType(ImageView.ScaleType.CENTER_CROP);

            rootLayout.addView(bg, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24), dp(14), dp(24), dp(8));
        content.setBackgroundColor(Color.TRANSPARENT);

        rootLayout.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return content;
    }

    private LinearLayout createGameScreen() {
        rootLayout.removeAllViews();
        makeFullScreen();

        if (contrastMode) {
            rootLayout.setBackgroundColor(Color.WHITE);
        } else {
            rootLayout.setBackgroundColor(Color.TRANSPARENT);

            ImageView bg = new ImageView(this);
            bg.setImageResource(R.drawable.bg_awan);
            bg.setScaleType(ImageView.ScaleType.CENTER_CROP);

            rootLayout.addView(bg, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24), dp(18), dp(24), dp(8));
        content.setBackgroundColor(Color.TRANSPARENT);

        rootLayout.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return content;
    }

    private LinearLayout createSuccessScreen() {
        rootLayout.removeAllViews();
        makeFullScreen();

        if (contrastMode) {
            rootLayout.setBackgroundColor(Color.WHITE);
        } else {
            rootLayout.setBackgroundColor(Color.TRANSPARENT);

            ImageView bg = new ImageView(this);
            bg.setImageResource(R.drawable.bg_awan);
            bg.setScaleType(ImageView.ScaleType.CENTER_CROP);

            rootLayout.addView(bg, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24), dp(16), dp(24), dp(8));
        content.setBackgroundColor(Color.TRANSPARENT);

        rootLayout.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return content;
    }

    private LinearLayout createScreen() {
        rootLayout.removeAllViews();
        makeFullScreen();

        if (contrastMode) {
            rootLayout.setBackgroundColor(Color.WHITE);
        } else {
            rootLayout.setBackgroundColor(Color.TRANSPARENT);

            ImageView bg = new ImageView(this);
            bg.setImageResource(R.drawable.bg_awan);
            bg.setScaleType(ImageView.ScaleType.CENTER_CROP);

            rootLayout.addView(bg, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.setPadding(dp(24), 0, dp(24), dp(20));
        scrollView.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(0, dp(28), 0, dp(28));
        content.setBackgroundColor(Color.TRANSPARENT);

        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        rootLayout.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return content;
    }

    private LinearLayout topBack() {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = smallCircleButton("←", PINK);
        back.setTextSize(25);
        back.setOnClickListener(v -> showHome());

        top.addView(back);
        return top;
    }

    private TextView answerButton(String label, final String value) {
        final TextView btn = text(label, 18, PURPLE, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(roundStroke(Color.WHITE, 18, Color.parseColor("#E8D8F4"), 2));
        addElevation(btn, 0);

        btn.setOnClickListener(v -> {
            if (answerLocked) return;

            answerLocked = true;
            btn.setEnabled(false);
            btn.setTextColor(Color.WHITE);
            btn.setBackground(roundStroke(PINK, 18, PINK, 3));

            btn.postDelayed(() -> checkAnswer(value), 350);
        });

        return btn;
    }

    private LinearLayout.LayoutParams optionParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(62), 1);
        params.setMargins(dp(5), dp(6), dp(5), dp(6));
        return params;
    }

    private void addDictionaryItem(LinearLayout parent, final String word, String meaning, int imageRes) {
        LinearLayout item = card();
        item.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView title = text(word, 26, PINK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        item.addView(title);

        ImageView image = new ImageView(this);
        image.setImageResource(imageRes);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setBackground(roundStroke(Color.WHITE, 22, Color.parseColor("#F0DDF8"), 2));
        image.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams imageParams = matchWrap();
        imageParams.height = dp(215);
        imageParams.setMargins(0, dp(14), 0, dp(14));
        item.addView(image, imageParams);

        TextView desc = text(meaning, 17, DARK, Typeface.NORMAL);
        desc.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams descParams = matchWrap();
        descParams.setMargins(0, dp(8), 0, dp(16));
        item.addView(desc, descParams);

        item.addView(button("Dengarkan", Color.parseColor("#B579E8"), PURPLE, v -> speak(word)));

        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, dp(10), 0, dp(10));
        parent.addView(item, params);
    }

    private void addFooter(LinearLayout parent) {
        TextView footer = text("💗 © 2026 Andini Rizki Puswari 💗", 13, Color.parseColor("#8A74A5"), Typeface.BOLD);
        footer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, dp(10), 0, dp(4));
        parent.addView(footer, params);
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundBg(Color.argb(215, 255, 255, 255), 28));

        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(params);

        addElevation(card, 0);
        return card;
    }

    private TextView button(String label, int color1, int color2, View.OnClickListener listener) {
        TextView btn = text(label, 20, Color.WHITE, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(roundGradient(color1, color2, 32));
        btn.setOnClickListener(listener);
        addElevation(btn, 0);

        LinearLayout.LayoutParams params = matchWrap();
        params.height = dp(62);
        btn.setLayoutParams(params);

        return btn;
    }

    private TextView outlineButton(String label, int color, View.OnClickListener listener) {
        TextView btn = text(label, 20, color, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(roundStroke(Color.WHITE, 32, color, 3));
        btn.setOnClickListener(listener);
        addElevation(btn, 0);

        LinearLayout.LayoutParams params = matchWrap();
        params.height = dp(66);
        btn.setLayoutParams(params);

        return btn;
    }

    private TextView smallCircleButton(String label, int color) {
        TextView v = text(label, 15, color, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        v.setBackground(ovalBg(Color.WHITE));
        v.setPadding(dp(3), dp(3), dp(3), dp(3));
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
        addElevation(v, 0);
        return v;
    }

    private TextView smallChip(String label) {
        TextView v = text(label, 13, PURPLE, Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        v.setBackground(roundBg(Color.WHITE, 25));
        v.setPadding(dp(8), 0, dp(8), 0);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(92), dp(40)));
        addElevation(v, 0);
        return v;
    }

    private TextView text(String value, int size, int color, int style) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(bigText ? size + 4 : size);
        t.setTextColor(contrastMode ? Color.BLACK : color);
        t.setTypeface(Typeface.DEFAULT, style);
        t.setIncludeFontPadding(true);
        return t;
    }

    private View space(int height) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(height)));
        return v;
    }

    private View spaceHorizontal(int width) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(width), 1));
        return v;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private GradientDrawable roundBg(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        return bg;
    }

    private GradientDrawable roundStroke(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable bg = roundBg(color, radius);
        bg.setStroke(dp(strokeWidth), strokeColor);
        return bg;
    }

    private GradientDrawable roundGradient(int color1, int color2, int radius) {
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color1, color2}
        );
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(radius));
        return bg;
    }

    private GradientDrawable ovalBg(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        return bg;
    }

    private void addElevation(View view, int elevation) {
        view.setElevation(0);
    }

    private void speak(String message) {
        if (tts == null) return;
        if (!soundOn) return;

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "tts_id");
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(120);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}