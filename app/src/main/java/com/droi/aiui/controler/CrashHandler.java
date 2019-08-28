package com.droi.aiui.controler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    // log�ļ��ĺ�׺��
    public static final String FILE_NAME_SUFFIX = "FATAL.log";
    private Context mContext;
    private static CrashHandler sInstance = null;

    private CrashHandler(Context context) {
        Thread.getDefaultUncaughtExceptionHandler();
        // ����ǰʵ����ΪϵͳĬ�ϵ��쳣������
        Thread.setDefaultUncaughtExceptionHandler(this);
        // ��ȡContext�������ڲ�ʹ��
        this.mContext = context.getApplicationContext();
    }

    public static synchronized CrashHandler create(Context context) {
        if (sInstance == null) {
            sInstance = new CrashHandler(context);
        }
        return sInstance;
    }

    /**
     * �������ؼ��ĺ���������������δ��������쳣��ϵͳ�����Զ�����#uncaughtException����
     * threadΪ����δ�����쳣���̣߳�exΪδ������쳣���������ex�����ǾͿ��Եõ��쳣��Ϣ��
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // �����쳣��Ϣ��SD����
        try {
            saveToSDCard(ex);
        } catch (Exception localException) {

        } finally {
            // ex.printStackTrace();// ����ʱ��ӡ��־��Ϣ
            System.exit(0);
        }
    }


    private void saveToSDCard(Throwable ex) throws Exception {
        File    file   = getSaveFile(mContext.getPackageName(), FILE_NAME_SUFFIX);
        boolean append = false;
        if (System.currentTimeMillis() - file.lastModified() > 5000) {
            append = true;
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
                file, append)));
        // ���������쳣��ʱ��
        pw.println(getDataTime("yyyy-MM-dd HH:mm:ss"));
        // �����ֻ���Ϣ
        dumpPhoneInfo(pw);
        pw.println();
        // �����쳣�ĵ���ջ��Ϣ
        ex.printStackTrace(pw);
        pw.println();
        pw.close();
    }

    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        // Ӧ�õİ汾���ƺͰ汾��
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);
        pw.println();

        // android�汾��
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        pw.println();

        // �ֻ�������
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);
        pw.println();

        // �ֻ��ͺ�
        pw.print("Model: ");
        pw.println(Build.MODEL);
        pw.println();

        // cpu�ܹ�
        pw.print("CPU ABI: ");
        pw.println(Build.CPU_ABI);
        pw.println();
    }


    /**
     * ָ����ʽ���ص�ǰϵͳʱ��
     */
    public static String getDataTime(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }

    /**
     * ��ָ���ļ��л�ȡ�ļ�
     *
     * @return ����ļ��������򴴽�, ����޷������ļ����ļ���Ϊ���򷵻�null
     */
    public static File getSaveFile(String folderPath, String fileNmae) {
        File file = new File(getSavePath(folderPath) + File.separator + fileNmae);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * ��ȡSD����ָ���ļ��еľ���·��
     *
     * @return ����SD���µ�ָ���ļ��еľ���·��
     */
    public static String getSavePath(String folderName) {
        return getSaveFolder(folderName).getAbsolutePath();
    }

    /**
     * ��ȡ�ļ��ж���
     *
     * @return ����SD���µ�ָ���ļ��ж������ļ��в������򴴽�
     */
    public static File getSaveFolder(String folderName) {
        File file = new File(getRootPath() + File.separator + folderName
                + File.separator);
        file.mkdirs();
        return file;
    }

    /**
     * �õ�SD����Ŀ¼.
     */
    public static File getRootPath() {
        File path;
        if (sdCardIsAvailable()) {
            path = Environment.getExternalStorageDirectory(); // ȡ��sdcard�ļ�·��
        } else {
            path = Environment.getDataDirectory();
        }
        return path;
    }

    /**
     * SD���Ƿ����.
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else
            return false;
    }
}