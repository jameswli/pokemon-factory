package DeviceGraphicsDisplay;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;

import Networking.Request;
import Utils.Location;

public class PartGraphicsDisplay extends DeviceGraphicsDisplay {
	Location partLocation;
	
	//NEED IMAGE NAMES
	Image partImage = Toolkit.getDefaultToolkit().getImage("PUT IMAGE NAME HERE");

	
	public void setLocation (Location newLocation) {
		partLocation = newLocation;
	}

	public void draw(JFrame myJFrame, Graphics2D g) {
		g.drawImage(partImage, partLocation.getX(), partLocation.getY(), myJFrame);
	}

	public void receiveData(Request req) {
	}
	
}
