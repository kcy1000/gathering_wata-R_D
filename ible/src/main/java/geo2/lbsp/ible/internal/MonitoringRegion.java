package geo2.lbsp.ible.internal;

import android.os.Messenger;

import java.util.concurrent.TimeUnit;

public class MonitoringRegion extends RangingRegion {
    static final long EXPIRATION_MILLIS;
    private static final int NOT_SEEN = -1;
    private long lastSeenTimeMillis = -1L;
    private boolean wasInside;

    public MonitoringRegion(geo2.lbsp.ible.Region region, Messenger replyTo) {
        super(region, replyTo);
    }

    public boolean markAsSeen(long currentTimeMillis) {
        this.lastSeenTimeMillis = currentTimeMillis;
        if(!this.wasInside) {
            this.wasInside = true;
            return true;
        } else {
            return false;
        }
    }

    public boolean isInside(long currentTimeMillis) {
        return this.lastSeenTimeMillis != -1L && currentTimeMillis - this.lastSeenTimeMillis < EXPIRATION_MILLIS;
    }

    public boolean didJustExit(long currentTimeMillis) {
        if(this.wasInside && !this.isInside(currentTimeMillis)) {
            this.lastSeenTimeMillis = -1L;
            this.wasInside = false;
            return true;
        } else {
            return false;
        }
    }

    static {
        EXPIRATION_MILLIS = TimeUnit.SECONDS.toMillis(10L);
    }
}