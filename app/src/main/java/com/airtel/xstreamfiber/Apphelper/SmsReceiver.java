package com.airtel.xstreamfiber.Apphelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

//Helping in receiving msg from sms received for auto reading OTP
public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener mListener;
    Boolean b;
    String abcd,xyz;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle data  = intent.getExtras();
        try {
            if (data != null) {
                Object[] pdus = (Object[]) data.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    b = sender.endsWith("AIRSZS");  //Just to fetch otp sent from "Whatever your sms sender name is" in place of WNRCRP
                    String messageBody = smsMessage.getMessageBody();
                    abcd = messageBody.replaceAll("[^0-9]", "");   // here abcd contains otp which is in number format


                    // mListener.messageReceived(abcd);

                    //Pass on the text to our listener.
                    if (b) {
                        mListener.messageReceived(abcd);  // attach value to interface object
                    }
//            else
//            {
//            }
                }
            }
        }
        catch (Exception ignored)
        {
        }

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}
