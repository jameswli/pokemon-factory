package DeviceGraphicsDisplay;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

import Networking.Client;
import Networking.Request;
import Utils.Constants;
import Utils.Location;

/**
 * This class handles drawing of the feeder and diverter.
 * @author Harry Trieu
 *
 */

public class FeederGraphicsDisplay extends DeviceGraphicsDisplay {
	// this will store a reference to the client
	private Client client;
	
	private static final int FEEDER_HEIGHT = 120;
	private static final int FEEDER_WIDTH = 120;
	private static final int DIVERTER_HEIGHT = 40;
	private static final int DIVERTER_WIDTH = 120;
	
	private static final double DIVERTER_POINTING_TOP_ANGLE = 0.09;
	private static final double DIVERTER_POINTING_BOTTOM_ANGLE = -0.09;
	private static final double DIVERTER_STEP = Math.abs((DIVERTER_POINTING_TOP_ANGLE-DIVERTER_POINTING_BOTTOM_ANGLE)/20);
	private static final int STEPS_TO_ROTATE_DIVERTER = (1000/Constants.TIMER_DELAY);
	
	// image of the diverter
	private Image diverterImage;
	// image of the feeder
	private Image feederImage;
	// true if the diverter is pointing to the top lane
	private boolean diverterTop;
	// number of steps remaining for the diverter to finish rotating
	private int animationCounter;
	
	// new bin to animate
	private BinGraphicsDisplay bgd; 
	
	private boolean haveBin;
	
	// location of the feeder
	private Location feederLocation;
	// location of the diverter
	private Location diverterLocation;
	
	/**
	 * constructor
	 */
	public FeederGraphicsDisplay(Client cli, Location loc) {
		// store a reference to the client
		client = cli;
		
		// set the path of the diverter image
		diverterImage = Toolkit.getDefaultToolkit().getImage("src/images/Diverter.png");
		// set the path of the feeder image
		feederImage = Toolkit.getDefaultToolkit().getImage("src/images/Feeder.png");
		
		// set the feeder's default location
		feederLocation = loc;
		
		// set the diverter's default location
		diverterLocation = new Location(feederLocation.getX()-90, feederLocation.getY()+(FEEDER_HEIGHT/2)-(DIVERTER_HEIGHT/2));
		
		// diverter initially points to the top lane
		diverterTop = true;
		
		// do not animate the diverter rotating
		animationCounter = -1;
					
		// force an initial repaint to display feeder and diverter
		client.repaint();
	}
	
	@Override
	public void draw(JComponent c, Graphics2D g) {
		AffineTransform originalTransform = g.getTransform();
		
		if (animationCounter < 0) {
			if (diverterTop) {
				g.rotate(DIVERTER_POINTING_TOP_ANGLE, feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
			} else {
				g.rotate(DIVERTER_POINTING_BOTTOM_ANGLE, feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
			}
		} else {
			if (diverterTop) {
				g.rotate(DIVERTER_POINTING_BOTTOM_ANGLE - ((STEPS_TO_ROTATE_DIVERTER-animationCounter)*DIVERTER_STEP), feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
				animationCounter--;
			} else {
				g.rotate(DIVERTER_POINTING_TOP_ANGLE + ((STEPS_TO_ROTATE_DIVERTER-animationCounter)*DIVERTER_STEP), feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
				animationCounter--;
			}
		}
		 
		if (haveBin) {
			bgd.draw();
		}
		
		
		g.drawImage(diverterImage, diverterLocation.getX(), diverterLocation.getY(), c);
		g.setTransform(originalTransform);
		g.drawImage(feederImage, feederLocation.getX(), feederLocation.getY(), c);
		
	}

	@Override
	public void setLocation(Location newLocation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveData(Request req) {
		if (req.getCommand().equals(Constants.FEEDER_FLIP_DIVERTER_COMMAND)) {
			animationCounter = 20;
			diverterTop = !diverterTop;
		} if (req.getCommand().equals(Constants.FEEDER_BIN_RECEIVED)) {
			bgd = new BinGraphicsDisplay(new Location())
			haveBin = true;
		}
	}

}
