package com.example.lbj.mobilesafe.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by LBJ on 2017/5/26.
 */
public class ToastUtils {
    /**
     * @param ctx	上下文环境
     * @param msg	打印文本内容
     */
    public static void show(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }
}
