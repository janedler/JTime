package jtime.jtime.ntp;

/**
 * Created by janedler on 2017/6/6.
 */

public class JNtpUtil {

    public static JNtpUtil instance = new JNtpUtil();

    public long getcurrentTimeMillis() {
        long currentTimeMillis = System.currentTimeMillis();
        SntpClient client = new SntpClient();
        if (client.requestTime("cn.pool.ntp.org", 30000)) {
            currentTimeMillis = client.getNtpTime() + System.nanoTime() / 1000 - client.getNtpTimeReference();
        }
        return currentTimeMillis;
    }


}
