package geo2.lbsp.ible.okio;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ByteString implements Serializable {
    static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final long serialVersionUID = 1L;
    public static final ByteString EMPTY = of();
    final byte[] data;
    transient int hashCode;
    transient String utf8;

    ByteString(byte[] data) {
        this.data = data;
    }

    public static ByteString of(byte... data) {
        if(data == null) {
            throw new IllegalArgumentException("data == null");
        } else {
            return new ByteString(data.clone());
        }
    }

    public static ByteString of(byte[] data, int offset, int byteCount) {
        if(data == null) {
            throw new IllegalArgumentException("data == null");
        } else {
            Util.checkOffsetAndCount((long)data.length, (long)offset, (long)byteCount);
            byte[] copy = new byte[byteCount];
            System.arraycopy(data, offset, copy, 0, byteCount);
            return new ByteString(copy);
        }
    }

    public static ByteString encodeUtf8(String s) {
        if(s == null) {
            throw new IllegalArgumentException("s == null");
        } else {
            ByteString byteString = new ByteString(s.getBytes(Util.UTF_8));
            byteString.utf8 = s;
            return byteString;
        }
    }

    public String utf8() {
        String result = this.utf8;
        return result != null?result:(this.utf8 = new String(this.data, Util.UTF_8));
    }

    public String base64() {
        return Base64.encode(this.data);
    }

    public ByteString md5() {
        return this.digest("MD5");
    }

    public ByteString sha256() {
        return this.digest("SHA-256");
    }

    private ByteString digest(String digest) {
        try {
            return of(MessageDigest.getInstance(digest).digest(this.data));
        } catch (NoSuchAlgorithmException var3) {
            throw new AssertionError(var3);
        }
    }

    public String base64Url() {
        return Base64.encodeUrl(this.data);
    }

    public static ByteString decodeBase64(String base64) {
        if(base64 == null) {
            throw new IllegalArgumentException("base64 == null");
        } else {
            byte[] decoded = Base64.decode(base64);
            return decoded != null?new ByteString(decoded):null;
        }
    }

    public String hex() {
        char[] result = new char[this.data.length * 2];
        int c = 0;
        byte[] arr$ = this.data;
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            byte b = arr$[i$];
            result[c++] = HEX_DIGITS[b >> 4 & 15];
            result[c++] = HEX_DIGITS[b & 15];
        }

        return new String(result);
    }

    public static ByteString decodeHex(String hex) {
        if(hex == null) {
            throw new IllegalArgumentException("hex == null");
        } else if(hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Unexpected hex string: " + hex);
        } else {
            byte[] result = new byte[hex.length() / 2];

            for(int i = 0; i < result.length; ++i) {
                int d1 = decodeHexDigit(hex.charAt(i * 2)) << 4;
                int d2 = decodeHexDigit(hex.charAt(i * 2 + 1));
                result[i] = (byte)(d1 + d2);
            }

            return of(result);
        }
    }

    private static int decodeHexDigit(char c) {
        if(c >= 48 && c <= 57) {
            return c - 48;
        } else if(c >= 97 && c <= 102) {
            return c - 97 + 10;
        } else if(c >= 65 && c <= 70) {
            return c - 65 + 10;
        } else {
            throw new IllegalArgumentException("Unexpected hex digit: " + c);
        }
    }

    public static ByteString read(InputStream in, int byteCount) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("in == null");
        } else if(byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else {
            byte[] result = new byte[byteCount];

            int read;
            for(int offset = 0; offset < byteCount; offset += read) {
                read = in.read(result, offset, byteCount - offset);
                if(read == -1) {
                    throw new EOFException();
                }
            }

            return new ByteString(result);
        }
    }

    public ByteString toAsciiLowercase() {
        for(int i = 0; i < this.data.length; ++i) {
            byte c = this.data[i];
            if(c >= 65 && c <= 90) {
                byte[] lowercase = this.data.clone();

                for(lowercase[i++] = (byte)(c - -32); i < lowercase.length; ++i) {
                    c = lowercase[i];
                    if(c >= 65 && c <= 90) {
                        lowercase[i] = (byte)(c - -32);
                    }
                }

                return new ByteString(lowercase);
            }
        }

        return this;
    }

    public ByteString toAsciiUppercase() {
        for(int i = 0; i < this.data.length; ++i) {
            byte c = this.data[i];
            if(c >= 97 && c <= 122) {
                byte[] lowercase = this.data.clone();

                for(lowercase[i++] = (byte)(c - 32); i < lowercase.length; ++i) {
                    c = lowercase[i];
                    if(c >= 97 && c <= 122) {
                        lowercase[i] = (byte)(c - 32);
                    }
                }

                return new ByteString(lowercase);
            }
        }

        return this;
    }

    public ByteString substring(int beginIndex) {
        return this.substring(beginIndex, this.data.length);
    }

    public ByteString substring(int beginIndex, int endIndex) {
        if(beginIndex < 0) {
            throw new IllegalArgumentException("beginIndex < 0");
        } else if(endIndex > this.data.length) {
            throw new IllegalArgumentException("endIndex > length(" + this.data.length + ")");
        } else {
            int subLen = endIndex - beginIndex;
            if(subLen < 0) {
                throw new IllegalArgumentException("endIndex < beginIndex");
            } else if(beginIndex == 0 && endIndex == this.data.length) {
                return this;
            } else {
                byte[] copy = new byte[subLen];
                System.arraycopy(this.data, beginIndex, copy, 0, subLen);
                return new ByteString(copy);
            }
        }
    }

    public byte getByte(int pos) {
        return this.data[pos];
    }

    public int size() {
        return this.data.length;
    }

    public byte[] toByteArray() {
        return this.data.clone();
    }

    public void write(OutputStream out) throws IOException {
        if(out == null) {
            throw new IllegalArgumentException("out == null");
        } else {
            out.write(this.data);
        }
    }

    void write(Buffer buffer) {
        buffer.write(this.data, 0, this.data.length);
    }

    public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
        return other.rangeEquals(otherOffset, this.data, offset, byteCount);
    }

    public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
        return offset <= this.data.length - byteCount && otherOffset <= other.length - byteCount && Util.arrayRangeEquals(this.data, offset, other, otherOffset, byteCount);
    }

    public boolean equals(Object o) {
        return o == this || o instanceof ByteString && ((ByteString) o).size() == this.data.length && ((ByteString) o).rangeEquals(0, this.data, 0, this.data.length);
    }

    public int hashCode() {
        int result = this.hashCode;
        return result != 0?result:(this.hashCode = Arrays.hashCode(this.data));
    }

    public String toString() {
        return this.data.length == 0?"ByteString[size=0]":(this.data.length <= 16?String.format("ByteString[size=%s data=%s]", Integer.valueOf(this.data.length), this.hex()):String.format("ByteString[size=%s md5=%s]", Integer.valueOf(this.data.length), this.md5().hex()));
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int dataLength = in.readInt();
        ByteString byteString = read(in, dataLength);

        try {
            Field e = ByteString.class.getDeclaredField("data");
            e.setAccessible(true);
            e.set(this, byteString.data);
        } catch (NoSuchFieldException var5) {
            throw new AssertionError();
        } catch (IllegalAccessException var6) {
            throw new AssertionError();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.data.length);
        out.write(this.data);
    }
}