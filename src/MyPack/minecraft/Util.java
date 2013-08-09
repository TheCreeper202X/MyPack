/*     */ package net.minecraft;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.URI;
/*     */ import java.net.URL;
/*     */ import java.net.URLEncoder;
/*     */ import java.security.PublicKey;
/*     */ import java.security.cert.Certificate;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import javax.net.ssl.HttpsURLConnection;

import mypack.MyPack;
/*     */ 
/*     */ public class Util
/*     */ {
/*     */   public static OS getPlatform()
/*     */   {
/*  23 */     String str = System.getProperty("os.name").toLowerCase();
/*  24 */     if (str.contains("win")) return OS.WINDOWS;
/*  25 */     if (str.contains("mac")) return OS.MACOS;
/*  26 */     if (str.contains("solaris")) return OS.SOLARIS;
/*  27 */     if (str.contains("sunos")) return OS.SOLARIS;
/*  28 */     if (str.contains("linux")) return OS.LINUX;
/*  29 */     if (str.contains("unix")) return OS.LINUX;
/*  30 */     return OS.UNKNOWN;
/*     */   }
/*     */ 
/*     */   public static File getWorkingDirectory()
/*     */   {
/*  37 */     return MyPack.getWorkingDirectory();
/*     */   }
/*     */ 
/*     */   public static File getWorkingDirectory(String paramString) {
/*  41 */     String str1 = System.getProperty("user.home", ".");
/*     */     File localFile;
/*  44 */     switch (getPlatform().ordinal()) {
/*     */     case 1:
/*     */     case 2:
/*  47 */       localFile = new File(str1, '.' + paramString + '/');
/*  48 */       break;
/*     */     case 3:
/*  50 */       String str2 = System.getenv("APPDATA");
/*  51 */       String str3 = str2 != null ? str2 : str1;
/*     */ 
/*  53 */       localFile = new File(str3, '.' + paramString + '/');
/*  54 */       break;
/*     */     case 4:
/*  56 */       localFile = new File(str1, "Library/Application Support/" + paramString);
/*  57 */       break;
/*     */     default:
/*  59 */       localFile = new File(str1, paramString + '/');
/*     */     }
/*  61 */     if ((!localFile.exists()) && (!localFile.mkdirs()))
/*  62 */       throw new RuntimeException("The working directory could not be created: " + localFile);
/*  63 */     return localFile;
/*     */   }
/*     */ 
/*     */   public static String buildQuery(Map<String, Object> paramMap) {
/*  67 */     StringBuilder localStringBuilder = new StringBuilder();
/*     */ 
/*  69 */     for (Map.Entry localEntry : paramMap.entrySet()) {
/*  70 */       if (localStringBuilder.length() > 0) {
/*  71 */         localStringBuilder.append('&');
/*     */       }
/*     */       try
/*     */       {
/*  75 */         localStringBuilder.append(URLEncoder.encode((String)localEntry.getKey(), "UTF-8"));
/*     */       } catch (UnsupportedEncodingException localUnsupportedEncodingException1) {
/*  77 */         localUnsupportedEncodingException1.printStackTrace();
/*     */       }
/*     */ 
/*  80 */       if (localEntry.getValue() != null) {
/*  81 */         localStringBuilder.append('=');
/*     */         try {
/*  83 */           localStringBuilder.append(URLEncoder.encode(localEntry.getValue().toString(), "UTF-8"));
/*     */         } catch (UnsupportedEncodingException localUnsupportedEncodingException2) {
/*  85 */           localUnsupportedEncodingException2.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  90 */     return localStringBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static String executePost(String paramString, Map<String, Object> paramMap) {
/*  94 */     return executePost(paramString, buildQuery(paramMap));
/*     */   }
/*     */ 
/*     */   public static String executePost(String paramString1, String paramString2)
/*     */   {
/*  99 */     HttpsURLConnection localHttpsURLConnection = null;
/*     */     try
/*     */     {
/* 102 */       URL localURL = new URL(paramString1);
/* 103 */       localHttpsURLConnection = (HttpsURLConnection)localURL.openConnection();
/* 104 */       localHttpsURLConnection.setRequestMethod("POST");
/* 105 */       localHttpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
/*     */ 
/* 107 */       localHttpsURLConnection.setRequestProperty("Content-Length", "" + Integer.toString(paramString2.getBytes().length));
/* 108 */       localHttpsURLConnection.setRequestProperty("Content-Language", "en-US");
/*     */ 
/* 110 */       localHttpsURLConnection.setUseCaches(false);
/* 111 */       localHttpsURLConnection.setDoInput(true);
/* 112 */       localHttpsURLConnection.setDoOutput(true);
/*     */ 
/* 114 */       localHttpsURLConnection.connect();
/* 115 */       Certificate[] arrayOfCertificate = localHttpsURLConnection.getServerCertificates();
/*     */ 
/* 117 */       byte[] arrayOfByte1 = new byte[294];
/* 118 */       DataInputStream localDataInputStream = new DataInputStream(Util.class.getResourceAsStream("minecraft.key"));
/* 119 */       localDataInputStream.readFully(arrayOfByte1);
/* 120 */       localDataInputStream.close();
/*     */ 
/* 122 */       Certificate localCertificate = arrayOfCertificate[0];
/* 123 */       PublicKey localPublicKey = localCertificate.getPublicKey();
/* 124 */       byte[] arrayOfByte2 = localPublicKey.getEncoded();
/*     */ 
/* 126 */       for (int i = 0; i < arrayOfByte2.length; i++) {
/* 127 */         if (arrayOfByte2[i] != arrayOfByte1[i]) throw new RuntimeException("Public key mismatch");
/*     */ 
/*     */       }
/*     */ 
/* 131 */       DataOutputStream localDataOutputStream = new DataOutputStream(localHttpsURLConnection.getOutputStream());
/* 132 */       localDataOutputStream.writeBytes(paramString2);
/* 133 */       localDataOutputStream.flush();
/* 134 */       localDataOutputStream.close();
/*     */ 
/* 137 */       InputStream localInputStream = localHttpsURLConnection.getInputStream();
/* 138 */       BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
/*     */ 
/* 140 */       StringBuffer localStringBuffer = new StringBuffer();
/*     */       String str1;
/* 141 */       while ((str1 = localBufferedReader.readLine()) != null) {
/* 142 */         localStringBuffer.append(str1);
/* 143 */         localStringBuffer.append('\r');
/*     */       }
/* 145 */       localBufferedReader.close();
/*     */ 
/* 147 */       return localStringBuffer.toString();
/*     */     }
/*     */     catch (Exception localException)
/*     */     {
/*     */       byte[] arrayOfByte1;
/* 151 */       localException.printStackTrace();
/* 152 */       return null;
/*     */     }
/*     */     finally
/*     */     {
/* 156 */       if (localHttpsURLConnection != null)
/* 157 */         localHttpsURLConnection.disconnect();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isEmpty(String paramString)
/*     */   {
/* 163 */     return (paramString == null) || (paramString.length() == 0);
/*     */   }
/*     */ 
/*     */   public static void openLink(URI paramURI) {
/*     */     try {
/* 168 */       Object localObject = Class.forName("java.awt.Desktop").getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
/* 169 */       localObject.getClass().getMethod("browse", new Class[] { URI.class }).invoke(localObject, new Object[] { paramURI });
/*     */     } catch (Throwable localThrowable) {
/* 171 */       System.out.println("Failed to open link " + paramURI.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static enum OS
/*     */   {
/*  15 */     LINUX, 
/*  16 */     SOLARIS, 
/*  17 */     WINDOWS, 
/*  18 */     MACOS, 
/*  19 */     UNKNOWN;
/*     */   }
/*     */ }

/* Location:           C:\Users\Roman\Documents\jad\src\CommonLauncher.jar
 * Qualified Name:     net.minecraft.Util
 * JD-Core Version:    0.6.2
 */