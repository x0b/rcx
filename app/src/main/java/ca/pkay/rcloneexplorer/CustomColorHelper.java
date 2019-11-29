package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.os.Build;
import androidx.annotation.ColorRes;

public class CustomColorHelper {

    public static int getPrimaryColorTheme(Context context, int color) {
        if (isColor(context, R.color.colorPrimary_Red, color)) {
            return R.style.CustomPrimaryRed;
        }
        if (isColor(context, R.color.colorPrimary_Pink, color)) {
            return R.style.CustomPrimaryPink;
        }
        if (isColor(context, R.color.colorPrimary_Purple, color)) {
            return R.style.CustomPrimaryPurple;
        }
        if (isColor(context, R.color.colorPrimary_DeepPurple, color)) {
            return R.style.CustomPrimaryDeepPurple;
        }
        if (isColor(context, R.color.colorPrimary_Indigo, color)) {
            return R.style.CustomPrimaryIndigo;
        }
        if (isColor(context, R.color.colorPrimary_Blue, color)) {
            return R.style.CustomPrimaryBlue;
        }
        if (isColor(context, R.color.colorPrimary_LightBlue, color)) {
            return R.style.CustomPrimaryLightBlue;
        }
        if (isColor(context, R.color.colorPrimary_Cyan, color)) {
            return R.style.CustomPrimaryCyan;
        }
        if (isColor(context, R.color.colorPrimary_Teal, color)) {
            return R.style.CustomPrimaryTeal;
        }
        if (isColor(context, R.color.colorPrimary_Green, color)) {
            return R.style.CustomPrimaryGreen;
        }
        if (isColor(context, R.color.colorPrimary_LightGreen, color)) {
            return R.style.CustomPrimaryLightGreen;
        }
        if (isColor(context, R.color.colorPrimary_Lime, color)) {
            return R.style.CustomPrimaryLime;
        }
        if (isColor(context, R.color.colorPrimary_Yellow, color)) {
            return R.style.CustomPrimaryYellow;
        }
        if (isColor(context, R.color.colorPrimary_Amber, color)) {
            return R.style.CustomPrimaryAmber;
        }
        if (isColor(context, R.color.colorPrimary_Orange, color)) {
            return R.style.CustomPrimaryOrange;
        }
        if (isColor(context, R.color.colorPrimary_DeepOrange, color)) {
            return R.style.CustomPrimaryDeepOrange;
        }
        if (isColor(context, R.color.colorPrimary_Brown, color)) {
            return R.style.CustomPrimaryBrown;
        }
        if (isColor(context, R.color.colorPrimary_Grey, color)) {
            return R.style.CustomPrimaryGrey;
        }
        if (isColor(context, R.color.colorPrimary_BlueGrey, color)) {
            return R.style.CustomPrimaryBlueGrey;
        }
        return R.style.CustomPrimaryGreen;
    }

    public static int getAccentColorTheme(Context context, int color) {
        if (isColor(context, R.color.colorAccent_Red, color)) {
            return R.style.CustomAccentRed;
        }
        if (isColor(context, R.color.colorAccent_Pink, color)) {
            return R.style.CustomAccentPink;
        }
        if (isColor(context, R.color.colorAccent_Purple, color)) {
            return R.style.CustomAccentPurple;
        }
        if (isColor(context, R.color.colorAccent_DeepPurple, color)) {
            return R.style.CustomAccentDeepPurple;
        }
        if (isColor(context, R.color.colorAccent_Indigo, color)) {
            return R.style.CustomAccentIndigo;
        }
        if (isColor(context, R.color.colorAccent_Blue, color)) {
            return R.style.CustomAccentBlue;
        }
        if (isColor(context, R.color.colorAccent_LightBlue, color)) {
            return R.style.CustomAccentLightBlue;
        }
        if (isColor(context, R.color.colorAccent_Cyan, color)) {
            return R.style.CustomAccentCyan;
        }
        if (isColor(context, R.color.colorAccent_Teal, color)) {
            return R.style.CustomAccentTeal;
        }
        if (isColor(context, R.color.colorAccent_Green, color)) {
            return R.style.CustomAccentGreen;
        }
        if (isColor(context, R.color.colorAccent_LightGreen, color)) {
            return R.style.CustomAccentLightGreen;
        }
        if (isColor(context, R.color.colorAccent_Lime, color)) {
            return R.style.CustomAccentLime;
        }
        if (isColor(context, R.color.colorAccent_Yellow, color)) {
            return R.style.CustomAccentYellow;
        }
        if (isColor(context, R.color.colorAccent_Amber, color)) {
            return R.style.CustomAccentAmber;
        }
        if (isColor(context, R.color.colorAccent_Orange, color)) {
            return R.style.CustomAccentOrange;
        }
        if (isColor(context, R.color.colorAccent_DeepOrange, color)) {
            return R.style.CustomAccentDeepOrange;
        }
        if (isColor(context, R.color.colorAccent_Brown, color)) {
            return R.style.CustomAccentBrown;
        }
        if (isColor(context, R.color.colorAccent_Grey, color)) {
            return R.style.CustomAccentGrey;
        }
        if (isColor(context, R.color.colorAccent_BlueGrey, color)) {
            return R.style.CustomAccentBlueGrey;
        }
        return R.style.CustomAccentOrange;
    }

    private static boolean isColor(Context context, @ColorRes int definedColor, int givenColor) {
        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = context.getResources().getColor(definedColor, null);
        } else {
            //noinspection deprecation
            color = context.getResources().getColor(definedColor);
        }
        return color == givenColor;
    }
}
