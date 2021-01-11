package geo2.lbsp.ible.okio;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Buffer implements BufferedSource, geo2.lbsp.ible.okio.BufferedSink, Cloneable {
    private static final byte[] DIGITS = new byte[]{(byte)48, (byte)49, (byte)50, (byte)51, (byte)52, (byte)53, (byte)54, (byte)55, (byte)56, (byte)57, (byte)97, (byte)98, (byte)99, (byte)100, (byte)101, (byte)102};
    geo2.lbsp.ible.okio.Segment head;
    long size;

    public Buffer() {
    }

    public long size() {
        return this.size;
    }

    public Buffer buffer() {
        return this;
    }

    public OutputStream outputStream() {
        return new OutputStream() {
            public void write(int b) {
                Buffer.this.writeByte((byte)b);
            }

            public void write(byte[] data, int offset, int byteCount) {
                Buffer.this.write(data, offset, byteCount);
            }

            public void flush() {
            }

            public void close() {
            }

            public String toString() {
                return this + ".outputStream()";
            }
        };
    }

    public Buffer emitCompleteSegments() {
        return this;
    }

    public geo2.lbsp.ible.okio.BufferedSink emit() {
        return this;
    }

    public boolean exhausted() {
        return this.size == 0L;
    }

    public void require(long byteCount) throws EOFException {
        if(this.size < byteCount) {
            throw new EOFException();
        }
    }

    public boolean request(long byteCount) {
        return this.size >= byteCount;
    }

    public InputStream inputStream() {
        return new InputStream() {
            public int read() {
                return Buffer.this.size > 0L?Buffer.this.readByte() & 255:-1;
            }

            public int read(byte[] sink, int offset, int byteCount) {
                return Buffer.this.read(sink, offset, byteCount);
            }

            public int available() {
                return (int)Math.min(Buffer.this.size, 2147483647L);
            }

            public void close() {
            }

            public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }

    public Buffer copyTo(OutputStream out) throws IOException {
        return this.copyTo(out, 0L, this.size);
    }

    public Buffer copyTo(OutputStream out, long offset, long byteCount) throws IOException {
        if(out == null) {
            throw new IllegalArgumentException("out == null");
        } else {
            geo2.lbsp.ible.okio.Util.checkOffsetAndCount(this.size, offset, byteCount);
            if(byteCount == 0L) {
                return this;
            } else {
                geo2.lbsp.ible.okio.Segment s;
                for(s = this.head; offset >= (long)(s.limit - s.pos); s = s.next) {
                    offset -= (long)(s.limit - s.pos);
                }

                while(byteCount > 0L) {
                    int pos = (int)((long)s.pos + offset);
                    int toCopy = (int)Math.min((long)(s.limit - pos), byteCount);
                    out.write(s.data, pos, toCopy);
                    byteCount -= (long)toCopy;
                    offset = 0L;
                    s = s.next;
                }

                return this;
            }
        }
    }

    public Buffer copyTo(Buffer out, long offset, long byteCount) {
        if(out == null) {
            throw new IllegalArgumentException("out == null");
        } else {
            geo2.lbsp.ible.okio.Util.checkOffsetAndCount(this.size, offset, byteCount);
            if(byteCount == 0L) {
                return this;
            } else {
                out.size += byteCount;

                geo2.lbsp.ible.okio.Segment s;
                for(s = this.head; offset >= (long)(s.limit - s.pos); s = s.next) {
                    offset -= (long)(s.limit - s.pos);
                }

                while(byteCount > 0L) {
                    geo2.lbsp.ible.okio.Segment copy = new geo2.lbsp.ible.okio.Segment(s);
                    copy.pos = (int)((long)copy.pos + offset);
                    copy.limit = Math.min(copy.pos + (int)byteCount, copy.limit);
                    if(out.head == null) {
                        out.head = copy.next = copy.prev = copy;
                    } else {
                        out.head.prev.push(copy);
                    }

                    byteCount -= (long)(copy.limit - copy.pos);
                    offset = 0L;
                    s = s.next;
                }

                return this;
            }
        }
    }

    public Buffer writeTo(OutputStream out) throws IOException {
        return this.writeTo(out, this.size);
    }

    public Buffer writeTo(OutputStream out, long byteCount) throws IOException {
        if(out == null) {
            throw new IllegalArgumentException("out == null");
        } else {
            geo2.lbsp.ible.okio.Util.checkOffsetAndCount(this.size, 0L, byteCount);
            geo2.lbsp.ible.okio.Segment s = this.head;

            while(byteCount > 0L) {
                int toCopy = (int)Math.min(byteCount, (long)(s.limit - s.pos));
                out.write(s.data, s.pos, toCopy);
                s.pos += toCopy;
                this.size -= (long)toCopy;
                byteCount -= (long)toCopy;
                if(s.pos == s.limit) {
                    geo2.lbsp.ible.okio.Segment toRecycle = s;
                    this.head = s = s.pop();
                    geo2.lbsp.ible.okio.SegmentPool.recycle(toRecycle);
                }
            }

            return this;
        }
    }

    public Buffer readFrom(InputStream in) throws IOException {
        this.readFrom(in, 9223372036854775807L, true);
        return this;
    }

    public Buffer readFrom(InputStream in, long byteCount) throws IOException {
        if(byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else {
            this.readFrom(in, byteCount, false);
            return this;
        }
    }

    private void readFrom(InputStream in, long byteCount, boolean forever) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("in == null");
        } else {
            while(byteCount > 0L || forever) {
                geo2.lbsp.ible.okio.Segment tail = this.writableSegment(1);
                int maxToCopy = (int)Math.min(byteCount, (long)(2048 - tail.limit));
                int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
                if(bytesRead == -1) {
                    if(forever) {
                        return;
                    }

                    throw new EOFException();
                }

                tail.limit += bytesRead;
                this.size += (long)bytesRead;
                byteCount -= (long)bytesRead;
            }

        }
    }

    public long completeSegmentByteCount() {
        long result = this.size;
        if(result == 0L) {
            return 0L;
        } else {
            geo2.lbsp.ible.okio.Segment tail = this.head.prev;
            if(tail.limit < 2048 && tail.owner) {
                result -= (long)(tail.limit - tail.pos);
            }

            return result;
        }
    }

    public byte readByte() {
        if(this.size == 0L) {
            throw new IllegalStateException("size == 0");
        } else {
            geo2.lbsp.ible.okio.Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            byte[] data = segment.data;
            byte b = data[pos++];
            --this.size;
            if(pos == limit) {
                this.head = segment.pop();
                geo2.lbsp.ible.okio.SegmentPool.recycle(segment);
            } else {
                segment.pos = pos;
            }

            return b;
        }
    }

    public byte getByte(long pos) {
        geo2.lbsp.ible.okio.Util.checkOffsetAndCount(this.size, pos, 1L);
        geo2.lbsp.ible.okio.Segment s = this.head;

        while(true) {
            int segmentByteCount = s.limit - s.pos;
            if(pos < (long)segmentByteCount) {
                return s.data[s.pos + (int)pos];
            }

            pos -= (long)segmentByteCount;
            s = s.next;
        }
    }

    public short readShort() {
        if(this.size < 2L) {
            throw new IllegalStateException("size < 2: " + this.size);
        } else {
            geo2.lbsp.ible.okio.Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            if(limit - pos < 2) {
                int var6 = (this.readByte() & 255) << 8 | this.readByte() & 255;
                return (short)var6;
            } else {
                byte[] data = segment.data;
                int s = (data[pos++] & 255) << 8 | data[pos++] & 255;
                this.size -= 2L;
                if(pos == limit) {
                    this.head = segment.pop();
                    geo2.lbsp.ible.okio.SegmentPool.recycle(segment);
                } else {
                    segment.pos = pos;
                }

                return (short)s;
            }
        }
    }

    public int readInt() {
        if(this.size < 4L) {
            throw new IllegalStateException("size < 4: " + this.size);
        } else {
            geo2.lbsp.ible.okio.Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            if(limit - pos < 4) {
                return (this.readByte() & 255) << 24 | (this.readByte() & 255) << 16 | (this.readByte() & 255) << 8 | this.readByte() & 255;
            } else {
                byte[] data = segment.data;
                int i = (data[pos++] & 255) << 24 | (data[pos++] & 255) << 16 | (data[pos++] & 255) << 8 | data[pos++] & 255;
                this.size -= 4L;
                if(pos == limit) {
                    this.head = segment.pop();
                    geo2.lbsp.ible.okio.SegmentPool.recycle(segment);
                } else {
                    segment.pos = pos;
                }

                return i;
            }
        }
    }

    public long readLong() {
        if(this.size < 8L) {
            throw new IllegalStateException("size < 8: " + this.size);
        } else {
            geo2.lbsp.ible.okio.Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            if(limit - pos < 8) {
                return ((long)this.readInt() & 4294967295L) << 32 | (long)this.readInt() & 4294967295L;
            } else {
                byte[] data = segment.data;
                long v = ((long)data[pos++] & 255L) << 56 | ((long)data[pos++] & 255L) << 48 | ((long)data[pos++] & 255L) << 40 | ((long)data[pos++] & 255L) << 32 | ((long)data[pos++] & 255L) << 24 | ((long)data[pos++] & 255L) << 16 | ((long)data[pos++] & 255L) << 8 | (long)data[pos++] & 255L;
                this.size -= 8L;
                if(pos == limit) {
                    this.head = segment.pop();
                    geo2.lbsp.ible.okio.SegmentPool.recycle(segment);
                } else {
                    segment.pos = pos;
                }

                return v;
            }
        }
    }

    public short readShortLe() {
        return geo2.lbsp.ible.okio.Util.reverseBytesShort(this.readShort());
    }

    public int readIntLe() {
        return geo2.lbsp.ible.okio.Util.reverseBytesInt(this.readInt());
    }

    public long readLongLe() {
        return geo2.lbsp.ible.okio.Util.reverseBytesLong(this.readLong());
    }

    public long readDecimalLong() {
        if(this.size == 0L) {
            throw new IllegalStateException("size == 0");
        } else {
            long value = 0L;
            int seen = 0;
            boolean negative = false;
            boolean done = false;
            long overflowZone = -922337203685477580L;
            long overflowDigit = -7L;

            while(true) {
                geo2.lbsp.ible.okio.Segment segment = this.head;
                byte[] data = segment.data;
                int pos = segment.pos;
                int limit = segment.limit;

                while(true) {
                    label72: {
                        if(pos < limit) {
                            byte b = data[pos];
                            if(b >= 48 && b <= 57) {
                                int digit = 48 - b;
                                if(value < overflowZone || value == overflowZone && (long)digit < overflowDigit) {
                                    Buffer buffer = (new Buffer()).writeDecimalLong(value).writeByte(b);
                                    if(!negative) {
                                        buffer.readByte();
                                    }

                                    throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                                }

                                value *= 10L;
                                value += (long)digit;
                                break label72;
                            }

                            if(b == 45 && seen == 0) {
                                negative = true;
                                --overflowDigit;
                                break label72;
                            }

                            if(seen == 0) {
                                throw new NumberFormatException("Expected leading [0-9] or \'-\' character but was 0x" + Integer.toHexString(b));
                            }

                            done = true;
                        }

                        if(pos == limit) {
                            this.head = segment.pop();
                            geo2.lbsp.ible.okio.SegmentPool.recycle(segment);
                        } else {
                            segment.pos = pos;
                        }

                        if(!done && this.head != null) {
                            break;
                        }

                        this.size -= (long)seen;
                        return negative?value:-value;
                    }

                    ++pos;
                    ++seen;
                }
            }
        }
    }

    public long readHexadecimalUnsignedLong() {
        if(this.size == 0L) {
            throw new IllegalStateException("size == 0");
        } else {
            long value = 0L;
            int seen = 0;
            boolean done = false;

            do {
                geo2.lbsp.ible.okio.Segment segment = this.head;
                byte[] data = segment.data;
                int pos = segment.pos;

                int limit;
                for(limit = segment.limit; pos < limit; ++seen) {
                    byte b = data[pos];
                    int digit;
                    if(b >= 48 && b <= 57) {
                        digit = b - 48;
                    } else if(b >= 97 && b <= 102) {
                        digit = b - 97 + 10;
                    } else {
                        if(b < 65 || b > 70) {
                            if(seen == 0) {
                                throw new NumberFormatException("Expected leading [0-9a-fA-F] character but was 0x" + Integer.toHexString(b));
                            }

                            done = true;
                            break;
                        }

                        digit = b - 65 + 10;
                    }

                    if((value & -1152921504606846976L) != 0L) {
                        Buffer buffer = (new Buffer()).writeHexadecimalUnsignedLong(value).writeByte(b);
                        throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                    }

                    value <<= 4;
                    value |= (long)digit;
                    ++pos;
                }

                if(pos == limit) {
                    this.head = segment.pop();
                    geo2.lbsp.ible.okio.SegmentPool.recycle(segment);
                } else {
                    segment.pos = pos;
                }
            } while(!done && this.head != null);

            this.size -= (long)seen;
            return value;
        }
    }

    public geo2.lbsp.ible.okio.ByteString readByteString() {
        return new geo2.lbsp.ible.okio.ByteString(this.readByteArray());
    }

    public geo2.lbsp.ible.okio.ByteString readByteString(long byteCount) throws EOFException {
        return new geo2.lbsp.ible.okio.ByteString(this.readByteArray(byteCount));
    }

    public void readFully(Buffer sink, long byteCount) throws EOFException {
        if(this.size < byteCount) {
            sink.write(this, this.size);
            throw new EOFException();
        } else {
            sink.write(this, byteCount);
        }
    }

    public long readAll(geo2.lbsp.ible.okio.Sink sink) throws IOException {
        long byteCount = this.size;
        if(byteCount > 0L) {
            sink.write(this, byteCount);
        }

        return byteCount;
    }

    public String readUtf8() {
        try {
            return this.readString(this.size, geo2.lbsp.ible.okio.Util.UTF_8);
        } catch (EOFException var2) {
            throw new AssertionError(var2);
        }
    }

    public String readUtf8(long byteCount) throws EOFException {
        return this.readString(byteCount, geo2.lbsp.ible.okio.Util.UTF_8);
    }

    public String readString(Charset charset) {
        try {
            return this.readString(this.size, charset);
        } catch (EOFException var3) {
            throw new AssertionError(var3);
        }
    }

    public String readString(long byteCount, Charset charset) throws EOFException {
        geo2.lbsp.ible.okio.Util.checkOffsetAndCount(this.size, 0L, byteCount);
        if(charset == null) {
            throw new IllegalArgumentException("charset == null");
        } else if(byteCount > 2147483647L) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        } else if(byteCount == 0L) {
            return "";
        } else {
            geo2.lbsp.ible.okio.Segment s = this.head;
            if((long)s.pos + byteCount > (long)s.limit) {
                return new String(this.readByteArray(byteCount), charset);
            } else {
                String result = new String(s.data, s.pos, (int)byteCount, charset);
                s.pos = (int)((long)s.pos + byteCount);
                this.size -= byteCount;
                if(s.pos == s.limit) {
                    this.head = s.pop();
                    geo2.lbsp.ible.okio.SegmentPool.recycle(s);
                }

                return result;
            }
        }
    }

    public String readUtf8Line() throws EOFException {
        long newline = this.indexOf((byte)10);
        return newline == -1L?(this.size != 0L?this.readUtf8(this.size):null):this.readUtf8Line(newline);
    }

    public String readUtf8LineStrict() throws EOFException {
        long newline = this.indexOf((byte)10);
        if(newline == -1L) {
            Buffer data = new Buffer();
            this.copyTo(data, 0L, Math.min(32L, this.size));
            throw new EOFException("\\n not found: size=" + this.size() + " content=" + data.readByteString().hex() + "...");
        } else {
            return this.readUtf8Line(newline);
        }
    }

    String readUtf8Line(long newline) throws EOFException {
        String result;
        if(newline > 0L && this.getByte(newline - 1L) == 13) {
            result = this.readUtf8(newline - 1L);
            this.skip(2L);
            return result;
        } else {
            result = this.readUtf8(newline);
            this.skip(1L);
            return result;
        }
    }

    public byte[] readByteArray() {
        try {
            return this.readByteArray(this.size);
        } catch (EOFException var2) {
            throw new AssertionError(var2);
        }
    }

    public byte[] readByteArray(long byteCount) throws EOFException {
        geo2.lbsp.ible.okio.Util.checkOffsetAndCount(this.size, 0L, byteCount);
        if(byteCount > 2147483647L) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        } else {
            byte[] result = new byte[(int)byteCount];
            this.readFully(result);
            return result;
        }
    }

    public int read(byte[] sink) {
        return this.read(sink, 0, sink.length);
    }

    public void readFully(byte[] sink) throws EOFException {
        int read;
        for(int offset = 0; offset < sink.length; offset += read) {
            read = this.read(sink, offset, sink.length - offset);
            if(read == -1) {
                throw new EOFException();
            }
        }

    }

    public int read(byte[] sink, int offset, int byteCount) {
        geo2.lbsp.ible.okio.Util.checkOffsetAndCount((long)sink.length, (long)offset, (long)byteCount);
        geo2.lbsp.ible.okio.Segment s = this.head;
        if(s == null) {
            return -1;
        } else {
            int toCopy = Math.min(byteCount, s.limit - s.pos);
            System.arraycopy(s.data, s.pos, sink, offset, toCopy);
            s.pos += toCopy;
            this.size -= (long)toCopy;
            if(s.pos == s.limit) {
                this.head = s.pop();
                geo2.lbsp.ible.okio.SegmentPool.recycle(s);
            }

            return toCopy;
        }
    }

    public void clear() {
        try {
            this.skip(this.size);
        } catch (EOFException var2) {
            throw new AssertionError(var2);
        }
    }

    public void skip(long byteCount) throws EOFException {
        while(byteCount > 0L) {
            if(this.head == null) {
                throw new EOFException();
            }

            int toSkip = (int)Math.min(byteCount, (long)(this.head.limit - this.head.pos));
            this.size -= (long)toSkip;
            byteCount -= (long)toSkip;
            this.head.pos += toSkip;
            if(this.head.pos == this.head.limit) {
                geo2.lbsp.ible.okio.Segment toRecycle = this.head;
                this.head = toRecycle.pop();
                geo2.lbsp.ible.okio.SegmentPool.recycle(toRecycle);
            }
        }

    }

    public Buffer write(geo2.lbsp.ible.okio.ByteString byteString) {
        if(byteString == null) {
            throw new IllegalArgumentException("byteString == null");
        } else {
            byteString.write(this);
            return this;
        }
    }

    public Buffer writeUtf8(String string) {
        if(string == null) {
            throw new IllegalArgumentException("string == null");
        } else {
            int i = 0;
            int length = string.length();

            while(true) {
                while(i < length) {
                    char c = string.charAt(i);
                    if(c < 128) {
                        geo2.lbsp.ible.okio.Segment var10 = this.writableSegment(1);
                        byte[] var11 = var10.data;
                        int segmentOffset = var10.limit - i;
                        int runLimit = Math.min(length, 2048 - segmentOffset);

                        for(var11[segmentOffset + i++] = (byte)c; i < runLimit; var11[segmentOffset + i++] = (byte)c) {
                            c = string.charAt(i);
                            if(c >= 128) {
                                break;
                            }
                        }

                        int runSize = i + segmentOffset - var10.limit;
                        var10.limit += runSize;
                        this.size += (long)runSize;
                    } else if(c < 2048) {
                        this.writeByte(c >> 6 | 192);
                        this.writeByte(c & 63 | 128);
                        ++i;
                    } else if(c >= '\ud800' && c <= '\udfff') {
                        char low = i + 1 < length?string.charAt(i + 1):0;
                        if(c <= '\udbff' && low >= '\udc00' && low <= '\udfff') {
                            int codePoint = 65536 + ((c & -55297) << 10 | low & -56321);
                            this.writeByte(codePoint >> 18 | 240);
                            this.writeByte(codePoint >> 12 & 63 | 128);
                            this.writeByte(codePoint >> 6 & 63 | 128);
                            this.writeByte(codePoint & 63 | 128);
                            i += 2;
                        } else {
                            this.writeByte(63);
                            ++i;
                        }
                    } else {
                        this.writeByte(c >> 12 | 224);
                        this.writeByte(c >> 6 & 63 | 128);
                        this.writeByte(c & 63 | 128);
                        ++i;
                    }
                }

                return this;
            }
        }
    }

    public Buffer writeString(String string, Charset charset) {
        if(string == null) {
            throw new IllegalArgumentException("string == null");
        } else if(charset == null) {
            throw new IllegalArgumentException("charset == null");
        } else if(charset.equals(geo2.lbsp.ible.okio.Util.UTF_8)) {
            return this.writeUtf8(string);
        } else {
            byte[] data = string.getBytes(charset);
            return this.write(data, 0, data.length);
        }
    }

    public Buffer write(byte[] source) {
        if(source == null) {
            throw new IllegalArgumentException("source == null");
        } else {
            return this.write(source, 0, source.length);
        }
    }

    public Buffer write(byte[] source, int offset, int byteCount) {
        if(source == null) {
            throw new IllegalArgumentException("source == null");
        } else {
            geo2.lbsp.ible.okio.Util.checkOffsetAndCount((long)source.length, (long)offset, (long)byteCount);

            geo2.lbsp.ible.okio.Segment tail;
            int toCopy;
            for(int limit = offset + byteCount; offset < limit; tail.limit += toCopy) {
                tail = this.writableSegment(1);
                toCopy = Math.min(limit - offset, 2048 - tail.limit);
                System.arraycopy(source, offset, tail.data, tail.limit, toCopy);
                offset += toCopy;
            }

            this.size += (long)byteCount;
            return this;
        }
    }

    public long writeAll(geo2.lbsp.ible.okio.Source source) throws IOException {
        if(source == null) {
            throw new IllegalArgumentException("source == null");
        } else {
            long totalBytesRead;
            long readCount;
            for(totalBytesRead = 0L; (readCount = source.read(this, 2048L)) != -1L; totalBytesRead += readCount) {
            }

            return totalBytesRead;
        }
    }

    public geo2.lbsp.ible.okio.BufferedSink write(geo2.lbsp.ible.okio.Source source, long byteCount) throws IOException {
        while(byteCount > 0L) {
            long read = source.read(this, byteCount);
            if(read == -1L) {
                throw new EOFException();
            }

            byteCount -= read;
        }

        return this;
    }

    public Buffer writeByte(int b) {
        geo2.lbsp.ible.okio.Segment tail = this.writableSegment(1);
        tail.data[tail.limit++] = (byte)b;
        ++this.size;
        return this;
    }

    public Buffer writeShort(int s) {
        geo2.lbsp.ible.okio.Segment tail = this.writableSegment(2);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte)(s >>> 8 & 255);
        data[limit++] = (byte)(s & 255);
        tail.limit = limit;
        this.size += 2L;
        return this;
    }

    public Buffer writeShortLe(int s) {
        return this.writeShort(geo2.lbsp.ible.okio.Util.reverseBytesShort((short)s));
    }

    public Buffer writeInt(int i) {
        geo2.lbsp.ible.okio.Segment tail = this.writableSegment(4);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte)(i >>> 24 & 255);
        data[limit++] = (byte)(i >>> 16 & 255);
        data[limit++] = (byte)(i >>> 8 & 255);
        data[limit++] = (byte)(i & 255);
        tail.limit = limit;
        this.size += 4L;
        return this;
    }

    public Buffer writeIntLe(int i) {
        return this.writeInt(geo2.lbsp.ible.okio.Util.reverseBytesInt(i));
    }

    public Buffer writeLong(long v) {
        geo2.lbsp.ible.okio.Segment tail = this.writableSegment(8);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte)((int)(v >>> 56 & 255L));
        data[limit++] = (byte)((int)(v >>> 48 & 255L));
        data[limit++] = (byte)((int)(v >>> 40 & 255L));
        data[limit++] = (byte)((int)(v >>> 32 & 255L));
        data[limit++] = (byte)((int)(v >>> 24 & 255L));
        data[limit++] = (byte)((int)(v >>> 16 & 255L));
        data[limit++] = (byte)((int)(v >>> 8 & 255L));
        data[limit++] = (byte)((int)(v & 255L));
        tail.limit = limit;
        this.size += 8L;
        return this;
    }

    public Buffer writeLongLe(long v) {
        return this.writeLong(geo2.lbsp.ible.okio.Util.reverseBytesLong(v));
    }

    public Buffer writeDecimalLong(long v) {
        if(v == 0L) {
            return this.writeByte(48);
        } else {
            boolean negative = false;
            if(v < 0L) {
                v = -v;
                if(v < 0L) {
                    return this.writeUtf8("-9223372036854775808");
                }

                negative = true;
            }

            int width = v < 100000000L?(v < 10000L?(v < 100L?(v < 10L?1:2):(v < 1000L?3:4)):(v < 1000000L?(v < 100000L?5:6):(v < 10000000L?7:8))):(v < 1000000000000L?(v < 10000000000L?(v < 1000000000L?9:10):(v < 100000000000L?11:12)):(v < 1000000000000000L?(v < 10000000000000L?13:(v < 100000000000000L?14:15)):(v < 100000000000000000L?(v < 10000000000000000L?16:17):(v < 1000000000000000000L?18:19))));
            if(negative) {
                ++width;
            }

            geo2.lbsp.ible.okio.Segment tail = this.writableSegment(width);
            byte[] data = tail.data;

            int pos;
            for(pos = tail.limit + width; v != 0L; v /= 10L) {
                int digit = (int)(v % 10L);
                --pos;
                data[pos] = DIGITS[digit];
            }

            if(negative) {
                --pos;
                data[pos] = 45;
            }

            tail.limit += width;
            this.size += (long)width;
            return this;
        }
    }

    public Buffer writeHexadecimalUnsignedLong(long v) {
        if(v == 0L) {
            return this.writeByte(48);
        } else {
            int width = Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4 + 1;
            geo2.lbsp.ible.okio.Segment tail = this.writableSegment(width);
            byte[] data = tail.data;
            int pos = tail.limit + width - 1;

            for(int start = tail.limit; pos >= start; --pos) {
                data[pos] = DIGITS[(int)(v & 15L)];
                v >>>= 4;
            }

            tail.limit += width;
            this.size += (long)width;
            return this;
        }
    }

    geo2.lbsp.ible.okio.Segment writableSegment(int minimumCapacity) {
        if(minimumCapacity >= 1 && minimumCapacity <= 2048) {
            if(this.head == null) {
                this.head = geo2.lbsp.ible.okio.SegmentPool.take();
                return this.head.next = this.head.prev = this.head;
            } else {
                geo2.lbsp.ible.okio.Segment tail = this.head.prev;
                if(tail.limit + minimumCapacity > 2048 || !tail.owner) {
                    tail = tail.push(geo2.lbsp.ible.okio.SegmentPool.take());
                }

                return tail;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void write(Buffer source, long byteCount) {
        if(source == null) {
            throw new IllegalArgumentException("source == null");
        } else if(source == this) {
            throw new IllegalArgumentException("source == this");
        } else {
            geo2.lbsp.ible.okio.Util.checkOffsetAndCount(source.size, 0L, byteCount);

            while(byteCount > 0L) {
                geo2.lbsp.ible.okio.Segment segmentToMove;
                if(byteCount < (long)(source.head.limit - source.head.pos)) {
                    segmentToMove = this.head != null?this.head.prev:null;
                    if(segmentToMove != null && segmentToMove.owner && byteCount + (long)segmentToMove.limit - (long)(segmentToMove.shared?0:segmentToMove.pos) <= 2048L) {
                        source.head.writeTo(segmentToMove, (int)byteCount);
                        source.size -= byteCount;
                        this.size += byteCount;
                        return;
                    }

                    source.head = source.head.split((int)byteCount);
                }

                segmentToMove = source.head;
                long movedByteCount = (long)(segmentToMove.limit - segmentToMove.pos);
                source.head = segmentToMove.pop();
                if(this.head == null) {
                    this.head = segmentToMove;
                    this.head.next = this.head.prev = this.head;
                } else {
                    geo2.lbsp.ible.okio.Segment tail = this.head.prev;
                    tail = tail.push(segmentToMove);
                    tail.compact();
                }

                source.size -= movedByteCount;
                this.size += movedByteCount;
                byteCount -= movedByteCount;
            }

        }
    }

    public long read(Buffer sink, long byteCount) {
        if(sink == null) {
            throw new IllegalArgumentException("sink == null");
        } else if(byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if(this.size == 0L) {
            return -1L;
        } else {
            if(byteCount > this.size) {
                byteCount = this.size;
            }

            sink.write(this, byteCount);
            return byteCount;
        }
    }

    public long indexOf(byte b) {
        return this.indexOf(b, 0L);
    }

    public long indexOf(byte b, long fromIndex) {
        if(fromIndex < 0L) {
            throw new IllegalArgumentException("fromIndex < 0");
        } else {
            geo2.lbsp.ible.okio.Segment s = this.head;
            if(s == null) {
                return -1L;
            } else {
                long offset = 0L;

                do {
                    int segmentByteCount = s.limit - s.pos;
                    if(fromIndex >= (long)segmentByteCount) {
                        fromIndex -= (long)segmentByteCount;
                    } else {
                        byte[] data = s.data;
                        long pos = (long)s.pos + fromIndex;

                        for(long limit = (long)s.limit; pos < limit; ++pos) {
                            if(data[(int)pos] == b) {
                                return offset + pos - (long)s.pos;
                            }
                        }

                        fromIndex = 0L;
                    }

                    offset += (long)segmentByteCount;
                    s = s.next;
                } while(s != this.head);

                return -1L;
            }
        }
    }

    public long indexOfElement(geo2.lbsp.ible.okio.ByteString targetBytes) {
        return this.indexOfElement(targetBytes, 0L);
    }

    public long indexOfElement(geo2.lbsp.ible.okio.ByteString targetBytes, long fromIndex) {
        if(fromIndex < 0L) {
            throw new IllegalArgumentException("fromIndex < 0");
        } else {
            geo2.lbsp.ible.okio.Segment s = this.head;
            if(s == null) {
                return -1L;
            } else {
                long offset = 0L;
                byte[] toFind = targetBytes.toByteArray();

                do {
                    int segmentByteCount = s.limit - s.pos;
                    if(fromIndex >= (long)segmentByteCount) {
                        fromIndex -= (long)segmentByteCount;
                    } else {
                        byte[] data = s.data;
                        long pos = (long)s.pos + fromIndex;
                        long limit = (long)s.limit;

                        while(true) {
                            if(pos >= limit) {
                                fromIndex = 0L;
                                break;
                            }

                            byte b = data[(int)pos];
                            byte[] arr$ = toFind;
                            int len$ = toFind.length;

                            for(int i$ = 0; i$ < len$; ++i$) {
                                byte targetByte = arr$[i$];
                                if(b == targetByte) {
                                    return offset + pos - (long)s.pos;
                                }
                            }

                            ++pos;
                        }
                    }

                    offset += (long)segmentByteCount;
                    s = s.next;
                } while(s != this.head);

                return -1L;
            }
        }
    }

    public void flush() {
    }

    public void close() {
    }

    public geo2.lbsp.ible.okio.Timeout timeout() {
        return geo2.lbsp.ible.okio.Timeout.NONE;
    }

    List<Integer> segmentSizes() {
        if(this.head == null) {
            return Collections.emptyList();
        } else {
            ArrayList result = new ArrayList();
            result.add(Integer.valueOf(this.head.limit - this.head.pos));

            for(geo2.lbsp.ible.okio.Segment s = this.head.next; s != this.head; s = s.next) {
                result.add(Integer.valueOf(s.limit - s.pos));
            }

            return result;
        }
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof Buffer)) {
            return false;
        } else {
            Buffer that = (Buffer)o;
            if(this.size != that.size) {
                return false;
            } else if(this.size == 0L) {
                return true;
            } else {
                geo2.lbsp.ible.okio.Segment sa = this.head;
                geo2.lbsp.ible.okio.Segment sb = that.head;
                int posA = sa.pos;
                int posB = sb.pos;

                long count;
                for(long pos = 0L; pos < this.size; pos += count) {
                    count = (long)Math.min(sa.limit - posA, sb.limit - posB);

                    for(int i = 0; (long)i < count; ++i) {
                        if(sa.data[posA++] != sb.data[posB++]) {
                            return false;
                        }
                    }

                    if(posA == sa.limit) {
                        sa = sa.next;
                        posA = sa.pos;
                    }

                    if(posB == sb.limit) {
                        sb = sb.next;
                        posB = sb.pos;
                    }
                }

                return true;
            }
        }
    }

    public int hashCode() {
        geo2.lbsp.ible.okio.Segment s = this.head;
        if(s == null) {
            return 0;
        } else {
            int result = 1;

            do {
                int pos = s.pos;

                for(int limit = s.limit; pos < limit; ++pos) {
                    result = 31 * result + s.data[pos];
                }

                s = s.next;
            } while(s != this.head);

            return result;
        }
    }

    public String toString() {
        if(this.size == 0L) {
            return "Buffer[size=0]";
        } else if(this.size <= 16L) {
            geo2.lbsp.ible.okio.ByteString e1 = this.clone().readByteString();
            return String.format("Buffer[size=%s data=%s]", Long.valueOf(this.size), e1.hex());
        } else {
            try {
                MessageDigest e = MessageDigest.getInstance("MD5");
                e.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);

                for(geo2.lbsp.ible.okio.Segment s = this.head.next; s != this.head; s = s.next) {
                    e.update(s.data, s.pos, s.limit - s.pos);
                }

                return String.format("Buffer[size=%s md5=%s]", Long.valueOf(this.size), ByteString.of(e.digest()).hex());
            } catch (NoSuchAlgorithmException var3) {
                throw new AssertionError();
            }
        }
    }

    public Buffer clone() {
        Buffer result = new Buffer();
        if(this.size == 0L) {
            return result;
        } else {
            result.head = new geo2.lbsp.ible.okio.Segment(this.head);
            result.head.next = result.head.prev = result.head;

            for(geo2.lbsp.ible.okio.Segment s = this.head.next; s != this.head; s = s.next) {
                result.head.prev.push(new geo2.lbsp.ible.okio.Segment(s));
            }

            result.size = this.size;
            return result;
        }
    }

    public geo2.lbsp.ible.okio.ByteString snapshot() {
        if(this.size > 2147483647L) {
            throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + this.size);
        } else {
            return this.snapshot((int)this.size);
        }
    }

    public geo2.lbsp.ible.okio.ByteString snapshot(int byteCount) {
        return byteCount == 0? ByteString.EMPTY:new SegmentedByteString(this, byteCount);
    }
}
