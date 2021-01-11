package geo2.lbsp.ible.internal;

import android.os.Messenger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import geo2.lbsp.ible.Beacon;
import geo2.lbsp.ible.Region;
import geo2.lbsp.ible.Utils;

public class RangingRegion {
	private static final Comparator<Object> BEACON_ACCURACY_COMPARATOR = new Comparator<Object>() {

		@Override
		public int compare(Object lhs, Object rhs) {
			return Double.compare(Utils.computeAccuracy((Beacon)lhs), Utils.computeAccuracy((Beacon)rhs));
		}

    };
    private final List<Beacon> beacons;
    public final Region region;
    public final Messenger replyTo;

    public RangingRegion(Region region, Messenger replyTo) {
        this.region = region;
        this.replyTo = replyTo;
        this.beacons = new ArrayList<Beacon>();
    }

    public final Collection<Beacon> getSortedBeacons() {
        Collections.sort(this.beacons, BEACON_ACCURACY_COMPARATOR);
        return this.beacons;
    }

    public final void processFoundBeacons(List<Beacon> beaconsFoundInScanCycle) {
        this.beacons.clear();
        Iterator<Beacon> i$ = beaconsFoundInScanCycle.iterator();

        while(i$.hasNext()) {
            Beacon beacon = i$.next();
            this.beacons.add(beacon);
        }

    }
}