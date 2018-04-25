package ca.pkay.rcloneexplorer;

import android.content.Context;

public class CustomColorHelper {

    public static int getPrimaryColorTheme(Context context, int color) {
        if (context.getResources().getColor(R.color.colorPrimary_Red) == color) {
            return R.style.CustomPrimaryRed;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Pink) == color) {
            return R.style.CustomPrimaryPink;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Purple) == color) {
            return R.style.CustomPrimaryPurple;
        }
        if (context.getResources().getColor(R.color.colorPrimary_DeepPurple) == color) {
            return R.style.CustomPrimaryDeepPurple;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Indigo) == color) {
            return R.style.CustomPrimaryIndigo;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Blue) == color) {
            return R.style.CustomPrimaryBlue;
        }
        if (context.getResources().getColor(R.color.colorPrimaryLight_Blue) == color) {
            return R.style.CustomPrimaryLightBlue;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Cyan) == color) {
            return R.style.CustomPrimaryCyan;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Teal) == color) {
            return R.style.CustomPrimaryTeal;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Green) == color) {
            return R.style.CustomPrimaryGreen;
        }
        if (context.getResources().getColor(R.color.colorPrimary_LightGreen) == color) {
            return R.style.CustomPrimaryLightGreen;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Lime) == color) {
            return R.style.CustomPrimaryLime;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Yellow) == color) {
            return R.style.CustomPrimaryYellow;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Amber) == color) {
            return R.style.CustomPrimaryAmber;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Orange) == color) {
            return R.style.CustomPrimaryOrange;
        }
        if (context.getResources().getColor(R.color.colorPrimary_DeepOrange) == color) {
            return R.style.CustomPrimaryDeepOrange;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Brown) == color) {
            return R.style.CustomPrimaryBrown;
        }
        if (context.getResources().getColor(R.color.colorPrimary_Grey) == color) {
            return R.style.CustomPrimaryGrey;
        }
        if (context.getResources().getColor(R.color.colorPrimary_BlueGrey) == color) {
            return R.style.CustomPrimaryBlueGrey;
        }
        return R.style.CustomPrimaryGreen;
    }

    public static int getAccentColorTheme(Context context, int color) {
        if (context.getResources().getColor(R.color.colorAccent_Red) == color) {
            return R.style.CustomAccentRed;
        }
        if (context.getResources().getColor(R.color.colorAccent_Pink) == color) {
            return R.style.CustomAccentPink;
        }
        if (context.getResources().getColor(R.color.colorAccent_Purple) == color) {
            return R.style.CustomAccentPurple;
        }
        if (context.getResources().getColor(R.color.colorAccent_DeepPurple) == color) {
            return R.style.CustomAccentDeepPurple;
        }
        if (context.getResources().getColor(R.color.colorAccent_Indigo) == color) {
            return R.style.CustomAccentIndigo;
        }
        if (context.getResources().getColor(R.color.colorAccent_Blue) == color) {
            return R.style.CustomAccentBlue;
        }
        if (context.getResources().getColor(R.color.colorAccent_LightBlue) == color) {
            return R.style.CustomAccentLightBlue;
        }
        if (context.getResources().getColor(R.color.colorAccent_Cyan) == color) {
            return R.style.CustomAccentCyan;
        }
        if (context.getResources().getColor(R.color.colorAccent_Teal) == color) {
            return R.style.CustomAccentTeal;
        }
        if (context.getResources().getColor(R.color.colorAccent_Green) == color) {
            return R.style.CustomAccentGreen;
        }
        if (context.getResources().getColor(R.color.colorAccent_LightGreen) == color) {
            return R.style.CustomAccentLightGreen;
        }
        if (context.getResources().getColor(R.color.colorAccent_Lime) == color) {
            return R.style.CustomAccentLime;
        }
        if (context.getResources().getColor(R.color.colorAccent_Yellow) == color) {
            return R.style.CustomAccentYellow;
        }
        if (context.getResources().getColor(R.color.colorAccent_Amber) == color) {
            return R.style.CustomAccentAmber;
        }
        if (context.getResources().getColor(R.color.colorAccent_Orange) == color) {
            return R.style.CustomAccentOrange;
        }
        if (context.getResources().getColor(R.color.colorAccent_DeepOrange) == color) {
            return R.style.CustomAccentDeepOrange;
        }
        if (context.getResources().getColor(R.color.colorAccent_Brown) == color) {
            return R.style.CustomAccentBrown;
        }
        if (context.getResources().getColor(R.color.colorAccent_Grey) == color) {
            return R.style.CustomAccentGrey;
        }
        if (context.getResources().getColor(R.color.colorAccent_BlueGrey) == color) {
            return R.style.CustomAccentBlueGrey;
        }
        return R.style.CustomAccentOrange;
    }
}
