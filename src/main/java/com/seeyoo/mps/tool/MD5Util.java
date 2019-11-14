package com.seeyoo.mps.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };
	protected static MessageDigest messagedigest = null;
	static {
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static boolean checkPassword(String password, String md5PwdStr) {
		String s = getMD5String(password);
		return s.equals(md5PwdStr);
	}

	public static String getFileMD5String(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		FileChannel ch = in.getChannel();
		MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		messagedigest.update(byteBuffer);
		return bufferToHex(messagedigest.digest());
	}

	public static String getMD5String(String s) {
		try {
			return getMD5String(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public static String getMD5String(byte[] bytes) {
		messagedigest.update(bytes);
		return bufferToHex(messagedigest.digest());
	}

	public static String getLargeMD5(String path) throws IOException, NoSuchAlgorithmException {

		File f = new File(path);

		if (!f.exists()) {
			return null;
		}

		InputStream ins = new FileInputStream(f);

		byte[] buffer = new byte[8192];

		int len;
		while ((len = ins.read(buffer)) != -1) {
			messagedigest.update(buffer, 0, len);
		}

		ins.close();

		return DigestUtils.md5Hex(messagedigest.digest());
	}

	public static String getMD5(String path) throws IOException {
		InputStream ins = null;
		try {
			File f = new File(path);

			if (!f.exists()) {
				return null;
			}
			ins = new FileInputStream(f);
			return DigestUtils.md5Hex(ins);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ins.close();
		}
		return null;
	}

	public static String getMD5FOss(String path) throws IOException {
		InputStream ins = null;
		try {
			ins = new URL(path).openStream();
			return DigestUtils.md5Hex(ins);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ins.close();
		}
		return null;
	}

	public static String getMyMd5(String path) {
		try {
			String File_md5 = getMD5FOss(path);
			String md5 = getMD5String(File_md5 + "20160822");
			return md5;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getLocalMd5(String path) {
		try {
			String File_md5 = getMD5(path);
			String md5 = getMD5String(File_md5 + "20160822");
			return md5;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getMd5ByIns(InputStream inputStream){
		try {
			String File_md5 = DigestUtils.md2Hex(inputStream);
			String md5 = getMD5String(File_md5 + "20191015");
			return md5;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String aString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Templates><ID>0</ID><TaskName></TaskName><RootTask></RootTask><TaskType>1</TaskType><Resolution>1920*1080</Resolution><Version>2</Version><Section><SectionID>0</SectionID><TMode>1</TMode><SectionStartTime>2000-01-01 00:00:00</SectionStartTime><SectionEndTime>2000-01-01 23:59:59</SectionEndTime><ValidDate><StartDate/><EndDate/></ValidDate><Template><ID>0</ID><TVMode>1</TVMode><BackgroudImgName/><TemplatePlayTime>86399</TemplatePlayTime><VideoArea><Property><VideoID>000001</VideoID><VideoLeft>431</VideoLeft><VideoTop>125</VideoTop><VideoWidth>317</VideoWidth><VideoHeight>374</VideoHeight></Property><PlayList><ID>000000</ID><VideoStartTime>2003-01-01 00:00:00</VideoStartTime><VideoEndTime>2003-01-01 23:59:59</VideoEndTime><Video><ID>1</ID><VideoPlayName>(2)_07303ea8e34384cfd1e0e1ef5022d7a8.avi</VideoPlayName><VideoTimes>1</VideoTimes><VideoSound>100</VideoSound></Video></PlayList></VideoArea><ImgArea><Property><ImgID>000002</ImgID><ImgLeft>1122</ImgLeft><ImgTop>20</ImgTop><ImgWidth>317</ImgWidth><ImgHeight>374</ImgHeight></Property><PlayList><ID>000000</ID><ImgStartTime>2003-01-01 00:00:00</ImgStartTime><ImgEndTime>2003-01-01 23:59:59</ImgEndTime><Image><ID>1</ID><ImgPlayName>1_ff098e9752ae3f7412bf9d7ebc6c608c.jpg</ImgPlayName><ImgPlayTime>10</ImgPlayTime><ImgEffectSpeed>1</ImgEffectSpeed><ImgEffect>0</ImgEffect></Image></PlayList></ImgArea><FontArea><Property><FontID>000003</FontID><FontLeft>908</FontLeft><FontTop>554</FontTop><FontWidth>317</FontWidth><FontHeight>374</FontHeight><Background/><FontMode>0</FontMode><FontStyle>1</FontStyle></Property><PlayList><ID>000000</ID><FontStartTime>2003-01-01 00:00:00</FontStartTime><FontEndTime>2003-01-01 23:59:59</FontEndTime><Font><ID>1</ID><FontPlayName>test_a35479ca87a528731d6c82c5f798fb81.txt</FontPlayName><FontPlayTime>10</FontPlayTime><FontTimes>1</FontTimes><FontSpeed>1</FontSpeed></Font></PlayList></FontArea></Template></Section></Templates>";
		String bString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Templates><ID>0</ID><TaskName></TaskName><RootTask></RootTask><TaskType>1</TaskType><Resolution>1920*1080</Resolution><Version>2</Version><Section><SectionID>0</SectionID><TMode>1</TMode><SectionStartTime>2000-01-01 00:00:00</SectionStartTime><SectionEndTime>2000-01-01 23:59:59</SectionEndTime><ValidDate><StartDate/><EndDate/></ValidDate><Template><ID>0</ID><TVMode>1</TVMode><BackgroudImgName/><TemplatePlayTime>86399</TemplatePlayTime><VideoArea><Property><VideoID>000001</VideoID><VideoLeft>948</VideoLeft><VideoTop>252</VideoTop><VideoWidth>317</VideoWidth><VideoHeight>374</VideoHeight></Property><PlayList><ID>000000</ID><VideoStartTime>2003-01-01 00:00:00</VideoStartTime><VideoEndTime>2003-01-01 23:59:59</VideoEndTime><Video><ID>1</ID><VideoPlayName>(2)_07303ea8e34384cfd1e0e1ef5022d7a8.avi</VideoPlayName><VideoTimes>1</VideoTimes><VideoSound>100</VideoSound></Video></PlayList></VideoArea></Template></Section></Templates>";
		System.out.println(getMD5String(bString));
	}
}