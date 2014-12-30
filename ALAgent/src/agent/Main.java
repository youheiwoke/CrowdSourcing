package agent;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class Main {

	public static void main(String[] args) {
		String[] arg = { "-accept-foreign-agents", "true" };
		jade.Boot.main(arg);
		jade.core.Runtime rt = jade.core.Runtime.instance();
		Profile p = new ProfileImpl(false);
		AgentContainer ac = rt.createMainContainer(p);

		Object[] args1 = new Object[1];
		try {
			ac.createNewAgent("ALAgent", "agent.ALAgent", args1);
			AgentController cont = ac.getAgent("ALAgent");
			cont.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
	}

}
