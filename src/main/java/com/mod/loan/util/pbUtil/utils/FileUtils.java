package com.mod.loan.util.pbUtil.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    /**
     * 读取本地JSON数据
     *
     * @param path       本地路径
     * @return json      返回数据
     */
    public static String read(String path){

        if(StringUtils.isBlank(path)){
            return null;
        }

        try {
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            return stringBuilder.toString();//得到JSONobject对象
        } catch (IOException e) {
            System.out.println("#######文件内容读取失败#######:"+ path);
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject readJson(String path){
        return JSON.parseObject(read(path));
    }


}
