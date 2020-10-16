package com.airtel.xstreamfiber.Util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airtel.xstreamfiber.R;

//Showing and dismissing dialog
public class UIUtils {

    private static Toast toast = null;

    /**
     * show Progress Dialog
     *
     * @return ProgressDialog
     */
    public static Dialog showProgressDialog(Context activityContext) {
        Dialog dialog = null;

        try {
            dialog = new Dialog(activityContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
            dialog.setContentView(R.layout.progress_dialog_layout);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception ignored) {
        }

        return dialog;
    }

    public static Dialog showProgressDialogWithQuestions(Context activityContext) {
        Dialog dialog = null;

        try {
            dialog = new Dialog(activityContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.progress_dialog_layout_questions);
            dialog.setCancelable(false);
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                window.setGravity(Gravity.CENTER_HORIZONTAL);
                window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
        } catch (Exception ignored) {
        }

        return dialog;
    }

    /**
     * dismiss progress dialog
     *
     * @author Pinelabs
     */
    public static void dismissDialog(Dialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (IllegalArgumentException ignored) {

        }
    }

    /**
     * Show Toast
     *
     * @param toastMessage Toast Message Content
     */
    public static void showToast(Context context, String toastMessage) {
        // Check Toast.
        if (toast != null) {
            View view = toast.getView();

            if (view.isShown()) {
                return;
            }
        }

        // Create new Toast.

        toast = Toast.makeText(context, toastMessage, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Show Toast
     *
     * @param toastMessage Toast Message Content Id
     */
    public static void showToast(Context context, int toastMessage) {
        // Check Toast.
        if (toast != null) {
            View view = toast.getView();

            if (view.isShown()) {
                return;
            }
        }

        // Create new Toast.
        toast = Toast.makeText(context, toastMessage, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showCustomToast(Context context, String msg)
    {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.getBackground().setColorFilter(context.getResources().getColor(R.color.toast_background), PorterDuff.Mode.SRC_IN);
       // view.setBackgroundColor(context.getResources().getColor(R.color.black));
        TextView text = (TextView) view.findViewById(android.R.id.message);
        text.setTextColor(context.getResources().getColor(R.color.black));
        /*Here you can do anything with above textview like text.setTextColor(Color.parseColor("#000000"));*/
        toast.show();
    }
}