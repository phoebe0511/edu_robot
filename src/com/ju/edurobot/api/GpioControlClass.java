package com.ju.edurobot.api;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.trigger.GpioBlinkStopStateTrigger;
import com.pi4j.util.CommandArgumentParser;

//pi4j gpio_21 --> rpi gpio_5 button red BTN HEAD
//pi4j gpio_26 --> rpi gpio_12 button yellow BTN HEART
public class GpioControlClass {
	final GpioController m_gpio;
	public final GpioPinDigitalInput GPI_BTN_HEART;
	public final GpioPinDigitalInput GPI_BTN_HEAD;
	public final GpioPinDigitalInput GPI_BTN_VUP;
	public final GpioPinDigitalInput GPI_BTN_VDOWN;
	//final GpioPinPwmOutput pwm_blue;
	//final GpioPinPwmOutput pwm_red;
	//final GpioPinPwmOutput pwm_green;
	public final GpioPinDigitalOutput GPO_GREEN;
	boolean mbLoop = false;
	Pin pin_green;
	public static enum LED_COLOR{
		BLUE, GREEN, RED
	};
	public static enum LED_STATUS{
		ON, OFF, BLINK
	};
	public GpioControlClass() {
		m_gpio = GpioFactory.getInstance();
		PinPullResistance pull = CommandArgumentParser.getPinPullResistance(PinPullResistance.PULL_UP, "");
		Pin pin_btn_heart = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_27, "");
		Pin pin_btn_head = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_26, "");
		Pin pin_btn_vup = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_07, "");
		Pin pin_btn_vdown = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_00, "");
		GPI_BTN_HEAD = m_gpio.provisionDigitalInputPin(pin_btn_head, "BtnHead", pull);
		GPI_BTN_HEART = m_gpio.provisionDigitalInputPin(pin_btn_heart, "BtnHeart", pull);
		GPI_BTN_VUP = m_gpio.provisionDigitalInputPin(pin_btn_vup, "BtnVUp", pull);
		GPI_BTN_VDOWN = m_gpio.provisionDigitalInputPin(pin_btn_vdown, "BtnVDown", pull);
		GPI_BTN_VUP.setShutdownOptions(true);
		GPI_BTN_VDOWN.setShutdownOptions(true);
		GPI_BTN_HEAD.setShutdownOptions(true);
		GPI_BTN_HEART.setShutdownOptions(true);
		//Pin pin_blue = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_23, "");
		//Pin pin_red = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_24, "");
		pin_green = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_25, "");  
		//pwm_blue = m_gpio.provisionPwmOutputPin(pin_blue);
		//pwm_red = m_gpio.provisionPwmOutputPin(pin_red);
		//pwm_green = m_gpio.provisionSoftPwmOutputPin(pin_green);
		GPO_GREEN = m_gpio.provisionDigitalOutputPin(pin_green);
		//pwm_blue.setShutdownOptions(true);
		//pwm_red.setShutdownOptions(true);
		//pwm_green.setShutdownOptions(true);
	}

	@Override
	protected void finalize() throws Throwable {
		m_gpio.shutdown();
		super.finalize();
	}
	
	public void led(LED_COLOR color, LED_STATUS status)
	{
		//pwm_blue.setPwmRange(100);
		Log.d("led " + color.toString() + "," + status.toString());
		//pwm_green.setPwmRange(100);
		//GPO_GREEN.high();
		switch (status) {
		case OFF:
			new GpioBlinkStopStateTrigger(GPO_GREEN).invoke(m_gpio.getProvisionedPin(pin_green), PinState.LOW);
			GPO_GREEN.low();
			break;
		case ON:
			new GpioBlinkStopStateTrigger(GPO_GREEN).invoke(m_gpio.getProvisionedPin(pin_green), PinState.LOW);
			GPO_GREEN.high();
			break;
		case BLINK:
			GPO_GREEN.blink(100);
			break;
		default:
			break;
		}
		
	}
}
