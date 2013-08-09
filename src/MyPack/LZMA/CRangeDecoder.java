
package LZMA;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package LZMA:
//            LzmaException

class CRangeDecoder
{

    CRangeDecoder(InputStream inputstream)
        throws IOException
    {
        buffer = new byte[16384];
        inStream = inputstream;
        Code = 0;
        Range = -1;
        for(int i = 0; i < 5; i++)
            Code = Code << 8 | Readbyte();

    }

    int Readbyte()
        throws IOException
    {
        if(buffer_size == buffer_ind)
        {
            buffer_size = inStream.read(buffer);
            buffer_ind = 0;
            if(buffer_size < 1)
                throw new LzmaException("LZMA : Data Error");
        }
        return buffer[buffer_ind++] & 0xff;
    }

    int DecodeDirectBits(int i)
        throws IOException
    {
        int j = 0;
        for(int k = i; k > 0; k--)
        {
            Range >>>= 1;
            int l = Code - Range >>> 31;
            Code -= Range & l - 1;
            j = j << 1 | 1 - l;
            if(Range < 0x1000000)
            {
                Code = Code << 8 | Readbyte();
                Range <<= 8;
            }
        }

        return j;
    }

    int BitDecode(int ai[], int i)
        throws IOException
    {
        int j = (Range >>> 11) * ai[i];
        if(((long)Code & 0xffffffffL) < ((long)j & 0xffffffffL))
        {
            Range = j;
            ai[i] += 2048 - ai[i] >>> 5;
            if((Range & 0xff000000) == 0)
            {
                Code = Code << 8 | Readbyte();
                Range <<= 8;
            }
            return 0;
        }
        Range -= j;
        Code -= j;
        ai[i] -= ai[i] >>> 5;
        if((Range & 0xff000000) == 0)
        {
            Code = Code << 8 | Readbyte();
            Range <<= 8;
        }
        return 1;
    }

    int BitTreeDecode(int ai[], int i, int j)
        throws IOException
    {
        int k = 1;
        for(int l = j; l > 0; l--)
            k = k + k + BitDecode(ai, i + k);

        return k - (1 << j);
    }

    int ReverseBitTreeDecode(int ai[], int i, int j)
        throws IOException
    {
        int k = 1;
        int l = 0;
        for(int i1 = 0; i1 < j; i1++)
        {
            int j1 = BitDecode(ai, i + k);
            k = k + k + j1;
            l |= j1 << i1;
        }

        return l;
    }

    byte LzmaLiteralDecode(int ai[], int i)
        throws IOException
    {
        int j = 1;
        do
            j = j + j | BitDecode(ai, i + j);
        while(j < 256);
        return (byte)j;
    }

    byte LzmaLiteralDecodeMatch(int ai[], int i, byte byte0)
        throws IOException
    {
        int j = 1;
        do
        {
            int k = byte0 >> 7 & 1;
            byte0 <<= 1;
            int l = BitDecode(ai, i + (1 + k << 8) + j);
            j = j << 1 | l;
            if(k == l)
                continue;
            for(; j < 256; j = j + j | BitDecode(ai, i + j));
            break;
        } while(j < 256);
        return (byte)j;
    }

    int LzmaLenDecode(int ai[], int i, int j)
        throws IOException
    {
        if(BitDecode(ai, i + 0) == 0)
            return BitTreeDecode(ai, i + 2 + (j << 3), 3);
        if(BitDecode(ai, i + 1) == 0)
            return 8 + BitTreeDecode(ai, i + 130 + (j << 3), 3);
        else
            return 16 + BitTreeDecode(ai, i + 258, 8);
    }

    static final int kNumTopBits = 24;
    static final int kTopValue = 0x1000000;
    static final int kTopValueMask = 0xff000000;
    static final int kNumBitModelTotalBits = 11;
    static final int kBitModelTotal = 2048;
    static final int kNumMoveBits = 5;
    InputStream inStream;
    int Range;
    int Code;
    byte buffer[];
    int buffer_size;
    int buffer_ind;
    static final int kNumPosBitsMax = 4;
    static final int kNumPosStatesMax = 16;
    static final int kLenNumLowBits = 3;
    static final int kLenNumLowSymbols = 8;
    static final int kLenNumMidBits = 3;
    static final int kLenNumMidSymbols = 8;
    static final int kLenNumHighBits = 8;
    static final int kLenNumHighSymbols = 256;
    static final int LenChoice = 0;
    static final int LenChoice2 = 1;
    static final int LenLow = 2;
    static final int LenMid = 130;
    static final int LenHigh = 258;
    static final int kNumLenProbs = 514;
}
