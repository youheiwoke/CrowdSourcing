package serv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import opration.MicroTaskOperation;

import org.apache.axis.encoding.Base64;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import aggregator.Aggregator;
import aggregator.NaiveAggregator;

public class JudgeImage {
	/**
	 * @param picStr
	 *            the string of the picture encoded by apache axis base64.
	 * @param format
	 *            is the format of the picture.
	 * @param guid
	 *            consumer id.
	 * @param deadline
	 *            deadline of the work(yyyy-MM-dd HH:mm:ss).
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static String uploadPicture(String question, String picStr,
			String format, String id, String deadline)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		if (picStr == null) {
			return "Picture is null.";
		}

		// decode
		byte[] b = Base64.decode(picStr);

		// write to file.
		UUID uuid = UUID.randomUUID();
		String imgFilePath;
		imgFilePath = "\\share\\tmp\\" + uuid.toString() + "." + format;
		File f = new File(imgFilePath);
		while (f.exists()) {
			uuid = UUID.randomUUID();
			imgFilePath = "\\share\\tmp\\" + uuid.toString() + "." + format;
			f = new File(imgFilePath);
		}
		try {
			f.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			return "Create file exception.";
		}
		OutputStream out;
		try {
			out = new FileOutputStream(f);
			out.write(b);
			out.flush();
			out.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return "File not found.";
		} catch (IOException e) {
			e.printStackTrace();
			return "Output exception. File path: " + imgFilePath;
		}

		// generate XML file.
		try {
			generateXML(question, imgFilePath, uuid);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return "generate XML file not found.";
		} catch (IOException e1) {
			e1.printStackTrace();
			return "generate XML IO exception.";
		}

		try {
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(deadline);
		} catch (ParseException e) {
			e.printStackTrace();
			return "The deadline should be in the format of 'yyyy-MM-dd HH:mm:ss'";
		}

		// create a new micro task record in the database.
		MicroTaskOperation.insertMicroTask("\\share\\tmp\\" + uuid.toString()
				+ ".xml", id, deadline);

		Aggregator agg = new NaiveAggregator();
		String answer = agg.aggrerator("\\share\\tmp\\" + uuid.toString()
				+ ".xml", deadline);

		return answer;
	}

	/**
	 * 
	 * @param question
	 * @param imgFilePath
	 *            path of the received image file
	 * @param uuid
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void generateXML(String question, String imgFilePath,
			UUID uuid) throws FileNotFoundException, IOException {
		Element task = new Element("task");
		Element quesEle = new Element("question");
		quesEle.setContent(new Text(question));
		task.addContent(quesEle);
		Element attachEle = new Element("attachment");
		task.addContent(attachEle);
		attachEle.setContent(new Text(imgFilePath));
		Element optTrue = new Element("option");
		optTrue.setAttribute("value", "true");
		task.addContent(optTrue);
		Element optFalse = new Element("option");
		optFalse.setAttribute("value", "false");
		task.addContent(optFalse);
		Document doc = new Document(task);

		Format xmlFormat = Format.getCompactFormat();
		xmlFormat.setEncoding("utf-8");
		xmlFormat.setIndent(" ");
		XMLOutputter XMLOut = new XMLOutputter(xmlFormat);
		XMLOut.output(doc, new FileOutputStream(System.getProperty("user.dir")
				+ "\\tmp\\" + uuid.toString() + ".xml"));
	}
}
