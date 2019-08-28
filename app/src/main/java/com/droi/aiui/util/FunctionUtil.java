package com.droi.aiui.util;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import com.droi.aiui.R;
import com.droi.aiui.bean.AppInfo;
import com.droi.aiui.bean.RemindDataTime;
import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.bean.Song;
import com.droi.aiui.ui.RemindActivity;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cuixiaojun on 17-12-6.
 * �����Ժ�����չ��
 */

public class FunctionUtil {

    public static final String TAG = "FunctionUtil";
    /**
     * Unknown network class
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;

    /**
     * wifi net work
     */
    public static final int NETWORK_WIFI = 1;

    /**
     * "2G" networks
     */
    public static final int NETWORK_CLASS_2_G = 2;

    /**
     * "3G" networks
     */
    public static final int NETWORK_CLASS_3_G = 3;

    /**
     * "4G" networks
     */
    public static final int NETWORK_CLASS_4_G = 4;

    /**
     * ��ʽ��ʱ���ʽ
     */
    public static final String TIME_FORMATE = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMATE1 = "yyyy��MM��dd�� HHʱmm��ss��";

    /**
     * �������������ļ�
     */
    public static final String AIUI_SETTINGS = "aiui_settings";

    public static final String KEY_REMINDINFO_DATA = "key_remindInfo";
    public static final String KEY_REMINDINFO = "remindInfo";
    private static AudioManager audioManager;
    private static ContentResolver contentResolver;

