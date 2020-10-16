package com.airtel.xstreamfiber.Util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MethodsUtil {

    public static String decryptSerial(Context context, String userSerialEncrypted) {
        MCrypt mcrypt = new MCrypt();
        String userSerial = null;
        try {
            userSerial = new String(mcrypt.decrypt(userSerialEncrypted));
        } catch (Exception ignored) {
        }
        return userSerial;
    }

    public static String encryptSerial(Context context, String userSerial) {
        MCrypt mcrypt = new MCrypt();
        String encryptedUserSerial = null;
        try {
            encryptedUserSerial = MCrypt.bytesToHex(mcrypt.encrypt(userSerial)); //https://github.com/serpro/Android-PHP-Encrypt-Decrypt
        } catch (Exception ignored) {
        }

        return encryptedUserSerial;
    }

    public static boolean isRebootCacheOver(Context ctx) {
        boolean flag = true;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("reboot_cache_time", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() < (sharedPreferences.getLong("reboot_cache_time", 0))) {
                //reboot cache time is over
                flag = false;
            }
        }
        return flag;
    }

    public static void setRebootCacheTime(Context ctx, int minutes) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE) + minutes);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("reboot_cache_time", reeboot).apply();
    }


    public static boolean isMGenerateOTPCacheOver(Context ctx) {
        boolean flag = true;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("cpe_mgen_otp_cache_time", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() < (sharedPreferences.getLong("cpe_mgen_otp_cache_time", 0))) {
                //reboot cache time is over
                flag = false;
            }
        }
        return flag;
    }
    public static boolean isAGenerateOTPCacheOver(Context ctx) {
        boolean flag = true;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("cpe_agen_otp_cache_time", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() < (sharedPreferences.getLong("cpe_agen_otp_cache_time", 0))) {
                //reboot cache time is over
                flag = false;
            }
        }
        return flag;
    }

    public static void setMGenerateOTPCacheTime(Context ctx, int seconds) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
        reboot_time.set(Calendar.SECOND, currentTime.get(Calendar.SECOND) + seconds);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("cpe_mgen_otp_cache_time", reeboot).apply();
    }
    public static void setAGenerateOTPCacheTime(Context ctx, int seconds) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
        reboot_time.set(Calendar.SECOND, currentTime.get(Calendar.SECOND) + seconds);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("cpe_agen_otp_cache_time", reeboot).apply();
    }

    public static boolean isRegOTPCacheOver(Context ctx) {
        boolean flag = true;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("reg_otp_cache_time", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() < (sharedPreferences.getLong("reg_otp_cache_time", 0))) {
                //reboot cache time is over
                flag = false;
            }
        }
        return flag;
    }

    public static void setRegOTPCacheTime(Context ctx, int seconds) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
        reboot_time.set(Calendar.SECOND, currentTime.get(Calendar.SECOND) + seconds);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("reg_otp_cache_time", reeboot).apply();
    }

    public static boolean isOTPCacheOver(Context ctx) {
        boolean flag = true;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("otp_cache_time", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() < (sharedPreferences.getLong("otp_cache_time", 0))) {
                //reboot cache time is over
                flag = false;
            }
        }
        return flag;
    }

    public static void setOTPCacheTime(Context ctx, int seconds) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
        reboot_time.set(Calendar.SECOND, currentTime.get(Calendar.SECOND) + seconds);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("otp_cache_time", reeboot).apply();
    }

    public static String getAuth() {
        String credentials = BuildConfig.basicAuthUsername + ":" + BuildConfig.basicAuthPass;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return auth;
    }

    public static boolean isLoginSessionOver(Context ctx) {
        boolean flag = false;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("loginSessionTime", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() > (sharedPreferences.getLong("loginSessionTime", 0))) {
                //login session time is over
                flag = true;
            }
        }
        return flag;
    }

    public static void setLoginSessionTime(Context ctx, int hours) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
        reboot_time.set(Calendar.SECOND, currentTime.get(Calendar.SECOND));
        reboot_time.add(Calendar.HOUR_OF_DAY, hours);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("loginSessionTime", reeboot).apply();
    }


    public static boolean isLoginCpeSessionOver(Context ctx) {
        boolean flag = false;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("loginCpeSessionTime", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() > (sharedPreferences.getLong("loginCpeSessionTime", 0))) {
                //login session time is over
                flag = true;
            }
        } else if (sharedPreferences.getLong("loginCpeSessionTime", 0) == 0) {
            flag = true;
        }
        return flag;
    }

    public static void setLoginCpeSessionTime(Context ctx, int hours) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
        reboot_time.set(Calendar.SECOND, currentTime.get(Calendar.SECOND));
        reboot_time.add(Calendar.HOUR_OF_DAY, hours);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("loginCpeSessionTime", reeboot).apply();
    }


    public static boolean isUpdateOnceStarted(Context ctx) {
        boolean flag = false;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        if (sharedPreferences.getLong("updateOnceTime", 0) != 0) {
            if (Calendar.getInstance().getTime().getTime() > (sharedPreferences.getLong("updateOnceTime", 0))) {
                //login session time is over
                flag = true;
            }
        } else if (sharedPreferences.getLong("updateOnceTime", 0) == 0) {
            flag = true;
        }
        return flag;
    }

    public static void setUpdateOnceStarted(Context ctx, int min) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constant.LOGIN_PREF, ctx.MODE_PRIVATE);
        Calendar currentTime = Calendar.getInstance();
        Calendar reboot_time = Calendar.getInstance();

        reboot_time.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
        reboot_time.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
        reboot_time.set(Calendar.SECOND, currentTime.get(Calendar.SECOND));
        reboot_time.add(Calendar.MINUTE, min);
