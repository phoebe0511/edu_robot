package com.ju.edurobot.api;

public class Log {
	public static int getLineNumber() {
		return Thread.currentThread().getStackTrace()[3].getLineNumber();
	}

	public static String getFileName() {
		return Thread.currentThread().getStackTrace()[3].getFileName();
	}

	public static void d(String msg)
	{
		System.out.println("[Debug:" + Utility.getTodayString() + "] " + msg);
	}
	
	public static void e(String msg)
	{
		System.out.println("[Error:" + Utility.getTodayString() + " " + getFileName() + ":" + getLineNumber() + "] " + msg);
	}
	
	public static void i(String msg)
	{
		System.out.println("[Info:" + Utility.getTodayString() + "] " + msg);
	}
	
	
	public static void w(String msg)
	{
		System.out.println("[Warning:" + Utility.getTodayString() + getFileName() + ":" + getLineNumber() + "] " + msg);
	}
}
