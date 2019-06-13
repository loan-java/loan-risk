
package com.mod.loan.util.pbUtil.utils.gzip;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtil {

    public GZipUtil() {
    }

    public static String compressData(String src) {
        if (StringUtils.isBlank(src)) {
            throw new RuntimeException("GZipUtil.compressData error,params is blank");
        } else {
            try {
                return new String(compressToByte(src), StandardCharsets.UTF_8);
            } catch (Exception var2) {
                throw new RuntimeException("GZipUtil.compressData error", var2);
            }
        }
    }

    public static byte[] compressToByte(String src) {
        if (StringUtils.isBlank(src)) {
            throw new RuntimeException("GZipUtil.compressToByte error,params is blank");
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                GZIPOutputStream gzip = new GZIPOutputStream(out);
                gzip.write(src.getBytes(StandardCharsets.UTF_8));
                gzip.close();
            } catch (IOException var4) {
                throw new RuntimeException("GZipUtil.compressToByte error", var4);
            }

            return out.toByteArray();
        }
    }

    public static String uncompressToString(byte[] bytes) {
        if (null != bytes && bytes.length != 0) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            return uncompressToString((InputStream)in);
        } else {
            throw new RuntimeException("GZipUtil.uncompressToString error,params is blank");
        }
    }

    public static String uncompressToString(InputStream inputStream) {
        if (inputStream == null) {
            throw new RuntimeException("GZipUtil.uncompressToString error,params is blank");
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                GZIPInputStream gunzip = new GZIPInputStream(inputStream);
                byte[] buffer = new byte[256];

                int n;
                while((n = gunzip.read(buffer)) >= 0) {
                    out.write(buffer, 0, n);
                }

                return out.toString("UTF-8");
            } catch (IOException var5) {
                throw new RuntimeException("GZipUtil.uncompressToString error", var5);
            }
        }
    }
}
