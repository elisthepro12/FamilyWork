package com.example.familywork;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    // מפתח יום בפורמט yyyy-MM-dd לשימוש ב-Firebase (קל למיון ותצוגה)
    public static String dayKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // מחרוזת קריאה נחמדה ליום להצגה
    public static String dayTitle(String dayKey) {
        try {
            SimpleDateFormat src = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dst = new SimpleDateFormat("EEEE - dd/MM/yyyy", Locale.getDefault());
            Date d = src.parse(dayKey);
            return dst.format(d);
        } catch (Exception e) {
            return dayKey;
        }
    }


        public static String bitmapToBase64(Bitmap bitmap) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        public static Bitmap base64ToBitmap(String base64) {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

    }
