package ca.pkay.rcloneexplorer.Dialogs;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class Dialogs {

    /**
     * Dismiss a dialog, but only if it is still attached to a host activity.
     * <br><br>
     * This should be used especially in callbacks, since they have a higher
     * chance of being executed in a different lifecycle phase and thus an
     * elevated propability of calling DialogFrament.dismiss() on an invalid
     * fragment.
     * @param dialog the dialog to dismiss
     */
    public static void dismissSilently(@Nullable DialogFragment dialog) {
        if(dialog != null) {
            if(dialog.isStateSaved()){
                dialog.dismissAllowingStateLoss();
            } else if(dialog.isAdded()) {
                dialog.dismiss();
            }
        }
    }
}
