package com.geotwo.common;

import android.graphics.Typeface;

import com.geotwo.o2mapmobile.O2Map;
import com.geotwo.o2mapmobile.element.Element;
import com.geotwo.o2mapmobile.element.ElementHelper;
import com.geotwo.o2mapmobile.geometry.Vector;
import com.geotwo.o2mapmobile.shape.Polyline;
import com.geotwo.o2mapmobile.util.Color;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by hyuck on 2017. 1. 12..
 */

public class GatheringO2MapUtils {

    /**
     * COLOR_PATH_LINE - 현재 패스 표시용, 파란색
     */
    private final Color COLOR_PATH = new Color(0, 0, 255, 255);

    /**
     * COLOR_PATH_CURRENT - 현재까지의 위치 표시용, 빨간색
     */
    private final Color COLOR_PATH_CURRENT = new Color(255, 0, 0, 255);

    /**
     * COLOR_PATH_COMPLETED - 이전 수집된 로그 표시용, 초록색(default)
     */
    private final Color COLOR_PATH_COMPLETED = new Color(0, 255, 0, 255);

    /**
     * COLOR_PATH_COMPLETED_IMAGE - 이전 수집된 로그 표시용(영상), 핑크색
     */
    private final Color COLOR_PATH_COMPLETED_IMAGE = new Color(255, 0, 255, 255);

    /**
     * COLOR_POINTER - POINTER 컬러
     */
    private final Color COLOR_POINTER = Color.RED;

    /**
     * COLOR_POSITION - position 컬러
     */
    private final Color COLOR_POSITION = new Color(44, 233, 41, 255);

    /**
     * completedPathList - 완료된 패스 리스트 (PATH)
     * completedPathListForImage - 완료된 패스 리스트 (PATH, 영상)
     * currentPath - 현재까지의 패스 (PATH_CURRENT)
     * path - 현재 패스 (PATH)
     * currentPosition - 현재 지점 (POINTER)
     * currentDistance - 목적지 까지 남은 거리 텍스트 (POINTER)
     * startPosition - 시작 지점 (POINTER)
     * endPosition - 끝 지점 (POINTER)
     */
    private LinkedList<Polyline> completedPathList = null;
    private LinkedList<Polyline> completedPathListForImage = null;
    private Polyline currentPath = null;
    private Polyline path = null;
    private Element currentPosition = null;
    private Element currentDistance = null;
    private Element startPosition = null;
    private Element endPosition = null;

    /**
     * instance - O2map
     */
    private O2Map instance = null;

    /**
     * Z values
     * ZINDEX_POINTER - (currentPosition, currentDistance, startPosition, endPosition)
     * ZINDEX_PATH_CURRENT - (currentPath)
     * ZINDEX_PATH_COMPLETED - (completedPathList, completedPathListForImage)
     * ZINDEX_PATH - (path)
     */
    private final double ZINDEX_POINTER = 0.13d;
    private final double ZINDEX_PATH_CURRENT = 0.12d;
    private final double ZINDEX_PATH_COMPLETED = 0.11d;
    private final double ZINDEX_PATH = 0.10d;

    /**
     * SIZE_WIDTH_CURRENT_POINTER - 현재위치 커서 크기(in meters)
     */
    private final double SIZE_CURRENT_POINTER = 3;

    /**
     * TEXT_END_POSITION - 끝 지점 텍스트
     */
    private final String TEXT_END_POSITION = "G";

    /**
     * TEXT_SIZE_END_POSITION - 끝 지점 텍스트 크기
     */
    private final float TEXT_SIZE_END_POSITION = 20.0f;

    public GatheringO2MapUtils(O2Map instance) {
        this.instance = instance;

        completedPathList = new LinkedList();
        completedPathListForImage = new LinkedList();

        currentPosition = ElementHelper.quadPyramidFromPoint(0, 0, ZINDEX_POINTER, SIZE_CURRENT_POINTER, SIZE_CURRENT_POINTER, SIZE_CURRENT_POINTER, COLOR_POINTER, COLOR_POINTER, COLOR_POINTER, false);
        startPosition = ElementHelper.fromTextBillboard(new Vector(0 , 0 , ZINDEX_POINTER), 4f, 4f, true, TEXT_END_POSITION, TEXT_SIZE_END_POSITION, Typeface.DEFAULT_BOLD, COLOR_POSITION);
    }

    public void addCompletedPath(){

    }

    public void addCompletedPathForImage(){

    }

    public void setCurrentPath(Polyline currentPath) {
        this.currentPath = currentPath;
    }

    public void setPath(Polyline path) {
        this.path = path;
    }

    public void setCurrentPosition(Element currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void setStartPosition(Element startPosition) {
        this.startPosition = startPosition;
    }

    public void setEndPosition(Element endPosition) {
        this.endPosition = endPosition;
    }
}
