package com.example.yandexgeofence2;

import android.content.Context;
import com.yandex.mapkit.MapKitFactory;

public class MapKitInitializer {
    private static boolean initialized = false;

    public static void init(Context context, String apiKey) {
        if (!initialized) {
            MapKitFactory.setApiKey(apiKey);
            MapKitFactory.initialize(context.getApplicationContext());
            initialized = true;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
