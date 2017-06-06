package jtime.jtime.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;

/**
 * 通过网络请求获取网络时间
 * Created by janedler on 2017/6/6.
 */

public class JTimeUtil {

    private final static String CACHE_KEY = "time_cache";
    private final static String NETTIME_KEY = "net_time";
    private final static String LOCALTIME_KEY = "local_time";

    private final static long ONE_HOUR = 60*60*1000;


    private static String[] URLS = new String[7];

    static {
        URLS[0] = "http://www.baidu.com";
        URLS[1] = "https://www.so.com";
        URLS[2] = "http://www.taobao.com";
        URLS[3] = "http://www.ntsc.ac.cn";
        URLS[4] = "http://www.360.cn";
        URLS[5] = "http://www.beijing-time.org";
        URLS[6] = "http://www.jd.com";
    }

    private static CountDownLatch mCountDownLatch = new CountDownLatch(1);

    public static JTimeUtil instance = new JTimeUtil();

    /**
     * 在APP启动的时候 请调用此方法进行初始化
     * @param context
     */
    public void initTime(Context context){
        startTimeService(context);
    }

    private void startTimeService(Context context){
        new Thread(new URLTimeThread(context,null)).start();
    }

    /**
     * 建议使用此方法 直接返回本地缓存通过计算后的时间
     * 使用此方法前 请在启动页先调用initTime方法 不然时间会不准确
     * @param context
     * @return
     */
    public long getCurrentTimeMillis(Context context) {
        long netTime = getCache(context,NETTIME_KEY);
        if (netTime <= 0) netTime = System.currentTimeMillis();
        return netTime + (System.currentTimeMillis() - getCache(context,LOCALTIME_KEY));
    }


    /**
     * 阻塞式 时间过长可能会爆出ANR
     * @param context
     * @return
     */
    public long getSyncCurrentTimeMillis(Context context) {
        return getCurrentTimeMillis(context,null);

    }

    /**
     * 异步式 异步返回当前网络时间戳
     * @param context
     * @param timeCallBack
     * @return
     */
    public void getAsynCurrentTimeMillis(Context context,TimeCallBack timeCallBack) {
        if (timeCallBack == null) return;
        getCurrentTimeMillis(context,timeCallBack);
    }


    private long getCurrentTimeMillis(Context context,TimeCallBack timeCallBack) {
        if(getCache(context,NETTIME_KEY) <=0 || System.currentTimeMillis() - getCache(context,LOCALTIME_KEY) > ONE_HOUR){
            try {
                URLTimeThread thread = new URLTimeThread(context,timeCallBack);
                new Thread(thread).start();
                if(timeCallBack == null){
                    mCountDownLatch.await();
                    return thread.getURLTime();
                }
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return System.currentTimeMillis();
            }
        }
        if (timeCallBack == null) return getCurrentTimeMillis(context);
        timeCallBack.onTimeCallBack(getCurrentTimeMillis(context));
        return 0;
    }

    private class URLTimeThread implements Runnable{

        private Context mContext;
        private TimeCallBack mTimeCallBack;

        public URLTimeThread(Context context,TimeCallBack timeCallBack){
            this.mContext = context;
            this.mTimeCallBack = timeCallBack;
        }

        private long mURLTime;

        public long getURLTime(){
            return mURLTime;
        }

        @Override
        public void run() {
            mURLTime = getURLTime(0);
            if(mTimeCallBack == null){
                mCountDownLatch.countDown();
            }else{
                mTimeCallBack.onTimeCallBack(mURLTime);
            }
        }

        private long getURLTime(int index) {
            if (index > (URLS.length - 1)) {
                saveCache(mContext,LOCALTIME_KEY,System.currentTimeMillis());
                saveCache(mContext,NETTIME_KEY,0);
                return System.currentTimeMillis();
            }
            String urlPath = URLS[index];
            try {
                URL url = new URL(urlPath);
                long startTime = System.currentTimeMillis();
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(3000);
                urlConnection.connect();
                long serverTime = urlConnection.getDate();
                long endTime = System.currentTimeMillis();
                serverTime += (endTime - startTime) / 2;
                if (Math.abs(serverTime - System.currentTimeMillis()) <= 3000) {
                    serverTime = System.currentTimeMillis();
                }
                saveCache(mContext,LOCALTIME_KEY,System.currentTimeMillis());
                saveCache(mContext,NETTIME_KEY,serverTime);
                return serverTime;
            } catch (Exception e) {
                return getURLTime(++index);
            }
        }
    }

    private void saveCache(Context context,String key, long value){
        SharedPreferences sp = context.getSharedPreferences(CACHE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private long getCache(Context context,String key){
        SharedPreferences sp = context.getSharedPreferences(CACHE_KEY, Context.MODE_PRIVATE);
        return sp.getLong(key,0);
    }

    public interface TimeCallBack{
        void onTimeCallBack(long time);
    }

}
