package geo2.lbsp.ible.okio;

import java.io.UnsupportedEncodingException;

final class Base64 {
    private static final byte[] MAP = new byte[]{(byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70, (byte)71, (byte)72, (byte)73, (byte)74, (byte)75, (byte)76, (byte)77, (byte)78, (byte)79, (byte)80, (byte)81, (byte)82, (byte)83, (byte)84, (byte)85, (byte)86, (byte)87, (byte)88, (byte)89, (byte)90, (byte)97, (byte)98, (byte)99, (byte)100, (byte)101, (byte)102, (byte)103, (byte)104, (byte)105, (byte)106, (byte)107, (byte)108, (byte)109, (byte)110, (byte)111, (byte)112, (byte)113, (byte)114, (byte)115, (byte)116, (byte)117, (byte)118, (byte)119, (byte)120, (byte)121, (byte)122, (byte)48, (byte)49, (byte)50, (byte)51, (byte)52, (byte)53, (byte)54, (byte)55, (byte)56, (byte)57, (byte)43, (byte)47};
    private static final byte[] URL_MAP = new byte[]{(byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70, (byte)71, (byte)72, (byte)73, (byte)74, (byte)75, (byte)76, (byte)77, (byte)78, (byte)79, (byte)80, (byte)81, (byte)82, (byte)83, (byte)84, (byte)85, (byte)86, (byte)87, (byte)88, (byte)89, (byte)90, (byte)97, (byte)98, (byte)99, (byte)100, (byte)101, (byte)102, (byte)103, (byte)104, (byte)105, (byte)106, (byte)107, (byte)108, (byte)109, (byte)110, (byte)111, (byte)112, (byte)113, (byte)114, (byte)115, (byte)116, (byte)117, (byte)118, (byte)119, (byte)120, (byte)121, (byte)122, (byte)48, (byte)49, (byte)50, (byte)51, (byte)52, (byte)53, (byte)54, (byte)55, (byte)56, (byte)57, (byte)45, (byte)95};

    private Base64() {
    }

    public static byte[] decode(String in) {
        int limit;
        for(limit = in.length(); limit > 0; --limit) {
            char out = in.charAt(limit - 1);
            if(out != 61 && out != 10 && out != 13 && out != 32 && out != 9) {
                break;
            }
        }

        byte[] var9 = new byte[(int)((long)limit * 6L / 8L)];
        int outCount = 0;
        int inCount = 0;
        int word = 0;

        int lastWordChars;
        for(lastWordChars = 0; lastWordChars < limit; ++lastWordChars) {
            char prefix = in.charAt(lastWordChars);
            int bits;
            if(prefix >= 65 && prefix <= 90) {
                bits = prefix - 65;
            } else if(prefix >= 97 && prefix <= 122) {
                bits = prefix - 71;
            } else if(prefix >= 48 && prefix <= 57) {
                bits = prefix + 4;
            } else if(prefix != 43 && prefix != 45) {
                if(prefix != 47 && prefix != 95) {
                    if(prefix != 10 && prefix != 13 && prefix != 32 && prefix != 9) {
                        return null;
                    }
                    continue;
                }

                bits = 63;
            } else {
                bits = 62;
            }

            word = word << 6 | (byte)bits;
            ++inCount;
            if(inCount % 4 == 0) {
                var9[outCount++] = (byte)(word >> 16);
                var9[outCount++] = (byte)(word >> 8);
                var9[outCount++] = (byte)word;
            }
        }

        lastWordChars = inCount % 4;
        if(lastWordChars == 1) {
            return null;
        } else {
            if(lastWordChars == 2) {
                word <<= 12;
                var9[outCount++] = (byte)(word >> 16);
            } else if(lastWordChars == 3) {
                word <<= 6;
                var9[outCount++] = (byte)(word >> 16);
                var9[outCount++] = (byte)(word >> 8);
            }

            if(outCount == var9.length) {
                return var9;
            } else {
                byte[] var10 = new byte[outCount];
                System.arraycopy(var9, 0, var10, 0, outCount);
                return var10;
            }
        }
    }

    public static String encode(byte[] in) {
        return encode(in, MAP);
    }

    public static String encodeUrl(byte[] in) {
        return encode(in, URL_MAP);
    }

    private static String encode(byte[] in, byte[] map) {
        int length = (in.length + 2) * 4 / 3;
        byte[] out = new byte[length];
        int index = 0;
        int end = in.length - in.length % 3;

        for(int e = 0; e < end; e += 3) {
            out[index++] = map[(in[e] & 255) >> 2];
            out[index++] = map[(in[e] & 3) << 4 | (in[e + 1] & 255) >> 4];
            out[index++] = map[(in[e + 1] & 15) << 2 | (in[e + 2] & 255) >> 6];
            out[index++] = map[in[e + 2] & 63];
        }

        switch(in.length % 3) {
        case 1:
            out[index++] = map[(in[end] & 255) >> 2];
            out[index++] = map[(in[end] & 3) << 4];
            out[index++] = 61;
            out[index++] = 61;
            break;
        case 2:
            out[index++] = map[(in[end] & 255) >> 2];
            out[index++] = map[(in[end] & 3) << 4 | (in[end + 1] & 255) >> 4];
            out[index++] = map[(in[end + 1] & 15) << 2];
            out[index++] = 61;
        }

        try {
            return new String(out, 0, index, "US-ASCII");
        } catch (UnsupportedEncodingException var7) {
            throw new AssertionError(var7);
        }
    }
}