package DeviceGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Networking.Request;
import Networking.Server;
import Utils.Constants;
import Utils.Location;
import Utils.PartData;
import agent.Agent;
import agent.PartsRobotAgent;

// import factory.data.Kit;

public class PartsRobotGraphics implements
		GraphicsInterfaces.PartsRobotGraphics, DeviceGraphics {

	Location initialLocation; // initial location of robot
	Location currentLocation; // current location of robot
	boolean arm1, arm2, arm3, arm4; // whether the arm is full, initialized
									// empty
	List<PartGraphics> partArray = Collections
			.synchronizedList(new ArrayList<PartGraphics>()); // an array of
																// parts that
																// allocates
																// memory for 4
																// parts
	KitGraphics kit;
	int i;
	private final Server server;
	private final PartsRobotAgent partsRobotAgent;

	public PartsRobotGraphics(Server s, Agent a) {
		partsRobotAgent = (PartsRobotAgent) a;
		initialLocation = new Location(250, 450);
		currentLocation = initialLocation;
		arm1 = false;
		arm2 = false;
		arm3 = false;
		arm4 = false;
		// partArray = new ArrayList<PartGraphics>();
		i = 0;
		server = s;
	}

	@Override
	public void pickUpPart(PartGraphics pg) {
		// TODO Auto-generated method stub
		partArray.add(pg);
		rotateArm();

		PartData pd = new PartData(pg.getLocation(), pg.getPartType());
		// V0 hack
		Location tempLoc = new Location(550, 100);
		// server.sendData(new Request(Constants.PARTS_ROBOT_PICKUP_COMMAND,
		// Constants.PARTS_ROBOT_TARGET, tempLoc));
		server.sendData(new Request(Constants.PARTS_ROBOT_PICKUP_COMMAND,
				Constants.PARTS_ROBOT_TARGET, pd));
	}

	@Override
	public void givePartToKit(PartGraphics part, KitGraphics kit) {
		synchronized (partArray) {
			for (PartGraphics p : partArray) {
				if (p == part) {
					partArray.remove(p);
				}
			}
			PartData pd = new PartData(kit.getLocation());
			Location tempLoc = new Location(200, 400);
			server.sendData(new Request(Constants.PARTS_ROBOT_GIVE_COMMAND,
					Constants.PARTS_ROBOT_TARGET, pd));
		}
	}

	// rotates the arm
	public void rotateArm() {
		if (!isFullArm1()) {
			arm1 = true;
		} else if (!isFullArm2()) {
			arm2 = true;
		} else if (!isFullArm3()) {
			arm3 = true;
		} else if (!isFullArm4()) {
			arm4 = true;
		}
		i++;
	}

	public void derotateArm() {
		if (isFullArm4()) {
			arm4 = false;
		} else if (isFullArm3()) {
			arm3 = false;
		} else if (isFullArm2()) {
			arm2 = false;
		} else if (isFullArm1()) {
			arm1 = false;
		}
		i--;
	}

	// returns true if arm1 is full
	public boolean isFullArm1() {
		return arm1;
	}

	// returns true if arm2 is full
	public boolean isFullArm2() {
		return arm2;
	}

	// returns true if arm3 is full
	public boolean isFullArm3() {
		return arm3;
	}

	// returns true if arm4 is full
	public boolean isFullArm4() {
		return arm4;
	}

	// returns the current Location
	public Location getLocation() {
		return currentLocation;
	}

	@Override
	public void receiveData(Request req) {
		if (req.getCommand().equals(
				Constants.PARTS_ROBOT_RECEIVE_PART_COMMAND
						+ Constants.DONE_SUFFIX)) {
			partsRobotAgent.msgPickUpPartDone();
		} else if (req.getCommand().equals(
				Constants.PARTS_ROBOT_GIVE_COMMAND + Constants.DONE_SUFFIX)) {
			partsRobotAgent.msgGivePartToKitDone();
		}
	}

}
