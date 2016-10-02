package com.example.xyzreader.util;

import android.os.Build;
import android.view.View;
import android.view.Window;

public final class ViewUtils {

    private ViewUtils() { throw new AssertionError(); }

    public static int getSystemUiVisibilityFlags(Window window) {

        final int sdkInt = Build.VERSION.SDK_INT;
        int systemFlags = window.getDecorView().getSystemUiVisibility();

        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            systemFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }

        return systemFlags;

    }

}
