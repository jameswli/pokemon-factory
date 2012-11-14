//Radius of Rotation arm 190

package DeviceGraphicsDisplay;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.ArrayList;

import javax.swing.JComponent;

import Networking.Client;
import Networking.Request;
import Utils.Constants;
import Utils.Location;

/**
 * Contains display components of ConveyorGraphics object
 * @author neetugeo
 */

public class ConveyorGraphicsDisplay extends DeviceGraphicsDisplay {

	Location locationGood;
	ArrayList<Location> conveyorLines;
	ArrayList<Location> conveyorLinesGood;
	ArrayList<Location> conveyorLinesBad;
	ArrayList<Location> exitLines;
	ArrayList<KitGraphicsDisplay> kitsOnConveyor;
	ArrayList<KitGraphicsDisplay> kitsToLeave;
	int velocity;
	Client client;
	boolean pickMe;

	public ConveyorGraphicsDisplay(Client cli) {
		locationGood = Constants.CONVEYOR_LOC; // location for exit lane, based off of input lane
		client = cli;
		conveyorLines = new ArrayList<Location>();
		conveyorLinesGood = new ArrayList<Location>();
		conveyorLinesBad = new ArrayList<Location>();
		
		//Filling Arrays with locations
		for (int i = 0; i < 8; i++) {
			conveyorLines.add(new Location(locationGood.getX() + (i * 20), locationGood.getY() + 120)); // creating an array list of conveyor line locations for painting
		}
		
		//Filling Arrays with locations
		for (int i = 0; i < 13; i ++) {
			conveyorLinesGood.add(new Location(locationGood.getX() + (i * 20), locationGood.getY()));
		}
		
		//Filling Arrays with locations
		for (int i = 0; i < 13; i ++) {
			conveyorLinesBad.add(new Location(locationGood.getX() + (i * 20), locationGood.getY() + 240));
		}
		
		velocity = 5;
		kitsOnConveyor = new ArrayList<KitGraphicsDisplay>();
		kitsToLeave = new ArrayList<KitGraphicsDisplay>();
		pickMe = true;
	}

	@Override
	public void setLocation(Location newLocation) {
		location = newLocation;
	}

	public void newKit() {
		print("Making new kit");
		KitGraphicsDisplay temp = new KitGraphicsDisplay();
		temp.setLocation(new Location(0, 200));
		kitsOnConveyor.add(temp);
	}

	public void giveKitAway() {
		System.out.println("Give it away");
		kitsOnConveyor.remove(0);
		velocity = 5;
		pickMe = true;
	}

	public void sendOut() {
		kitsToLeave.remove(0);
	}

	public void newExitKit() {
		KitGraphicsDisplay temp = new KitGraphicsDisplay();
		temp.setLocation(new Location(0,400));
		kitsToLeave.add(temp);
	}

	public void animationDone(Request r) {
		client.sendData(r);
	}

