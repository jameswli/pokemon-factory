package DeviceGraphicsDisplay;

import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JComponent;

import Networking.Client;
import Networking.Request;
import Utils.Constants;
import Utils.Location;
import factory.PartType;

/**
 * This class contains the graphics display components for a lane.
 * 
 * @author Shalynn Ho
 * 
 */
public class LaneGraphicsDisplay extends DeviceGraphicsDisplay {
	private static final int PART_OFFSET = 19;

	// horizontal length of the Lane image
	private static final int LANE_LENGTH = 210;
	// start and end x-coordinates of Part on the Lane
	private static final int LANE_BEG_X = 850, LANE_END_X = 640;
	// width and height of the part
	private static final int PART_WIDTH = 20;
	// max number of parts that can be on a Lane
	private static final int MAX_PARTS = LANE_LENGTH / PART_WIDTH;
	// number of lines on the lane
	private static final int NUMLINES = 1 + LANE_LENGTH / (4 * PART_WIDTH);
	// space between each line
	private static final int LINESPACE = LANE_LENGTH / NUMLINES;
	// width of lane lines
	private static final int LINE_WIDTH = 3;

	// stores the parts on the lane
	private ArrayList<PartGraphicsDisplay> partsOnLane;
	// start location of parts on this lane
	private final Location partStartLoc;
	// array list of locations of the lane lines
	private ArrayList<Location> laneLines;

	// the LaneManager (client) which talks to the Server
	private Client client;
	// the ID of this Lane
	private int laneID;
	// the amplitude of this lane
	private int amplitude = 1;
	// true if Lane is on
	private boolean laneOn = true;
	// use to make sure only 1 message is sent to agent for each part that reaches end of lane
	private boolean receivePartDoneSent = false;
	private boolean purgeDoneSent = false;
	// V0 only, stops parts from going down lane without bin
	private boolean partAtLaneEnd = false;
	private boolean purging = false;

	/**
	 * LGD constructor
	 * 
	 * @param lm - the lane manager (client)
	 * @param lid  - lane ID
	 */
	public LaneGraphicsDisplay(Client c, int lid) {
		client = c;
		laneID = lid;

		partsOnLane = new ArrayList<PartGraphicsDisplay>();
		// generate lane start location
		location = new Location(LANE_END_X, 53 + laneID * 75);
		
		// for reference only
		partStartLoc = new Location(LANE_BEG_X, location.getY()
				+ (PART_WIDTH / 2) - PART_OFFSET);
		

		resetLaneLineLocs();
	}


	/**
	 * Animates lane movement and sets location of parts moving down lane
	 * 
	 * @param c - component on which this is drawn
	 * @param g - the graphics component on which this draws
	 */
	@Override
	public void draw(JComponent c, Graphics2D g) {
		if (laneOn) {
			if (laneID % 2 == 0) {
				g.drawImage(Constants.LANE_IMAGE1, location.getX() + client.getOffset(), location.getY(), c);
			} else {
				g.drawImage(Constants.LANE_IMAGE2, location.getX() + client.getOffset(), location.getY(), c);
			}

			// animate lane movements using lines
			for (int i = 0; i < laneLines.size(); i++) {
				g.drawImage(Constants.LANE_LINE, laneLines.get(i).getX() + client.getOffset(),
						laneLines.get(i).getY(), c);
			}
			moveLane();

			// animate parts moving down lane
			if (partsOnLane.size() > 0) {
								
				int min = (MAX_PARTS < partsOnLane.size()) ? MAX_PARTS
						: partsOnLane.size(); // whichever is less
				
				for (int i = 0; i < min; i++) {
					PartGraphicsDisplay pgd = partsOnLane.get(i);
					Location loc = pgd.getLocation();

					if (i == 0) { // first part on the lane
						if (loc.getX() > LANE_END_X) { // hasn't reached end of lane
							loc.incrementX(-amplitude);
							partAtLaneEnd = false;
						} else { // at end of lane
							if (!purging) {
								partAtLaneEnd = true;
								msgAgentReceivePartDone();
							} else {	// purging, continue till off lane
								if (loc.getX() > LANE_END_X + PART_WIDTH) {
									loc.incrementX(-amplitude);
								} else {	// once off lane and not visible, remove
									if (partsOnLane.size() > 0) {
										partsOnLane.remove(0);
									} else {	// all parts removed, done purging
										purging = false;
										msgAgentPurgingDone();
									}
								}
							}
						}
						
					} else { // all other parts on lane (not first)
						
						// part in front of i
						PartGraphicsDisplay pgdInFront = partsOnLane.get(i - 1);
						Location locInFront = pgdInFront.getLocation();

						// makes sure parts are spaced out as they appear on
						// lane, but don't overlap part in front
						if (locInFront.getX() <= (LANE_BEG_X - (2 * PART_WIDTH))
								&& (loc.getX() > (locInFront.getX() + PART_WIDTH))) {
							loc.incrementX(-amplitude);
						}
					}
					vibrateParts(loc);
					pgd.setLocation(loc);
					pgd.drawWithOffset(c, g, client.getOffset());
					
				} // end for loop
				
			} else if (purging) {
				purging = false;
				msgAgentPurgingDone();
			}
		} else { // lane is off
			if (laneID % 2 == 0) {
				g.drawImage(Constants.LANE_IMAGE1, location.getX() + client.getOffset(), location.getY(), c);
			} else {
				g.drawImage(Constants.LANE_IMAGE2, location.getX() + client.getOffset(), location.getY(), c);
			}
			
			// draw lane lines
			for (int i = 0; i < laneLines.size(); i++) {
				g.drawImage(Constants.LANE_LINE, laneLines.get(i).getX() + client.getOffset(), laneLines.get(i)
						.getY(), c);
			}
		}
	}

