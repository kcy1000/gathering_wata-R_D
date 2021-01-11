package com.geotwo.ImageMap;


import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.o2mapmobile.shape.Geometry;
import com.geotwo.o2mapmobile.shape.Point;
import com.geotwo.o2mapmobile.shape.Points;
import com.geotwo.o2mapmobile.shape.Polygonal;
import com.geotwo.o2mapmobile.shape.Polyline;
import com.geotwo.o2mapmobile.util.InputStreamWrapper;
import com.geotwo.o2mapmobile.util.LittleEndianByteUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class LineReader {
    private File file;
    private FileInputStream fileInStream;
    private InputStreamWrapper wrapper;
    //private WCSFReader$WCSFHeader header;
    private byte[] e;
    private int recordCnt;
    private int g;
    private int h;

    private final int l = 101;
    private final int m = 67;
    private double n = 1.0D;
    private double o = 1.0D;
    private double p = 1.0D;

    // Ethan
    private double start_x;
    private double start_y;
    private double finish_x;
    private double finish_y;

    private HashMap<Integer, PathLine> map_Path = new HashMap<>();

    public class PathLine {
        int id;
        Point start_point;
        Point end_point;
    }

    LineReader(String jsonData) {
        if (jsonData != null) {            // File is existed
            WataLog.d("JSON Data : " + jsonData);
            try {
                JSONObject jObject = new JSONObject(jsonData);

                // Path
                Double x;
                Double y;
                JSONArray arrayPaths = jObject.getJSONArray("LINE_LIST");
                for (int i = 0; i < arrayPaths.length(); i++) {
                    JSONObject jsonPath = arrayPaths.getJSONObject(i);

                    start_x = jsonPath.getDouble("start_pointx");
                    start_y = jsonPath.getDouble("start_pointy");
                    finish_x = jsonPath.getDouble("end_pointx");
                    finish_y = jsonPath.getDouble("end_pointy");

                    PathLine path = new PathLine();

                    JSONObject jsonProperties = jsonPath.getJSONObject("properties");

                    int id = jsonProperties.getInt("id");
                    path.id = id;

                    JSONObject jsonGeo = jsonPath.getJSONObject("geometry");
                    Point start_pt = new Point();
                    Point end_pt = new Point();

                    JSONArray arrayCoodi = jsonGeo.getJSONArray("coordinates");
                    for (int j = 0; j < arrayCoodi.length(); j++) {
                        JSONArray arrayPoint = arrayCoodi.getJSONArray(j);

                        if (j == 0) {
                            start_pt.x = arrayPoint.getDouble(0);
                            start_pt.y = arrayPoint.getDouble(1);
                            path.start_point = start_pt;
                        } else if (j == 1) {
                            end_pt.x = arrayPoint.getDouble(0);
                            end_pt.y = arrayPoint.getDouble(1);
                            path.end_point = end_pt;
                        }
                    }

                    map_Path.put(path.id, path);

                }

            } catch (Exception e) {
                WataLog.d("Error in Reading: " + e.getLocalizedMessage());
            }
        }

    }


    public static LineReader getParsing(String var0) {
        if (var0 == null) {
            return null;
        } else {
            LineReader var1 = null;

            try {
                var1 = new LineReader(var0);
            } catch (Exception var3) {
                var1 = null;
            }

            return var1;
        }
    }


    LineReader(File var1) {
        this.file = var1;
        try {
            this.fileInStream = new FileInputStream(var1);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        this.wrapper = new InputStreamWrapper(this.fileInStream);
        this.e = null;

        this.recordCnt = -1;
        this.g = 0;
        this.n = 1.0D;
        this.o = 1.0D;
        this.p = 1.0D;
    }


    public static LineReader getReader(String var0) {
        if (var0 == null) {
            return null;
        } else {
            LineReader var1 = null;

            try {
                File var2 = new File(var0);
                var1 = new LineReader(var2);
            } catch (Exception var3) {
                var1 = null;
            }

            return var1;
        }
    }

    public static int b(byte[] var0, int var1) {
        return LittleEndianByteUtil.toInt(var0, var1, 4);
    }

    private static long c(byte[] var0, int var1) {
        int var2 = LittleEndianByteUtil.toInt(var0, var1, 4);
        long var3 = (long) var2 & 4294967295L;
        return var3;
    }

    private int a() {
        int var1 = LittleEndianByteUtil.toInt(this.e, this.h, 4);
        this.h += 4;
        return var1;
    }

    private byte b() {
        byte var1 = this.e[this.h];
        ++this.h;
        return var1;
    }

    private int c() {
        int var1 = LittleEndianByteUtil.toShortSigned(this.e, this.h);
        this.h += 2;
        return var1;
    }

    public Geometry getLine(int index) {

        if (index < getCount() && index >= 0) {

            Polyline line = new Polyline();
            line.makeParts(1);

            Points pts = new Points();
            pts.makePoints(2);

            //PathLine path = listCollectPath.get(index);
            PathLine path = map_Path.get(index + 1);

            pts.setPoint(0, path.start_point.x, path.start_point.y, 0.0f);
            pts.setPoint(1, path.end_point.x, path.end_point.y, 0.0f);

            WataLog.d("start_point.x=" + path.start_point.x + "//" + "start_point.y=" + path.start_point.y);
            WataLog.d("end_point.x=" + path.end_point.x + "//" + "end_point.y=" + path.end_point.y);
            line.setParts(0, pts);

            return line;
        } else {
            return null;
        }
    }

    /*
        public Geometry getGeometry() {
            if (this.recordCnt < this.header.recordCount && this.recordCnt >= 0) {
                this.h = this.record.geometryStart;
                boolean var1 = false;
                byte var2 = this.b();
                byte var3 = (byte)(var2 & 128);
                int var4 = var2 & 128;
                var1 = var4 == 128;
                int var5 = var2 & 63;
                byte[] var6 = new byte[1];
                Geometry var7 = null;
                switch(var5) {
                    case 1:
                        var7 = this.a(var1);
                    case 2:
                    default:
                        break;
                    case 3:         // Line
                        Polyline var9 = new Polyline();
                        var7 = this.a(var1, var9);
                        break;
                    case 4:
                        Polygon var8 = new Polygon();
                        var7 = this.a(var1, var8);
                }

                return var7;
            } else {
                return null;
            }
        }
    */
    private Geometry a(boolean var1) {
        int var3 = this.a();
        int var4 = this.a();
        int var5 = 0;
        if (var1) {
            var5 = this.a();
        }

        Point var6 = new Point();
        return var6;
    }

    private Geometry getLine(Polygonal line) {

        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        // TODO : get Line Start, End Point
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        pts.setPoint(0, x, y, z);
        pts.setPoint(1, x, y, z);

        line.setParts(0, pts);

        return line;
    }

    /*
    private Geometry a(boolean var1, Polygonal var2) {
        int var4 = this.a();
        int var5 = this.a();
        int var6 = this.a();
        int var7 = this.a();
        int var8 = this.a();
        int var9 = this.a();
        int[] var10 = new int[var8];

        int var11;
        for(var11 = 0; var11 < var8; ++var11) {
            var10[var11] = this.a();
        }

        var2.makeParts(var8);

        Points var12;
        int var22;
        for(var11 = 0; var11 < var8; ++var11) {
            var12 = new Points();
            var12.makePoints(var10[var11]);

            for(var22 = 0; var22 < var10[var11]; ++var22) {
                int var13 = this.a();
                int var14 = this.a();
                byte var15 = 0;
                double var16 = this.compress.a(var13);
                double var18 = this.compress.b(var14);
                var16 *= this.n;
                var18 *= this.o;
                var12.setPoint(var22, var16, var18, (double)var15);
            }

            var2.setParts(var11, var12);
        }

        if (var1) {
            for(var11 = 0; var11 < var8; ++var11) {
                var12 = var2.getPart(var11);

                for(var22 = 0; var22 < var10[var11]; ++var22) {
                    int var23 = this.a();
                    double var20 = this.compress.c(var23);
                    var20 *= this.p;
                    var12.data[var22].z = var20;
                }
            }
        }

        var2.updateEnvelope();
        return var2;
    }
*/
    public int getCount() {
        return map_Path.size(); //listCollectPath.size();
    }

    public int getPathId(int index) {
        return map_Path.get(index + 1).id; //listCollectPath.get(index).id;
    }

    public void setScale(double var1, double var3, double var5) {
        this.n = var1;
        this.o = var3;
        this.p = var5;
    }

    public void setScale(double var1) {
        this.n = this.o = this.p = var1;
    }

    public double getScaleX() {
        return this.n;
    }

    public double getScaleY() {
        return this.o;
    }

    public double getScaleZ() {
        return this.p;
    }

    private static int a(byte[] var0) {
        for (int var1 = 0; var1 < var0.length; var1 += 2) {
            byte var2 = var0[var1];
            byte var3 = var0[var1 + 1];
            if (var2 == 0 && var3 == 0) {
                return var1;
            }
        }

        return 0;
    }


    public void close() {
        if (this.fileInStream != null) {
            try {
                this.fileInStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