//        reboot_time.add(Calendar.HOUR_OF_DAY, hours);

        long reeboot = reboot_time.getTime().getTime();
        sharedPreferences.edit().putLong("updateOnceTime", reeboot).apply();
    }

    public static void checkPlaystoreVersion(Context context) {
        VersionChecker versionChecker = new VersionChecker(context);
        versionChecker.execute();
        /*try {
            String latestVersion = versionChecker.execute().get();
            String versionName = BuildConfig.VERSION_NAME.replace("-DEBUG", "");
            if (latestVersion != null && !latestVersion.isEmpty()) {
                if (!latestVersion.equals(versionName)) {
                    if (checkIfUpdateAvailable(versionName, latestVersion))
                        showUpdateAvailableDialogNormal(context);
                }
            }

        } catch (InterruptedException | ExecutionException ignored) {
        }*/
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

    public static void checkApkVersion(Context context) {
        String checkApkVersionEndUrl = "v1/check_apk_version";
        Calendar reboot_time = Calendar.getInstance();
        int dateToday = reboot_time.get(Calendar.DATE);
        String key = md5(dateToday + Constant.STATIC_KEY);

        JSONObject mainJObject = new JSONObject();
        try {
            mainJObject.put("version", BuildConfig.VERSION_NAME);
            mainJObject.put("os", "android");

            RequestQueue queue = Volley.newRequestQueue(context);
            String url = BuildConfig.baseUrl + checkApkVersionEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if (statusCode.equals("200")) {
                                    boolean isUpdateAvailableForce = response.optBoolean("force_update");
                                    boolean isUpdateAvailableNormal = response.optBoolean("normal_update");
                                    if (isUpdateAvailableForce) {
                                        showUpdateAvailableDialogForce(context);
                                    } else if (isUpdateAvailableNormal) {
                                        showUpdateAvailableDialogNormal(context);
                                    } else {
                                        MethodsUtil.checkPlaystoreVersion(context);
                                    }
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            MethodsUtil.checkPlaystoreVersion(context);
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("key", key);
                    String auth = MethodsUtil.getAuth();
                    params.put("Authorization", auth);

                    return params;
                }

            };
            //Handling timeout, increasing the wait time
            getRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 30000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 0;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    throw error;
                }
            });
            queue.add(getRequest);

        } catch (Exception e) { // for caught any exception during the excecution of the service

        }
    }

    public static String getStatusCodeMessage(byte[] data) {
        String result = "";
        try {
            String s = new String(data);
            JSONObject object = new JSONObject(s);
            result = object.optString("Message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
//        return new String(data);
    }

    private static void showUpdateAvailableDialogNormal(Context context) {
        //showDialog To SendToPlayStore
        String app_name = context.getResources().getString(R.string.app_name);
        UtilsDisplay.showUpdateAvailableDialog(context, "normal", context.getResources().getString(R.string.update_title), /*app_name + " " + */context.getResources().getString(R.string.update_content_normal), context.getResources().getString(R.string.update_nothanks), context.getResources().getString(R.string.update_upgrade), "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                sendToPlayStore(context);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
//                MethodsUtil.setUpdateOnceStarted(context, Constant.UPDATE_ONCE_TIME);
            }
        }).show();
    }

    private static void showUpdateAvailableDialogForce(Context context) {
        //showDialog To SendToPlayStore
        String app_name = context.getResources().getString(R.string.app_name);
        AlertDialog dialog = UtilsDisplay.showUpdateAvailableDialog(context, "force", context.getResources().getString(R.string.update_title), /*app_name + " " +*/ context.getResources().getString(R.string.update_content_force), context.getResources().getString(R.string.update_cancel), context.getResources().getString(R.string.update_upgrade), "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
                sendToPlayStore(context);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
                minimizeApp(context);
                ((Activity) context).finish();
            }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                dialog.dismiss();
                sendToPlayStore(context);
            }
        });
    }

    public static void minimizeApp(Context context){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(a);
    }

    public static void sendToPlayStore(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    private static StringBuilder hexString;

    private static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException ignored) {
        }
        return "";
    }

    public static boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constant.LOADING_QUES, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
        {
            editor.putBoolean(arrayName + "_" + i+"used",false);
            editor.putString(arrayName + "_" + i, array[i]);
        }
        return editor.commit();
    }
    public static boolean saveUsedArray(String key, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constant.LOADING_QUES, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key+"used",true);
        return editor.commit();
    }
    /*public static String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constant.LOADING_QUES, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }*/
    public static List<String> loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constant.LOADING_QUES, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        List<String> stringList = new ArrayList<>();
        for(int i=0;i<size;i++)
        {
            if (!prefs.getBoolean(arrayName + "_" + i+"used",false)){
                array[i] = prefs.getString(arrayName + "_" + i, null);
                stringList.add(prefs.getString(arrayName + "_" + i, null));
            }
        }
        if (stringList.size()==0){
            SharedPreferences.Editor editor = prefs.edit();
            for(int i=0;i<size;i++)
            {
                editor.putBoolean(arrayName + "_" + i+"used",false);
            }
            editor.apply();
        }
        return stringList;
    }
    public static void initLoadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(Constant.LOADING_QUES, 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        SharedPreferences.Editor editor = prefs.edit();
        for(int i=0;i<size;i++)
        {
            editor.putBoolean(arrayName + "_" + i+"used",false);
        }
        editor.apply();
    }
}
