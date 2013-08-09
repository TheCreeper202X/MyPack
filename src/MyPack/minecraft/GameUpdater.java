/*     */ package net.minecraft;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FilePermission;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.StringWriter;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.math.BigInteger;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.JarURLConnection;
/*     */ import java.net.SocketPermission;
/*     */ import java.net.URI;
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.net.URLConnection;
/*     */ import java.security.AccessControlException;
/*     */ import java.security.AccessController;
/*     */ import java.security.CodeSource;
/*     */ import java.security.MessageDigest;
/*     */ import java.security.PermissionCollection;
/*     */ import java.security.PrivilegedExceptionAction;
/*     */ import java.security.ProtectionDomain;
/*     */ import java.security.SecureClassLoader;
/*     */ import java.security.cert.Certificate;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ import java.util.jar.JarEntry;
/*     */ import java.util.jar.JarFile;
/*     */ import java.util.jar.JarOutputStream;
/*     */ import java.util.jar.Pack200;
/*     */ import java.util.jar.Pack200.Unpacker;

import mypack.MyPack;
/*     */ 
/*     */ public class GameUpdater
/*     */   implements Runnable
/*     */ {
/*     */   public static final int STATE_INIT = 1;
/*     */   public static final int STATE_DETERMINING_PACKAGES = 2;
/*     */   public static final int STATE_CHECKING_CACHE = 3;
/*     */   public static final int STATE_DOWNLOADING = 4;
/*     */   public static final int STATE_EXTRACTING_PACKAGES = 5;
/*     */   public static final int STATE_UPDATING_CLASSPATH = 6;
/*     */   public static final int STATE_SWITCHING_APPLET = 7;
/*     */   public static final int STATE_INITIALIZE_REAL_APPLET = 8;
/*     */   public static final int STATE_START_REAL_APPLET = 9;
/*     */   public static final int STATE_DONE = 10;
/*     */   public int percentage;
/*     */   public int currentSizeDownload;
/*     */   public int totalSizeDownload;
/*     */   public int currentSizeExtract;
/*     */   public int totalSizeExtract;
/*     */   protected URL[] urlList;
/*     */   private static ClassLoader classLoader;
/*     */   protected Thread loaderThread;
/*     */   public boolean fatalError;
/*     */   public String fatalErrorDescription;
/*  76 */   protected String subtaskMessage = "";
/*  77 */   protected int state = 1;
/*     */ 
/*  79 */   protected boolean lzmaSupported = false;
/*  80 */   protected boolean pack200Supported = false;
/*     */   protected boolean certificateRefused;
/*  84 */   protected static boolean natives_loaded = false;
/*  85 */   public static boolean forceUpdate = false;
/*     */   private String latestVersion;
/*     */   private String mainGameUrl;
/*     */   public boolean pauseAskUpdate;
/*     */   public boolean shouldUpdate;
/*     */   public boolean skipUpdate;
/*     */ 
/*     */   public GameUpdater(String paramString1, String paramString2, boolean paramBoolean)
/*     */   {
/*  94 */     this.latestVersion = paramString1;
/*  95 */     this.mainGameUrl = paramString2;
/*  96 */     this.skipUpdate = paramBoolean;
/*     */   }
/*     */ 
/*     */   public void init() {
/* 100 */     this.state = 1;
/*     */     try
/*     */     {
/* 103 */       Class.forName("LZMA.LzmaInputStream");
/* 104 */       this.lzmaSupported = true;
/*     */     }
/*     */     catch (ClassNotFoundException localClassNotFoundException) {
/*     */     }
/*     */     try {
/* 109 */       Pack200.class.getSimpleName();
/* 110 */       this.pack200Supported = true;
/*     */     } catch (Throwable localThrowable) {
/*     */     }
/*     */   }
/*     */ 
/*     */   private String generateStacktrace(Exception paramException) {
/* 116 */     StringWriter localStringWriter = new StringWriter();
/* 117 */     PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
/* 118 */     paramException.printStackTrace(localPrintWriter);
/* 119 */     return localStringWriter.toString();
/*     */   }
/*     */ 
/*     */   protected String getDescriptionForState()
/*     */   {
/* 126 */     switch (this.state) {
/*     */     case 1:
/* 128 */       return "Initializing loader";
/*     */     case 2:
/* 130 */       return "Determining packages to load";
/*     */     case 3:
/* 132 */       return "Checking cache for existing files";
/*     */     case 4:
/* 134 */       return "Downloading packages";
/*     */     case 5:
/* 136 */       return "Extracting downloaded packages";
/*     */     case 6:
/* 138 */       return "Updating classpath";
/*     */     case 7:
/* 140 */       return "Switching applet";
/*     */     case 8:
/* 142 */       return "Initializing real applet";
/*     */     case 9:
/* 144 */       return "Starting real applet";
/*     */     case 10:
/* 146 */       return "Done loading";
/*     */     }
/* 148 */     return "unknown state";
/*     */   }
/*     */ 
/*     */   protected String trimExtensionByCapabilities(String paramString)
/*     */   {
/* 153 */     if (!this.pack200Supported) {
/* 154 */       paramString = paramString.replaceAll(".pack", "");
/*     */     }
/*     */ 
/* 157 */     if (!this.lzmaSupported) {
/* 158 */       paramString = paramString.replaceAll(".lzma", "");
/*     */     }
/* 160 */     return paramString;
/*     */   }
/*     */ 
/*     */   protected void loadJarURLs() throws Exception {
/* 164 */     this.state = 2;
/* 165 */     String str1 = "lwjgl.jar, jinput.jar, lwjgl_util.jar, " + this.mainGameUrl;
/* 166 */     str1 = trimExtensionByCapabilities(str1);
/*     */ 
/* 168 */     StringTokenizer localStringTokenizer = new StringTokenizer(str1, ", ");
/* 169 */     int i = localStringTokenizer.countTokens() + 1;
/*     */ 
/* 171 */     this.urlList = new URL[i];
/* 172 */     URL localURL = new URL("http://s3.amazonaws.com/MinecraftDownload/");
/*     */ 
/* 174 */     for (int j = 0; j < i - 1; j++) {
/* 175 */       this.urlList[j] = new URL(localURL, localStringTokenizer.nextToken());
/*     */     }
/*     */ 
/* 178 */     String str2 = System.getProperty("os.name");
/* 179 */     String str3 = null;
/*     */ 
/* 181 */     if (str2.startsWith("Win"))
/* 182 */       str3 = "windows_natives.jar.lzma";
/* 183 */     else if (str2.startsWith("Linux"))
/* 184 */       str3 = "linux_natives.jar.lzma";
/* 185 */     else if (str2.startsWith("Mac"))
/* 186 */       str3 = "macosx_natives.jar.lzma";
/* 187 */     else if ((str2.startsWith("Solaris")) || (str2.startsWith("SunOS")))
/* 188 */       str3 = "solaris_natives.jar.lzma";
/*     */     else {
/* 190 */       fatalErrorOccured("OS (" + str2 + ") not supported", null);
/*     */     }
/*     */ 
/* 193 */     if (str3 == null) {
/* 194 */       fatalErrorOccured("no lwjgl natives files found", null);
/*     */     } else {
/* 196 */       str3 = trimExtensionByCapabilities(str3);
/* 197 */       this.urlList[(i - 1)] = new URL(localURL, str3);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 203 */     init();
/* 204 */     this.state = 3;
/*     */ 
/* 206 */     this.percentage = 5;
/*     */     try
/*     */     {
/* 209 */       loadJarURLs();
/*     */ 
/* 212 */       String str = (String)AccessController.doPrivileged(new PrivilegedExceptionAction() {
/*     */         public Object run() throws Exception {
/* 214 */           return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
/*     */         }
/*     */       });
/* 218 */       File localFile1 = new File(str);
/*     */ 
/* 220 */       if (!localFile1.exists()) {
/* 221 */         localFile1.mkdirs();
/*     */       }
/*     */ 
/* 224 */       if (this.latestVersion != null) {
/* 225 */         File localFile2 = new File(localFile1, "version");
/*     */ 
/* 227 */         int i = 0;
/* 228 */         if ((!this.skipUpdate) && (!forceUpdate) && (localFile2.exists()) && (
/* 229 */           (this.latestVersion.equals("-1")) || (this.latestVersion.equals(readVersionFile(localFile2))))) {
/* 230 */           i = 1;
/* 231 */           this.percentage = 90;
/*     */         }
/*     */ 
/* 235 */         if ((!this.skipUpdate) && ((forceUpdate) || (i == 0))) {
/* 236 */           this.shouldUpdate = true;
/* 237 */           if ((!forceUpdate) && (localFile2.exists()))
/*     */           {
/* 240 */             checkShouldUpdate();
/*     */           }
/* 242 */           if (this.shouldUpdate)
/*     */           {
/* 245 */             writeVersionFile(localFile2, "");
/*     */ 
/* 247 */             downloadJars(str);
/* 248 */             extractJars(str);
/* 249 */             extractNatives(str);
/*     */ 
/* 251 */             if (this.latestVersion != null) {
/* 252 */               this.percentage = 90;
/* 253 */               writeVersionFile(localFile2, this.latestVersion);
/*     */             }
/*     */           } else {
/* 256 */             i = 1;
/* 257 */             this.percentage = 90;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 262 */       updateClassPath(localFile1);
MyPack.onClassPathLoaded(classLoader);
/* 263 */       this.state = 10;
/*     */     } catch (AccessControlException localAccessControlException) {
/* 265 */       fatalErrorOccured(localAccessControlException.getMessage(), localAccessControlException);
/* 266 */       this.certificateRefused = true;
/*     */     } catch (Exception localException) {
/* 268 */       fatalErrorOccured(localException.getMessage(), localException);
/*     */     } finally {
/* 270 */       this.loaderThread = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void checkShouldUpdate() {
/* 275 */     this.pauseAskUpdate = true;
/* 276 */     while (this.pauseAskUpdate)
/*     */       try {
/* 278 */         Thread.sleep(1000L);
/*     */       } catch (InterruptedException localInterruptedException) {
/* 280 */         localInterruptedException.printStackTrace();
/*     */       }
/*     */   }
/*     */ 
/*     */   protected String readVersionFile(File paramFile) throws Exception
/*     */   {
/* 286 */     DataInputStream localDataInputStream = new DataInputStream(new FileInputStream(paramFile));
/* 287 */     String str = localDataInputStream.readUTF();
/* 288 */     localDataInputStream.close();
/* 289 */     return str;
/*     */   }
/*     */ 
/*     */   protected void writeVersionFile(File paramFile, String paramString) throws Exception {
/* 293 */     DataOutputStream localDataOutputStream = new DataOutputStream(new FileOutputStream(paramFile));
/* 294 */     localDataOutputStream.writeUTF(paramString);
/* 295 */     localDataOutputStream.close();
/*     */   }
/*     */ 
/*     */   protected void updateClassPath(File paramFile) throws Exception
/*     */   {
/* 300 */     this.state = 6;
/*     */ 
/* 302 */     this.percentage = 95;
/*     */ 
/* 304 */     URL[] arrayOfURL = new URL[this.urlList.length];
/* 305 */     for (int i = 0; i < this.urlList.length; i++) {
/* 306 */       arrayOfURL[i] = new File(paramFile, getJarName(this.urlList[i])).toURI().toURL();
/*     */     }
/*     */ 
/* 309 */     if (classLoader == null) {
/* 310 */       classLoader = new URLClassLoader(arrayOfURL)
/*     */       {
/*     */         protected PermissionCollection getPermissions(CodeSource paramAnonymousCodeSource) {
/* 313 */           PermissionCollection localPermissionCollection = null;
/*     */           try
/*     */           {
/* 318 */             Method localMethod = SecureClassLoader.class.getDeclaredMethod("getPermissions", new Class[] { CodeSource.class });
/* 319 */             localMethod.setAccessible(true);
/* 320 */             localPermissionCollection = (PermissionCollection)localMethod.invoke(getClass().getClassLoader(), new Object[] { paramAnonymousCodeSource });
/*     */ 
/* 323 */             localPermissionCollection.add(new SocketPermission("www.minecraft.net", "connect,accept"));
/* 324 */             localPermissionCollection.add(new FilePermission("<<ALL FILES>>", "read"));
/*     */           }
/*     */           catch (Exception localException) {
/* 327 */             localException.printStackTrace();
/*     */           }
/*     */ 
/* 330 */           return localPermissionCollection;
/*     */         }
/*     */       };
/*     */     }
/*     */ 
/* 335 */     String str = paramFile.getAbsolutePath();
/* 336 */     if (!str.endsWith(File.separator)) str = str + File.separator;
/* 337 */     unloadNatives(str);
/*     */ 
/* 339 */     System.setProperty("org.lwjgl.librarypath", str + "natives");
/* 340 */     System.setProperty("net.java.games.input.librarypath", str + "natives");
/*     */ 
/* 342 */     natives_loaded = true;
/*     */   }
/*     */ 
/*     */   private void unloadNatives(String paramString)
/*     */   {
/* 347 */     if (!natives_loaded) {
/* 348 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 352 */       Field localField = ClassLoader.class.getDeclaredField("loadedLibraryNames");
/* 353 */       localField.setAccessible(true);
/* 354 */       Vector localVector = (Vector)localField.get(getClass().getClassLoader());
/*     */ 
/* 356 */       String str1 = new File(paramString).getCanonicalPath();
/*     */ 
/* 358 */       for (int i = 0; i < localVector.size(); i++) {
/* 359 */         String str2 = (String)localVector.get(i);
/*     */ 
/* 361 */         if (str2.startsWith(str1)) {
/* 362 */           localVector.remove(i);
/* 363 */           i--;
/*     */         }
/*     */       }
/*     */     } catch (Exception localException) {
/* 367 */       localException.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public Applet createApplet() throws ClassNotFoundException, InstantiationException, IllegalAccessException
/*     */   {
/* 373 */     Class localClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
/* 374 */     return (Applet)localClass.newInstance();
/*     */   }
/*     */ 
/*     */   protected void downloadJars(String paramString) throws Exception {
/* 378 */     File localFile = new File(paramString, "md5s");
/* 379 */     Properties localProperties = new Properties();
/* 380 */     if (localFile.exists()) {
/*     */       try {
/* 382 */         FileInputStream localFileInputStream = new FileInputStream(localFile);
/* 383 */         localProperties.load(localFileInputStream);
/* 384 */         localFileInputStream.close();
/*     */       } catch (Exception localException1) {
/* 386 */         localException1.printStackTrace();
/*     */       }
/*     */     }
/* 389 */     this.state = 4;
/*     */ 
/* 394 */     int[] arrayOfInt = new int[this.urlList.length];
/* 395 */     boolean[] arrayOfBoolean = new boolean[this.urlList.length];
/*     */     URLConnection localURLConnection;
/* 398 */     for (int i = 0; i < this.urlList.length; i++) {
/* 399 */       localURLConnection = this.urlList[i].openConnection();
/* 400 */       localURLConnection.setDefaultUseCaches(false);
/* 401 */       arrayOfBoolean[i] = false;
/* 402 */       if ((localURLConnection instanceof HttpURLConnection)) {
/* 403 */         ((HttpURLConnection)localURLConnection).setRequestMethod("HEAD");
/*     */ 
/* 405 */         Object localObject = "\"" + localProperties.getProperty(getFileName(this.urlList[i])) + "\"";
/*     */ 
/* 407 */         if ((!forceUpdate) && (localObject != null)) localURLConnection.setRequestProperty("If-None-Match", (String)localObject);
/*     */ 
/* 409 */         int j = ((HttpURLConnection)localURLConnection).getResponseCode();
/* 410 */         if (j / 100 == 3) {
/* 411 */           arrayOfBoolean[i] = true;
/*     */         }
/*     */       }
/* 414 */       arrayOfInt[i] = localURLConnection.getContentLength();
/* 415 */       this.totalSizeDownload += arrayOfInt[i];
/*     */     }
/*     */ 
/* 418 */     int i = this.percentage = 10;
/*     */ 
/* 421 */     byte[] localObject = new byte[65536];
/* 422 */     for (int j = 0; j < this.urlList.length; j++) {
/* 423 */       if (arrayOfBoolean[j]) {
/* 424 */         this.percentage = (i + arrayOfInt[j] * 45 / this.totalSizeDownload);
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 431 */           localProperties.remove(getFileName(this.urlList[j]));
/* 432 */           localProperties.store(new FileOutputStream(localFile), "md5 hashes for downloaded files");
/*     */         } catch (Exception localException2) {
/* 434 */           localException2.printStackTrace();
/*     */         }
/*     */ 
/* 437 */         int k = 0;
/* 438 */         int m = 3;
/* 439 */         int n = 1;
/*     */ 
/* 442 */         while (n != 0) {
/* 443 */           n = 0;
/*     */ 
/* 445 */           localURLConnection = this.urlList[j].openConnection();
/*     */ 
/* 447 */           String str1 = "";
/*     */ 
/* 449 */           if ((localURLConnection instanceof HttpURLConnection)) {
/* 450 */             localURLConnection.setRequestProperty("Cache-Control", "no-cache");
/* 451 */             localURLConnection.connect();
/*     */ 
/* 453 */             str1 = localURLConnection.getHeaderField("ETag");
/* 454 */             str1 = str1.substring(1, str1.length() - 1);
/*     */           }
/*     */ 
/* 457 */           String str2 = getFileName(this.urlList[j]);
/* 458 */           InputStream localInputStream = getJarInputStream(str2, localURLConnection);
/* 459 */           FileOutputStream localFileOutputStream = new FileOutputStream(paramString + str2);
/*     */ 
/* 462 */           long l1 = System.currentTimeMillis();
/* 463 */           int i2 = 0;
/* 464 */           int i3 = 0;
/* 465 */           String str3 = "";
/*     */ 
/* 467 */           MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
/*     */           int i1;
/* 468 */           while ((i1 = localInputStream.read((byte[])localObject, 0, localObject.length)) != -1) {
/* 469 */             localFileOutputStream.write((byte[])localObject, 0, i1);
/* 470 */             localMessageDigest.update((byte[])localObject, 0, i1);
/* 471 */             this.currentSizeDownload += i1;
/* 472 */             i3 += i1;
/* 473 */             this.percentage = (i + this.currentSizeDownload * 45 / this.totalSizeDownload);
/* 474 */             this.subtaskMessage = ("Retrieving: " + str2 + " " + this.currentSizeDownload * 100 / this.totalSizeDownload + "%");
/*     */ 
/* 476 */             i2 += i1;
/* 477 */             long l2 = System.currentTimeMillis() - l1;
/*     */ 
/* 479 */             if (l2 >= 1000L) {
/* 480 */               float f = i2 / (float)l2;
/* 481 */               f = (int)(f * 100.0F) / 100.0F;
/* 482 */               str3 = " @ " + f + " KB/sec";
/* 483 */               i2 = 0;
/* 484 */               l1 += 1000L;
/*     */             }
/*     */ 
/* 487 */             this.subtaskMessage += str3;
/*     */           }
/*     */ 
/* 490 */           localInputStream.close();
/* 491 */           localFileOutputStream.close();
/* 492 */           String str4 = new BigInteger(1, localMessageDigest.digest()).toString(16);
/* 493 */           while (str4.length() < 32) {
/* 494 */             str4 = "0" + str4;
/*     */           }
/* 496 */           boolean bool = true;
/* 497 */           if (str1 != null) {
/* 498 */             bool = str4.equals(str1);
/*     */           }
/*     */ 
/* 501 */           if ((localURLConnection instanceof HttpURLConnection)) {
/* 502 */             if ((bool) && ((i3 == arrayOfInt[j]) || (arrayOfInt[j] <= 0)))
/*     */             {
/*     */               try {
/* 505 */                 localProperties.setProperty(getFileName(this.urlList[j]), str1);
/* 506 */                 localProperties.store(new FileOutputStream(localFile), "md5 hashes for downloaded files");
/*     */               } catch (Exception localException3) {
/* 508 */                 localException3.printStackTrace();
/*     */               }
/*     */             } else {
/* 511 */               k++;
/* 512 */               if (k < m) {
/* 513 */                 n = 1;
/* 514 */                 this.currentSizeDownload -= i3;
/*     */               } else {
/* 516 */                 throw new Exception("failed to download " + str2);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 523 */     this.subtaskMessage = "";
/*     */   }
/*     */ 
/*     */   protected InputStream getJarInputStream(String paramString, final URLConnection paramURLConnection)
/*     */     throws Exception
/*     */   {
/* 534 */     final InputStream[] arrayOfInputStream = new InputStream[1];
/*     */ 
/* 538 */     for (int i = 0; (i < 3) && (arrayOfInputStream[0] == null); i++) {
/* 539 */       Thread local3 = new Thread()
/*     */       {
/*     */         public void run() {
/*     */           try {
/* 543 */             arrayOfInputStream[0] = paramURLConnection.getInputStream();
/*     */           }
/*     */           catch (IOException localIOException)
/*     */           {
/*     */           }
/*     */         }
/*     */       };
/* 549 */       local3.setName("JarInputStreamThread");
/* 550 */       local3.start();
/*     */ 
/* 552 */       int j = 0;
/* 553 */       while ((arrayOfInputStream[0] == null) && (j++ < 5)) {
/*     */         try {
/* 555 */           local3.join(1000L);
/*     */         }
/*     */         catch (InterruptedException localInterruptedException1)
/*     */         {
/*     */         }
/*     */       }
/* 561 */       if (arrayOfInputStream[0] == null) {
/*     */         try {
/* 563 */           local3.interrupt();
/* 564 */           local3.join();
/*     */         }
/*     */         catch (InterruptedException localInterruptedException2)
/*     */         {
/*     */         }
/*     */       }
/*     */     }
/* 571 */     if (arrayOfInputStream[0] == null) {
/* 572 */       throw new Exception("Unable to download " + paramString);
/*     */     }
/*     */ 
/* 575 */     return arrayOfInputStream[0];
/*     */   }
/*     */ 
/*     */   protected void extractLZMA(String paramString1, String paramString2)
/*     */     throws Exception
/*     */   {
/* 587 */     File localFile = new File(paramString1);
/* 588 */     if (!localFile.exists()) return;
/* 589 */     FileInputStream localFileInputStream = new FileInputStream(localFile);
/*     */ 
/* 592 */     Class localClass = Class.forName("LZMA.LzmaInputStream");
/* 593 */     Constructor localConstructor = localClass.getDeclaredConstructor(new Class[] { InputStream.class });
/* 594 */     InputStream localInputStream = (InputStream)localConstructor.newInstance(new Object[] { localFileInputStream });
/*     */ 
/* 597 */     FileOutputStream localFileOutputStream = new FileOutputStream(paramString2);
/*     */ 
/* 599 */     byte[] arrayOfByte = new byte[16384];
/*     */ 
/* 601 */     int i = localInputStream.read(arrayOfByte);
/* 602 */     while (i >= 1) {
/* 603 */       localFileOutputStream.write(arrayOfByte, 0, i);
/* 604 */       i = localInputStream.read(arrayOfByte);
/*     */     }
/*     */ 
/* 607 */     localInputStream.close();
/* 608 */     localFileOutputStream.close();
/*     */ 
/* 610 */     localFileOutputStream = null;
/* 611 */     localInputStream = null;
/*     */ 
/* 614 */     localFile.delete();
/*     */   }
/*     */ 
/*     */   protected void extractPack(String paramString1, String paramString2)
/*     */     throws Exception
/*     */   {
/* 625 */     File localFile = new File(paramString1);
/* 626 */     if (!localFile.exists()) return;
/*     */ 
/* 628 */     FileOutputStream localFileOutputStream = new FileOutputStream(paramString2);
/* 629 */     JarOutputStream localJarOutputStream = new JarOutputStream(localFileOutputStream);
/*     */ 
/* 631 */     Pack200.Unpacker localUnpacker = Pack200.newUnpacker();
/* 632 */     localUnpacker.unpack(localFile, localJarOutputStream);
/* 633 */     localJarOutputStream.close();
/*     */ 
/* 636 */     localFile.delete();
/*     */   }
/*     */ 
/*     */   protected void extractJars(String paramString)
/*     */     throws Exception
/*     */   {
/* 646 */     this.state = 5;
/*     */ 
/* 648 */     float f = 10.0F / this.urlList.length;
/*     */ 
/* 650 */     for (int i = 0; i < this.urlList.length; i++) {
/* 651 */       this.percentage = (55 + (int)(f * (i + 1)));
/* 652 */       String str = getFileName(this.urlList[i]);
/*     */ 
/* 654 */       if (str.endsWith(".pack.lzma")) {
/* 655 */         this.subtaskMessage = ("Extracting: " + str + " to " + str.replaceAll(".lzma", ""));
/* 656 */         extractLZMA(paramString + str, paramString + str.replaceAll(".lzma", ""));
/*     */ 
/* 658 */         this.subtaskMessage = ("Extracting: " + str.replaceAll(".lzma", "") + " to " + str.replaceAll(".pack.lzma", ""));
/* 659 */         extractPack(paramString + str.replaceAll(".lzma", ""), paramString + str.replaceAll(".pack.lzma", ""));
/* 660 */       } else if (str.endsWith(".pack")) {
/* 661 */         this.subtaskMessage = ("Extracting: " + str + " to " + str.replace(".pack", ""));
/* 662 */         extractPack(paramString + str, paramString + str.replace(".pack", ""));
/* 663 */       } else if (str.endsWith(".lzma")) {
/* 664 */         this.subtaskMessage = ("Extracting: " + str + " to " + str.replace(".lzma", ""));
/* 665 */         extractLZMA(paramString + str, paramString + str.replace(".lzma", ""));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void extractNatives(String paramString) throws Exception
/*     */   {
/* 672 */     this.state = 5;
/*     */ 
/* 674 */     int i = this.percentage;
/*     */ 
/* 676 */     String str = getJarName(this.urlList[(this.urlList.length - 1)]);
/*     */ 
/* 678 */     Certificate[] arrayOfCertificate = Launcher.class.getProtectionDomain().getCodeSource().getCertificates();
/*     */ 
/* 680 */     if (arrayOfCertificate == null) {
/* 681 */       URL localObject1 = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
/*     */ 
/* 683 */       URLConnection localObject2 = (JarURLConnection)new URL("jar:" + ((URL)localObject1).toString() + "!/net/minecraft/Launcher.class").openConnection();
/* 684 */       ((JarURLConnection)localObject2).setDefaultUseCaches(true);
/*     */       try {
/* 686 */         arrayOfCertificate = ((JarURLConnection)localObject2).getCertificates();
/*     */       }
/*     */       catch (Exception localException)
/*     */       {
/*     */       }
/*     */     }
/* 692 */     Object localObject1 = new File(paramString + "natives");
/* 693 */     if (!((File)localObject1).exists()) {
/* 694 */       ((File)localObject1).mkdir();
/*     */     }
/*     */ 
/* 697 */     Object localObject2 = new File(paramString + str);
/* 698 */     if (!((File)localObject2).exists()) return;
/* 699 */     JarFile localJarFile = new JarFile((File)localObject2, true);
/* 700 */     Enumeration localEnumeration = localJarFile.entries();
/*     */ 
/* 702 */     this.totalSizeExtract = 0;
/*     */ 
/* 705 */     while (localEnumeration.hasMoreElements()) {
/* 706 */       JarEntry localObject3 = (JarEntry)localEnumeration.nextElement();
/*     */ 
/* 710 */       if ((!((JarEntry)localObject3).isDirectory()) && (((JarEntry)localObject3).getName().indexOf('/') == -1))
/*     */       {
/* 713 */         this.totalSizeExtract = ((int)(this.totalSizeExtract + ((JarEntry)localObject3).getSize()));
/*     */       }
/*     */     }
/* 716 */     this.currentSizeExtract = 0;
/*     */ 
/* 718 */     localEnumeration = localJarFile.entries();
/*     */ 
/* 720 */     while (localEnumeration.hasMoreElements()) {
/* 721 */       JarEntry localObject3 = (JarEntry)localEnumeration.nextElement();
/*     */ 
/* 723 */       if ((!((JarEntry)localObject3).isDirectory()) && (((JarEntry)localObject3).getName().indexOf('/') == -1))
/*     */       {
/* 727 */         File localFile = new File(paramString + "natives" + File.separator + ((JarEntry)localObject3).getName());
/* 728 */         if ((!localFile.exists()) || 
/* 729 */           (localFile.delete()))
/*     */         {
/* 734 */           InputStream localInputStream = localJarFile.getInputStream(localJarFile.getEntry(((JarEntry)localObject3).getName()));
/* 735 */           FileOutputStream localFileOutputStream = new FileOutputStream(paramString + "natives" + File.separator + ((JarEntry)localObject3).getName());
/*     */ 
/* 738 */           byte[] arrayOfByte = new byte[65536];
/*     */           int j;
/* 740 */           while ((j = localInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1) {
/* 741 */             localFileOutputStream.write(arrayOfByte, 0, j);
/* 742 */             this.currentSizeExtract += j;
/*     */ 
/* 744 */             this.percentage = (i + this.currentSizeExtract * 20 / this.totalSizeExtract);
/* 745 */             this.subtaskMessage = ("Extracting: " + ((JarEntry)localObject3).getName() + " " + this.currentSizeExtract * 100 / this.totalSizeExtract + "%");
/*     */           }
/*     */ 
/* 748 */           validateCertificateChain(arrayOfCertificate, ((JarEntry)localObject3).getCertificates());
/*     */ 
/* 750 */           localInputStream.close();
/* 751 */           localFileOutputStream.close();
/*     */         }
/*     */       }
/*     */     }
/* 753 */     this.subtaskMessage = "";
/*     */ 
/* 755 */     localJarFile.close();
/*     */ 
/* 757 */     Object localObject3 = new File(paramString + str);
/* 758 */     ((File)localObject3).delete();
/*     */   }
/*     */ 
/*     */   protected static void validateCertificateChain(Certificate[] paramArrayOfCertificate1, Certificate[] paramArrayOfCertificate2)
/*     */     throws Exception
/*     */   {
/* 768 */     if (paramArrayOfCertificate1 == null) return;
/* 769 */     if (paramArrayOfCertificate2 == null) {
/* 770 */       throw new Exception("Unable to validate certificate chain. Native entry did not have a certificate chain at all");
/*     */     }
/* 772 */     if (paramArrayOfCertificate1.length != paramArrayOfCertificate2.length) {
/* 773 */       throw new Exception("Unable to validate certificate chain. Chain differs in length [" + paramArrayOfCertificate1.length + " vs " + paramArrayOfCertificate2.length + "]");
/*     */     }
/* 775 */     for (int i = 0; i < paramArrayOfCertificate1.length; i++)
/* 776 */       if (!paramArrayOfCertificate1[i].equals(paramArrayOfCertificate2[i]))
/* 777 */         throw new Exception("Certificate mismatch: " + paramArrayOfCertificate1[i] + " != " + paramArrayOfCertificate2[i]);
/*     */   }
/*     */ 
/*     */   protected String getJarName(URL paramURL)
/*     */   {
/* 783 */     String str = paramURL.getFile();
/*     */ 
/* 785 */     if (str.contains("?")) {
/* 786 */       str = str.substring(0, str.indexOf("?"));
/*     */     }
/* 788 */     if (str.endsWith(".pack.lzma"))
/* 789 */       str = str.replaceAll(".pack.lzma", "");
/* 790 */     else if (str.endsWith(".pack"))
/* 791 */       str = str.replaceAll(".pack", "");
/* 792 */     else if (str.endsWith(".lzma")) {
/* 793 */       str = str.replaceAll(".lzma", "");
/*     */     }
/*     */ 
/* 796 */     return str.substring(str.lastIndexOf('/') + 1);
/*     */   }
/*     */ 
/*     */   protected String getFileName(URL paramURL) {
/* 800 */     String str = paramURL.getFile();
/* 801 */     if (str.contains("?")) {
/* 802 */       str = str.substring(0, str.indexOf("?"));
/*     */     }
/* 804 */     return str.substring(str.lastIndexOf('/') + 1);
/*     */   }
/*     */ 
/*     */   protected void fatalErrorOccured(String paramString, Exception paramException) {
/* 808 */     paramException.printStackTrace();
/* 809 */     this.fatalError = true;
/* 810 */     this.fatalErrorDescription = ("Fatal error occured (" + this.state + "): " + paramString);
/* 811 */     System.out.println(this.fatalErrorDescription);
/* 812 */     System.out.println(generateStacktrace(paramException));
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline()
/*     */   {
/*     */     try
/*     */     {
/* 819 */       String str1 = (String)AccessController.doPrivileged(new PrivilegedExceptionAction() {
/*     */         public Object run() throws Exception {
/* 821 */           return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
/*     */         }
/*     */       });
/* 825 */       File localFile = new File(str1);
/* 826 */       if (!localFile.exists()) return false;
/*     */ 
/* 828 */       localFile = new File(localFile, "version");
/* 829 */       if (!localFile.exists()) return false;
/*     */ 
/* 831 */       if (localFile.exists()) {
/* 832 */         String str2 = readVersionFile(localFile);
/* 833 */         if ((str2 != null) && (str2.length() > 0))
/* 834 */           return true;
/*     */       }
/*     */     }
/*     */     catch (Exception localException) {
/* 838 */       localException.printStackTrace();
/* 839 */       return false;
/*     */     }
/* 841 */     return false;
/*     */   }
/*     */ }

/* Location:           C:\Users\Roman\Documents\jad\src\CommonLauncher.jar
 * Qualified Name:     net.minecraft.GameUpdater
 * JD-Core Version:    0.6.2
 */