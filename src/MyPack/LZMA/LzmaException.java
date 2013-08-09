// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LzmaException.java

package LZMA;

import java.io.IOException;

public class LzmaException extends IOException
{

    public LzmaException()
    {
    }

    public LzmaException(String s)
    {
        super(s);
    }

    private static final long serialVersionUID = 0x3333353534383736L;
}
