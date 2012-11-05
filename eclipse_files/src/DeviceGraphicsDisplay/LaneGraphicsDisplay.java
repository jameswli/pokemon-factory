package DeviceGraphicsDisplay;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import factory.data.PartType;

import DeviceGraphics.PartGraphics;
import Networking.Client;
import Networking.Request;
import Utils.Animation;
import Utils.Constants;
import Utils.Location;

/**
 * This class contains the graphics display components for a lane.
 * 
 * @author Shalynn Ho
 * 
 */
public class LaneGraphicsDisplay extends DeviceGraphicsDisplay {
	// horizontal length of the Lane image
	private static final int LANE_LENGTH = 400;
	// start and end x-coordinates of Part on the Lane
	private static final int LANE_BEG_X = 599;
	private static final int LANE_END_X = 199;
	// width and height of the part
	private static final int PART_WIDTH = 20, PART_HEIGHT = 20;
	// max number of parts that can be on a Lane
	private static final int MAX_PARTS = LANE_LENGTH / PART_WIDTH;
	// space in between lane lines (from upper left to upper left)
	private static final int NUMLINES = LANE_LENGTH/(PART_WIDTH) - 2; // may need to change this, don't have images yet
	// width of lane lines
	private static final int LINE_WIDTH = 3;
	
	// y-coordinates of Part on Lane, depending on laneID
	private static final int LANE0_Y = 500;
	private static final int LANE1_Y = 450;
	private static final int LANE2_Y = 400;
	private static final int LANE3_Y = 350;
	private static final int LANE4_Y = 300;
	private static final int LANE5_Y = 250;
	private static final int LANE6_Y = 200;
	private static final int LANE7_Y = 150;

	// stores static ImageIcon emptyLane1, emptyLane2
	private static Image laneImg;
	private static Image laneLine;


	// stores the parts on the lane
	private ArrayList<PartGraphicsDisplay> partsOnLane;

	// Location of this lane
	private Location laneLoc;
	// start location of parts on this lane
	private Location partStartLoc;
	// array list of locations of the lane lines
	private ArrayList<Location> laneLines;

	// the LaneManager (client) which talks to the Server
	private Client laneManager;
	// the ID of this Lane
	private int laneID;
	// the amplitude of this lane
	private int amplitude = 5;
	// true if Lane is on
	private boolean laneOn = true;
	// counter
	private int counter = 0;

	public LaneGraphicsDisplay(Client lm, int lid) {
		laneManager = lm;
		laneID = lid;

		// TODO: load empty lane images, add to array list (get image from
		// CONSTANTS when added)
		laneImg = Toolkit.getDefaultToolkit().getImage("src/images/lane.png");
		laneLine = Toolkit.getDefaultToolkit().getImage("src/images/laneline.png");


		partsOnLane = new ArrayList<PartGraphicsDisplay>();
		setLaneLoc(laneID);
		partStartLoc = new Location(LANE_BEG_X, laneLoc.getY()
				+ (PART_WIDTH / 2));
		
		// create array list of location for lane lines
		resetLaneLineLocs();
		
	}

	public LaneGraphicsDisplay(Client lm, Location loc, int lid) {
		laneManager = lm;
		laneLoc = loc;
		
		System.out.println("lane loc, x: "+laneLoc.getX()+", y: "+laneLoc.getY());
		laneID = lid;

		// TODO: load empty lane images, add to array list (get image from
		// CONSTANTS when added)
		laneImg = Toolkit.getDefaultToolkit().getImage("src/images/lane.png");
		laneLine = Toolkit.getDefaultToolkit().getImage("src/images/laneline.png");

		partsOnLane = new ArrayList<PartGraphicsDisplay>();
		partStartLoc = new Location(laneLoc.getX() + LANE_LENGTH, laneLoc.getY()
				+ (PART_WIDTH / 2));
		
		// create array list of location for lane lines
		resetLaneLineLocs();
	}

	@Override
	public void draw(JComponent c, Graphics2D g) {
		if (laneOn) {
			// need image(s) of lane and/or lane lines?
			g.drawImage(laneImg, laneLoc.getX(), laneLoc.getY(), c);

			// TODO: animate lane movements, using lines??
			for(int i = 0; i < laneLines.size(); i++){
				g.drawImage(laneLine, laneLines.get(i).getX(), laneLines.get(i).getY(), c);
			}
			laneMove();

			// TODO: animate parts moving down lane
			if (partsOnLane != null) {
				int min = (MAX_PARTS < partsOnLane.size()) ? MAX_PARTS
						: partsOnLane.size(); // whichever is less
				for (int i = 0; i < min; i++) {
					PartGraphicsDisplay pgd = partsOnLane.get(i);
					Location loc = pgd.getLocation();

					if (i == 0) { // first part on the lane
						if (loc.getX() > LANE_END_X) {	// hasn't reached end of lane
							loc.incrementX(-amplitude);
						}
					} else {
						// part in front of i
						PartGraphicsDisplay pgdInFront = partsOnLane.get(i - 1);
						Location locInFront = pgdInFront.getLocation();
						// makes sure parts are spaced out as they appear on lane, but don't overlap part in front
						if (locInFront.getX() <= (LANE_BEG_X - (2 * PART_WIDTH)) && (loc.getX() > (locInFront.getX() + PART_WIDTH))) {
							loc.incrementX(-amplitude);
						}
					}
					vibrateParts(counter, loc);
					pgd.setLocation(loc);
					
					pgd.draw(c,g); //TODO: remove later, for v0 testing purposes only
				}
			}
		} else { // lane is off
			g.drawImage(laneImg, laneLoc.getX(), laneLoc.getY(), c);
		}
	}

