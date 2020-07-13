package ca.pkay.rcloneexplorer.SAFProvider;

import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class BufferedTransferThread extends Thread {
    private static String TAG = "BufferedTransferThread";

    private InputStream is;
    private OutputStream os;
    private int bufferSize;
    private @Nullable CancellationSignal cancellationSignal;

    public BufferedTransferThread(
        InputStream is,
        OutputStream os,
        @Nullable CancellationSignal cancellationSignal
    ) {
        this(is, os, 65536, cancellationSignal);
    }

    public BufferedTransferThread(
        InputStream is,
        OutputStream os,
        int bufferSize,
        @Nullable CancellationSignal cancellationSignal
    ) {
        this.is = is;
        this.os = os;
        this.bufferSize = bufferSize;
        this.cancellationSignal = cancellationSignal;
    }

    private boolean isCanceled() {
        return cancellationSignal != null && cancellationSignal.isCanceled();
    }

    @Override
    public void run() {
        byte[] buf = new byte[this.bufferSize];
        int len;

        try {
            while (!isCanceled() && (len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }
        } catch (IOException e) {
            Log.i(TAG, "Couldn't write file.");
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                Log.i(TAG, "Couldn't close file.");
            }
        }

    }
}
