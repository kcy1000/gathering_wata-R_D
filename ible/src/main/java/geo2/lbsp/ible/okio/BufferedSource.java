package geo2.lbsp.ible.okio;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface BufferedSource extends Source
{
    Buffer buffer();

    boolean exhausted() throws IOException;

    void require(long var1) throws IOException;

    boolean request(long var1) throws IOException;

    byte readByte() throws IOException;

    short readShort() throws IOException;

    short readShortLe() throws IOException;

    int readInt() throws IOException;

    int readIntLe() throws IOException;

    long readLong() throws IOException;

    long readLongLe() throws IOException;

    long readDecimalLong() throws IOException;

    long readHexadecimalUnsignedLong() throws IOException;

    void skip(long var1) throws IOException;

    geo2.lbsp.ible.okio.ByteString readByteString() throws IOException;

    geo2.lbsp.ible.okio.ByteString readByteString(long var1) throws IOException;

    byte[] readByteArray() throws IOException;

    byte[] readByteArray(long var1) throws IOException;

    int read(byte[] var1) throws IOException;

    void readFully(byte[] var1) throws IOException;

    int read(byte[] var1, int var2, int var3) throws IOException;

    void readFully(Buffer var1, long var2) throws IOException;

    long readAll(geo2.lbsp.ible.okio.Sink var1) throws IOException;

    String readUtf8() throws IOException;

    String readUtf8(long var1) throws IOException;

    String readUtf8Line() throws IOException;

    String readUtf8LineStrict() throws IOException;

    String readString(Charset var1) throws IOException;

    String readString(long var1, Charset var3) throws IOException;

    long indexOf(byte var1) throws IOException;

    long indexOf(byte var1, long var2) throws IOException;

    long indexOfElement(geo2.lbsp.ible.okio.ByteString var1) throws IOException;

    long indexOfElement(geo2.lbsp.ible.okio.ByteString var1, long var2) throws IOException;

    InputStream inputStream();
}