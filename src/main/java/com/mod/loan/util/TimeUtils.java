package com.mod.loan.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

	public final static String dateformat0 = "yyyy-MM-dd HH:mm";
	public final static String dateformat1 = "yyyy-MM-dd HH:mm:ss";
	public final static String dateformat2 = "yyyy-MM-dd";
	public final static String dateformat3 = "EEE MMM dd HH:mm:ss zzzZZZ yyyy";
	public final static String dateformat4 = "yyyyMMdd";
	public final static String dateformat5 = "yyyyMMddHHmmss";
	public final static String dateformat6 = "yyyyMMddHHmmssSSS";

	private TimeUtils() {
		throw new Error("can't instance this tool class");
	}

	public static String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat1);
		return sdf.format(new Date());
	}

	public static String parseTime(Date date, String dateformat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		return sdf.format(date);
	}

	public static Date parseTime(String str, String dateformat) {
		SimpleDateFormat sf = new SimpleDateFormat(dateformat);
		Date d = null;
		try {
			d = sf.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}


}
