/*     */ package com.ruoyi.common.utils;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesTool {
public static final String KEY_ALGORITHM = "AES";
public static final String CIPHER_ALGORITHM = "AES";
public static final String ivParameter = "1234567890abcdef";
public static String Encrypt(String key, String text)
{
/*     */     try
/*     */     {
/*  24 */       byte[] keyBytes = key.getBytes();
/*  25 */       SecretKeySpec sKeySpec = new SecretKeySpec(keyBytes, "AES");
/*  26 */       Cipher cipher = Cipher.getInstance("AES");
/*  27 */       cipher.init(1, sKeySpec);
/*  28 */       byte[] encrypted = cipher.doFinal(text.getBytes("utf-8"));
/*  29 */       return byteArr2HexStr(encrypted);
/*     */     } catch (Exception e) {
/*  31 */       System.err.print("AesTool加密异常：" + e.getStackTrace());
/*     */     }
/*  33 */     return null;
}
public static String Decrypt(String Key, String text)
{
/*     */     try
/*     */     {
/*  44 */       byte[] raw = Key.getBytes("ASCII");
/*  45 */       SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
/*  46 */       Cipher cipher = Cipher.getInstance("AES");
/*  47 */       cipher.init(2, skeySpec);
/*  48 */       byte[] encrypted1 = hexStr2ByteArr(text);
/*  49 */       byte[] original = cipher.doFinal(encrypted1);
/*  50 */       return new String(original, "utf-8");
/*     */     }
/*     */     catch (Exception e) {
/*  53 */       System.err.print("AesTool解密异常：" + e.getStackTrace());
/*     */     }
/*  55 */     return null;
}
private static String byteArr2HexStr(byte[] buf)
{
/*  69 */     int iLen = buf.length;
/*  71 */     StringBuffer sb = new StringBuffer(iLen * 2);
/*  72 */     for (int i = 0; i < iLen; i++) {
/*  73 */       int intTmp = buf[i];
/*  75 */       while (intTmp < 0) {
/*  76 */         intTmp += 256;
/*     */       }
/*  79 */       if (intTmp < 16) {
/*  80 */         sb.append("0");
/*     */       }
/*  82 */       sb.append(Integer.toString(intTmp, 16));
/*     */     }
/*  84 */     return sb.toString();
}
private static byte[] hexStr2ByteArr(String strIn)
/*     */     throws Exception
{
/*  98 */     byte[] arrB = strIn.getBytes();
/*  99 */     int iLen = arrB.length;
/* 102 */     byte[] arrOut = new byte[iLen / 2];
/* 103 */     for (int i = 0; i < iLen; i += 2) {
/* 104 */       String strTmp = new String(arrB, i, 2);
/* 105 */       arrOut[(i / 2)] = ((byte)Integer.parseInt(strTmp, 16));
/*     */     }
/* 107 */     return arrOut;
}
public static void main(String[] args)
{
/* 112 */     String key = "1234567890123456";
/* 113 */     String str = "{\"username\":\"liping\",\"password\":\"12344\",\"authtype\":0}";
/* 114 */     String enc = Encrypt(key, str);
/* 115 */     String dec = Decrypt(key, enc);
/* 116 */     System.out.println("(" + str.getBytes().length / 1024 + "KB)en:" + enc);
/* 117 */     System.out.println("(" + dec.getBytes().length / 1024 + "KB)de:" + dec);
}}

/* Location:           C:\Users\hejj59\Desktop\wxservice\WEB-INF\classes\
 * Qualified Name:     com.ruoyi.common.util.AesTool
 * JD-Core Version:    0.6.2
 */