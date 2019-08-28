package com.droi.aiui.apkupdate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

/**
 * @author : TJ
 * @date : 2017/8/31 15:52
 * @description :MD5����
 */

public class MD5Utils {

    public MD5Utils() {
    }

    public static InputStream ungzipInputStream(InputStream input, boolean gzip) throws IOException {
        Object is = null;
        if (input != null) {
            if (gzip) {
                GZIPInputStream     is1 = new GZIPInputStream(input);
                BufferedInputStream bis = new BufferedInputStream(is1);
                bis.mark(2);
                byte[] header = new byte[2];
                int    result = bis.read(header);
                bis.reset();
                int ss = header[0] & 255 | (header[1] & 255) << 8;
                /*if (result != -1 && ss == '�z') {
                    is = new GZIPInputStream(bis);
                } else {
                    is = bis;
                }*/
            } else {
                is = new BufferedInputStream(input);
            }
        }

        return (InputStream) is;
    }

    public static String md5Encode(String inStr) {
        MessageDigest md5 = null;
        new StringBuffer();

        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] e        = inStr.getBytes("UTF-8");
            byte[] md5Bytes = md5.digest(e);
            String content  = binToHex(md5Bytes);
            return content;
        } catch (Exception var6) {
            System.out.println(var6.toString());
            var6.printStackTrace();
            return "";
        }
    }

    public static String binToHex(byte[] md) {
        StringBuffer sb   = new StringBuffer("");
        boolean      read = false;

        for (int i = 0; i < md.length; ++i) {
            int var4 = md[i];
            if (var4 < 0) {
                var4 += 256;
            }

            if (var4 < 16) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(var4));
        }

        return sb.toString();
    }

    public static String md5File(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md.digest());
            fis.close();
            return  bigInt.toString(16);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                fis.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
}
