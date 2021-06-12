package ca.pkay.rcloneexplorer.util;

import android.os.Bundle;
import android.os.Parcel;

import androidx.annotation.NonNull;

public class LargeParcel {

    public static int calculateBundleSize(@NonNull Bundle bundle) {
        Parcel parcel = Parcel.obtain();
        parcel.writeBundle(bundle);
        int size = parcel.dataSize();
        parcel.recycle();
        return size;
    }
}
