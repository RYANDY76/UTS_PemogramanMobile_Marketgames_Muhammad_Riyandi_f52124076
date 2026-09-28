package com.muh_riyandi_f52124076.marketgames_uts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {
    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

    public void load(String url, ImageView view) {
        view.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap cached = cache.get(url);
        if (cached != null) { view.setImageBitmap(cached); return; }
        view.setTag(url);
        new AsyncTask<Void,Void,Bitmap>() {
            protected Bitmap doInBackground(Void... v) {
                try {
                    URL u = new URL(url);
                    HttpURLConnection c = (HttpURLConnection) u.openConnection();
                    c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    c.setInstanceFollowRedirects(true);
                    c.setConnectTimeout(8000);
                    c.setReadTimeout(10000);
                    c.setDoInput(true);
                    c.connect();
                    int responseCode = c.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                        String newUrl = c.getHeaderField("Location");
                        c = (HttpURLConnection) new URL(newUrl).openConnection();
                        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                        c.connect();
                    }
                    InputStream in = c.getInputStream();
                    Bitmap b = BitmapFactory.decodeStream(in);
                    in.close();
                    c.disconnect();
                    return b;
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            protected void onPostExecute(Bitmap b) {
                if (b != null) {
                    cache.put(url, b);
                    Object tag = view.getTag();
                    if (url.equals(tag)) view.setImageBitmap(b);
                }
            }
        }.execute();
    }
}
