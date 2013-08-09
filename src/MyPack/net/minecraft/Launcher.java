/*     */ package net.minecraft;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.applet.AppletStub;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Image;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.MouseListener;
/*     */ import java.awt.image.BufferedImage;
/*     */ import java.awt.image.VolatileImage;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import javax.imageio.ImageIO;

import mypack.MyPack;
/*     */ 
/*     */ public class Launcher extends Applet
/*     */   implements Runnable, AppletStub, MouseListener
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  15 */   public Map<String, String> customParameters = new HashMap();
/*     */   private GameUpdater gameUpdater;
/*  18 */   private boolean gameUpdaterStarted = false;
/*     */   private Applet applet;
/*     */   private Image bgImage;
/*  21 */   private boolean active = false;
/*  22 */   private int context = 0;
/*  23 */   private boolean hasMouseListener = false;
/*     */   private VolatileImage img;
/*     */ 
/*     */   public boolean isActive()
/*     */   {
/*  27 */     if (this.context == 0) {
/*  28 */       this.context = -1;
/*     */       try {
/*  30 */         if (getAppletContext() != null) this.context = 1; 
/*     */       }
/*     */       catch (Exception localException) {  }
/*     */ 
/*     */     }
/*  34 */     if (this.context == -1) return this.active;
/*  35 */     return super.isActive();
/*     */   }
/*     */ 
/*     */   public void init(String paramString1, String paramString2, String paramString3, String paramString4)
/*     */   {
/*     */     try {
/*  41 */       this.bgImage = ImageIO.read(Launcher.class.getResource("dirt.png")).getScaledInstance(32, 32, 16);
/*     */     } catch (IOException localIOException) {
/*  43 */       localIOException.printStackTrace();
/*     */     }
/*     */ 
/*  46 */     this.customParameters.put("username", paramString1);
/*  47 */     this.customParameters.put("sessionid", paramString4);
/*     */ 
/*  49 */     this.gameUpdater = new GameUpdater(paramString2, "minecraft.jar?user=" + paramString1 + "&ticket=" + paramString3, this.customParameters.containsKey("noupdate"));
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline() {
/*  53 */     return this.gameUpdater.canPlayOffline();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  58 */     if (this.applet != null) {
/*  59 */       this.applet.init();
/*  60 */       return;
/*     */     }
/*  62 */     init(getParameter("userName"), getParameter("latestVersion"), getParameter("downloadTicket"), getParameter("sessionId"));
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/*  67 */     if (this.applet != null) {
/*  68 */       this.applet.start();
/*  69 */       return;
/*     */     }
/*  71 */     if (this.gameUpdaterStarted) return;
/*     */ 
/*  73 */     Object localObject = new Thread()
/*     */     {
/*     */       public void run() {
/*  76 */         Launcher.this.gameUpdater.run();
/*     */         try {
/*  78 */           if (!Launcher.this.gameUpdater.fatalError)
/*  79 */             Launcher.this.replace(Launcher.this.gameUpdater.createApplet());
/*     */         }
/*     */         catch (ClassNotFoundException localClassNotFoundException)
/*     */         {
/*  83 */           localClassNotFoundException.printStackTrace();
/*     */         } catch (InstantiationException localInstantiationException) {
/*  85 */           localInstantiationException.printStackTrace();
/*     */         } catch (IllegalAccessException localIllegalAccessException) {
/*  87 */           localIllegalAccessException.printStackTrace();
/*     */         }
/*     */       }
/*     */     };
/*  91 */     ((Thread)localObject).setDaemon(true);
/*  92 */     ((Thread)localObject).start();
/*     */ 
/*  94 */     localObject = new Thread()
/*     */     {
/*     */       public void run() {
/*  97 */         while (Launcher.this.applet == null) {
/*  98 */           Launcher.this.repaint();
/*     */           try {
/* 100 */             Thread.sleep(10L);
/*     */           } catch (InterruptedException localInterruptedException) {
/* 102 */             localInterruptedException.printStackTrace();
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 107 */     ((Thread)localObject).setDaemon(true);
/* 108 */     ((Thread)localObject).start();
/*     */ 
/* 110 */     this.gameUpdaterStarted = true;
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 115 */     if (this.applet != null) {
/* 116 */       this.active = false;
/* 117 */       this.applet.stop();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void destroy()
/*     */   {
/* 123 */     if (this.applet != null)
/* 124 */       this.applet.destroy();
/*     */   }
/*     */ 
/*     */   public void replace(Applet paramApplet)
/*     */   {
/* 129 */     this.applet = paramApplet;
/* 130 */     paramApplet.setStub(this);
/* 131 */     paramApplet.setSize(getWidth(), getHeight());
/*     */ 
/* 133 */     setLayout(new BorderLayout());
/* 134 */     add(paramApplet, "Center");
/*     */ 
/* 136 */     paramApplet.init();
/* 137 */     this.active = true;
/* 138 */     paramApplet.start();
/* 139 */     validate();
/*     */   }
/*     */ 
/*     */   public void update(Graphics paramGraphics)
/*     */   {
/* 146 */     paint(paramGraphics);
/*     */   }
/*     */ 
/*     */   public void paint(Graphics paramGraphics)
/*     */   {
/* 151 */     if (this.applet != null) return;
/*     */ 
/* 153 */     int i = getWidth() / 2;
/* 154 */     int j = getHeight() / 2;
/* 155 */     if ((this.img == null) || (this.img.getWidth() != i) || (this.img.getHeight() != j)) {
/* 156 */       this.img = createVolatileImage(i, j);
/*     */     }
/*     */ 
/* 159 */     Graphics localGraphics = this.img.getGraphics();
/* 160 */     for (int k = 0; k <= i / 32; k++)
/* 161 */       for (int m = 0; m <= j / 32; m++)
/* 162 */         localGraphics.drawImage(this.bgImage, k * 32, m * 32, null);
/*     */     String str;
/*     */     FontMetrics localFontMetrics;
/* 164 */     if (this.gameUpdater.pauseAskUpdate) {
/* 165 */       if (!this.hasMouseListener) {
/* 166 */         this.hasMouseListener = true;
/* 167 */         addMouseListener(this);
/*     */       }
/* 169 */       localGraphics.setColor(Color.LIGHT_GRAY);
/* 170 */       str = "New update available";
/* 171 */       localGraphics.setFont(new Font(null, 1, 20));
/* 172 */       localFontMetrics = localGraphics.getFontMetrics();
/* 173 */       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 - localFontMetrics.getHeight() * 2);
/*     */ 
/* 175 */       localGraphics.setFont(new Font(null, 0, 12));
/* 176 */       localFontMetrics = localGraphics.getFontMetrics();
/*     */ 
/* 178 */       localGraphics.fill3DRect(i / 2 - 56 - 8, j / 2, 56, 20, true);
/* 179 */       localGraphics.fill3DRect(i / 2 + 8, j / 2, 56, 20, true);
/*     */ 
/* 181 */       str = "Would you like to update?";
/* 182 */       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 - 8);
/*     */ 
/* 184 */       localGraphics.setColor(Color.BLACK);
/* 185 */       str = "Yes";
/* 186 */       localGraphics.drawString(str, i / 2 - 56 - 8 - localFontMetrics.stringWidth(str) / 2 + 28, j / 2 + 14);
/* 187 */       str = "Not now";
/* 188 */       localGraphics.drawString(str, i / 2 + 8 - localFontMetrics.stringWidth(str) / 2 + 28, j / 2 + 14);
/*     */     }
/*     */     else
/*     */     {
/* 192 */       localGraphics.setColor(Color.LIGHT_GRAY);
/*     */ 
/* 196 */       str = "Updating Minecraft";
/* 197 */       if (this.gameUpdater.fatalError) {
/* 198 */         str = "Failed to launch";
/*     */       }
/*     */ 
/* 201 */       localGraphics.setFont(new Font(null, 1, 20));
/* 202 */       localFontMetrics = localGraphics.getFontMetrics();
/* 203 */       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 - localFontMetrics.getHeight() * 2);
/*     */ 
/* 205 */       localGraphics.setFont(new Font(null, 0, 12));
/* 206 */       localFontMetrics = localGraphics.getFontMetrics();
/* 207 */       str = this.gameUpdater.getDescriptionForState();
/* 208 */       if (this.gameUpdater.fatalError) {
/* 209 */         str = this.gameUpdater.fatalErrorDescription;
/*     */       }
/*     */ 
/* 212 */       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 + localFontMetrics.getHeight() * 1);
/* 213 */       str = this.gameUpdater.subtaskMessage;
/* 214 */       localGraphics.drawString(str, i / 2 - localFontMetrics.stringWidth(str) / 2, j / 2 + localFontMetrics.getHeight() * 2);
/*     */ 
/* 216 */       if (!this.gameUpdater.fatalError) {
/* 217 */         localGraphics.setColor(Color.black);
/* 218 */         localGraphics.fillRect(64, j - 64, i - 128 + 1, 5);
/* 219 */         localGraphics.setColor(new Color(32768));
/* 220 */         localGraphics.fillRect(64, j - 64, this.gameUpdater.percentage * (i - 128) / 100, 4);
/* 221 */         localGraphics.setColor(new Color(2138144));
/* 222 */         localGraphics.fillRect(65, j - 64 + 1, this.gameUpdater.percentage * (i - 128) / 100 - 2, 1);
/*     */       }
/*     */     }
/*     */ 
/* 226 */     localGraphics.dispose();
/*     */ 
/* 228 */     paramGraphics.drawImage(this.img, 0, 0, i * 2, j * 2, null);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */   }
/*     */ 
/*     */   public String getParameter(String paramString) {
/* 236 */     String str = (String)this.customParameters.get(paramString);
/* 237 */     if (str != null) return str; try
/*     */     {
/* 239 */       return super.getParameter(paramString);
/*     */     } catch (Exception localException) {
/* 241 */       this.customParameters.put(paramString, null);
/* 242 */     }return null;
/*     */   }
/*     */ 
/*     */   public void appletResize(int paramInt1, int paramInt2)
/*     */   {
/*     */   }
/*     */ 
/*     */   public URL getDocumentBase()
/*     */   {
/*     */     try {
/* 252 */       return new URL("http://www.minecraft.net/game/");
/*     */     } catch (MalformedURLException localMalformedURLException) {
/* 254 */       localMalformedURLException.printStackTrace();
/*     */     }
/* 256 */     return null;
/*     */   }
/*     */ 
/*     */   public void mouseClicked(MouseEvent paramMouseEvent) {
/*     */   }
/*     */ 
/*     */   public void mouseEntered(MouseEvent paramMouseEvent) {
/*     */   }
/*     */ 
/*     */   public void mouseExited(MouseEvent paramMouseEvent) {
/*     */   }
/*     */ 
/*     */   public void mousePressed(MouseEvent paramMouseEvent) {
/* 269 */     int i = paramMouseEvent.getX() / 2;
/* 270 */     int j = paramMouseEvent.getY() / 2;
/* 271 */     int k = getWidth() / 2;
/* 272 */     int m = getHeight() / 2;
/*     */ 
/* 274 */     if (contains(i, j, k / 2 - 56 - 8, m / 2, 56, 20)) {
/* 275 */       removeMouseListener(this);
/* 276 */       this.gameUpdater.shouldUpdate = true;
/* 277 */       this.gameUpdater.pauseAskUpdate = false;
/* 278 */       this.hasMouseListener = false;
/*     */     }
/* 280 */     if (contains(i, j, k / 2 + 8, m / 2, 56, 20)) {
/* 281 */       removeMouseListener(this);
/* 282 */       this.gameUpdater.shouldUpdate = false;
/* 283 */       this.gameUpdater.pauseAskUpdate = false;
/* 284 */       this.hasMouseListener = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   private boolean contains(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6) {
/* 289 */     return (paramInt1 >= paramInt3) && (paramInt2 >= paramInt4) && (paramInt1 < paramInt3 + paramInt5) && (paramInt2 < paramInt4 + paramInt6);
/*     */   }
/*     */ 
/*     */   public void mouseReleased(MouseEvent paramMouseEvent)
/*     */   {
/*     */   }
/*     */ }

/* Location:           C:\Users\Roman\Documents\jad\src\CommonLauncher.jar
 * Qualified Name:     net.minecraft.Launcher
 * JD-Core Version:    0.6.2
 */