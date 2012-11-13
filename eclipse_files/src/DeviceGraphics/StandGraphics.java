package DeviceGraphics;

import factory.KitConfig;
import factory.PartType;
import Networking.Request;
import Networking.Server;
import Utils.Constants;
import agent.Agent;
import agent.StandAgent;

/**
 * Represents the stand for the two kitting stands and the parent class for the inspection stand.
 * 
 * @author Shalynn Ho, Matt Zecchini
 */
public class StandGraphics implements DeviceGraphics {
	
	protected Server server;
	protected StandAgent standAgent;
	protected int standID;
	
	// the kit on this stand
	protected KitGraphics kit;
	// false if there is a kit on the stand
	protected boolean isEmpty;
	
	/**
	 * 
	 * @param s - the server
	 * @param a - the stand agent
	 * @param id - stand ID - 0,1: kit stands; 2: inspection stand
	 */
	public StandGraphics(Server s, Agent a, int id) {
		server = s;
		standAgent = (StandAgent) a;
		standID = id;
		
		isEmpty = true;
	}
	
	/**
	 * Give kit to kit robot
	 * @param kg
	 * @return
	 */
	public void giveKit(KitGraphics kg) {
		if(!isEmpty) {
			kit = null;		// better way?
			isEmpty = true;
			server.sendData(new Request(Constants.STAND_GIVE_KIT_COMMAND, Constants.STAND_TARGET + standID, null));
		}
	}

	/**
	 * Kit robot places kit on stand
	 * @param kg - the kit
	 */
	public void receiveKit(KitGraphics kg) {
		if (isEmpty) {
			kit = kg;
			KitConfig config = kit.getKitConfig();
			isEmpty = false;
			server.sendData(new Request(Constants.STAND_RECEIVE_KIT_COMMAND, Constants.STAND_TARGET, config));
		}
	}

	/**
	 * Kit receives part from parts robot
	 * Can only receive a part if there is a kit on the stand.
	 * @param part - the part
	 */
	public void receivePart(PartGraphics part) {
		if(!isEmpty) {
			kit.addPart(part);
			PartType type = part.getPartType();
			server.sendData(new Request(Constants.STAND_RECEIVE_PART_COMMAND, Constants.STAND_TARGET, type));
		}
	}
	
	/**
	 * Used to send DONE messages back to agent
	 * @param r - the request
	 */
	public void receiveData(Request r) {
//		String cmd = r.getCommand();
		// TODO: double check with 201 that they don't need any done messages from stand
		
	}
	
}
