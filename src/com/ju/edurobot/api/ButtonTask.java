package com.ju.edurobot.api;

import com.pi4j.io.gpio.GpioPinDigitalInput;

public class ButtonTask extends ButtonBaseTask{
	public enum PRESSED{SHORT, LONG, DOUBLE, TRIPLE}
	public static interface BtnCB{
		public void onPressed(PRESSED status);
	}
	BtnCB m_cb = null;
	public ButtonTask(GpioPinDigitalInput pinInput, BtnCB cb) {
		super(pinInput);
		m_cb = cb;
	}

	@Override
	public void run() {
		int press_cnt = 0;
		final int DELTA_TIME = 250;
		final int LONG_PRESS_TIME = 1000;
		while(!m_bStop)
		{
			long start = waitPressDown(0);
			long btn_time = waitPressUp(start);
			if(btn_time < 50)
				continue;
			long start2 = waitPressDown(DELTA_TIME);
			if(btn_time >= LONG_PRESS_TIME)
			{
				m_cb.onPressed(PRESSED.LONG);
				continue;
			}
			
			if(start2 == 0)
			{
				switch (press_cnt) {
				case 0:
					m_cb.onPressed(PRESSED.SHORT);
					break;
				case 1:
					m_cb.onPressed(PRESSED.DOUBLE);
					break;
				case 2:
					m_cb.onPressed(PRESSED.TRIPLE);
					break;
				default:
					m_cb.onPressed(PRESSED.SHORT);
					break;
				}
				press_cnt = 0;

			}else{
				press_cnt++;
			}
		}
	}
	

}
