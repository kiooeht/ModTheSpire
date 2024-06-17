package com.evacipated.cardcrawl.modthespire.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class to help with compression.
 */
public class CompressionUtils {
    /**
     * Compresses and encodes a string using Base64.
     * @param toCompress : String object to compress.
     * @return : Compressed string object.
     */
    public static String compress(String toCompress){
        if(toCompress == null) {
            return null;
        }

        try(ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream(); GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baOutputStream)){
            gzipOutputStream.write(toCompress.getBytes());
            gzipOutputStream.close();

            return Base64.getEncoder().encodeToString(baOutputStream.toByteArray());
        }catch (Exception e){
            System.err.println("Failed to compress string due to " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Decodes and decompresses a Base64 encoded string.
     * @param toDecompress : String object to decompress.
     * @return : Decompressed string object.
     */
    public static String decompress(String toDecompress){
        if(toDecompress == null) {
            return null;
        }

        try{
            byte[] data = Base64.getDecoder().decode(toDecompress);

            try(ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); GZIPInputStream gzipInputStream = new GZIPInputStream(baInputStream); ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream()){
                byte[] buffer = new byte[1024];
                int len;

                while ((len = gzipInputStream.read(buffer)) > 0) {
                    baOutputStream.write(buffer, 0, len);
                }

                return baOutputStream.toString();
            }catch (Exception e){
                System.err.println("Failed to decompress string due to " + e.getMessage());
                e.printStackTrace();
            }
        }catch (Exception e){
            System.err.println("Failed to decompress string due to " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
