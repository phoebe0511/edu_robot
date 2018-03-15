package com.ju.edurobot.api;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ju.edurobot.api.GpioControlClass.LED_COLOR;
import com.ju.edurobot.api.GpioControlClass.LED_STATUS;

public class Utility {
	public static String getTodayStringWithMilli()
	{
		String today;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		Date now = new Date();

		today = sdf.format(now); 

		return today;
	}
	public static String getTodayString()
	{
		String today;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();

		today = sdf.format(now); 

		return today;
	}
	
	public static String getTodayString_File()
	{
		String today;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date now = new Date();

		today = sdf.format(now); 

		return today;
	}
	
	public static final void intToByteArray(int value, byte[] buff, int offset) {
	   buff[offset] = (byte)(value >>> 24);
	   buff[offset + 1] = (byte)(value >>> 16);
	   buff[offset + 2] = (byte)(value >>> 8);
	   buff[offset + 3] = (byte)(value);
	}
	
	public static void drawPic(String filename)
	{
		String command = "sudo fbi -T 1 -noverbose -a -d /dev/fb1 " + filename;
		try {
			Runtime.getRuntime().exec("killall fbi");
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void capturePic(String picName) {
		String command = "raspistill -vf -hf -h 768 -w 1024 -rot 180 -o /home/pi/camera/" + picName + ".jpg";
    	Log.d("get command to capture one picture :" + command);
    	Process proc;
		try {
			proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    	
    	Log.d("capturePic end");
	}
	
	public static void captureVideo(String videoName, int seconds) {
		String command = "raspivid -h 768 -w 1024 -o /home/pi/camera/" + videoName + ".h264 -t " + Integer.toString(seconds * 1000);
    	Log.d("get command to capture one video :" + command);
    	try {
    		Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    	command = "rm /home/pi/camera/" + videoName + ".mp4";
    	Log.d(command);
    	try {
    		Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    	command = "MP4Box -fps 30 -add " + "/home/pi/camera/" + videoName + ".h264 /home/pi/camera/" + videoName + ".mp4";
    	Log.d(command);
    	try {
    		Process proc = Runtime.getRuntime().exec(command);
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
    	Log.d("captureVideo end");
	}
	
	
	
	/**
	 * upload file to server
	 * @param file
	 * @param strServerIP
	 * @return
	 */
	public static Boolean uploadToServer(String fName, String robot_name, String serverIP) {
		Boolean bResult = false;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;  
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 2 * 1024; 
        File file = new File(fName);
         
        if (file.isFile()) {
             try { 
            	Log.d("uploadToServer http://" + serverIP + "/update_file.php?i=" + robot_name + "&v=" + getTodayString_File());
            	Log.d("file.getName()=" + file.getName());
    			URL url = new URL("http://" + serverIP + "/update_file.php?i=" + robot_name + "&v=" + getTodayString_File());
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(file);
				// Open a HTTP  connection to  the URL
				conn = (HttpURLConnection) url.openConnection(); 
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("sysinfo_file", file.getName()); 
				// Output Stream
				dos = new DataOutputStream(conn.getOutputStream());     
				dos.writeBytes(twoHyphens + boundary + lineEnd); 
				dos.writeBytes("Content-Disposition: form-data; name=\"sysinfo_file\";filename=\"" + file.getName() + "\"" + lineEnd);          
				dos.writeBytes(lineEnd);
				// create a buffer of  maximum size
				bytesAvailable = fileInputStream.available(); 
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				//Log.d(TAG, "bufferSize=" + bufferSize);
				buffer = new byte[bufferSize];
				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
				//Log.d(TAG, "bytesRead=" + bytesRead);
				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
				}
				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
				// Responses from the server (code and message)
				int serverResponseCode = conn.getResponseCode();
				String rc = conn.getResponseMessage();
				Log.d("getResponseMessage " + rc);
				//String serverResponseMessage = conn.getResponseMessage();
				if(serverResponseCode == 200){
					bResult = true;                
				    //Toast.makeText(UploadToServer.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
					Log.d("File Upload Complete.");
				}else {
					Log.e("serverResponseCode=" + serverResponseCode);
				}
				//close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();
                   
     		} catch (MalformedURLException e) {
    			e.printStackTrace();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    	return bResult;
	}
	
	public static Boolean downloadFromServer(String fName, String root_name, String serverIP) {
		Boolean bResult = false;
		File file = new File(fName);
		try {
			
			try {
				if(file.exists()) {
					Log.w("has file ");
					//return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Log.d("downloadFromServer http://"+ serverIP + "/download_file.php?i=" + root_name + "&v=" + fName);
			//set the download URL, a url that points to a file on the internet
			URL url = new URL("http://"+ serverIP + "/download_file.php?i=" + root_name + "&v=" + fName);
			//create the new connection
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			//set up some things on the connection
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
			//and connect!
			urlConnection.setRequestProperty("Accept-Encoding", "identity");
			urlConnection.connect();
	
			//this will be used to write the downloaded data into the file we created
			FileOutputStream fileOutput = new FileOutputStream(file);
			//this will be used in reading the data from the internet
			InputStream inputStream = urlConnection.getInputStream();
			//create buffer
			byte[] buffer = new byte[1024];
			int bufferLength = 0; //used to store a temporary size of the buffer
			//this is the total size of the file
	        int totalSize = urlConnection.getContentLength();
	        Log.d("totalSize=" + totalSize);
	        if(totalSize > 0) {
	        	totalSize = 0;
				while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
					//add the data in the buffer to the file in the file output stream (the file on the sd card
					fileOutput.write(buffer, 0, bufferLength);
					totalSize += bufferLength;
				}
				if(totalSize > 0)
					bResult = true;
	        }
			//close the output stream when done
	        fileOutput.flush();
			fileOutput.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bResult;
	}
	
}
