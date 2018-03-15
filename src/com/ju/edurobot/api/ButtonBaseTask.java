package com.ju.edurobot.api;


import com.pi4j.io.gpio.GpioPinDigitalInput;

public class ButtonBaseTask extends Thread{
	boolean m_bStop = false;
	final GpioPinDigitalInput m_pinInput;
	
	public ButtonBaseTask(GpioPinDigitalInput pinInput) {
		m_pinInput = pinInput;
	}
	
	long waitPressDown(long duration)
	{
		long start = System.currentTimeMillis();
		while (!m_bStop) {
			if(duration > 0 && ((System.currentTimeMillis() - start) > duration))
				break;
			if(m_pinInput.isLow()) //press down
			{
				return System.currentTimeMillis();
			}
			Sleep(10);
		}
		return 0;
	}
	
	long waitPressUp(long start)
	{
		while (!m_bStop) {
			if(m_pinInput.isHigh())	//press up
			{
				return (System.currentTimeMillis() - start);
			}
			Sleep(10);
		}
		return 0;
	}
	
	void Sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void close() {
		m_bStop = true;
	}
}
