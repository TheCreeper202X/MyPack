// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LzmaInputStream.java

package LZMA;

import java.io.*;

// Referenced classes of package LZMA:
//            LzmaException, CRangeDecoder

public class LzmaInputStream extends FilterInputStream
{

    public LzmaInputStream(InputStream inputstream)
        throws IOException
    {
        super(inputstream);
        isClosed = false;
        readHeader();
        fill_buffer();
    }

    private void LzmaDecode(int i)
        throws IOException
    {
        int j = (1 << pb) - 1;
        int k = (1 << lp) - 1;
        uncompressed_size = 0;
        if(RemainLen == -1)
            return;
        for(; RemainLen > 0 && uncompressed_size < i; RemainLen--)
        {
            int l = dictionaryPos - rep0;
            if(l < 0)
                l += dictionarySize;
            uncompressed_buffer[uncompressed_size++] = dictionary[dictionaryPos] = dictionary[l];
            if(++dictionaryPos == dictionarySize)
                dictionaryPos = 0;
        }

        byte byte0;
        if(dictionaryPos == 0)
            byte0 = dictionary[dictionarySize - 1];
        else
            byte0 = dictionary[dictionaryPos - 1];
        do
        {
            if(uncompressed_size >= i)
                break;
            int i1 = uncompressed_size + GlobalPos & j;
            if(RangeDecoder.BitDecode(probs, 0 + (State << 4) + i1) == 0)
            {
                int j1 = 1846 + 768 * (((uncompressed_size + GlobalPos & k) << lc) + ((byte0 & 0xff) >> 8 - lc));
                if(State < 4)
                    State = 0;
                else
                if(State < 10)
                    State -= 3;
                else
                    State -= 6;
                if(PreviousIsMatch)
                {
                    int k2 = dictionaryPos - rep0;
                    if(k2 < 0)
                        k2 += dictionarySize;
                    byte byte1 = dictionary[k2];
                    byte0 = RangeDecoder.LzmaLiteralDecodeMatch(probs, j1, byte1);
                    PreviousIsMatch = false;
                } else
                {
                    byte0 = RangeDecoder.LzmaLiteralDecode(probs, j1);
                }
                uncompressed_buffer[uncompressed_size++] = byte0;
                dictionary[dictionaryPos] = byte0;
                if(++dictionaryPos == dictionarySize)
                    dictionaryPos = 0;
                continue;
            }
            PreviousIsMatch = true;
            if(RangeDecoder.BitDecode(probs, 192 + State) == 1)
            {
                if(RangeDecoder.BitDecode(probs, 204 + State) == 0)
                {
                    if(RangeDecoder.BitDecode(probs, 240 + (State << 4) + i1) == 0)
                    {
                        if(uncompressed_size + GlobalPos == 0)
                            throw new LzmaException("LZMA : Data Error");
                        State = State >= 7 ? 11 : 9;
                        int k1 = dictionaryPos - rep0;
                        if(k1 < 0)
                            k1 += dictionarySize;
                        byte0 = dictionary[k1];
                        dictionary[dictionaryPos] = byte0;
                        if(++dictionaryPos == dictionarySize)
                            dictionaryPos = 0;
                        uncompressed_buffer[uncompressed_size++] = byte0;
                        continue;
                    }
                } else
                {
                    int l1;
                    if(RangeDecoder.BitDecode(probs, 216 + State) == 0)
                    {
                        l1 = rep1;
                    } else
                    {
                        if(RangeDecoder.BitDecode(probs, 228 + State) == 0)
                        {
                            l1 = rep2;
                        } else
                        {
                            l1 = rep3;
                            rep3 = rep2;
                        }
                        rep2 = rep1;
                    }
                    rep1 = rep0;
                    rep0 = l1;
                }
                RemainLen = RangeDecoder.LzmaLenDecode(probs, 1332, i1);
                State = State >= 7 ? 11 : 8;
            } else
            {
                rep3 = rep2;
                rep2 = rep1;
                rep1 = rep0;
                State = State >= 7 ? 10 : 7;
                RemainLen = RangeDecoder.LzmaLenDecode(probs, 818, i1);
                int i2 = RangeDecoder.BitTreeDecode(probs, 432 + ((RemainLen >= 4 ? 3 : RemainLen) << 6), 6);
                if(i2 >= 4)
                {
                    int l2 = (i2 >> 1) - 1;
                    rep0 = (2 | i2 & 1) << l2;
                    if(i2 < 14)
                    {
                        rep0 += RangeDecoder.ReverseBitTreeDecode(probs, (688 + rep0) - i2 - 1, l2);
                    } else
                    {
                        rep0 += RangeDecoder.DecodeDirectBits(l2 - 4) << 4;
                        rep0 += RangeDecoder.ReverseBitTreeDecode(probs, 802, 4);
                    }
                } else
                {
                    rep0 = i2;
                }
                rep0++;
            }
            if(rep0 == 0)
            {
                RemainLen = -1;
                break;
            }
            if(rep0 > uncompressed_size + GlobalPos)
                throw new LzmaException("LZMA : Data Error");
            RemainLen += 2;
            do
            {
                int j2 = dictionaryPos - rep0;
                if(j2 < 0)
                    j2 += dictionarySize;
                byte0 = dictionary[j2];
                dictionary[dictionaryPos] = byte0;
                if(++dictionaryPos == dictionarySize)
                    dictionaryPos = 0;
                uncompressed_buffer[uncompressed_size++] = byte0;
                RemainLen--;
            } while(RemainLen > 0 && uncompressed_size < i);
        } while(true);
        GlobalPos = GlobalPos + uncompressed_size;
    }

