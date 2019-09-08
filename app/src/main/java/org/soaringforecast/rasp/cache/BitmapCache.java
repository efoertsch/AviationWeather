package org.soaringforecast.rasp.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.io.File;


/**
 * Cache that combines memory cache with disk cache.
 * *Disk cache from https://developer.android.com/topic/performance/graphics/cache-bitmap.html and modified accordingly
 * Memory cache from Cache2k
 * Should be accessed on background thread
 */
public class BitmapCache {

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 48; // 48MB
    private static final String DISK_CACHE_SUBDIR = "cachedBitmaps";
    private static final int DISK_CACHE_VERSION = 1;
    private static BitmapCache bitmapCache;

    private final Object diskCacheLock = new Object();
    private boolean diskCacheStarting = true;

    private Cache<String, Bitmap> memoryCache;
    private DiskLruImageCache diskLruImageCache;
    private Context context;

    // Needed to make public for unit testing
    // TODO how to make private (mock context, AyncTask)
    public BitmapCache() {
    }

    public static BitmapCache getInstance(Context context) {
        if (bitmapCache == null) {
            bitmapCache = new BitmapCache();
            bitmapCache.context = context;
            bitmapCache.createMemoryCache();
            bitmapCache.createDiskCache(context);
        }
        return bitmapCache;

    }

    private void createMemoryCache() {
        memoryCache = new Cache2kBuilder<String, Bitmap>() {
        }
                .name("Bitmap Cache")
                .eternal(true)
                .entryCapacity(22)
                .build();
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    private void createDiskCache(Context context) {
        File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir.getAbsolutePath());
    }

    @SuppressLint("StaticFieldLeak")
    class InitDiskCacheTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            synchronized (diskCacheLock) {
                String cacheDir = params[0];
                diskLruImageCache = new DiskLruImageCache(context, cacheDir, DISK_CACHE_SIZE, 90);
                diskCacheStarting = false; // Finished initialization
                diskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }

    private Bitmap getBitmapFromDiskCache(String key) {
        synchronized (diskCacheLock) {
            // Wait while disk cache is started from background thread
            while (diskCacheStarting) {
                try {
                    diskCacheLock.wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            if (diskLruImageCache != null) {
                return diskLruImageCache.getBitmap(key);
            }
        }
        return null;
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    private static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public void put(String key, Bitmap bitmap) {
        // Add to memory cache as before
        if (getBitmapFromMemCache(key) == null) {
            addBitmapToMemoryCache(key, bitmap);
        }

        // Also add to disk cache
        synchronized (diskCacheLock) {
            if (diskLruImageCache != null && !diskLruImageCache.containsKey(key)) {
                diskLruImageCache.put(key, bitmap);
            }
        }
    }

    public Bitmap get(String url) {
        Bitmap bitmap;

        // memory
        bitmap = getBitmapFromMemCache(url);
        if (bitmap != null) {
            return bitmap;
        }
        // disk
        bitmap = getBitmapFromDiskCache(url);
        if (bitmap != null) {
            addBitmapToMemoryCache(url, bitmap);
            return bitmap;
        }
        return null;
    }

    public void clearCache() {
        diskLruImageCache.clearCache();
        createDiskCache(context);
        memoryCache.clear();
    }
}
