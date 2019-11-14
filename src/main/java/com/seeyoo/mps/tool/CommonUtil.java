package com.seeyoo.mps.tool;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * Created by alexis on 2019/8/27.
 */
public class CommonUtil {
    public static Map<String, Object> defaultResponse(int errCode, String errMsg) {
        Map<String, Object> res = new HashMap<>();
        res.put("errCode", errCode);
        res.put("errMsg", errMsg);
        return res;
    }

    public static String generateNewCode(int len, String code) {
        String defaultCode = "";
        for (int i = 0; i < len; i++) {
            defaultCode += "0";
        }
        if (StrUtil.isEmpty(code)) {
            return defaultCode;
        }
        String oldCode = code.substring(code.length() - len);
        boolean carry = true;
        String newCode = "";
        for (int i = len - 1; i >= 0; i--) {
            char c = oldCode.charAt(i);
            if (carry) {
                carry = false;
                if (c == 'Z') {
                    c = '0';
                    carry = true;
                } else if (c == '9') {
                    c = 'A';
                } else {
                    c += 1;
                }
            }
            newCode = c + newCode;
        }
        if (newCode.equals(defaultCode)) {
            return null;
        }
        return newCode;
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.contains(",")) {
            String[] ips = ip.split(",");
            if (ips != null && ips.length > 0) {
                ip = ips[0];
            }
        }
        return ip;
    }

    public static <T> List<T> castEntity(List<Object[]> list, Class<T> clazz) throws Exception {
        List<T> returnList = new ArrayList<T>();
        if (CollectionUtils.isEmpty(list)) {
            return returnList;
        }
        Object[] co = list.get(0);
        Class[] c2 = new Class[co.length];
        //确定构造方法
        for (int i = 0; i < co.length; i++) {
            if (co[i] != null) {
                c2[i] = co[i].getClass();
            } else {
                c2[i] = String.class;
            }
        }
        for (Object[] o : list) {
            Constructor<T> constructor = clazz.getConstructor(c2);
            returnList.add(constructor.newInstance(o));
        }
        return returnList;
    }

    public static String getMac() throws IOException {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < mac.length; i++) {
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static public String getRandomString(int length) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(36);//[0,62)
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