	/**
	 * Receives and sorts messages/data from the server
	 * 
	 * @param r
	 *            - the request to be parsed
	 */
	public void receiveData(Request r) {
		String cmd = r.getCommand();
		// parse data request here

		if (cmd.equals(Constants.LANE_PURGE_COMMAND)) {
			purge();

		} else if (cmd.equals(Constants.LANE_SET_AMPLITUDE_COMMAND)) {
			amplitude = (Integer) r.getData();

		} else if (cmd.equals(Constants.LANE_TOGGLE_COMMAND)) {
			laneOn = (Boolean) r.getData();

		} else if (cmd.equals(Constants.LANE_SET_STARTLOC_COMMAND)) {
			location = (Location) r.getData();
			
		} else if (cmd.equals(Constants.LANE_RECEIVE_PART_COMMAND)) {
				PartType type = (PartType) r.getData();
				receivePart(type);
			
		} else if (cmd.equals(Constants.LANE_GIVE_PART_TO_NEST)) {
			givePartToNest();

		} else {
			System.out.println("LANE_GD: command not recognized.");
		}
	}

	/**
	 * Set location of this lane
	 */
	@Override
	public void setLocation(Location newLocation) {
		location = newLocation;
	}

	/**
	 * Give part to nest, removes from this lane
	 */
	private void givePartToNest() {
		partsOnLane.remove(0);
		receivePartDoneSent = false; // reset
		client.sendData(new Request(Constants.LANE_GIVE_PART_TO_NEST
				+ Constants.DONE_SUFFIX, Constants.LANE_TARGET+laneID, null));
	}


	/**
	 * Animates the lane lines
	 */
	private void moveLane() {
			for (int i = 0; i < laneLines.size(); i++) {
				int xCurrent = laneLines.get(i).getX();
				if (xCurrent <= (LANE_END_X - LINE_WIDTH)) {
					if (i == 0) {
						int xPrev = laneLines.get(laneLines.size() - 1).getX();
						laneLines.get(i).setX(xPrev + LINESPACE);
					} else {
						int xPrev = laneLines.get(i-1).getX();
						laneLines.get(i).setX(xPrev + LINESPACE);
					}
				} else {
					laneLines.get(i).incrementX(-amplitude);
				}
			}
	}


	/**
	 * Purges lane of all parts
	 */
	private void purge() {
		System.out.println(laneID + ": lane purge() called");
		// TODO: lane should continue as is, parts fall off the lane
		purgeDoneSent = false;
		purging = true;
	}
	
	private void receivePart(PartType type) {
		PartGraphicsDisplay pg = new PartGraphicsDisplay(type);
		Location newLoc = new Location(location.getX() + LANE_LENGTH,
				location.getY() + (PART_WIDTH / 2) - PART_OFFSET);
		pg.setLocation(newLoc);
		partsOnLane.add(pg);
	}


	/**
	 * Creates an array list of Locations for the lane lines
	 */
	private void resetLaneLineLocs() {
		// create array list of location for lane lines
		laneLines = new ArrayList<Location>(NUMLINES);
		int startLineX = LANE_END_X + LINE_WIDTH;
		for (int i = 0; i < NUMLINES; i++) {
			laneLines.add(new Location(startLineX, location.getY()));
			startLineX += LINESPACE;
		}
	}

	/**
	 * Changes y-coords to show vibration down lane (may have to adjust values)
	 * 
	 * @param i
	 *            - counter, increments every call to draw
	 * @param loc
	 *            - location of the current part
	 */
	private void vibrateParts(Location loc) {
		// to show vibration down lane (may have to adjust values)
		if (loc.getY() >= (partStartLoc.getY() + 2)) {
			loc.incrementY(-2);
		} else if (loc.getY() <= (partStartLoc.getY() - 2)){
			loc.incrementY(2);
		}
	}
	
	/**
	 * Tells the agent that the part has reached the end of the lane.
	 * Make sure only sends message once for each part, not on every call to draw.
	 */
	private void msgAgentReceivePartDone() {
		if(partAtLaneEnd && (!receivePartDoneSent)) {
			client.sendData(new Request(Constants.LANE_RECEIVE_PART_COMMAND
					+ Constants.DONE_SUFFIX, Constants.LANE_TARGET+laneID, null));
			receivePartDoneSent = true;
		}
	}
	
	/**
	 * Tells the agent that purging is done.
	 * Make sure only sends message once for each part, not on every call to draw.
	 */
	private void msgAgentPurgingDone() {
		System.out.println(laneID + "msgAgentPurgingDone called. partsOnLane: "+partsOnLane.size() + ", purgeDoneSent: "+purgeDoneSent);

		if((partsOnLane.size() == 0) && (!purgeDoneSent)) {
			client.sendData(new Request(Constants.LANE_PURGE_COMMAND
					+ Constants.DONE_SUFFIX, Constants.LANE_TARGET+laneID, null));
			
			System.out.println(laneID + ": lane purge done sent");

			purgeDoneSent = true;
		}
	}

}