	public void givePartToNest() {
		partsOnLane.remove(0);
	}

	/**
	 * 
	 */
	public void purge() {
		// lane should continue as is
	}

	/**
	 * 
	 * @param r
	 */
	public void receiveData(Request r) {
		String cmd = r.getCommand();
		// parse data request here

		if (cmd.equals(Constants.LANE_PURGE_COMMAND)) {
			purge();

		} else if (cmd.equals(Constants.LANE_SEND_ANIMATION_COMMAND)) {
			Animation ani = (Animation) r.getData();
			// TODO: DO SOMETHING WITH THIS ANIMATION

		} else if (cmd.equals(Constants.LANE_SET_AMPLITUDE_COMMAND)) {
			amplitude = (Integer) r.getData();

		} else if (cmd.equals(Constants.LANE_TOGGLE_COMMAND)) {
			laneOn = (Boolean) r.getData();

		} else if (cmd.equals(Constants.LANE_SET_STARTLOC_COMMAND)) {
			laneLoc = (Location) r.getData();
		} else if (cmd.equals(Constants.LANE_NEW_PART_COMMAND)) {
			// TODO:
			PartType partType = (PartType) r.getData();
			PartGraphicsDisplay pgd = new PartGraphicsDisplay(partType);
			pgd.setLocation(partStartLoc);
			partsOnLane.add(pgd);
			System.out.println("partsOnLane size: "+partsOnLane.size());
		} else if (cmd.equals(Constants.LANE_GIVE_PART_TO_NEST)) {
			

		} else {
			System.out.println("LANEGRAPHICSDISP: command not recognized.");
		}
	}

	/**
	 * 
	 * @param amp
	 */
	public void setAmplitude(int amp) {
		amplitude = amp;
	}

	@Override
	public void setLocation(Location newLocation) {
		laneLoc = newLocation;
	}
	
	/**
	 * 
	 * @param on
	 */
	public void toggleSwitch(boolean on) {
		laneOn = on;
	}

	private void laneMove() {
		counter++;
		if (counter % (PART_WIDTH/amplitude) == 0) {	// reset lane lines
			resetLaneLineLocs();
		} else {
			for (int i = 0; i < laneLines.size(); i++) {
				laneLines.get(i).incrementX(-amplitude);
			}
		}
		
	}

	/**
	 * 
	 */
	private void lineUpParts() {
		// depends on if we're moving parts in logic or display side
	
	}

	/**
	 * 
	 */
	private void movePartDownLane() {
		// depends on if we're moving parts in logic or display side
	}

	private void setLaneLoc(int id) {

		switch (id) {
		case 0:
			laneLoc = new Location(LANE_BEG_X, LANE0_Y);
			break;
		case 1:
			laneLoc = new Location(LANE_BEG_X, LANE1_Y);
			break;
		case 2:
			laneLoc = new Location(LANE_BEG_X, LANE2_Y);
			break;
		case 3:
			laneLoc = new Location(LANE_BEG_X, LANE3_Y);
			break;
		case 4:
			laneLoc = new Location(LANE_BEG_X, LANE4_Y);
			break;
		case 5:
			laneLoc = new Location(LANE_BEG_X, LANE5_Y);
			break;
		case 6:
			laneLoc = new Location(LANE_BEG_X, LANE6_Y);
			break;
		case 7:
			laneLoc = new Location(LANE_BEG_X, LANE7_Y);
			break;
		default:
			System.out.println("id not recognized.");
		}
	}

	private void resetLaneLineLocs() {
		// create array list of location for lane lines
		laneLines = new ArrayList<Location>();
		int startLineX = LANE_BEG_X - LINE_WIDTH;
		for (int i = 0; i < NUMLINES; i++) {
			laneLines.add(new Location(startLineX, laneLoc.getY()));
			startLineX -= (PART_WIDTH + LINE_WIDTH);
		}
	}
	
	/**
	 * change y-coords to show vibration down lane (may have to adjust values)
	 */
	private void vibrateParts(int i, Location loc) {
		// to show vibration down lane (may have to adjust values)
		if (i % 2 == 0) {
			loc.incrementY(2);
		} else {
			loc.incrementY(-2);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
