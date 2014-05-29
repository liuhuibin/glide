package com.bumptech.glide;

import android.content.Context;
import android.os.Build;
import com.android.volley.RequestQueue;
import com.bumptech.glide.resize.Engine;
import com.bumptech.glide.resize.EngineBuilder;
import com.bumptech.glide.resize.bitmap_recycle.BitmapPool;
import com.bumptech.glide.resize.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.resize.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.resize.cache.DiskCache;
import com.bumptech.glide.resize.cache.DiskCacheAdapter;
import com.bumptech.glide.resize.cache.DiskLruCacheWrapper;
import com.bumptech.glide.resize.cache.LruResourceCache;
import com.bumptech.glide.resize.cache.MemoryCache;
import com.bumptech.glide.volley.RequestQueueWrapper;

import java.io.File;

public class GlideBuilder {
    private RequestQueue requestQueue;
    private Context context;
    private Engine engine;
    private BitmapPool bitmapPool;
    private MemoryCache memoryCache;
    private DiskCache diskCache;

    public GlideBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    public GlideBuilder setRequestQueue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
        return this;
    }

    public GlideBuilder setBitmapPool(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
        return this;
    }

    public GlideBuilder setMemoryCache(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    public GlideBuilder setDiskCache(DiskCache diskCache) {
        this.diskCache = diskCache;
        return this;
    }

    GlideBuilder setEngine(Engine engine) {
        this.engine = engine;
        return this;
    }

    Glide createGlide() {
        if (requestQueue == null) {
            requestQueue = RequestQueueWrapper.getRequestQueue(context);
        }

        final int safeCacheSize = Glide.getSafeMemoryCacheSize(context);
        final boolean isLowMemoryDevice = Glide.isLowMemoryDevice(context);
        if (bitmapPool == null) {
            if (Build.VERSION.SDK_INT >= 11) {
                bitmapPool = new LruBitmapPool(
                        isLowMemoryDevice ? safeCacheSize : 2 * safeCacheSize);
            } else {
                bitmapPool = new BitmapPoolAdapter();
            }
        }

        if (memoryCache == null) {
            memoryCache = new LruResourceCache(!isLowMemoryDevice && Glide.CAN_REUSE_BITMAPS ?
                    safeCacheSize / 2 : safeCacheSize);
        }

        if (diskCache == null) {
            File cacheDir = Glide.getPhotoCacheDir(context);
            if (cacheDir != null) {
                diskCache = DiskLruCacheWrapper.get(cacheDir, Glide.DEFAULT_DISK_CACHE_SIZE);
            }
            if (diskCache == null) {
                diskCache = new DiskCacheAdapter();
            }
        }

        if (engine == null) {
            engine = new EngineBuilder(memoryCache, diskCache)
                    .build();
        }

        return new Glide(engine, requestQueue, memoryCache, bitmapPool);
    }
}