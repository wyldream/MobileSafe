package com.example.lbj.mobilesafe.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.lbj.mobilesafe.R;
import com.example.lbj.mobilesafe.utils.StreamUtils;
import com.example.lbj.mobilesafe.utils.ToastUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends Activity {
    private final static String tag = "SplashActivity";

    /**
     * 更新新版本的状态码
     */
    protected static final int UPDATE_VERSION = 100;
    /**
     * 进入应用程序主界面状态码
     */
    protected static final int ENTER_HOME = 101;

    /**
     * url地址出错状态码
     */
    protected static final int URL_ERROR = 102;
    protected static final int IO_ERROR = 103;
    protected static final int JSON_ERROR = 104;

    private TextView tv_version_name;
    private int mLocalVersionCode;
    private String mVersionDes;
    private String mDownloadUrl;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VERSION:
                    //enterHome();
                    //弹出对话框,提示用户更新
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    //进入应用程序主界面,activity跳转过程
                    enterHome();
                    break;
                case URL_ERROR:
                    ToastUtils.show(getApplicationContext(), "url异常");
                    enterHome();
                    break;
                case IO_ERROR:
                    ToastUtils.show(getApplicationContext(), "读取异常");
                    enterHome();
                    break;
                case JSON_ERROR:
                    ToastUtils.show(getApplicationContext(), "json解析异常");
                    enterHome();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除掉当前activity头title,在主题中实现
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        //初始化UI
        initUI();
        //初始化数据
        initData();
    }

    /**
     * 初始化UI方法	alt+shift+j
     * 获取版本名称
     */
    private void initUI() {
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
    }

    private void initData() {
        //设置应用版本名称
        tv_version_name.setText(getVersionName());
        //检测是否有更新
        //1获取本地版本号
        mLocalVersionCode = getVersionCode();
        //2获取服务端版本号
        //json中内容包含:
		/* 更新版本的版本名称
		 * 新版本的描述信息
		 * 服务器版本号   和本地版本号比对，看是否更新
		 * 新版本apk下载地址*/
        checkVersion();
    }

    /**
     * 获取版本名称
     * 返回null为异常
     * @return
     */
    private String getVersionName(){
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(),0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取版本号
     * @return 0失败
     * 非0 成功
     */
    private int getVersionCode(){
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(),0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 检测版本号
     */
    private void checkVersion() {
        new Thread(){
            @Override
            public void run() {
                Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();
                try {
                    URL url = new URL("http://192.168.43.245:8080/login.json");
                    //2,开启一个链接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //3,设置常见请求参数(请求头)
                    //请求超时
                    connection.setConnectTimeout(5000);
                    //读取超时
                    connection.setReadTimeout(5000);
                    connection.setRequestMethod("GET");

                    //判断响应码
                    if(connection.getResponseCode() == 200){
                        //以流的形式获取数据
                        InputStream is = connection.getInputStream();
                        //将流转换为字符串
                        //6,将流转换成字符串(工具类封装)
                        String json = StreamUtils.streamToString(is);
                        Log.i(tag, json);
                        //7、解析json
                        JSONObject jsonObject = new JSONObject(json);

                        String versionName = jsonObject.getString("versionName");
                        mVersionDes = jsonObject.getString("versionDes");
                        String versionCode = jsonObject.getString("versionCode");
                        mDownloadUrl = jsonObject.getString("download");
                        Log.i(tag,versionName);
                        Log.i(tag,mVersionDes);
                        Log.i(tag,versionCode);
                        Log.i(tag,mDownloadUrl);
                        //对比版本号判断是否要更新
                        if(mLocalVersionCode<Integer.parseInt(versionCode)){
                            msg.what = UPDATE_VERSION;
                        }else{
                            msg.what = ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    msg.what = URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e){
                    msg.what = IO_ERROR;
                    e.printStackTrace();
                } catch (JSONException e){
                    msg.what = JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    //指定睡眠时间,请求网络的时长超过4秒则不做处理
                    //获取中止时间
                    long endTime = System.currentTimeMillis();
                    if(endTime - startTime<4000){
                        try {
                            Thread.sleep(4000 - (endTime - startTime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * 弹出对话框，提示用户更新
     */
    private void showUpdateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置左上角图标
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("版本更新");
        //设置描述内容
        builder.setMessage(mVersionDes);

        //设置按钮
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //去下载
                downloadApk();
                //enterHome();
            }
        });

        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //进入主界面
                enterHome();
            }
        });

        //点击取消事件监听
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //进入应用，对话框消失
                enterHome();
                dialogInterface.dismiss();
            }
        });

        //显示对话框
        builder.show();
    }

    /**
     * 使用xutils组件 下载应用
     */
    public void downloadApk(){
        //1.判断sd卡是否可用
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //2.获取sd卡路径
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"mobilesafe.apk";
            //3.发送请求，获取apk并放到指定路径
            HttpUtils httpUtils = new HttpUtils();
            //4.下载(下载地址，下载后存放路径，回调函数)
            httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
                //下载成功
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    Log.i(tag,"下载成功");
                    //下载下来的文件
                    File file = responseInfo.result;
                    //安装
                    installApk(file);
                }
                //下载失败
                @Override
                public void onFailure(HttpException e, String s) {
                    Log.i(tag,"下载失败");
                }
                //正在下载
                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    Log.i(tag, "下载中........");
                    Log.i(tag, "total = "+total);
                    Log.i(tag, "current = "+current);
                    super.onLoading(total, current, isUploading);
                }
            });
        }
    }

    /**
     * 安装apk
     */
    protected void installApk(File file){
        //系统安装apk的界面
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        //文件作为数据源
        intent.setData(Uri.fromFile(file));
        //设置安装的类型
        intent.setType("application/vnd.android.package-archive");
        //开启一个意图，第二个参数是返回码（Activity类中）
        startActivityForResult(intent,0);
    }
    /**
     * 开启一个Activity后，返回结果调用的方法,即startActivityForResult执行完后执行这个方法
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enterHome();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 进入程序主界面
     */
    private void enterHome(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //在开启一个界面后，将导航界面关闭
        finish();
    }

}
