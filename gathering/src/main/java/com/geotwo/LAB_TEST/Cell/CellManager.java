package com.geotwo.LAB_TEST.Cell;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class CellManager {

    private Activity activity;
    private ArrayList<wtCellInfo> listCell = new ArrayList<wtCellInfo>();
    private CellScanner scanner;
    private WcdmaCellIdentityValidator wcdmaValidator;

    public CellManager(Activity activity)
    {
        this.activity = activity;
        this.wcdmaValidator = new WcdmaCellIdentityValidator();
        scanner = new CellScanner(wcdmaValidator);
    }

    public ArrayList<wtCellInfo> getListCell()
    {
        return listCell;
    }

    public ArrayList<wtCellInfo> scanCellInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

        if( telephonyManager != null  && ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
            List<CellInfo> cellList = telephonyManager.getAllCellInfo();

            removeDuplicatedCells(cellList);

            listCell.clear();

            if (cellList != null) {
                Log.d("loadCellInfo", " size : " + cellList.size());
                int iN = 0;
                for (final CellInfo cell : cellList) {

                    wtCellInfo _cell = scanner.convert(cell);
                    listCell.add(_cell);
                }
            }
        }

        return listCell;
    }

    private void removeDuplicatedCells(List<CellInfo> cells) {
        List<CellInfo> cellsToRemove = new ArrayList<CellInfo>();
        Set<String> uniqueCellKeys = new HashSet<String>();

        for (CellInfo cell : cells) {
            if (cell == null)
                continue;
            String key = scanner.createCellKey(cell);
            if (uniqueCellKeys.contains(key)) {
                Timber.d("removeDuplicatedCells(): Remove duplicated cell: %s", key);
                cellsToRemove.add(cell);
            } else {
                uniqueCellKeys.add(key);
            }
        }

        cells.removeAll(cellsToRemove);
    }
}
