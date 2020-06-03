package com.mainback.wxfacepaydemo;

import android.text.TextUtils;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mainback.wxfacepaydemo.MD5.MD5Encode;

/**
 * @author Loki_Zhou
 * @Date 2020/5/29
 **/
public class FacePayUtil {
    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 时间戳转(10位时间戳)
     */
    public static String getUnixStamp() {
        String dateTime;
        Long time = System.currentTimeMillis() / 1000;
        return time + "";
    }

    //获取机器序列号 sn
    public static String getDeviceSN() {
        String serialNumber = android.os.Build.SERIAL;
        return serialNumber;
    }

    //对所有请求参数加密签名
    //注：serectKey为商户平台设置的密钥key
    public static String sign(LinkedHashMap<String, String> data, String serectKey) {
        //先更具ASCLL码值排序 从小到大
        data = orderMapByASCLL(data);
        String signParms = "";
        for (String key : data.keySet()) {
            if (TextUtils.isEmpty(data.get(key))) continue;//去除空值
            signParms += key + "=" + data.get(key) + "&";
        }
        signParms += "key=" + serectKey;
        return MD5Encode(signParms).toUpperCase();
    }

    //更具ASCLL 进行从小到大排序
    public static LinkedHashMap<String, String> orderMapByASCLL(HashMap<String, String> data) {
        //将map.entrySet()转换成list
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(data.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            //降序排序
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> item : list) {
            newMap.put(item.getKey(), item.getValue());
        }
        return newMap;
    }

    public static String MapToXML(HashMap<String, String> data) {
        String root = "<xml>";
        for (String key : data.keySet()) {
            if (data.get(key).isEmpty()) continue;
            root += String.format("<%s>%s</%s>", key, data.get(key), key);
        }
        root += "</xml>";
        String result = root;
        try {
            result = new String(root.getBytes("UTF-8"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    //获取xml字段中的值
    public static String getXMLValue(String value, String regItem) {
        String regStr = String.format("<%s>.*</%s>", regItem, regItem);
        String replaceStr = String.format("</?%s>", regItem);
        String result = "";
        try {
            if (!TextUtils.isEmpty(value)) {
                Matcher matcher;
                Pattern pattern = Pattern.compile(regStr);
                matcher = pattern.matcher(value);
                if (matcher.find()) {
                    MatchResult mr = matcher.toMatchResult();
                    String regex = mr.group();
                    pattern = Pattern.compile(replaceStr);
                    matcher = pattern.matcher(regex);
                    result = matcher.replaceAll("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
