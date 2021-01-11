package com.geotwo.LAB_TEST.Voucher;

import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.geotwo.o2mapmobile.geometry.Angle;
import com.geotwo.o2mapmobile.geometry.Vector;

public class AssistUtil {

	public static double getHeader(Vector from, Vector to) {
		Vector base = new Vector(0, 1, 0, 0);

		Vector dir = to.subtract3(from);
		dir = new Vector(dir.getX(), dir.getY(), 0, 0).normalize3();

		Vector cross = base.cross3(dir).normalize3();
		double d = dir.dot3(base);
		if (Double.isNaN(d))
			return 0;
		else {
			d = Math.acos(d);
			if (Double.isNaN(d))
				return 0;
			else {
				d = Angle.fromRadians(d).getDegrees();
				if (Double.isNaN(d))
					return 0;
				else if (cross.getZ() < 0) {
					d = 360 - d;
				}
			}
		}
		if (Double.isNaN(d))
			return 0;
		else
			return 360 - d;
	}


}
