package ca.pkay.rcloneexplorer.pkg;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;

import ca.pkay.rcloneexplorer.BuildConfig;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.Rfc3339Deserializer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PackageUpdate {

    private static final String TAG = "PackageUpdate";
    private static final int UPDATE_AVAILABLE = 201;
    private static final int NOTIFICATION_ID = 180;
    private static final String CHANNEL_ID = "io.github.x0b.rcx.update_channel";
    private static final String CHANNEL_NAME = "Updates";
    private static final String CHANNEL_DESCRIPTION = "Notify me when a new update is available.";

    private Context context;

    public PackageUpdate(Context context) {
        this.context = context;
    }

    static class MessageHandler extends Handler {

        private Context context;

        public MessageHandler(@NonNull Context context) {
            super(Looper.getMainLooper());
            this.context = context;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (UPDATE_AVAILABLE == msg.what) {
                Uri uri = Uri.parse(context.getString(R.string.app_latest_release_url));
                if (null != msg.obj) {
                    uri = Uri.parse((String) msg.obj);
                }
                Intent releasePage = new Intent(Intent.ACTION_VIEW, uri);
                releasePage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent releasePagePendingIntent = PendingIntent.getActivity(context, 0, releasePage, PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_refresh)
                        .setContentTitle(context.getString(R.string.app_update_notification_title))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setContentIntent(releasePagePendingIntent)
                        .setAutoCancel(true);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void checkForUpdate(boolean force) {
        // only check if not disabled
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!force && !sharedPreferences.getBoolean(context.getString(R.string.pref_key_app_updates), true)) {
            FLog.i(TAG, "checkForUpdate: Not checking, updates are disabled");
            return;
        }
        // only check if the last check was >6 hours ago
        long lastUpdateCheck = sharedPreferences.getLong(context.getString(R.string.pref_key_update_last_check), 0);
        long now = System.currentTimeMillis(); // * 60 * 6
        if (lastUpdateCheck + 1000 * 60 > now) {
            FLog.i(TAG, "checkForUpdate: recent check to new, not checking for updates");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        boolean checkBeta = sharedPreferences.getBoolean(context.getString(R.string.pref_key_app_updates_beta), false);
        String url;
        if (checkBeta) {
            url = context.getString(R.string.app_pre_release_api_url);
        } else {
            url = context.getString(R.string.app_relase_api_url);
        }
        Request request = new Request.Builder().url(url).build();
        Handler handler = new MessageHandler(context);
        client.newCall(request).enqueue(new UpdateRequestResultHandler(context, checkBeta, handler));
    }

    private static class UpdateRequestResultHandler implements Callback {

        private final SharedPreferences sharedPreferences;
        private final Context context;
        private final boolean betaCheck;
        private final Handler handler;

        public UpdateRequestResultHandler(Context context, boolean betaCheck, Handler handler) {
            this.context = context;
            this.betaCheck = betaCheck;
            this.handler = handler;
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            FLog.w(TAG, "onFailure: Update check failed", e);
            updateLastUpdateRequest();
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            updateLastUpdateRequest();
            if (response.code() != 200) {
                FLog.w(TAG, "onResponse: Update check failed, code=%d", response.code());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE);

            String url = null;
            if (betaCheck) {
                GitHubRelease[] releases = mapper.readValue(response.body().byteStream(), GitHubRelease[].class);
                Arrays.sort(releases, (i, j) -> Long.compare(i.createdAt, j.createdAt));

                for (GitHubRelease release : releases) {
                    url = getApkUrl(release);
                    if (url != null) {
                        break;
                    }
                }
            } else {
                GitHubRelease release = mapper.readValue(response.body().byteStream(), GitHubRelease.class);
                url = getApkUrl(release);
            }

            if (url != null) {
                FLog.i(TAG, "onResponse: App is not up-to-date");
                handler.obtainMessage(UPDATE_AVAILABLE, url).sendToTarget();
            } else {
                FLog.i(TAG, "onResponse: App is up-to-date");
            }
        }

        @Nullable
        private String getApkUrl(GitHubRelease release) {
            // Since the app is not published immediately during build, 60 minutes are added
            long minStamp = BuildConfig.BUILD_TIME + 1000 * 60 * 60;
            String[] supportedAbis = Build.SUPPORTED_ABIS;
            for (ReleaseAsset asset : release.getAssets()) {
                for (String supportedAbi : supportedAbis) {
                    if (asset.getName().contains(supportedAbi)
                            && release.getCreatedAt() >= minStamp) {
                        return asset.getUrl();
                    }
                }
            }
            return null;
        }

        private void updateLastUpdateRequest() {
            long now = System.currentTimeMillis();
            sharedPreferences.edit().putLong(context.getString(R.string.pref_key_update_last_check), now).apply();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubRelease {

        @JsonProperty("html_url")
        String htmlUrl;

        @JsonProperty("assets")
        ReleaseAsset[] assets;

        @JsonDeserialize(using = Rfc3339Deserializer.class)
        @JsonProperty("created_at")
        long createdAt;

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public ReleaseAsset[] getAssets() {
            return assets;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ReleaseAsset {

        @JsonProperty("name")
        String name;

        @JsonProperty("browser_download_url")
        String url;

        @JsonDeserialize(using = Rfc3339Deserializer.class)
        @JsonProperty("created_at")
        long createdAt;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}
