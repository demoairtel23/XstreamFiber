package com.airtel.xstreamfiber.Util;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.airtel.xstreamfiber.R;
import com.google.android.material.snackbar.Snackbar;

import java.net.URL;

//https://github.com/javiersantos/AppUpdater/blob/master/library/src/main/java/com/github/javiersantos/appupdater/UtilsLibrary.java
public class UtilsDisplay {

    public static AlertDialog showUpdateAvailableDialog(final Context context, String type, String title, String content, String btnNegative, String btnPositive, String btnNeutral, final DialogInterface.OnClickListener updateClickListener, final DialogInterface.OnClickListener dismissClickListener) {
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.AlertDialogDanger);
        final AlertDialog alertDialog = new AlertDialog.Builder(themeWrapper).create();
        alertDialog.setTitle(Html.fromHtml(title));
        alertDialog.setCancelable(false);
        alertDialog.setMessage(content);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, btnPositive, updateClickListener);
        if (btnNegative!=null && !TextUtils.isEmpty(btnNegative))
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, btnNegative, dismissClickListener);
        return alertDialog;
    }

    static AlertDialog showUpdateAvailableDialogCustom(final Context context, String title, String content, String btnNegative, String btnPositive, String btnNeutral, final View.OnClickListener updateClickListener, final View.OnClickListener dismissClickListener) {
//        final Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(false);
//        dialog.setContentView(R.layout.custom_dialog_update);



        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        View dialog = LayoutInflater.from(context).inflate(R.layout.custom_dialog_update, null);
        alertBuilder.setView(dialog);
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setCancelable(false);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText(Html.fromHtml(title));
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        Button btnNo = dialog.findViewById(R.id.btnNegative);
        if (btnNegative!=null && !TextUtils.isEmpty(btnNegative))
        {
            btnNo.setVisibility(View.VISIBLE);
            btnNo.setText(btnNegative);
            btnNo.setOnClickListener(dismissClickListener);
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }else {
            btnNo.setVisibility(View.GONE);
        }
        Button btnYes = dialog.findViewById(R.id.btnPositive);
        btnYes.setText(btnPositive);
        btnYes.setOnClickListener(updateClickListener);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        return alertDialog;
    }

    static AlertDialog showUpdateNotAvailableDialog(final Context context, String title, String content) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(context.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create();
    }

    static Snackbar showUpdateAvailableSnackbar(final Context context, String content, Boolean indefinite, final URL url) {
        Activity activity = (Activity) context;
        int snackbarTime = indefinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;

        /*if (indefinite) {
            snackbarTime = Snackbar.LENGTH_INDEFINITE;
        } else {
            snackbarTime = Snackbar.LENGTH_LONG;
        }*/

        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), content, snackbarTime);
        snackbar.setAction(context.getResources().getString(R.string.appupdater_btn_update), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
                    context.startActivity(intent);
                }
            }
        });
        return snackbar;
    }

    static Snackbar showUpdateNotAvailableSnackbar(final Context context, String content, Boolean indefinite) {
        Activity activity = (Activity) context;
        int snackbarTime = indefinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;

        /*if (indefinite) {
            snackbarTime = Snackbar.LENGTH_INDEFINITE;
        } else {
            snackbarTime = Snackbar.LENGTH_LONG;
        }*/


        return Snackbar.make(activity.findViewById(android.R.id.content), content, snackbarTime);
    }

    static void showUpdateAvailableNotification(Context context, String title, String content, URL apk, int smallIconResourceId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        initNotificationChannel(context, notificationManager);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentUpdate = PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = getBaseNotification(context, contentIntent, title, content, smallIconResourceId)
                .addAction(R.drawable.ic_system_update_white_24dp, context.getResources().getString(R.string.appupdater_btn_update), pendingIntentUpdate);

        notificationManager.notify(0, builder.build());
    }
    static void showUpdateAvailableNotification2(Context context, String title, String content, int smallIconResourceId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        initNotificationChannel(context, notificationManager);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentUpdate = PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = getBaseNotification(context, contentIntent, title, content, smallIconResourceId)
                .addAction(R.drawable.ic_system_update_white_24dp, context.getResources().getString(R.string.appupdater_btn_update), pendingIntentUpdate);

        notificationManager.notify(0, builder.build());
    }

    static void showUpdateNotAvailableNotification(Context context, String title, String content, int smallIconResourceId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        initNotificationChannel(context, notificationManager);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = getBaseNotification(context, contentIntent, title, content, smallIconResourceId)
                .setAutoCancel(true);

        notificationManager.notify(0, builder.build());
    }

    private static NotificationCompat.Builder getBaseNotification(Context context, PendingIntent contentIntent, String title, String content, int smallIconResourceId) {
        return new NotificationCompat.Builder(context, context.getString(R.string.appupdater_channel))
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setSmallIcon(smallIconResourceId)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

    }

    private static void initNotificationChannel(Context context, NotificationManager notificationManager) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    context.getString(R.string.appupdater_channel),
                    context.getString(R.string.appupdater_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

}