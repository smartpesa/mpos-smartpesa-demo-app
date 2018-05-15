package com.smartpesa.smartpesa;

import timber.log.Timber;

public class TimberUtils {
    public static void initTimber() {
        Timber.plant(new CrittercismTree());
    }
}
