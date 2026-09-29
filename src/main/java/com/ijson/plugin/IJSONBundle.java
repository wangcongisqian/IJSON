package com.ijson.plugin;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class IJSONBundle extends DynamicBundle {
    @NonNls
    public static final String BUNDLE = "messages.IJSONBundle";
    private static final IJSONBundle INSTANCE = new IJSONBundle();

    private IJSONBundle() {
        super(BUNDLE);
    }

    public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                      Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }

}
