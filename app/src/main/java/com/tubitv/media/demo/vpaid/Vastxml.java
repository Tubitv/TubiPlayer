package com.tubitv.media.demo.vpaid;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by allensun on 8/9/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class Vastxml {


    private static final String adXmlBody = "dsfsdf";

    public static String getAdXmlBody() {
        if (TextUtils.isEmpty(adXmlBody)) {
            return "";
        }

        try {

            byte[] decodedData = Base64.decode(adXmlBody, Base64.DEFAULT);
            return new String(decodedData, "UTF-8");
        } catch (IllegalArgumentException | UnsupportedEncodingException ex) {
            Log.e("VastXML", ex.getMessage());
            return "";
        }
    }
}
