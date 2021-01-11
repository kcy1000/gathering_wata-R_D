package geo2.lbsp.ible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import geo2.lbsp.ible.internal.Preconditions;


@SuppressWarnings("unchecked")
public class BeaconScanner {
    private static final long EXPIRATION_MILLIS;
    private static final long TELEMETRY_EXPIRATION_MILLIS;
    public final ConcurrentHashMap<String, Observation> observations = new ConcurrentHashMap();
    private boolean secureBeaconsResolving;

    public BeaconScanner() {
    }

    public void setSecureBeaconsResolving(boolean secureBeaconsResolving) {
        this.secureBeaconsResolving = secureBeaconsResolving;
    }

    public void found(Beacon beacon, long timeMillis) {
        this.findAndUpdateObservation(this.observations, this.key(beacon), beacon, timeMillis);
    }

    private void removeExpiredObservations(Map<String, Observation> observations, long currentTimeMillis) {
        Iterator iterator = observations.entrySet().iterator();

        while(true) {
            while(iterator.hasNext()) {
                Entry entry = (Entry)iterator.next();
                Observation observation = (Observation)entry.getValue();
                if(this.isTelemetryExpired(currentTimeMillis, observation)) {
                    iterator.remove();
                } else if(this.isBeaconExpired(currentTimeMillis, observation)) {
                    iterator.remove();
                }
            }

            return;
        }
    }


    public SingleScan newCycle(long currentTimeMillis) {
        // 중요 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // 현재 시간과 비교 해서 비콘 삭제 하는 기존 코드를 주석처리함.
        // 2019.05.16
        // 주석하고 나서 티라유텍 삼성디스플레이 공장에서 성능이 더 안좋아 졌다고 알려옴.
        // gathering app도 주석 처리 하고 test 필요
        this.removeExpiredObservations(this.observations, currentTimeMillis);

        return new SingleScan(this.observations);
    }

    public void clear() {
        this.observations.clear();
    }

    private Observation findAndUpdateObservation(Map<String, Observation> observations, String key, Object observedBeacon, long currentTimeMillis) {
        Observation observation = observations.get(key);
        if(observation == null) {
            observation = new Observation(observedBeacon, Long.valueOf(currentTimeMillis));
            observations.put(key, observation);
        } else {
            observation.update(observedBeacon, currentTimeMillis);
        }

        return observation;
    }

    private String key(Beacon beacon) {
        return beacon.getMacAddress().toHexString();
    }


    private boolean isBeaconExpired(long currentTimeMillis, Observation observation) {
        return currentTimeMillis - observation.getLastSeenMillis().longValue() > EXPIRATION_MILLIS;
    }

    private boolean isTelemetryExpired(long currentTimeMillis, Observation observation) {
        return currentTimeMillis - observation.getLastSeenMillis().longValue() > TELEMETRY_EXPIRATION_MILLIS;
    }

    // beacon scan expiration
    // edited by namil. 2018.02.27
    static {
        EXPIRATION_MILLIS = TimeUnit.SECONDS.toMillis(1L);
//        TELEMETRY_EXPIRATION_MILLIS = TimeUnit.SECONDS.toMillis(100L);
        TELEMETRY_EXPIRATION_MILLIS = TimeUnit.SECONDS.toMillis(100L);
    }

    private static class Observation {
        private Object observedBeacon;
        private Long lastSeenMillis;

        public Observation(Object observedBeacon, Long lastSeenMillis) {
            Preconditions.checkNotNull(observedBeacon);
            this.observedBeacon = observedBeacon;
            this.lastSeenMillis = lastSeenMillis;
        }

        void update(Object observedBeacon, long currentTimeMillis) {
            Preconditions.checkNotNull(observedBeacon);
            this.observedBeacon = observedBeacon;
            this.lastSeenMillis = Long.valueOf(currentTimeMillis);
        }

        public Object getObservedBeacon() {
            return this.observedBeacon;
        }

        public Long getLastSeenMillis() {
            return this.lastSeenMillis;
        }
    }

    public static class SingleScan {
        public final List<Beacon> beacons;

        public SingleScan(ConcurrentHashMap<String, Observation> observations) {
            this.beacons = new ArrayList<Beacon>();

            Iterator var7 = observations.values().iterator();

            Observation var8;
            while(var7.hasNext()) {
                var8 = (Observation)var7.next();
                beacons.add((Beacon)var8.getObservedBeacon());
            }
        }

        public SingleScan(List<Beacon> beacons) {
            this.beacons = beacons;
        }
        
        public <T> List<T> getBeacons() {
            return (List<T>) (beacons != null ? this.beacons : Collections.<Beacon>emptyList());
        }
    }
}
