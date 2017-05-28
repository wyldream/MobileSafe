package com.example.lbj.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by LBJ on 2017/5/25.
 */
public class StreamUtils {
    public static String streamToString(InputStream is){
        //1,在读取的过程中,将读取的内容存储值缓存中,然后一次性的转换成字符串返回
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int temp = -1;
        System.out.println("什么情况       ");
        try {
            while((temp = is.read(bytes))!=-1){
                bos.write(bytes,0,temp);
            }
            return bos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
