package com.ju.edurobot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import com.ju.edurobot.api.GpioControlClass;
import com.ju.edurobot.api.Log;
import com.ju.edurobot.api.Utility;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;

public class RobotMain{
	static String mRobotName;
	static String mServerIP = "";
	public static void main(String argv[]) throws Exception {
		Log.d("RobotMain Start");
		FileReader fr;
		try {
			fr = new FileReader("/home/pi/edu_robot/name.cfg");
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				mRobotName = br.readLine();
			}
			fr.close();
		} catch (FileNotFoundException e) {
			FileWriter fw;
			try {
				fw = new FileWriter("/home/pi/edu_robot/name.cfg");
				Random rand = new Random();
				rand.setSeed(System.currentTimeMillis());
				int id = rand.nextInt(0xffff);
				mRobotName = "EduRobot" + id;
				fw.write(mRobotName);
				fw.flush();
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			fr = new FileReader("/home/pi/edu_robot/serverip.cfg");
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				mServerIP = br.readLine();
			}
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String command = "sudo fbi -T 1 -noverbose -a  -d /dev/fb1 /home/pi/edu_robot/media/kiduck.jpg"; //fbcp &
		Process proc_fbi = Runtime.getRuntime().exec(command);
		//Thread.sleep(1000);
		//proc_fbi.destroy();
		
		//command = "sudo SDL_VIDEODRIVER=fbcon SDL_FBDEV=/dev/fb1 mplayer -loop 5 /home/pi/edu_robot/RotatingRaspberry.gif";
    	//Process proc_gif = Runtime.getRuntime().exec(command);
    	
    	//command = "omxplayer /home/pi/edu_robot/media/tomato/let_it_go.mp3";
    	//Process proc_mp3 = Runtime.getRuntime().exec(command);
		
		//Process proc_mp3 = new ProcessBuilder("/usr/bin/omxplayer", "/home/pi/edu_robot/media/let_it_go.mp3").start();
		
    	command = "python /home/pi/edu_robot/edu_robot_rfcomm-server.py";
    	Runtime.getRuntime().exec(command);
    	command = "python /home/pi/heart/heartBeats.py";
    	Runtime.getRuntime().exec(command);
    	
    	GpioControlClass gpio_cc = new GpioControlClass();
		RobotService robotService = new RobotService(gpio_cc);
		char c;
		String strMsgString = "Enter 'q' to exit :";
        System.out.println(strMsgString);
        //InputStream si = System.in;
        
        try {
	        while ((c = (char) System.in.read()) > 0) {
	            
	            if (c == 'q' || c == 'Q') {
	            	//exit program
	                break;
	            }else if(c == 'j' || c == 'J'){
	            	command = "raspistill -vf -hf -rot 180 -o /home/pi/camera/" + Utility.getTodayString_File() + ".jpg";
	            	Log.d("get command to capture one picture :" + command);
	            	Process proc = Runtime.getRuntime().exec(command);
	            	proc.waitFor();
	            	Log.d("end");
				} else if(c == 'v' || c == 'V'){
	            	command = "raspivid -o /home/pi/camera/" + Utility.getTodayString_File() + ".h264 -t 5000";
	            	Log.d("get command to capture one video :" + command);
	            	Process proc = Runtime.getRuntime().exec(command);
	            	proc.waitFor(); 
	            	Log.d("end");
				} else if(c == 's' || c == 'S'){
					Log.d("got a s !");
				//	System.setIn(si);
					command = "arecord -D plughw:1,0 -f cd -t raw | lame -r - /home/pi/edu_robot/test.mp3";
					command = "arecord -f cd -t raw | lame -x - /home/pi/edu_robot/test.mp3";
					try {
						Process process = Runtime.getRuntime().exec("/home/pi/edu_robot/t.sh");
						Thread.sleep(5000);
						Runtime.getRuntime().exec("pkill lame");
						Runtime.getRuntime().exec("pkill arecord");
						Log.d("End!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if(c == 'u' || c == 'U'){
					//Utility.uploadToServer("/home/pi/camera/video.mp4", "phTest");
				}
				else if(c != 10){
	            	//System.out.println("wrong command :" + c + ", q" + strMsgString);
	            }
	
	        }
	    } catch (java.io.IOException ex) {
	        ex.printStackTrace();
	    }
		//mapService.close();

		robotService.close();
		Log.d("Server close!");
		System.gc();
		System.exit(0);
	}
	
	static void ledXXX()
	{
		 // create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate code)
        final Console console = new Console();

        // print program title/header
        console.title("<-- The Pi4J Project -->", "SoftPWM Example (Software-driven PWM Emulation)");

        // allow for user to exit program using CTRL-C
        console.promptForExit();

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // by default we will use gpio pin #01; however, if an argument
        // has been provided, then lookup the pin by address
        Pin pin = CommandArgumentParser.getPin(
                RaspiPin.class,    // pin provider class to obtain pin instance from
                RaspiPin.GPIO_25,  // default pin if no pin argument found
                "");             // argument array to search in

        // we will provision the pin as a software emulated PWM output
        // pins that support hardware PWM should be provisioned as normal PWM outputs
        // each software emulated PWM pin does consume additional overhead in
        // terms of CPU usage.
        //
        // Software emulated PWM pins support a range between 0 (off) and 100 (max) by default.
        //
        // Please see: http://wiringpi.com/reference/software-pwm-library/
        // for more details on software emulated PWM
        GpioPinPwmOutput pwm = gpio.provisionSoftPwmOutputPin(pin);

        // optionally set the PWM range (100 is default range)
        pwm.setPwmRange(100);

        // prompt user that we are ready
        console.println(" ... Successfully provisioned PWM pin: " + pwm.toString());
        console.emptyLine();

        // set the PWM rate to 100 (FULLY ON)
        pwm.setPwm(100);
        console.println("Software emulated PWM rate is: " + pwm.getPwm());

        console.println("Press ENTER to set the PWM to a rate of 50");
        System.console().readLine();

        // set the PWM rate to 50 (1/2 DUTY CYCLE)
        pwm.setPwm(50);
        console.println("Software emulated PWM rate is: " + pwm.getPwm());

        console.println("Press ENTER to set the PWM to a rate to 0 (stop PWM)");
        System.console().readLine();

        // set the PWM rate to 0 (FULLY OFF)
        pwm.setPwm(0);
        console.println("Software emulated PWM rate is: " + pwm.getPwm());

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
	}
	
	static void ChangeServerIP(String ip)
	{
		FileWriter fw;
		try {
			fw = new FileWriter("/home/pi/edu_robot/serverip.cfg");
			
			mServerIP = ip;
			fw.write(mServerIP);
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}