    /**
     * ��ȡassetĿ¼���ļ���
     *
     * @return content
     */
    public static String readFile(Context mContext, String file, String code) {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * ��ȡassetĿ¼���ļ���
     */
    public static byte[] readFile(Context ctx, String file) {
        int len = 0;
        byte[] buf = null;
        InputStream in = null;
        try {
            in = ctx.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return buf;
    }

    /**
     *
     * @param currentTime Ҫת����long���͵�ʱ��
     * @param formatType Ҫת����ʱ���ʽyyyy-MM-dd HH:mm:ss//yyyy��MM��dd�� HHʱmm��ss��
     * @return
     * @throws ParseException
     */
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // ����long���͵ĺ���������һ��date���͵�ʱ��
        String sDateTime = dateToString(dateOld, formatType); // ��date���͵�ʱ��ת��Ϊstring
        Date date = stringToDate(sDateTime, formatType); // ��String����ת��ΪDate����
        return date;
    }

    /**
     *
     * @param data Date���͵�ʱ��
     * @param formatType ��ʽΪyyyy-MM-dd HH:mm:ss//yyyy��MM��dd�� HHʱmm��ss��
     * @return
     */
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    /**
     *
     * @param currentTime Ҫת����long���͵�ʱ��
     * @param formatType Ҫת����string���͵�ʱ���ʽ
     * @return
     */
    public static String longToString(long currentTime, String formatType) {
        Date date = null; // long����ת��Date����
        try {
            date = longToDate(currentTime, formatType);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String strTime = dateToString(date, formatType); // date����ת��String
        return strTime;
    }

    /**
     *
     * @param strTime Ҫת����string���͵�ʱ�䣬formatTypeҪת���ĸ�ʽyyyy-MM-dd HH:mm:ss//yyyy��MM��dd�� HHʱmm��ss�룬
     * @param formatType strTime��ʱ���ʽ����Ҫ��formatType��ʱ���ʽ��ͬ
     * @return
     */
    public static Date stringToDate(String strTime, String formatType) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        try {
            date = formatter.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     *
     * @param strTime Ҫת����String���͵�ʱ��
     * @param formatType strTime��ʱ���ʽ��formatType��ʱ���ʽ������ͬ
     * @return
     */
    public static long stringToLong(String strTime, String formatType) {
        Date date = stringToDate(strTime, formatType);
        if (date == null) {
            return 0;
        } else {
            //date����ת��long����
            return dateToLong(date);
        }
    }

    /**
     * dateҪת����date���͵�ʱ��
      */
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * ͨ��longʱ���ȡ��Ӧ��������
     */
    public static RemindDataTime getRemindDateTime(long longTime) {
        RemindDataTime remindDataTime = new RemindDataTime();
        try {
            Date date = new Date(longTime);
            SimpleDateFormat formatYear = new SimpleDateFormat("yyyy��");
            SimpleDateFormat formatDay = new SimpleDateFormat("MM��dd��");
            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
            SimpleDateFormat formatWeek = new SimpleDateFormat("EEEE");
            SimpleDateFormat formatAmPm = new SimpleDateFormat("a");

            String year = formatYear.format(date);
            String day = formatDay.format(date);
            String time = formatTime.format(date);
            String week = formatWeek.format(date);
            String ampm = formatAmPm.format(date);

            remindDataTime.setYear(year);
            remindDataTime.setDay(day);
            remindDataTime.setTime(time);
            remindDataTime.setWeek(week);
            remindDataTime.setAmpm(ampm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return remindDataTime;
    }

    /**
     * ��⵱ǰ�����磨WLAN��3G/2G��״̬
     *
     * @param context Context
     * @return true ��ʾ�������
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = null;
        if (context != null) {
            connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // ��ǰ���������ӵ�
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // ��ǰ�����ӵ��������
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ���wifi�Ƿ񴦿�����״̬
     *
     * @return
     */
    private static boolean isWifiConnect(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    /**
     * ���wifiǿ���Ƿ����
     */
    public static boolean isWifiEnable(Context context) {
        if (isWifiConnect(context)) {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int wifi = mWifiInfo.getRssi();//��ȡwifi�ź�ǿ��
            if (wifi > -80 && wifi < 0) {//�źűȽ�ǿ
                return true;
            }
        }
        return false;
    }

    /**
     * ��ȡ��ǰ�������������
     *
     * @param context
     * @return
     */
    public static int getNetWorkStatus(Context context) {
        int netWorkType = NETWORK_CLASS_UNKNOWN;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();

            if (type == ConnectivityManager.TYPE_WIFI) {
                netWorkType = NETWORK_WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                netWorkType = getNetWorkClass(context);
            }
        }

        return netWorkType;
    }

    private static int getNetWorkClass(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    /**
     * ��������
     * �ж��ֻ����Ƿ�װ��ĳ��Ӧ��
     */
    public static boolean checkApkInstall(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo == null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ����app
     *
     * @param componentName
     * @param context
     */
    public static synchronized boolean startApp(ComponentName componentName, Context context) {
        if (componentName == null) {
            return false;
        }
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(componentName);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * �ж��Ƿ����SIM��
     *
     * @return ״̬
     */
    public static boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // û��SIM��
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }

    /**
     * ͨ��Ӧ�����ƻ�ȡӦ�õİ���������
     *
     * @param allApps
     * @param appName
     * @return
     */
    public static ComponentName getComponentByAppName(List<AppInfo> allApps, String appName) {
        ComponentName component = null;
        for (int i = 0; i < allApps.size(); i++) {
            AppInfo info = allApps.get(i);
            if (info.getAppName().equals(appName)) {
                String pckName = info.getPackageName();
                String className = info.getClassName();
                component = new ComponentName(pckName, className);
            }
        }
        return component;
    }

    /**
     * �жϵ�ǰwifi�Ƿ��
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * �򿪺͹ر�wifi
     *
     * @param context
     * @param state
     */
    public static boolean setWifiEnable(Context context, boolean state) {
        //���ȣ���Contextͨ��getSystemService��ȡwifimanager
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //����WifiManager��setWifiEnabled��������wifi�Ĵ򿪻��߹رգ�ֻ��������state��Ϊ����ֵ���ɣ�true:�� false:�رգ�
        return mWifiManager.setWifiEnabled(state);
    }

    /**
     * �жϵ�ǰ�����Ƿ��
     */
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int state = bluetoothAdapter.getState();
        if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_ON) {
            return true;
        }
        return false;
    }

    /**
     * �жϵ�ǰ�����Ƿ��
     */
    public static boolean setBluetoothEnabled(boolean state) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (state) {
            return bluetoothAdapter.enable();
        } else {
            return bluetoothAdapter.disable();
        }
    }

    /**
     * ����õ����ؽ��
     */
    public static String getAnswer(Context context) {
        Random random = new Random();
        int[] answer = new int[]{
                R.string.default_answer,
                R.string.default_answer1,
                R.string.default_answer2,
                R.string.default_answer3,
                R.string.default_answer4,
                R.string.default_answer5,
                R.string.default_answer6,
                R.string.default_answer7,
                R.string.default_answer8,
                R.string.default_answer9,
                R.string.default_answer10,
                R.string.default_answer11,
                R.string.default_answer12,
                R.string.default_answer13,
                R.string.default_answer14
        };
        int resId = random.nextInt(answer.length);
        return context.getResources().getString(answer[resId]);
    }

    /**
     * �жϵ�ǰ��Ļ�Ƿ����
     */
    public static boolean isScreenLocked(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * ��������֪ͨ
     *
     * @param
     */
    public static void sendNotification(Context context, RemindInfo remindInfo) {
        //��ȡNotificationManagerʵ��
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String time = FunctionUtil.getRemindDateTime(remindInfo.getTime()).getTime();
        Intent mainIntent = new Intent(context, RemindActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FunctionUtil.KEY_REMINDINFO,remindInfo);
        mainIntent.putExtra(FunctionUtil.KEY_REMINDINFO_DATA, bundle);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //ʵ����NotificationCompat.Builde�������������
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setContentText(remindInfo.getContent() + "\n" + time)
                .setContentTitle("����")
                .setSmallIcon(R.mipmap.cappu_remind)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setContentIntent(mainPendingIntent)
                .setOngoing(true)
                .setDefaults(Notification.FLAG_NO_CLEAR | Notification.FLAG_INSISTENT)
                .build();
        notifyManager.notify(0, notification);
    }

    //ȡ����Ӧ������֪ͨ
    public static void cancelNotification(Context context, RemindInfo remindInfo) {
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //notifyManager.cancel(Integer.parseInt(String.valueOf(remindInfo.getId())));
        notifyManager.cancel(0);
    }

    // ���ε����ť֮��ĵ�������������1000����
    private static final int MIN_CLICK_DELAY_TIME = 600;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = true;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    /**
     * �����ֵ�Ͳ
     */
    public static boolean handelFlashLight(Context context,CameraManager cameraManager, boolean open) {
        try {
            //��ȡCameraManager
            //��ȡ��ǰ�ֻ���������ͷ�豸ID
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                //��ѯ������ͷ����Ƿ���������
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable
                        && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    //�򿪻�ر��ֵ�Ͳ
                    cameraManager.setTorchMode(id, open);
                    setTorchState(context,open);
                    return true;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * �Ƿ�����ֵ�Ͳ
     *
     * @return
     */
    public static boolean isHasFlashLight(CameraManager cameraManager) {
        try {
            //��ȡ��ǰ�ֻ���������ͷ�豸ID
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                //��ѯ������ͷ����Ƿ���������
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (flashAvailable != null && flashAvailable) {
                    return true;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * �����ֵ�Ͳ��״̬
     *
     * @param isTorchOpen
     */
    private static void setTorchState(Context context,boolean isTorchOpen) {
        if(isTorchOpen){
            Settings.Secure.putInt(context.getContentResolver(),"flashlight.state",0);//�ֵ�Ͳ��
        }else{
            Settings.Secure.putInt(context.getContentResolver(),"flashlight.state",1);//�ֵ�Ͳ�ر�
        }
    }

    /**
     * ���ֵ�Ͳ��״̬
     *
     * @return
     */
    public static int getTorchState(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(),"flashlight.state");
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }



    /**
     * �򿪺͹ر�GPS
     */
    public static boolean setLocationEnabled(Context context, boolean enabled) {
        int currentUserId = ActivityManager.getCurrentUser();
        if (isUserLocationRestricted(context, currentUserId)) {
            return false;
        }
        final ContentResolver cr = context.getContentResolver();
        int mode = enabled
                ? Settings.Secure.LOCATION_MODE_HIGH_ACCURACY : Settings.Secure.LOCATION_MODE_OFF;
        return Settings.Secure
                .putIntForUser(cr, Settings.Secure.LOCATION_MODE, mode, currentUserId);
    }

    /**
     * �жϵ�ǰGPS�Ƿ��
     */
    public static boolean isLocationEnabled(Context context) {
        ContentResolver resolver = context.getContentResolver();
        int mode = Settings.Secure.getIntForUser(resolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF, ActivityManager.getCurrentUser());
        return mode != Settings.Secure.LOCATION_MODE_OFF;
    }

    /**
     * Returns true if the current user is restricted from using location.
     */
    public static boolean isUserLocationRestricted(Context context, int userId) {
        final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        return um.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION, new UserHandle(userId));
    }

    /**
     * �򿪺͹ر���������
     */
    public static void setDataEnabled(Context context, boolean enable) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        mTelephonyManager.setDataEnabled(enable);
    }

    /**
     * ��ȡ��ǰ��������״̬
     */
    public static boolean getDataEnabled(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return mTelephonyManager.getDataEnabled();
    }

    /**
     * ��ǰ����
     * <p>
     * shadow add
     *
     * @param context
     * @return
     */
    public static int getVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }


    /**
     * ��������
     *
     * @param context
     */
    public static void upVoice(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

    /**
     * ��С����
     *
     * @param context
     */
    public static void downVoice(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

    /**
     * ��ȡ��ǰ��Ļ����
     *
     * @param context
     * @return brightValue
     */
    public static int getBrightness(Context context) {
        int brightValue = 0;
        contentResolver = context.getContentResolver();
        try {
            brightValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightValue;
    }

    /**
     * ������Ļ����
     *
     * @param context
     */
    public static void upBrightness(Context context) {
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        int MaxBrightValue = getBrightness(context);
        if (204 <= MaxBrightValue) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
            context.getContentResolver().notifyChange(uri, null);
        } else {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, MaxBrightValue + 51);
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    /**
     * ������Ļ����
     *
     * @param context
     */
    public static void downBrightness(Context context) {
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        int MinBrightValue = getBrightness(context);
        if (MinBrightValue <= 55) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0);
            context.getContentResolver().notifyChange(uri, null);
        } else {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, MinBrightValue - 51);
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    /**
     * ����ַ��������ж������ַ�
     * @param normalStrng
     * @return
     */
    public static String formateString(String normalStrng){
        if (normalStrng != null && !"".equals(normalStrng.trim())) {
            String regEx="[\\s~��`!��@#��$%^����&*��()��\\-����\\-_=+��\\[\\]����{}��\\|��\\\\��;��:��'����\"��,��<��.��>��/��?]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(normalStrng);
            return m.replaceAll("");
        }
        return null;
    }

    /**
     * ��ȡ���ֲ����б�
     */
    public static long[] getPlayList(List<Song> allSongs){
        long[] list = new long[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            list[i] = allSongs.get(i).getId();
        }
        return list;
    }

    /**
     * ���Ÿ���
     *//*
    public static void playMusicByList(IMediaPlaybackService mMusicService,long[] list, int position){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(mMusicService.isPlaying()){
                        mMusicService.stop();
                    }
                    mMusicService.open(list,position);
                    mMusicService.play();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }, 3000);
    }*/

    /**
     * ���ò���ģʽ
     *//*
    public static int getShuffleMode(IMediaPlaybackService mMusicService){
        try {
            return mMusicService.getShuffleMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }*/

    /**
     * ����ѭ��ģʽ
     */
    /*public static void setRepeatMode(IMediaPlaybackService mMusicService, int mode){
        try {
            mMusicService.setRepeatMode(mode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * ֹͣ���Ÿ���
     */
    /*public static void stopPlaySong(IMediaPlaybackService mMusicService){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mMusicService.stop();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
    }*/

}