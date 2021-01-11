package geo2.lbsp.ible.okio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

final class SegmentedByteString extends geo2.lbsp.ible.okio.ByteString
{
    final transient byte[][] segments;
    final transient int[] directory;

    SegmentedByteString(Buffer buffer, int byteCount) {
    	super(null);
        geo2.lbsp.ible.okio.Util.checkOffsetAndCount(buffer.size, 0L, (long)byteCount);
        int offset = 0;
        int segmentCount = 0;

        Segment s;
        for(s = buffer.head; offset < byteCount; s = s.next) {
            if(s.limit == s.pos) {
                throw new AssertionError("s.limit == s.pos");
            }

            offset += s.limit - s.pos;
            ++segmentCount;
        }

        this.segments = new byte[segmentCount][];
        this.directory = new int[segmentCount * 2];
        offset = 0;
        segmentCount = 0;

        for(s = buffer.head; offset < byteCount; s = s.next) {
            this.segments[segmentCount] = s.data;
            offset += s.limit - s.pos;
            this.directory[segmentCount] = offset;
            this.directory[segmentCount + this.segments.length] = s.pos;
            s.shared = true;
            ++segmentCount;
        }

    }

    public String utf8() {
        return this.toByteString().utf8();
    }

    public String base64() {
        return this.toByteString().base64();
    }

    public String hex() {
        return this.toByteString().hex();
    }

    public geo2.lbsp.ible.okio.ByteString toAsciiLowercase() {
        return this.toByteString().toAsciiLowercase();
    }

    public geo2.lbsp.ible.okio.ByteString toAsciiUppercase() {
        return this.toByteString().toAsciiUppercase();
    }

    public geo2.lbsp.ible.okio.ByteString md5() {
        return this.toByteString().md5();
    }

    public geo2.lbsp.ible.okio.ByteString sha256() {
        return this.toByteString().sha256();
    }

    public String base64Url() {
        return this.toByteString().base64Url();
    }

    public geo2.lbsp.ible.okio.ByteString substring(int beginIndex) {
        return this.toByteString().substring(beginIndex);
    }

    public geo2.lbsp.ible.okio.ByteString substring(int beginIndex, int endIndex) {
        return this.toByteString().substring(beginIndex, endIndex);
    }

    public byte getByte(int pos) {
        geo2.lbsp.ible.okio.Util.checkOffsetAndCount((long)this.directory[this.segments.length - 1], (long)pos, 1L);
        int segment = this.segment(pos);
        int segmentOffset = segment == 0?0:this.directory[segment - 1];
        int segmentPos = this.directory[segment + this.segments.length];
        return this.segments[segment][pos - segmentOffset + segmentPos];
    }

    private int segment(int pos) {
        int i = Arrays.binarySearch(this.directory, 0, this.segments.length, pos + 1);
        return i >= 0?i:~i;
    }

    public int size() {
        return this.directory[this.segments.length - 1];
    }

    public byte[] toByteArray() {
        byte[] result = new byte[this.directory[this.segments.length - 1]];
        int segmentOffset = 0;
        int s = 0;

        for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            System.arraycopy(this.segments[s], segmentPos, result, segmentOffset, nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }

        return result;
    }

    public void write(OutputStream out) throws IOException {
        if(out == null) {
            throw new IllegalArgumentException("out == null");
        } else {
            int segmentOffset = 0;
            int s = 0;

            for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
                int segmentPos = this.directory[segmentCount + s];
                int nextSegmentOffset = this.directory[s];
                out.write(this.segments[s], segmentPos, nextSegmentOffset - segmentOffset);
                segmentOffset = nextSegmentOffset;
            }

        }
    }

    void write(Buffer buffer) {
        int segmentOffset = 0;
        int s = 0;

        for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            Segment segment = new Segment(this.segments[s], segmentPos, segmentPos + nextSegmentOffset - segmentOffset);
            if(buffer.head == null) {
                buffer.head = segment.next = segment.prev = segment;
            } else {
                buffer.head.prev.push(segment);
            }

            segmentOffset = nextSegmentOffset;
        }

        buffer.size += (long)segmentOffset;
    }

    public boolean rangeEquals(int offset, geo2.lbsp.ible.okio.ByteString other, int otherOffset, int byteCount) {
        if(offset > this.size() - byteCount) {
            return false;
        } else {
            for(int s = this.segment(offset); byteCount > 0; ++s) {
                int segmentOffset = s == 0?0:this.directory[s - 1];
                int segmentSize = this.directory[s] - segmentOffset;
                int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
                int segmentPos = this.directory[this.segments.length + s];
                int arrayOffset = offset - segmentOffset + segmentPos;
                if(!other.rangeEquals(otherOffset, this.segments[s], arrayOffset, stepSize)) {
                    return false;
                }

                offset += stepSize;
                otherOffset += stepSize;
                byteCount -= stepSize;
            }

            return true;
        }
    }

    public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
        if(offset <= this.size() - byteCount && otherOffset <= other.length - byteCount) {
            for(int s = this.segment(offset); byteCount > 0; ++s) {
                int segmentOffset = s == 0?0:this.directory[s - 1];
                int segmentSize = this.directory[s] - segmentOffset;
                int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
                int segmentPos = this.directory[this.segments.length + s];
                int arrayOffset = offset - segmentOffset + segmentPos;
                if(!geo2.lbsp.ible.okio.Util.arrayRangeEquals(this.segments[s], arrayOffset, other, otherOffset, stepSize)) {
                    return false;
                }

                offset += stepSize;
                otherOffset += stepSize;
                byteCount -= stepSize;
            }

            return true;
        } else {
            return false;
        }
    }

    private geo2.lbsp.ible.okio.ByteString toByteString() {
        return new geo2.lbsp.ible.okio.ByteString(this.toByteArray());
    }

    public boolean equals(Object o) {
        return o == this || o instanceof ByteString && ((ByteString) o).size() == this.size() && this.rangeEquals(0, (ByteString) o, 0, this.size());
    }

    public int hashCode() {
        int result = this.hashCode;
        if(result != 0) {
            return result;
        } else {
            result = 1;
            int segmentOffset = 0;
            int s = 0;

            for(int segmentCount = this.segments.length; s < segmentCount; ++s) {
                byte[] segment = this.segments[s];
                int segmentPos = this.directory[segmentCount + s];
                int nextSegmentOffset = this.directory[s];
                int segmentSize = nextSegmentOffset - segmentOffset;
                int i = segmentPos;

                for(int limit = segmentPos + segmentSize; i < limit; ++i) {
                    result = 31 * result + segment[i];
                }

                segmentOffset = nextSegmentOffset;
            }

            return this.hashCode = result;
        }
    }

    public String toString() {
        return this.toByteString().toString();
    }

    private Object writeReplace() {
        return this.toByteString();
    }
}
