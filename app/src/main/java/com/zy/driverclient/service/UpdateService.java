package com.zy.driverclient.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.zy.driverclient.MainActivity;
import com.zy.driverclient.R;
import com.zy.driverclient.config.Global;


public class UpdateService extends Service {
    // 标题
    private int titleId = 0;

    // 文件存储
    private File updateFile = null;
    // 下载文件的存放路径
    File updateDir;

    // 通知栏
    private NotificationManager updateNotificationManager = null;
    private Notification updateNotification = null;

    // 通知栏跳转Intent
    private Intent updateIntent = null;
    private PendingIntent updatePendingIntent = null;

    // 下载完成标记常量
    private final static int DOWNLOAD_COMPLETE = 0;
    private final static int DOWNLOAD_FAIL = 1;

    private Thread downThread;

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 下载成功
                case DOWNLOAD_COMPLETE:
                    // 点击安装
                    Uri uri = Uri.fromFile(updateFile);
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                    updatePendingIntent = PendingIntent.getActivity(UpdateService.this, 0, installIntent, 0);
                    updateNotification.defaults = Notification.DEFAULT_SOUND;
                    updateNotification.setLatestEventInfo(UpdateService.this, "汴梁约车司机版", "下载完成，点击安装", updatePendingIntent);
                    updateNotificationManager.notify(0, updateNotification);

                    // 停止服务
                    stopSelf();
                    break;
                // 下载失败
                case DOWNLOAD_FAIL:
                    // 下载失败,这里是看要提示重新下载还是另外做其他操作
                    updateNotification.setLatestEventInfo(UpdateService.this,
                            "汴梁约车司机版", "下载失败", updatePendingIntent);
                    updateNotificationManager.notify(0, updateNotification);
                    break;
                default:
                    stopSelf();
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 获取传值
        titleId = intent.getIntExtra("titleId", 0);
        // 创建文件
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
            // 组合下载地址
            updateDir = new File(Environment.getExternalStorageDirectory(), Global.downloadDir);
        } else {
            // files目录
            updateDir = getFilesDir();
        }
        // 拼凑下载文件文件名称
        updateFile = new File(updateDir.getPath(), getResources().getString(titleId) + ".apk");

        this.updateNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        this.updateNotification = new Notification();

        // 设置下载过程中，点击通知栏，回到主界面
        updateIntent = new Intent(this, MainActivity.class);
        updatePendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
        // 设置通知栏显示内容
        updateNotification.icon = R.mipmap.icon;
        updateNotification.tickerText = "开始下载";
        updateNotification.setLatestEventInfo(this, "正在下载", "0%", updatePendingIntent);
        // 发出通知
        updateNotificationManager.notify(0, updateNotification);

        // 开启一个新的线程下载，如果使用Service同步下载，会导致ANR问题，Service本身也会阻塞
        // new Thread(new updateRunnable()).start();// 这个是下载的重点，是下载的过程
        downThread = new Thread(updateRunnable);
        downThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 下载线程类
     *
     * @author Jerry
     *
     */
    private Runnable updateRunnable = new Runnable() {

        Message message = updateHandler.obtainMessage();

        @Override
        public void run() {
            message.what = DOWNLOAD_COMPLETE;
            try {
                // 文件目录是否存在
                if (!updateDir.exists()) {
                    updateDir.mkdirs();
                }
                // 文件是否存在
                if (!updateFile.exists()) {
                    updateFile.createNewFile();
                }
                // 下载函数
                // 增加权限
                long downloadSize = downloadUpdateFile(
                        "http://122.0.114.195:8088/Taxic/passengerAction-Download.action", updateFile);
                if (downloadSize > 0) {
                    // 下载成功,发送消息
                    updateHandler.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 设置失败标识
                message.what = DOWNLOAD_FAIL;
                // 下载失败，发送消息
                updateHandler.sendMessage(message);
            }
        }
    };
    /**
     * 下载文件
     *
     * @param downloadUrl
     *            下载路径
     * @param saveFile
     *            文件名称
     * @return
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    private long downloadUpdateFile(String downloadUrl, File saveFile) throws Exception {
        int downloadCount = 0;
        int currentSize = 0;
        long totalSize = 0;
        int updateTotalSize = 0;

        HttpURLConnection httpConnection = null;
        // 输入流
        InputStream is = null;
        // 文件输出流
        FileOutputStream fos = null;
        try {
            URL url = new URL(downloadUrl);
            httpConnection = (HttpURLConnection) url.openConnection();

            if (currentSize > 0) {
                httpConnection.setRequestProperty("Range", "bytes=" + currentSize + "-");
            }
            httpConnection.setConnectTimeout(10000);
            httpConnection.setReadTimeout(20000);
            httpConnection.connect();
            updateTotalSize = httpConnection.getContentLength();
            if (httpConnection.getResponseCode() == 404) {
                throw new Exception("fail!");
            }
            is = httpConnection.getInputStream();
            fos = new FileOutputStream(saveFile, false);
            byte buffer[] = new byte[4096];
            int readsize = 0;
            while ((readsize = is.read(buffer)) > 0) {
                fos.write(buffer, 0, readsize);
                totalSize += readsize;
                // 为了防止频繁的通知导致应用吃紧，百分比增加10才通知一次
                if ((downloadCount == 0) || (int) (totalSize * 100 / updateTotalSize) - 1 > downloadCount) {
                    downloadCount++;
                    updateNotification.setLatestEventInfo(UpdateService.this, "正在下载",
                            (int) totalSize * 100 / updateTotalSize + "%", updatePendingIntent);
                    updateNotificationManager.notify(0, updateNotification);
                }
            }
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return totalSize;
    }

    @Override
    public void onDestroy() {
        if (null != downThread) {
            downThread.interrupt();
        }
        super.onDestroy();
    }
}
