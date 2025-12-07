package com.example.androidexample;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Glide configuration module required for Glide v4+.
 * This class enables Glide's annotation processor to generate GlideApp,
 * and improves performance by disabling manifest parsing.
 */
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public boolean isManifestParsingEnabled() {
        // Disable manifest parsing to avoid adding similar modules twice
        return false;
    }
}