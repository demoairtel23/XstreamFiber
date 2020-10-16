package com.airtel.xstreamfiber.Util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.StringTokenizer;

public class VersionChecker extends AsyncTask<String, String, String> {

    private Context ctx;
    private String newVersion;
    private String package_name = "com.airtel.xstreamfiber";

    public VersionChecker(Context context) {
        this.ctx = context;
    }

    @Override
    protected String doInBackground(String... params) {
        //Link: https://stackoverflow.com/questions/48926962/android-get-play-store-app-version
        try {
            Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + package_name + "&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            if (document != null) {
                Elements element = document.getElementsContainingOwnText("Current Version");
                for (Element ele : element) {
                    if (ele.siblingElements() != null) {
                        Elements sibElemets = ele.siblingElements();
                        for (Element sibElemet : sibElemets) {
                            newVersion = sibElemet.text();
                        }
                    }
                }
            }
            return newVersion;
        } catch (IOException e) {
            return newVersion;
        }
    }

    @Override
    protected void onPostExecute(String latestVersion) {
        super.onPostExecute(latestVersion);
        String versionName = BuildConfig.VERSION_NAME.replace("-DEBUG", "");
        if (latestVersion != null && !latestVersion.isEmpty()) {
            if (!latestVersion.equals(versionName)) {
                if (checkIfUpdateAvailable(versionName, latestVersion))
                    showUpdateAvailableDialogNormal(ctx);
            }
        }
    }



    private static boolean checkIfUpdateAvailable(String currentApp, String playStoreApp) {
        boolean flag = false;
        if (currentApp.contains(".") && playStoreApp.contains(".") && currentApp.length() == playStoreApp.length()) {
            StringTokenizer tokens = new StringTokenizer(playStoreApp, ".");
            Integer[] verPartsInt = new Integer[tokens.countTokens()];
            int count = 0;
            while (tokens.hasMoreTokens()) {
                int token = Integer.parseInt(tokens.nextToken());
                verPartsInt[count] = token;
                count++;
            }

            StringTokenizer tokensMy = new StringTokenizer(currentApp, ".");
            Integer[] verPartsIntMy = new Integer[tokensMy.countTokens()];
            int countMy = 0;
            while (tokensMy.hasMoreTokens()) {
                int tokenMy = Integer.parseInt(tokensMy.nextToken());
                verPartsIntMy[countMy] = tokenMy;
                countMy++;
            }

            for (int i = 0; i < verPartsIntMy.length; i++) {
                if (verPartsInt[i] != null && verPartsIntMy[i] != null) {
                    if (verPartsInt[i] > verPartsIntMy[i]) {
                        flag = true;
                        break;
                    } else if (verPartsInt[i] < verPartsIntMy[i]) {
                        break;
                    }
                }
            }
        }

        return flag;
    }

    private static void showUpdateAvailableDialogNormal(Context context) {
        //showDialog To SendToPlayStore
        String app_name = context.getResources().getString(R.string.app_name);
        UtilsDisplay.showUpdateAvailableDialog(context, "normal", context.getResources().getString(R.string.update_title), /*app_name + " " + */context.getResources().getString(R.string.update_content_normal), context.getResources().getString(R.string.update_nothanks), context.getResources().getString(R.string.update_upgrade), "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                MethodsUtil.sendToPlayStore(context);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
//                MethodsUtil.setUpdateOnceStarted(context, Constant.UPDATE_ONCE_TIME);
            }
        }).show();
    }
}