    private void fill_buffer()
        throws IOException
    {
        if(GlobalNowPos < GlobalOutSize)
        {
            uncompressed_offset = 0;
            long l = GlobalOutSize - GlobalNowPos;
            int i;
            if(l > 0x10000L)
                i = 0x10000;
            else
                i = (int)l;
            LzmaDecode(i);
            if(uncompressed_size == 0)
                GlobalOutSize = GlobalNowPos;
            else
                GlobalNowPos += uncompressed_size;
        }
    }

    private void readHeader()
        throws IOException
    {
        byte abyte0[] = new byte[5];
        if(5 != in.read(abyte0))
            throw new LzmaException("LZMA header corrupted : Properties error");
        GlobalOutSize = 0L;
        for(int i = 0; i < 8; i++)
        {
            int k = in.read();
            if(k == -1)
                throw new LzmaException("LZMA header corrupted : Size error");
            GlobalOutSize += (long)k << i * 8;
        }

        if(GlobalOutSize == -1L)
            GlobalOutSize = 0x7fffffffffffffffL;
        int j = abyte0[0] & 0xff;
        if(j >= 225)
            throw new LzmaException("LZMA header corrupted : Properties error");
        pb = 0;
        for(; j >= 45; j -= 45)
            pb++;

        lp = 0;
        for(; j >= 9; j -= 9)
            lp++;

        lc = j;
        int l = 1846 + (768 << lc + lp);
        probs = new int[l];
        dictionarySize = 0;
        for(int i1 = 0; i1 < 4; i1++)
            dictionarySize += (abyte0[1 + i1] & 0xff) << i1 * 8;

        dictionary = new byte[dictionarySize];
        if(dictionary == null)
            throw new LzmaException("LZMA : can't allocate");
        int j1 = 1846 + (768 << lc + lp);
        RangeDecoder = new CRangeDecoder(in);
        dictionaryPos = 0;
        GlobalPos = 0;
        rep0 = rep1 = rep2 = rep3 = 1;
        State = 0;
        PreviousIsMatch = false;
        RemainLen = 0;
        dictionary[dictionarySize - 1] = 0;
        for(int k1 = 0; k1 < j1; k1++)
            probs[k1] = 1024;

        uncompressed_buffer = new byte[0x10000];
        uncompressed_size = 0;
        uncompressed_offset = 0;
        GlobalNowPos = 0L;
    }

    public int read(byte abyte0[], int i, int j)
        throws IOException
    {
        if(isClosed)
            throw new IOException("stream closed");
        if((i | j | i + j | abyte0.length - (i + j)) < 0)
            throw new IndexOutOfBoundsException();
        if(j == 0)
            return 0;
        if(uncompressed_offset == uncompressed_size)
            fill_buffer();
        if(uncompressed_offset == uncompressed_size)
        {
            return -1;
        } else
        {
            int k = Math.min(j, uncompressed_size - uncompressed_offset);
            System.arraycopy(uncompressed_buffer, uncompressed_offset, abyte0, i, k);
            uncompressed_offset += k;
            return k;
        }
    }

    public void close()
        throws IOException
    {
        isClosed = true;
        super.close();
    }

    boolean isClosed;
    CRangeDecoder RangeDecoder;
    byte dictionary[];
    int dictionarySize;
    int dictionaryPos;
    int GlobalPos;
    int rep0;
    int rep1;
    int rep2;
    int rep3;
    int lc;
    int lp;
    int pb;
    int State;
    boolean PreviousIsMatch;
    int RemainLen;
    int probs[];
    byte uncompressed_buffer[];
    int uncompressed_size;
    int uncompressed_offset;
    long GlobalNowPos;
    long GlobalOutSize;
    static final int LZMA_BASE_SIZE = 1846;
    static final int LZMA_LIT_SIZE = 768;
    static final int kBlockSize = 0x10000;
    static final int kNumStates = 12;
    static final int kStartPosModelIndex = 4;
    static final int kEndPosModelIndex = 14;
    static final int kNumFullDistances = 128;
    static final int kNumPosSlotBits = 6;
    static final int kNumLenToPosStates = 4;
    static final int kNumAlignBits = 4;
    static final int kAlignTableSize = 16;
    static final int kMatchMinLen = 2;
    static final int IsMatch = 0;
    static final int IsRep = 192;
    static final int IsRepG0 = 204;
    static final int IsRepG1 = 216;
    static final int IsRepG2 = 228;
    static final int IsRep0Long = 240;
    static final int PosSlot = 432;
    static final int SpecPos = 688;
    static final int Align = 802;
    static final int LenCoder = 818;
    static final int RepLenCoder = 1332;
    static final int Literal = 1846;
}
