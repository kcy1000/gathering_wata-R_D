package geo2.lbsp.ible.okio;


final class Segment {
    static final int SIZE = 2048;
    final byte[] data;
    int pos;
    int limit;
    boolean shared;
    boolean owner;
    Segment next;
    Segment prev;

    Segment() {
        this.data = new byte[2048];
        this.owner = true;
        this.shared = false;
    }

    Segment(Segment shareFrom) {
        this(shareFrom.data, shareFrom.pos, shareFrom.limit);
        shareFrom.shared = true;
    }

    Segment(byte[] data, int pos, int limit) {
        this.data = data;
        this.pos = pos;
        this.limit = limit;
        this.owner = false;
        this.shared = true;
    }

    public Segment pop() {
        Segment result = this.next != this?this.next:null;
        this.prev.next = this.next;
        this.next.prev = this.prev;
        this.next = null;
        this.prev = null;
        return result;
    }

    public Segment push(Segment segment) {
        segment.prev = this;
        segment.next = this.next;
        this.next.prev = segment;
        this.next = segment;
        return segment;
    }

    public Segment split(int byteCount) {
        if(byteCount > 0 && byteCount <= this.limit - this.pos) {
            Segment prefix = new Segment(this);
            prefix.limit = prefix.pos + byteCount;
            this.pos += byteCount;
            this.prev.push(prefix);
            return prefix;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void compact() {
        if(this.prev == this) {
            throw new IllegalStateException();
        } else if(this.prev.owner) {
            int byteCount = this.limit - this.pos;
            int availableByteCount = 2048 - this.prev.limit + (this.prev.shared?0:this.prev.pos);
            if(byteCount <= availableByteCount) {
                this.writeTo(this.prev, byteCount);
                this.pop();
                SegmentPool.recycle(this);
            }
        }
    }

    public void writeTo(Segment sink, int byteCount) {
        if(!sink.owner) {
            throw new IllegalArgumentException();
        } else {
            if(sink.limit + byteCount > 2048) {
                if(sink.shared) {
                    throw new IllegalArgumentException();
                }

                if(sink.limit + byteCount - sink.pos > 2048) {
                    throw new IllegalArgumentException();
                }

                System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
                sink.limit -= sink.pos;
                sink.pos = 0;
            }

            System.arraycopy(this.data, this.pos, sink.data, sink.limit, byteCount);
            sink.limit += byteCount;
            this.pos += byteCount;
        }
    }
}
