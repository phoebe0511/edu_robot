package com.ju.edurobot.api;

import com.pi4j.io.gpio.GpioPinDigitalInput;

public class ButtonSimpleTask extends ButtonBaseTask {
	public static interface BtnSimpleCB{
		public void onDown();
		public void onUp();
	}
	BtnSimpleCB m_cb = null;
	public ButtonSimpleTask(GpioPinDigitalInput pinInput, BtnSimpleCB cb) {
		super(pinInput);
		m_cb = cb;
	}
	@Override
	public void run() {
		while(!m_bStop)
		{
			waitPressDown(0);
			Sleep(10);
			if(waitPressDown(10) > 0)
			{
				Sleep(10);
				if(waitPressDown(10) > 0)
				{
					m_cb.onDown();
					Sleep(100);
					waitPressUp(0);
					m_cb.onUp();
				}
			}
		}
	}

}
