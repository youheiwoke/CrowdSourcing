package agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

import dbObject.AgentInfo;
import dbObject.MicroTask;
import opration.AgentDBOperation;
import opration.MicroTaskOperation;
import opration.ResponseDBOperation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;

public class ALAgent extends Agent {
	private final long MAXDIFF = 15 * 60 * 1000;
	private final long SLEEP_TIME = 60 * 1000;

	@Override
	protected void setup() {
		System.out.println("ALAgent address: "
				+ getAID().getAddressesArray()[0]);
		System.out.println("ALAgent guid: " + getAID().getName());
		ParallelBehaviour pb = new ParallelBehaviour(this,
				ParallelBehaviour.WHEN_ALL);
		pb.addSubBehaviour(processInitial());
		pb.addSubBehaviour(processMessage());
		pb.addSubBehaviour(checkActive());
		this.addBehaviour(pb);
		System.out.println("ALAgent started!");
	}

	/**
	 * Process incoming messages. A register message should be a request message
	 * and starts with "register me please". If the register succeed, ALAgent
	 * would reply with an agree message; else, ALAgent would reply with a
	 * failure message. A heartbeat message should be a request message and
	 * starts with "i am alive".If the register succeed, ALAgent would reply
	 * with an agree message; else, ALAgent would reply with a failure message.
	 * 
	 * @return
	 */
	private CyclicBehaviour processMessage() {
		return new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage message = receive();
				if (message == null) {
					return;
				}
				AID sender = message.getSender();
				String content = message.getContent();
				ACLMessage reply;

				if (message.getPerformative() == ACLMessage.REQUEST) {
					if (content.toLowerCase().startsWith("register me please")) {// register
																					// message.
						try {
							if (AgentDBOperation.register(sender.getName(),
									sender.getAddressesArray()[0])) {// register
																		// succeed.
								reply = new ACLMessage(ACLMessage.AGREE);
								reply.setContent("register succeed.");
							} else {// register failed.
								reply = new ACLMessage(ACLMessage.FAILURE);
								reply.setContent("register failed.");
							}
							reply.addReceiver(sender);
							send(reply);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else if (content.toLowerCase().startsWith("i am alive")) {
						try {
							if (AgentDBOperation.onLineUpdate(sender.getName())) {// register
																					// succeed.
								reply = new ACLMessage(ACLMessage.AGREE);
								reply.setContent("heartbeat succeed.");
							} else {// register failed.
								reply = new ACLMessage(ACLMessage.FAILURE);
								reply.setContent("heartbeat failed.");
							}
							reply.addReceiver(sender);
							send(reply);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {// not understood.
						reply = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent("not understood.");
						reply.addReceiver(sender);
						send(reply);
					}
				} else if (message.getPerformative() == ACLMessage.INFORM) {// Worker
																			// response
																			// message
					StringReader sr = new StringReader(content);
					InputSource is = new InputSource(sr);
					try {
						Document doc = (new SAXBuilder()).build(is);
						Element root = doc.getRootElement();
						if (ResponseDBOperation.insertResponse(
								// insert succeed.
								sender.getName(), root.getChildText("XMLpath"),
								root.getChildText("response"))) {
							reply = new ACLMessage(ACLMessage.INFORM);
							reply.setContent("response received.");
						} else {
							reply = new ACLMessage(ACLMessage.FAILURE);
							reply.setContent("response receiving failed.");
						}
						reply.addReceiver(sender);
						send(reply);
					} catch (JDOMException e) {// Cannot be resolved to an XML
												// file.
						e.printStackTrace();
						reply = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent("Cannot be resolved to an XML file.");
						reply.addReceiver(sender);
						send(reply);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	/**
	 * Polling for initial micro task, if found, set its state to creating and
	 * send the task to all online agents.
	 * 
	 * @return
	 */
	private CyclicBehaviour processInitial() {
		return new CyclicBehaviour() {

			@Override
			public void action() {
				try {
					// get all initial micro tasks.
					List<MicroTask> tasks = MicroTaskOperation
							.queryTask("initial");
					SAXBuilder sb = new SAXBuilder();
					for (MicroTask task : tasks) {
						String consumer = task.consumer;
						// Change the text of the <attachment> from file path to
						// the file stream encoded by base64
						// and transfer the XML to string.
						Document doc = sb.build(new FileInputStream(
								task.template));
						Element root = doc.getRootElement();
						Element pathEle = new Element("XMLpath");
						pathEle.setText(task.template);
						root.addContent(pathEle);
						String imgPath = root.getChildText("attachment");
						File f = new File(imgPath);
						long fileLen = f.length();
						InputStream is = new FileInputStream(f);
						byte[] b = new byte[(int) fileLen];
						is.read(b);
						Base64 base64 = new Base64();
						String picStr = base64.encode(b).toString();
						root.getChild("attachment").setText(picStr);

						Format format = Format.getPrettyFormat();
						XMLOutputter xmlout = new XMLOutputter(format);
						ByteArrayOutputStream bo = new ByteArrayOutputStream();
						xmlout.output(doc, bo);
						String content = bo.toString();

						ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF);
						message.setContent(content);

						// send task to all online agent except the consumer.
						List<AgentInfo> agents = AgentDBOperation
								.getOnlineAgents();
						for (AgentInfo agent : agents) {
							if (!agent.guid.equals(consumer)) {
								AID dst = new AID(agent.guid, true);
								dst.addAddresses(agent.address);
								message.addReceiver(dst);
							}
						}
						send(message);
						MicroTaskOperation.updateMicroTask(task.template,
								consumer, "creating");
					}
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		};
	}

	private CyclicBehaviour checkActive() {
		return new CyclicBehaviour() {
			@Override
			public void action() {
				try {
					AgentDBOperation.checkActive(MAXDIFF);
					Thread.sleep(SLEEP_TIME);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
}