	@Override
	public void draw(JComponent c, Graphics2D g2) {
		g2.drawImage(Constants.TEST_CONVEYOR_IMAGE, -140, 185, c);
		for (int i = 0; i < conveyorLines.size(); i++) {
			g2.drawImage(Constants.TEST_CONVEYOR_LINE_IMAGE, conveyorLines.get(i).getX(), conveyorLines.get(i).getY(),c);
			moveIn(i);
		}
		
		g2.drawImage(Constants.TEST_CONVEYOR_IMAGE, -40, 65, c);
		for (int i = 0; i < conveyorLinesGood.size(); i++) {
			g2.drawImage(Constants.TEST_CONVEYOR_LINE_IMAGE, conveyorLinesGood.get(i).getX(), conveyorLinesGood.get(i).getY(), c);
			moveOut(i, conveyorLinesGood);
		}
		
		g2.drawImage(Constants.TEST_CONVEYOR_IMAGE, -40, 305, c);
		for (int i = 0; i < conveyorLinesBad.size(); i++) {
			g2.drawImage(Constants.TEST_CONVEYOR_LINE_IMAGE, conveyorLinesBad.get(i).getX(), conveyorLinesBad.get(i).getY(), c);
			moveOut(i, conveyorLinesBad);
		}
		
		
		

		for (int j = 0; j < kitsOnConveyor.size(); j++) {
			if (kitsOnConveyor.get(0).getLocation().getX() < 90) {
				KitGraphicsDisplay tempKit = kitsOnConveyor.get(j);
				tempKit.draw(c, g2);
				Location temp = tempKit.getLocation();
				tempKit.setLocation(new Location(temp.getX() + velocity, temp.getY()));
			} else if (kitsOnConveyor.get(0).getLocation().getX() >= 90){
				velocity = 0;
				KitGraphicsDisplay tempKit = kitsOnConveyor.get(j);
				tempKit.draw(c, g2);
				Location temp = tempKit.getLocation();
				tempKit.setLocation(new Location(temp.getX() + velocity, temp.getY()));
				if (pickMe == true) {
					 
					 animationDone(new Request(
								Constants.CONVEYOR_MAKE_NEW_KIT_COMMAND
										+ Constants.DONE_SUFFIX,
								Constants.CONVEYOR_TARGET, null));
				
				pickMe = false;
				}
			}
		}

		for (int i = 0; i < kitsToLeave.size(); i++) {
			
			KitGraphicsDisplay tempKit = kitsToLeave.get(i);
			tempKit.draw(c, g2);
			if (tempKit.getLocation().getX() == 800) {
				animationDone(new Request(
						Constants.CONVEYOR_RECEIVE_KIT_COMMAND
								+ Constants.DONE_SUFFIX,
						Constants.CONVEYOR_TARGET, null));
			}
			Location temp = tempKit.getLocation();
			tempKit.setLocation(new Location(temp.getX() + velocity, temp
					.getY()));
		}
	}

	/**
	 * Moves conveyor lines into the factory
	 * @param i
	 */
	
	public void moveIn(int i) {
		// if bottom of black conveyor line is less than this y position
		if (conveyorLines.get(i).getX() < 150){
			// when a conveyor is done being painted, move the location for next
			// repaint
			conveyorLines.get(i).setX(conveyorLines.get(i).getX() + velocity);
		} else if (conveyorLines.get(i).getX() >= 150) {
			// if bottom of black conveyor line is greater than or equal to this
			// y position
			conveyorLines.get(i).setX(0);
		}
	}

	/**
	 * Move conveyor lines out of the factory.
	 * @param i
	 */
	
	public void moveOut(int i, ArrayList<Location> a) {
		if (a.get(i).getX() > 0) {
			a.get(i).setX(a.get(i).getX() - velocity);
			//ConveyorLines move backward this time.
		} else if (a.get(i).getX() <= 0) {
			a.get(i).setX(250);
		}
	}


	/**
	 * Function created to change the velocity of the conveyor
	 * @param i
	 */
	public void setVelocity(int i) {
		velocity = i;
	}

	@Override
	public void receiveData(Request req) {
		String command = req.getCommand();
		String target = req.getTarget();
		Object object = req.getData();

		if (command.equals(Constants.CONVEYOR_GIVE_KIT_TO_KIT_ROBOT_COMMAND)) {
			giveKitAway();
			print("Giving kit to Kit Robot");
		} else if (command.equals(Constants.CONVEYOR_MAKE_NEW_KIT_COMMAND)) {
			newKit();
		} else if (command.equals(Constants.CONVEYOR_CHANGE_VELOCITY_COMMAND)) {
			// must take in int somehow
		} else if (command.equals(Constants.CONVEYOR_RECEIVE_KIT_COMMAND)) {
			newExitKit();
		}
	}
	
	public String getName() {
		return "ConveyorGD";
	}
}
