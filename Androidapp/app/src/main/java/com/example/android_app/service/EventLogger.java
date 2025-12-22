package com.example.android_app.service;

import android.content.Context;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventLogger {

    private final Context context;
    private final SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public EventLogger(Context context) {
        this.context = context;
    }

    public void log(
            TemporalEventDetector.EventType type,
            long timestamp,
            float confidence
    ) {
        String line = fmt.format(new Date()) +
                "," + type.name() +
                ",confidence=" + confidence + "\n";

        try (FileWriter fw = new FileWriter(
                context.getFilesDir() + "/events.log",
                true
        )) {
            fw.write(line);
        } catch (Exception ignored) {}
    }
}
