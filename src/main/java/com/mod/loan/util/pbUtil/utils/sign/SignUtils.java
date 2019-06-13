package com.mod.loan.util.pbUtil.utils.sign;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignUtils {

    private static final String objectClass = "java.lang.Object";

    public static List<String> toArray(Object target) {
        List<String> res = new ArrayList<String>();
        Class thisClass = target.getClass();
        Field[] fields = new Field[0];
        while (!objectClass.equals(thisClass.getName())) {
            Field[] fieldTemp = thisClass.getDeclaredFields();
            thisClass = thisClass.getSuperclass();
            fields = ArrayUtils.addAll(fields, fieldTemp);
        }
        for (Field field : fields) {
            Object obj;
            try {
                field.setAccessible(true);
                obj = field.get(target);
                if (field.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    if (pt.getRawType().equals(List.class)) {
                        for (Object items : (List) obj) {
                            res.addAll(toArray(items));
                        }
                    }
                } else if (obj != null && StringUtils.isNotEmpty(obj.toString())) {
                    if ("sign".equals(field.getName())
                            || "riskData".equals(field.getName())
                            || "serialVersionUID".equals(field.getName()))
                        //不参与签名
                        continue;
                    res.add(field.getName() + obj.toString());
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static String getSign(Object object, String privateKey) {
        try {
            return RsaUtils.sign(signString(object), privateKey);
        } catch (Exception e) {
            System.out.println("签名错误");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifySign(Object object, String sign, String publicKey) {
        try {
            return RsaUtils.verify(signString(object), publicKey, sign);
        } catch (Exception e) {
            System.out.println("验签失败");
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] signString(Object object){
        List<String> lst = toArray(object);
        String[] arr = lst.toArray(new String[0]);
        Arrays.sort(arr);
        StringBuilder query = new StringBuilder();
        for (String item : arr) {
            query.append(item);
        }
        System.out.println("加签前拼接的字符串："+query.toString());
        return query.toString().getBytes(StandardCharsets.UTF_8);
    }
}
