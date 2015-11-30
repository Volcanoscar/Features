package com.mlt.floatmultitask;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laiyang on 15-7-9.
 */
public class SmsContent {

    /**

     * @return
     */
    public static List<SmsInfo> getSmsInfo(Context context, Uri uri) {
        List<SmsInfo> infos = new ArrayList<SmsInfo>();
        String[] projection = new String[] {"_id","thread_id","address","person","body","date","type"};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, "date desc");
        int nameColumn = cursor.getColumnIndex("person");
        int phoneNmuberColumn = cursor.getColumnIndex("address");
        int smsbodyColumn = cursor.getColumnIndex("body");
        int dateColumn = cursor.getColumnIndex("date");
        int typeColumn = cursor.getColumnIndex("type");
        int threadIdColumn = cursor.getColumnIndex("thread_id");
        int idColumn = cursor.getColumnIndex("_id");
        if(cursor != null) {
            while(cursor.moveToNext()) {
                SmsInfo smsinfo = new SmsInfo();
                smsinfo.setId(cursor.getInt(idColumn));
                smsinfo.setThreadId(cursor.getInt(threadIdColumn));
                smsinfo.setName(cursor.getString(nameColumn));
                smsinfo.setDate(cursor.getString(dateColumn));
                smsinfo.setPhoneNumber(cursor.getString(phoneNmuberColumn));
                smsinfo.setSmsbody(cursor.getString(smsbodyColumn));
                smsinfo.setType(cursor.getString(typeColumn));
                infos.add(smsinfo);
            }
            cursor.close();
        }
        return infos;
    }

    /**

     * @param context
     * @return
     */
    public static int getNewSmsCount(Context context) {
        int num = 0;
        Cursor csr = context.getContentResolver().query(Uri.parse("content://sms"), null, "type = 1 " +
                "and read = 0", null, null);
        if(csr != null) {
            num = csr.getCount();
            csr.close();
        }
        return num;
    }


}
