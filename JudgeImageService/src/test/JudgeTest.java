package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.axis.encoding.Base64;

import serv.JudgeImage;

public class JudgeTest {
	public static void main(String[] args) {
		File f = new File("E://523cbc31457ff5975edf0e1c.jpg");
		long fileLen = f.length();
		InputStream is = null;
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] b = new byte[(int) fileLen];
		try {
			is.read(b);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String picStr = Base64.encode(b);
		String format = f.getName().substring(f.getName().lastIndexOf(".") + 1);
		String question = "kickass?";

		String res;
		try {
			res = JudgeImage.uploadPicture(question, picStr, format,
					"questionAgent@192.168.1.180:1099", "2014-12-31 11:59:59");
			System.out.print(res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
