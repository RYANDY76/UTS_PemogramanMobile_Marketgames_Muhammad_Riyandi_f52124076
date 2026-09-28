package com.muh_riyandi_f52124076.marketgames_uts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

@SuppressWarnings({"deprecation", "StaticFieldLeak"})
public class ImageLoader {
    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

    public void load(String url, ImageView view) {
        view.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap cached = cache.get(url);
        if (cached != null) { view.setImageBitmap(cached); return; }
        view.setTag(url);
        new AsyncTask<Void,Void,Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... v) {
                try {
                    URL u = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setInstanceFollowRedirects(true);
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(10000);
                    conn.setDoInput(true);
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                        String newUrl = conn.getHeaderField("Location");
                        conn.disconnect();
                        conn = (HttpURLConnection) new URL(newUrl).openConnection();
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                        conn.connect();
                    }
                    InputStream in = conn.getInputStream();
                    Bitmap b = BitmapFactory.decodeStream(in);
                    in.close();
                    conn.disconnect();
                    return b;
                } catch(Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap b) {
                if (b != null) {
                    cache.put(url, b);
                    Object tag = view.getTag();
                    if (Objects.equals(url, tag)) view.setImageBitmap(b);
                }
            }
        }.execute();
    }
}
