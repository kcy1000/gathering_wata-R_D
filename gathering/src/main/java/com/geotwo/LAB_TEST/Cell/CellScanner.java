package com.geotwo.LAB_TEST.Cell;

import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;


import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;

import timber.log.Timber;

public class CellScanner {

    private static final boolean isApi26 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    //private static final boolean isApi29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

    private final WcdmaCellIdentityValidator wcdmaValidator;

    public CellScanner(WcdmaCellIdentityValidator wcdmaValidator) {
        this.wcdmaValidator = wcdmaValidator;
    }

    public wtCellInfo convert(CellInfo cellInfo) {
        wtCellInfo cell = new wtCellInfo();
        //cell.setNeighboring(!cellInfo.isRegistered());
        if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
            CellIdentityGsm identity = gsmCellInfo.getCellIdentity();
            CellSignalStrengthGsm signal = gsmCellInfo.getCellSignalStrength();
            if (wcdmaValidator.isValid(identity)) {
                Timber.d("update(): Updating WCDMA reported by API 17 as GSM");
                cell.setWcdmaCellInfo(identity.getMcc(), identity.getMnc(), identity.getLac(), identity.getCid(), identity.getPsc());
            } else {
                cell.setGsmCellInfo(identity.getMcc(), identity.getMnc(), identity.getLac(), identity.getCid());
            }
            Timber.d("GSM(Mcc,Mnc,Lac,Cid) : (" + identity.getMcc() + "," + identity.getMnc() + "," + identity.getLac() + "," + identity.getCid()+")" );
        } else if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma identity = wcdmaCellInfo.getCellIdentity();
            cell.setWcdmaCellInfo(identity.getMcc(), identity.getMnc(), identity.getLac(), identity.getCid(), identity.getPsc());
            Timber.d("WCDMA(Mcc,Mnc,Lac,Cid,Psc) : (" + identity.getMcc() + "," + identity.getMnc() + "," + identity.getLac() + "," + identity.getCid()+"," + identity.getPsc()+")" );
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
            CellIdentityLte identity = lteCellInfo.getCellIdentity();
            cell.setLteCellInfo(identity.getMcc(), identity.getMnc(), identity.getTac(), identity.getCi(), identity.getPci());
            Timber.d("LTE(Mcc,Mnc,Tac,Ci,Pci) : (" + identity.getMcc() + "," + identity.getMnc() + "," + identity.getTac() + "," + identity.getCi()+"," + identity.getPci()+")" );

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoNr) {
            CellInfoNr lteCellInfo = (CellInfoNr) cellInfo;
            CellIdentityNr identity = (CellIdentityNr) lteCellInfo.getCellIdentity();
            cell.setNrCellInfo(identity.getMccString(), identity.getMncString(), identity.getTac(), identity.getNci(), identity.getPci());
            Timber.d("5G(Mcc,Mnc,Tac,NCi,Pci) : (" + identity.getMccString() + "," + identity.getMncString() + "," + identity.getTac() + "," + identity.getNci()+"," + identity.getPci()+")" );

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoTdscdma) {
            CellInfoTdscdma lteCellInfo = (CellInfoTdscdma) cellInfo;
            CellIdentityTdscdma identity = lteCellInfo.getCellIdentity();
            cell.setTdscdmaCellInfo(identity.getMccString(), identity.getMncString(), identity.getLac(), identity.getCid(), identity.getCpid());
            Timber.d("TDSCDMA(Mcc,Mnc,Lac,Cid,Cpid) : (" + identity.getMccString() + "," + identity.getMncString() + "," + identity.getLac() + "," + identity.getCid()+"," + identity.getCpid()+")" );

        } else if (cellInfo instanceof CellInfoCdma) {
            CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
            CellIdentityCdma identity = cdmaCellInfo.getCellIdentity();
            cell.setCdmaCellInfo(identity.getSystemId(), identity.getNetworkId(), identity.getBasestationId());
            Timber.d("CDMA(systemId,networkId,baseId) : (" + identity.getSystemId() + "," + identity.getNetworkId() + "," + identity.getBasestationId()+")" );

        } else {
            throw new UnsupportedOperationException("Cell identity type not supported `" + cellInfo.getClass().getName() + "`");
        }

        update(cell, cellInfo);

        return cell;
    }

    public void update(wtCellInfo cell, CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
            CellSignalStrengthGsm signal = gsmCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            int dbm = signal.getDbm();
            int ta = isApi26 ? signal.getTimingAdvance() : wtCellInfo.UNKNOWN_SIGNAL;
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = wtCellInfo.UNKNOWN_SIGNAL;
            // TODO add RSSI to GSM on Android R
            cell.setGsmSignalInfo(asu, dbm, ta);
            Timber.d("GSM(asu,dbm,ta) : (" + asu + "," + dbm + "," + ta+")" );

        } else if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
            CellSignalStrengthWcdma signal = wcdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = wtCellInfo.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            // TODO add EC/NO to WCDMA on Android R
            cell.setWcdmaSignalInfo(asu, dbm);
            Timber.d("WCDMA(asu,dbm) : (" + asu + "," + dbm + ")" );
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
            CellSignalStrengthLte signal = lteCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = wtCellInfo.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int ta = signal.getTimingAdvance();
            int rsrp = isApi26 ? signal.getRsrp() : wtCellInfo.UNKNOWN_SIGNAL;
            int rsrq = isApi26 ? signal.getRsrq() : wtCellInfo.UNKNOWN_SIGNAL;
            //int rssi = isApi29 ? signal.getRssi() : wtCellInfo.UNKNOWN_SIGNAL;
            int rssi = wtCellInfo.UNKNOWN_SIGNAL;
            int rssnr = isApi26 ? signal.getRssnr() : wtCellInfo.UNKNOWN_SIGNAL;
            int cqi = isApi26 ? signal.getCqi() : wtCellInfo.UNKNOWN_SIGNAL;
            cell.setLteSignalInfo(asu, dbm, ta, rsrp, rsrq, rssi, rssnr, cqi);
            Timber.d("LTE(asu,dbm,ta,rsrp,rsrq,rssi,rssnr,cqi) : (" + asu + "," + dbm + "," + ta+"," + rsrp+"," + rsrq+"," + rssi+"," + rssnr+"," + cqi+")" );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoNr) {
            CellInfoNr nrCellInfo = (CellInfoNr) cellInfo;
            CellSignalStrengthNr signal = (CellSignalStrengthNr) nrCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == 99) // CellSignalStrengthNr.UNKNOWN_ASU_LEVEL not available
                asu = wtCellInfo.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int csiRsrp = signal.getCsiRsrp();
            int csiRsrq = signal.getCsiRsrq();
            int csiSinr = signal.getCsiSinr();
            int ssRsrp = signal.getSsRsrp();
            int ssRsrq = signal.getSsRsrq();
            int ssSinr = signal.getSsSinr();
            cell.setNrSignalInfo(asu, dbm, csiRsrp, csiRsrq, csiSinr, ssRsrp, ssRsrq, ssSinr);
            Timber.d("NR(asu,dbm,csiRsrp,csiRsrq,csiSinr,ssRsrp,ssRsrq,ssSinr) : (" + asu + "," + dbm + "," + csiRsrp+"," + csiRsrq+"," + csiSinr+"," + ssRsrp+"," + ssRsrq+"," + ssSinr+")" );

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoTdscdma) {
            CellInfoTdscdma tdscdmaCellInfo = (CellInfoTdscdma) cellInfo;
            CellSignalStrengthTdscdma signal = tdscdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == 255 || asu == 99) // depending on RSSI (99) or RSCP (255)
                asu = wtCellInfo.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int rscp = signal.getRscp();
            cell.setTdscdmaSignalInfo(asu, dbm, rscp);
            Timber.d("TDSCDMA(asu,dbm,rscp) : (" + asu + "," + dbm + "," + rscp+")" );
        } else if (cellInfo instanceof CellInfoCdma) {
            CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
            CellSignalStrengthCdma signal = cdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = wtCellInfo.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int cdmaDbm = signal.getCdmaDbm();
            int cdmaEcio = signal.getCdmaEcio();
            int evdoDbm = signal.getEvdoDbm();
            int evdoEcio = signal.getEvdoEcio();
            int evdoSnr = signal.getEvdoSnr();
            cell.setCdmaSignalInfo(asu, dbm, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr);
            Timber.d("CDMA(asu,dbm,cdmaDbm,cdmaEcio,evdoDbm,evdoEcio,evdoSnr) : (" + asu + "," + dbm + "," + cdmaDbm+"," + cdmaEcio+"," + evdoDbm+"," + evdoEcio+"," + evdoSnr+")" );

        } else {
            throw new UnsupportedOperationException("Cell signal type not supported `" + cellInfo.getClass().getName() + "`");
        }
    }

    public String createCellKey(wtCellInfo cell) {
        StringBuilder sb = new StringBuilder();
        sb.append(cell.getMcc())
                .append("_").append(cell.getMnc())
                .append("_").append(cell.getLac())
                .append("_").append(cell.getCid());
        return sb.toString();
    }

    public String createCellKey(CellInfo cellInfo) {
        StringBuilder sb = new StringBuilder();
        if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
            CellIdentityGsm identity = gsmCellInfo.getCellIdentity();
            sb.append(identity.getMcc())
                    .append("_").append(identity.getMnc())
                    .append("_").append(identity.getLac())
                    .append("_").append(identity.getCid());
        } else if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma identity = wcdmaCellInfo.getCellIdentity();
            sb.append(identity.getMcc())
                    .append("_").append(identity.getMnc())
                    .append("_").append(identity.getLac())
                    .append("_").append(identity.getCid());
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
            CellIdentityLte identity = lteCellInfo.getCellIdentity();
            sb.append(identity.getMcc())
                    .append("_").append(identity.getMnc())
                    .append("_").append(identity.getTac())
                    .append("_").append(identity.getCi());
        }/* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoNr) {
            CellInfoNr nrCellInfo = (CellInfoNr) cellInfo;
            CellIdentityNr identity = (CellIdentityNr) nrCellInfo.getCellIdentity();
            sb.append(identity.getMccString())
                    .append("_").append(identity.getMncString())
                    .append("_").append(identity.getTac())
                    .append("_").append(identity.getNci());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoTdscdma) {
            CellInfoTdscdma tdscdmaCellInfo = (CellInfoTdscdma) cellInfo;
            CellIdentityTdscdma identity = tdscdmaCellInfo.getCellIdentity();
            sb.append(identity.getMccString())
                    .append("_").append(identity.getMncString())
                    .append("_").append(identity.getLac())
                    .append("_").append(identity.getCid());
        } */else if (cellInfo instanceof CellInfoCdma) {
            CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
            CellIdentityCdma identity = cdmaCellInfo.getCellIdentity();
            sb.append(wtCellInfo.UNKNOWN_CID)
                    .append("_").append(identity.getSystemId())
                    .append("_").append(identity.getNetworkId())
                    .append("_").append(identity.getBasestationId());
        } else {
            /*
            Exception ex = new UnsupportedOperationException("Cell identity type not supported `" + cellInfo.getClass().getName() + "` = `" + cellInfo.toString() + "`");
            Timber.e(ex);
            MyApplication.handleSilentException(ex);
            */

        }
        return sb.toString();
    }
}
