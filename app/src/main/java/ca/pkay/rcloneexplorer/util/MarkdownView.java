package ca.pkay.rcloneexplorer.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;
import org.markdownj.MarkdownProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MarkdownView extends WebView {

    public MarkdownView(Context context) {
        super(patchContext(context));
    }

    public MarkdownView(Context context, AttributeSet attrs) {
        super(patchContext(context), attrs);
    }

    private static Context patchContext(Context context) {
        // TODO: Only affects appcompat 1.1.x, remove with 1.2.x
        //       https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview/58131421
        if (Build.VERSION.SDK_INT == 22 || Build.VERSION.SDK_INT == 23) {
            return context.createConfigurationContext(new Configuration());
        }
        return context;
    }

    public void loadAsset(String path) {
        new LoadMarkdownAsset(path, this).execute();
    }

    private static class LoadMarkdownAsset extends AsyncTask<Void, Void, String> {

        private static final String TAG = "LoadMarkdownAsset";
        private final String assetName;
        private final WebView webView;

        public LoadMarkdownAsset(String assetName, WebView webView) {
            this.assetName = assetName;
            this.webView = webView;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Context context = webView.getContext();
            AssetManager assetManager = context.getAssets();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(assetName)))) {
                StringBuilder markdown = new StringBuilder(4096);
                String line;
                while ((line = br.readLine()) != null) {
                    // Use \n as line seperator so that the processor does not
                    // have to replace this.
                    markdown.append(line).append('\n');
                }
                return new MarkdownProcessor().markdown(markdown.toString());
            } catch (IOException e) {
                FLog.e(TAG, "Could not load asset ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String html) {
            if (null == html) {
                webView.loadUrl("about:blank");
            } else {
                webView.loadDataWithBaseURL("local://", html, "text/html", "UTF-8", null);
            }
        }
    }
}
