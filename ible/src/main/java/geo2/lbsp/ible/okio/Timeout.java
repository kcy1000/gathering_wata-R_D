package geo2.lbsp.ible.okio;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

public class Timeout {
    public static final Timeout NONE = new Timeout() {
        public Timeout timeout(long timeout, TimeUnit unit) {
            return this;
        }

        public Timeout deadlineNanoTime(long deadlineNanoTime) {
            return this;
        }

        public void throwIfReached() throws IOException {
        }
    };
    private boolean hasDeadline;
    private long deadlineNanoTime;
    private long timeoutNanos;

    public Timeout() {
    }

    public Timeout timeout(long timeout, TimeUnit unit) {
        if(timeout < 0L) {
            throw new IllegalArgumentException("timeout < 0: " + timeout);
        } else if(unit == null) {
            throw new IllegalArgumentException("unit == null");
        } else {
            this.timeoutNanos = unit.toNanos(timeout);
            return this;
        }
    }

    public long timeoutNanos() {
        return this.timeoutNanos;
    }

    public boolean hasDeadline() {
        return this.hasDeadline;
    }

    public long deadlineNanoTime() {
        if(!this.hasDeadline) {
            throw new IllegalStateException("No deadline");
        } else {
            return this.deadlineNanoTime;
        }
    }

    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        this.hasDeadline = true;
        this.deadlineNanoTime = deadlineNanoTime;
        return this;
    }

    public final Timeout deadline(long duration, TimeUnit unit) {
        if(duration <= 0L) {
            throw new IllegalArgumentException("duration <= 0: " + duration);
        } else if(unit == null) {
            throw new IllegalArgumentException("unit == null");
        } else {
            return this.deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
        }
    }

    public Timeout clearTimeout() {
        this.timeoutNanos = 0L;
        return this;
    }

    public Timeout clearDeadline() {
        this.hasDeadline = false;
        return this;
    }

    public void throwIfReached() throws IOException {
        if(Thread.interrupted()) {
            throw new InterruptedIOException();
        } else if(this.hasDeadline && System.nanoTime() > this.deadlineNanoTime) {
            throw new IOException("deadline reached");
        }
    }
}

