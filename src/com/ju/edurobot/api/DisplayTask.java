package com.ju.edurobot.api;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class DisplayTask extends Thread{
	Process mProcess = null;
	private PrintStream mPrintStream = null;
	boolean mbStop = false;
	String mStrFile = "";
	String mStrWin;
	boolean mbLoop = true;
	
	public DisplayTask(String fileName, int x, int y, int x1, int y1, boolean bThread) {
		mStrFile = fileName;
		mStrWin = Integer.toString(x) + "," + Integer.toString(y) + "," 
				+ Integer.toString(x1) + "," + Integer.toString(y1);
		if(bThread)
			start();
	}
	public void setNoLoop(){
		mbLoop = false;
	}
	@Override
	public void run() {
		mbStop = false;
		//while (!mbStop) 
		{
			String command;
			if(mbLoop)
				command = "omxplayer -b --no-osd --loop --win " + mStrWin + " " + mStrFile;
			else
				command = "omxplayer -b --no-osd --win " + mStrWin + " " + mStrFile;
			Log.d("display command=" + command);
			try {
				mProcess = Runtime.getRuntime().exec(command);
				mPrintStream = new PrintStream(mProcess.getOutputStream());
				try {
					while (mProcess != null && !mProcess.waitFor(100, TimeUnit.MILLISECONDS) && !mbStop)
					{
						//Thread.sleep(1000);
					}
					quit_comm();
					waitEnd();
					Log.d("end face");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// mPrintStream = new PrintStream(mProcess.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void quit_comm() {
		if(mPrintStream == null)
			return;
		Log.d("quit_comm");
		mPrintStream.print("q");
		mPrintStream.flush();
	}
	private void waitEnd() {
		if(mProcess == null)
			return;
		try {
			mProcess.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NullPointerException exc) {
			exc.printStackTrace();
		}finally{
			mPrintStream = null;
			mProcess = null;
			
		}
	}

	public void stopAndWaitProcess()
	{
		Log.d("stopAndWaitProcess");
		mbStop = true;
		if(mProcess == null)
			return;

		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
