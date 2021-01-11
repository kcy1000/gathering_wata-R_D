package geo2.lbsp.ible.okio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public interface BufferedSink extends Sink
{
    Buffer buffer();

    BufferedSink write(geo2.lbsp.ible.okio.ByteString var1) throws IOException;

    BufferedSink write(byte[] var1) throws IOException;

    BufferedSink write(byte[] var1, int var2, int var3) throws IOException;

    long writeAll(geo2.lbsp.ible.okio.Source var1) throws IOException;

    BufferedSink write(geo2.lbsp.ible.okio.Source var1, long var2) throws IOException;

    BufferedSink writeUtf8(String var1) throws IOException;

    BufferedSink writeString(String var1, Charset var2) throws IOException;

    BufferedSink writeByte(int var1) throws IOException;

    BufferedSink writeShort(int var1) throws IOException;

    BufferedSink writeShortLe(int var1) throws IOException;

    BufferedSink writeInt(int var1) throws IOException;

    BufferedSink writeIntLe(int var1) throws IOException;

    BufferedSink writeLong(long var1) throws IOException;

    BufferedSink writeLongLe(long var1) throws IOException;

    BufferedSink writeDecimalLong(long var1) throws IOException;

    BufferedSink writeHexadecimalUnsignedLong(long var1) throws IOException;

    BufferedSink emitCompleteSegments() throws IOException;

    BufferedSink emit() throws IOException;

    OutputStream outputStream();
}