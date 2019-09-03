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
import android.util.Log;

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
 * 功能性函数扩展类
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
     * 格式化时间格式
     */
    public static final String TIME_FORMATE = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMATE1 = "yyyy年MM月dd日 HH时mm分ss秒";

    /**
     * 智能语音本地文件
     */
    public static final String AIUI_SETTINGS = "aiui_settings";

    public static final String KEY_REMINDINFO_DATA = "key_remindInfo";
    public static final String KEY_REMINDINFO = "remindInfo";
    private static AudioManager audioManager;
    private static ContentResolver contentResolver;

    /**
     * 读取asset目录下文件。
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
     * 读取asset目录下文件。
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
     * @param currentTime 要转换的long类型的时间
     * @param formatType 要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
     * @return
     * @throws ParseException
     */
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    /**
     *
     * @param data Date类型的时间
     * @param formatType 格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
     * @return
     */
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    /**
     *
     * @param currentTime 要转换的long类型的时间
     * @param formatType 要转换的string类型的时间格式
     * @return
     */
    public static String longToString(long currentTime, String formatType) {
        Date date = null; // long类型转成Date类型
        try {
            date = longToDate(currentTime, formatType);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    /**
     *
     * @param strTime 要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒，
     * @param formatType strTime的时间格式必须要与formatType的时间格式相同
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
     * @param strTime 要转换的String类型的时间
     * @param formatType strTime的时间格式和formatType的时间格式必须相同
     * @return
     */
    public static long stringToLong(String strTime, String formatType) {
        Date date = stringToDate(strTime, formatType);
        if (date == null) {
            return 0;
        } else {
            //date类型转成long类型
            return dateToLong(date);
        }
    }

    /**
     * date要转换的date类型的时间
      */
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * 通过long时间获取对应的年月日
     */
    public static RemindDataTime getRemindDateTime(long longTime) {
        RemindDataTime remindDataTime = new RemindDataTime();
        try {
            Date date = new Date(longTime);
            SimpleDateFormat formatYear = new SimpleDateFormat("yyyy年");
            SimpleDateFormat formatDay = new SimpleDateFormat("MM月dd日");
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
     * 检测当前的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = null;
        if (context != null) {
            connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查wifi是否处开连接状态
     *
     * @return
     */
    private static boolean isWifiConnect(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    /**
     * 检查wifi强弱是否可用
     */
    public static boolean isWifiEnable(Context context) {
        if (isWifiConnect(context)) {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int wifi = mWifiInfo.getRssi();//获取wifi信号强度
            if (wifi > -80 && wifi < 0) {//信号比较强
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前网络的连接类型
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
     * 给定包名
     * 判断手机中是否安装了某个应用
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
     * 启动app
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
            Log.d(TAG, "应用打开失败！");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public static boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        Log.d(TAG, result ? "有SIM卡" : "无SIM卡");
        return result;
    }

    /**
     * 通过应用名称获取应用的包名的类名
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
     * 判断当前wifi是否打开
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * 打开和关闭wifi
     *
     * @param context
     * @param state
     */
    public static boolean setWifiEnable(Context context, boolean state) {
        //首先，用Context通过getSystemService获取wifimanager
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //调用WifiManager的setWifiEnabled方法设置wifi的打开或者关闭，只需把下面的state改为布尔值即可（true:打开 false:关闭）
        return mWifiManager.setWifiEnabled(state);
    }

    /**
     * 判断当前蓝牙是否打开
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
     * 判断当前蓝牙是否打开
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
     * 随机得到返回结果
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
        Log.d(TAG, "getAnswer--->resId = " + resId);
        return context.getResources().getString(answer[resId]);
    }

    /**
     * 判断当前屏幕是否解锁
     */
    public static boolean isScreenLocked(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 发送提醒通知
     *
     * @param
     */
    public static void sendNotification(Context context, RemindInfo remindInfo) {
        Log.d(TAG, "发送通知=--->id = " + Integer.parseInt(String.valueOf(remindInfo.getId())) + ",remindInfo = " + remindInfo);
        //获取NotificationManager实例
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String time = FunctionUtil.getRemindDateTime(remindInfo.getTime()).getTime();
        Intent mainIntent = new Intent(context, RemindActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FunctionUtil.KEY_REMINDINFO,remindInfo);
        mainIntent.putExtra(FunctionUtil.KEY_REMINDINFO_DATA, bundle);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setContentText(remindInfo.getContent() + "\n" + time)
                .setContentTitle("提醒")
                .setSmallIcon(R.mipmap.cappu_remind)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setContentIntent(mainPendingIntent)
                .setOngoing(true)
                .setDefaults(Notification.FLAG_NO_CLEAR | Notification.FLAG_INSISTENT)
                .build();
        notifyManager.notify(0, notification);
    }

    //取消对应的提醒通知
    public static void cancelNotification(Context context, RemindInfo remindInfo) {
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "取消通知---->id = " + Integer.parseInt(String.valueOf(remindInfo.getId())));
        //notifyManager.cancel(Integer.parseInt(String.valueOf(remindInfo.getId())));
        notifyManager.cancel(0);
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
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
     * 操作手电筒
     */
    public static boolean handelFlashLight(Context context,CameraManager cameraManager, boolean open) {
        try {
            //获取CameraManager
            //获取当前手机所有摄像头设备ID
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                //查询该摄像头组件是否包含闪光灯
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable
                        && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    //打开或关闭手电筒
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
     * 是否包含手电筒
     *
     * @return
     */
    public static boolean isHasFlashLight(CameraManager cameraManager) {
        try {
            //获取当前手机所有摄像头设备ID
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                //查询该摄像头组件是否包含闪光灯
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
     * 设置手电筒的状态
     *
     * @param isTorchOpen
     */
    private static void setTorchState(Context context,boolean isTorchOpen) {
        Log.d(TAG,"[setTorchState]isTorchOpen = "+isTorchOpen);
        if(isTorchOpen){
            Settings.Secure.putInt(context.getContentResolver(),"flashlight.state",0);//手电筒打开
        }else{
            Settings.Secure.putInt(context.getContentResolver(),"flashlight.state",1);//手电筒关闭
        }
    }

    /**
     * 获手电筒的状态
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
     * 打开和关闭GPS
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
     * 判断当前GPS是否打开
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
     * 打开和关闭数据流量
     */
    public static void setDataEnabled(Context context, boolean enable) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        mTelephonyManager.setDataEnabled(enable);
    }

    /**
     * 获取当前数据流量状态
     */
    public static boolean getDataEnabled(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return mTelephonyManager.getDataEnabled();
    }

    /**
     * 当前音量
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
     * 增大音量
     *
     * @param context
     */
    public static void upVoice(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

    /**
     * 减小音量
     *
     * @param context
     */
    public static void downVoice(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

    /**
     * 获取当前屏幕亮度
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
     * 增大屏幕亮度
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
     * 降低屏幕亮度
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
     * 清楚字符串中所有额特殊字符
     * @param normalStrng
     * @return
     */
    public static String formateString(String normalStrng){
        if (normalStrng != null && !"".equals(normalStrng.trim())) {
            String regEx="[\\s~·`!！@#￥$%^……&*（()）\\-——\\-_=+【\\[\\]】｛{}｝\\|、\\\\；;：:‘'“”\"，,《<。.》>、/？?]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(normalStrng);
            return m.replaceAll("");
        }
        return null;
    }

    /**
     * 获取音乐播放列表
     */
    public static long[] getPlayList(List<Song> allSongs){
        Log.d(TAG,"[MusicParseAdapter][getPlayList]size = "+allSongs.size());
        long[] list = new long[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            list[i] = allSongs.get(i).getId();
        }
        return list;
    }

    /**
     * 播放歌曲
     */
    /*public static void playMusicByList(IMediaPlaybackService mMusicService,long[] list, int position){
        CappuLog.d(TAG,"[FunctionUtil][playMusicByList]mMusicService = "+mMusicService+",list.size = "+list+",position = "+position);
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
     * 设置播放模式
     */
    /*public static int getShuffleMode(IMediaPlaybackService mMusicService){
        try {
            return mMusicService.getShuffleMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }*/

    /**
     * 设置循环模式
     */
    /*public static void setRepeatMode(IMediaPlaybackService mMusicService, int mode){
        try {
            mMusicService.setRepeatMode(mode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * 停止播放歌曲
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