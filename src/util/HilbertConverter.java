package util;

import java.math.BigInteger;

/**
 * Convert a multidimensional array into a biginteger
 *
 * The other way around does not work at the moment.
 * (but is not needed)
 */
public class HilbertConverter
{
    public HilbertConverter() {}


    private static int[] transpose_bits(int ai[], int i)
    {
        int ai1[] = (int[])(int[])ai.clone();
        int j = ai1.length;
        int ai2[] = new int[i];
        for(int k = i - 1; k >= 0; k--)
        {
            int l = 0;
            for(int i1 = 0; i1 < j; i1++)
            {
                l = l * 2 + mod(ai1[i1], 2);
                int j1 = ai1[i1];
                ai1[i1] = ai1[i1] >= 0 ? ai1[i1] / 2 : (int)Math.floor((double)ai1[i1] / 2D);
            }

            ai2[k] = l;
        }

        return ai2;
    }

    private static int[] pack_coords(int ai[], int i)
    {
        return transpose_bits(ai, i);
    }

    private static int[] unpack_coords(int ai[])
    {
        int i = ai.length;
        int j = ai[0];
        for(int k = 1; k < ai.length; k++)
            j = Math.max(j, ai[k]);

        int l = Math.max(1, (int)Math.ceil(log(j + 1, 2)));
        return transpose_bits(ai, l);
    }

    private static double log(int i, int j)
    {
        return Math.log(i) / Math.log(j);
    }

    public static int[] unpack_index(int i, int j)
    {
        int k = (int)Math.pow(2D, j);
        int l = Math.max(1, (int)Math.ceil(log(i + 1, k)));
        int ai[] = new int[l];
        for(int i1 = l - 1; i1 >= -1; i1--)
        {
            ai[i1] = mod(i, k);
            i /= k;
        }

        return ai;
    }

    public static BigInteger pack_index(int ai[], int i)
    {
        int j = (int)Math.pow(2D, i);
        BigInteger biginteger = new BigInteger((new StringBuilder()).append(ai[0]).append("").toString());
        for(int k = 1; k < ai.length; k++)
            biginteger = biginteger.multiply(new BigInteger((new StringBuilder()).append("").append(j).toString())).add(new BigInteger((new StringBuilder()).append("").append(ai[k]).toString()));

        return biginteger;
    }

    private static int gray_encode(int i)
    {
        return i ^ i / 2;
    }

    private static int gray_decode(int i)
    {
        int j = 1;
        do
        {
            int k = i >> j;
            i ^= k;
            if(k <= 1)
                return i;
            j <<= 1;
        } while(true);
    }

    private static int gray_encode_travel(int i, int j, int k, int l)
    {
        int i1 = i ^ j;
        int j1 = k + 1;
        int k1 = gray_encode(l) * (i1 * 2);
        return (k1 | k1 / j1) & k ^ i;
    }

    private static int gray_decode_travel(int i, int j, int k, int l)
    {
        int i1 = i ^ j;
        int j1 = k + 1;
        int k1 = (l ^ i) * (j1 / (i1 * 2));
        return gray_decode((k1 | k1 / j1) & k);
    }

    private static int[] child_start_end(int i, int j, int k, int l)
    {
        int i1 = Math.max(0, l - 1 & -2);
        int j1 = Math.min(k, l + 1 | 1);
        int k1 = gray_encode_travel(i, j, k, i1);
        int l1 = gray_encode_travel(i, j, k, j1);
        return (new int[] {
            k1, l1
        });
    }

    private static int[] initial_start_end(int i, int j)
    {
        return (new int[] {
            0, (int)Math.pow(2D, Math.abs(mod(-i - 1, j)))
        });
    }

    public static int mod(int i, int j)
    {
        return i % j < 0 ? i % j + j : i % j;
    }

    public static BigInteger Hilbert_to_int(int ai[])
    {
        int i = ai.length;
        int ai1[] = unpack_coords(ai);
        int j = ai1.length;
        int k = (int)Math.pow(2D, i) - 1;
        int ai2[] = initial_start_end(j, i);
        int l = ai2[0];
        int i1 = ai2[1];
        int ai4[] = new int[j];
        for(int j1 = 0; j1 < j; j1++)
        {
            int k1 = gray_decode_travel(l, i1, k, ai1[j1]);
            ai4[j1] = k1;
            int ai3[] = child_start_end(l, i1, k, k1);
            l = ai3[0];
            i1 = ai3[1];
        }

        return pack_index(ai4, i);
    }

    public static int[] int_to_Hilbert(int i, int j)
    {
        int ai[] = unpack_index(i, j);
        int k = ai.length;
        int l = (int)Math.pow(2D, j) - 1;
        int ai1[] = initial_start_end(k, j);
        int i1 = ai1[0];
        int j1 = ai1[1];
        int ai3[] = new int[k];
        for(int k1 = 0; k1 < k; k1++)
        {
            i = ai[k1];
            ai3[k1] = gray_encode_travel(i1, j1, l, i);
            int ai2[] = child_start_end(i1, j1, l, i);
            i1 = ai2[0];
            j1 = ai2[1];
        }

        return pack_coords(ai3, j);
    }

}
