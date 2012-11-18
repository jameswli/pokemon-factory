package DeviceGraphicsDisplay;

import java.awt.Graphics2D;

import javax.swing.JComponent;

import Networking.Client;
import Networking.Request;
import Utils.Constants;
import Utils.Location;
import factory.KitConfig;
import factory.PartType;

/**
 * Graphics side of the kitting stand, and parent class to inspection stand
 * @author Shalynn Ho
 *
 */
public class StandGraphicsDisplay extends DeviceGraphicsDisplay {
	
	public static final int RIGHT_X_LOC = 280;
	public static final int Y_OFFSET = 100;
	public static final int STAND_WIDTH = 80;
	
	protected Client client;
	protected int standID;
	
	// the kit that is currently on the stand
	protected KitGraphicsDisplay kit;
	// the location of this stand
	protected Location location;
	// false if there is a kit on the stand
	protected boolean isEmpty;
	// the configuration of the current kit on the stand
	protected KitConfig kitConfig;
	
	/**
	 * 
	 * @param km - the kit manager
	 * @param id - stand ID - 1,2: kit stands; 0: inspection stand
	 */
	public StandGraphicsDisplay(Client c, int id) {
		client = c;
		standID = id;
		isEmpty = true;
		kit = new KitGraphicsDisplay();
		// TODO: set location of kit based on standID
		if (id % 2 == 0) {
			location = new Location((RIGHT_X_LOC - STAND_WIDTH/2) + client.getOffset(), standID*Y_OFFSET + Y_OFFSET);
		} else {
			location = new Location(RIGHT_X_LOC + client.getOffset(), standID*Y_OFFSET + Y_OFFSET);
		}
	}
	
	@Override
	public void draw(JComponent c, Graphics2D g) {
		g.drawImage(Constants.STAND_IMAGE, location.getX() + client.getOffset(), location.getY(), c);
		if (!isEmpty) {
			kit.draw(c,g);
		}	
	}

	public void giveKit() {
		isEmpty = true;
		kitConfig = null;
	}
	
	public void receiveKit(KitConfig config) {
		isEmpty = false;
		kitConfig = config;
	}
	
	public void receivePart(PartGraphicsDisplay pgd) {
		kit.receivePart(pgd);
	}


	/**
	 * Receives and sorts messages/data from the server
	 * 
	 * @param r - the request to be parsed
	 */
	public void receiveData(Request r) {
		String cmd = r.getCommand();
		
		if (cmd.equals(Constants.STAND_GIVE_KIT_COMMAND)) {
			giveKit();
			
		} else if (cmd.equals(Constants.STAND_RECEIVE_KIT_COMMAND)) {
			KitConfig config = (KitConfig) r.getData();
			receiveKit(config);
			
		} else if (cmd.equals(Constants.STAND_RECEIVE_PART_COMMAND)) {
			PartType type = (PartType) r.getData();
			receivePart(new PartGraphicsDisplay(type));
		}
		
		
	}

	@Override
	public void setLocation(Location newLocation) {
		location = newLocation;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
