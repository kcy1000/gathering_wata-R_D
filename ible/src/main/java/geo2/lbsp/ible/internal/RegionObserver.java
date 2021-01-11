package geo2.lbsp.ible.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import geo2.lbsp.ible.Beacon;
import geo2.lbsp.ible.BeaconScanner;


public class RegionObserver {
    private final List<RangingRegion> rangedRegions = new ArrayList();
    private final List<MonitoringRegion> monitoredRegions = new ArrayList();

    public RegionObserver() {
    }

    public void add(RangingRegion rangingRegion) {
        this.rangedRegions.add(rangingRegion);
    }

    public void add(MonitoringRegion monitoringRegion) {
        this.monitoredRegions.add(monitoringRegion);
    }

    public void removeByRangingId(String regionId) {
        Iterator iterator = this.rangedRegions.iterator();

        while(iterator.hasNext()) {
            RangingRegion rangingRegion = (RangingRegion)iterator.next();
            if(regionId.equals(rangingRegion.region.getIdentifier())) {
                iterator.remove();
            }
        }

    }

    public void removeByMonitoringId(String regionId) {
        Iterator iterator = this.monitoredRegions.iterator();

        while(iterator.hasNext()) {
            MonitoringRegion monitoringRegion = (MonitoringRegion)iterator.next();
            if(regionId.equals(monitoringRegion.region.getIdentifier())) {
                iterator.remove();
            }
        }

    }

    public boolean isObserving() {
        return !this.rangedRegions.isEmpty() || !this.monitoredRegions.isEmpty();
    }

    public boolean isRanging() {
        return !this.rangedRegions.isEmpty();
    }

    public Observation process(BeaconScanner.SingleScan singleScan, long now) {
        List beacons = singleScan.getBeacons();
        Iterator i$ = this.rangedRegions.iterator();

        while(i$.hasNext()) {
            RangingRegion rangedRegion = (RangingRegion)i$.next();
            rangedRegion.processFoundBeacons(beacons);
        }

        return new Observation(this.findEnteredRegions(beacons,  now), this.findExitedRegions(now), this.rangedRegions);
    }

    private List<MonitoringRegion> findEnteredRegions(List<Beacon> beacons, long currentTimeMillis) {
        ArrayList<MonitoringRegion> didEnterRegions = new ArrayList<MonitoringRegion>();
        ArrayList<Beacon> allBeacons = new ArrayList<Beacon>();
        allBeacons.addAll(beacons);
        Iterator<Beacon> i$all = allBeacons.iterator();

        while(i$all.hasNext()) {
            Beacon beacon = i$all.next();
            Iterator<?> i$1 = this.matchingMonitoredRegions(beacon).iterator();

            while(i$1.hasNext()) {
                MonitoringRegion monitoringRegion = (MonitoringRegion)i$1.next();
                monitoringRegion.processFoundBeacons(beacons);
                if(monitoringRegion.markAsSeen(currentTimeMillis)) {
                    didEnterRegions.add(monitoringRegion);
                }
            }
        }

        return didEnterRegions;
    }

    private List<MonitoringRegion> matchingMonitoredRegions(Beacon beacon) {
        ArrayList<MonitoringRegion> results = new ArrayList<MonitoringRegion>();
        Iterator<MonitoringRegion> i$ = this.monitoredRegions.iterator();

        while(i$.hasNext()) {
            MonitoringRegion monitoredRegion = i$.next();
            if(geo2.lbsp.ible.Utils.isBeaconInRegion(beacon, monitoredRegion.region)) {
                results.add(monitoredRegion);
            }
        }

        return results;
    }

    private List<MonitoringRegion> findExitedRegions(long currentTimeMillis) {
        ArrayList<MonitoringRegion> didExitMonitors = new ArrayList<MonitoringRegion>();
        Iterator<MonitoringRegion> i$ = this.monitoredRegions.iterator();

        while(i$.hasNext()) {
            MonitoringRegion monitoredRegion = i$.next();
            if(monitoredRegion.didJustExit(currentTimeMillis)) {
                didExitMonitors.add(monitoredRegion);
            }
        }

        return didExitMonitors;
    }

    public static class Observation {
        public final List<MonitoringRegion> enteredRegions;
        public final List<MonitoringRegion> exitedRegions;
        public final List<RangingRegion> rangedRegions;

        public Observation(List<MonitoringRegion> enteredRegions, List<MonitoringRegion> exitedRegions, List<RangingRegion> rangedRegions) {
            this.enteredRegions = enteredRegions;
            this.exitedRegions = exitedRegions;
            this.rangedRegions = rangedRegions;
        }
    }
}