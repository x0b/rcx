package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import ca.pkay.rcloneexplorer.InteractiveRunner;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;
import es.dmoral.toasty.Toasty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides utility methods for authorization of OAuth remotes
 */
public class OauthHelper {

    private static final String TAG = "OAuthHelper";
    private static final String regex = "go to the following link: ([^\\s]+)";

    /**
     * Save the options in the rclone config file and start the OAuth authentication process
     * @param options a list of rclone options, starting with remote name and type
     * @param rclone the rclone to use
     * @param context a context to start
     * @return true if successful
     **/
    public static boolean createOptionsWithOauth(ArrayList<String> options, Rclone rclone, Context context) {
        Process process = rclone.configCreate(options);
        if (null == process) {
            return false;
        }
        Thread authThread = new OauthHelper.UrlAuthThread(process, context);
        authThread.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            authThread.interrupt();
            FLog.e(TAG, "createOptionsWithOauth: aborted", e);
        }
        return 0 == process.exitValue();
    }

    /**
     * Monitor a rclone process for an authentication url and launch a browser
     * tab for the user. Note: this consumes the processes InputStream (stdout).
     */
    public static class UrlAuthThread extends Thread {
        private static final Pattern pattern = Pattern.compile(regex, 0);

        private static final String TAG = "UrlAuthThread";
        private final Process process;
        private final Context context;

        public UrlAuthThread(Process process, Context context) {
            this.process = process;
            this.context = context;
        }

        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while (null != (line = br.readLine())) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String url = matcher.group(1);

                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(context, Uri.parse(url));

                        // Do NOT break here, or the stream will be closed.
                        // When rclone then tries to write to the stream, it will receive SIGPIPE
                        // and rclone will exit confused why it can't just output its log messages.
                        // Instead, wait for rclone to close the stream.
                    }
                }
            } catch (IOException e) {
                FLog.e(TAG, "doInBackground: could not read auth url", e);
                process.destroy();
            }
        }
    }

    private static class OauthAction implements InteractiveRunner.Action {

        private static final Pattern pattern = Pattern.compile(regex, 0);
        private Context context;

        public OauthAction(Context context) {
            this.context = context;
        }

        @Override
        public void onTrigger(String cliBuffer) {
            Matcher matcher = pattern.matcher(cliBuffer);
            if (matcher.find()) {
                String url = matcher.group(1);

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(context, Uri.parse(url));
            } else {
                FLog.w(TAG, "onTrigger: could not extract auth URL from buffer: %s", cliBuffer);
            }
        }

        @Override
        public String getInput() {
            return "";
        }
    }

    public static class InitOauthStep extends InteractiveRunner.Step {
        private static final String TRIGGER = "Log in and authorize rclone for access";

        public InitOauthStep(Context context) {
            super(TRIGGER,  new OauthHelper.OauthAction(context));
        }
    }

    public static class OauthFinishStep extends InteractiveRunner.Step {

        private static final String TRIGGER = "Got code\n";

        public OauthFinishStep() {
            super(TRIGGER, new InteractiveRunner.StringAction(""));
        }

        @Override
        public long getTimeout() {
            return 5 * 60 * 1000L;
        }
    }
}
