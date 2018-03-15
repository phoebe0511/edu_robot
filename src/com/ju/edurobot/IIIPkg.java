package com.ju.edurobot;

import java.nio.ByteBuffer;
import java.util.Base64;

import com.ju.edurobot.api.Utility;

public class IIIPkg {
	final static int UPDATE_PULSE = 1;
	int length = 0;
	int command = 0;
	int status = 0;
	static int sq_number = 0;
	byte body = 0;
	public IIIPkg(byte data)
	{
		sq_number++;
		if(sq_number == 0x80000000)
			sq_number = 1;
		command = UPDATE_PULSE;
		length = 17;
		body = data;
	}
	
	public IIIPkg(String data_base64)
	{
		byte[] rx_buf = Base64.getDecoder().decode(data_base64);
		int index = 0;
		ByteBuffer bb = ByteBuffer.wrap(rx_buf, index, 4);
		length = bb.getInt();
		index += 4;
		bb = ByteBuffer.wrap(rx_buf, index, 4);
		command = bb.getInt();
		index += 4;
		bb = ByteBuffer.wrap(rx_buf, index, 4);
		status = bb.getInt();
		index += 4;
		bb = ByteBuffer.wrap(rx_buf, index, 4);
		sq_number = bb.getInt();
		index += 4;
		body = rx_buf[index];
	}
	
	public String getBase64Data() {
		String data = "";
		byte[] buffer = new byte[length];
		int len = 0;
		Utility.intToByteArray(length, buffer, len);
		len += 4;
		Utility.intToByteArray(command, buffer, len);
		len += 4;
		Utility.intToByteArray(status, buffer, len);
		len += 4;
		Utility.intToByteArray(sq_number, buffer, len);
		len += 4;
		buffer[len] = body;
		data = Base64.getEncoder().encodeToString(buffer);
		return data;
	}
}
