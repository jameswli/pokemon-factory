package manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import manager.util.OverlayPanel;
import DeviceGraphicsDisplay.CameraGraphicsDisplay;
import DeviceGraphicsDisplay.DeviceGraphicsDisplay;
import DeviceGraphicsDisplay.FeederGraphicsDisplay;
import DeviceGraphicsDisplay.GantryGraphicsDisplay;
import DeviceGraphicsDisplay.LaneGraphicsDisplay;
import DeviceGraphicsDisplay.NestGraphicsDisplay;
import Networking.Client;
import Networking.Request;
import Utils.Constants;

public class LaneManager extends Client implements ActionListener{
	// Temp values. Feel free to change
	private static final int WINDOW_WIDTH = 400;
	private static final int WINDOW_HEIGHT = 700;
	
	// Create a new timer
	private Timer timer;
	
	private OverlayPanel messagePanel;
	private JLabel currentMessage;
	
	/**
	 * Constructor
	 */
	public LaneManager() {
		super();
		clientName = Constants.LANE_MNGR_CLIENT;
		offset = -540;
		
		initStreams();
		initGUI();
		initDevices();
	}
	
	/**
	 * Initialize the GUI and start the timer.
	 */
	public void initGUI() {
		messagePanel = new OverlayPanel();
		add(messagePanel, BorderLayout.SOUTH);
		
		currentMessage = new JLabel("Click anywhere on the lane to produce a jam at that location.");
		currentMessage.setForeground(Color.WHITE);
		currentMessage.setFont(new Font("SansSerif", Font.PLAIN, 12));
		currentMessage.setHorizontalAlignment(JLabel.CENTER);
		currentMessage.setVisible(true);
		messagePanel.add(currentMessage);
		
		messagePanel.setVisible(true);
		
		timer = new Timer(Constants.TIMER_DELAY, this);
		timer.start();
	}
	
	/**
	 * Initialize the devices
	 */
	public void initDevices() {
		for (int i = 0; i < Constants.LANE_COUNT; i++) {
			addDevice(Constants.LANE_TARGET + i, new LaneGraphicsDisplay(this, i));
		}
	
		for (int i = 0; i < Constants.NEST_COUNT; i++) {
			addDevice(Constants.NEST_TARGET + i, new NestGraphicsDisplay(this, i));
		}

		for (int i = 0; i < Constants.FEEDER_COUNT; i++) {
			addDevice(Constants.FEEDER_TARGET + i, new FeederGraphicsDisplay(this, i));
		}
		
		addDevice(Constants.CAMERA_TARGET, new CameraGraphicsDisplay(this));
		
		addDevice(Constants.GANTRY_ROBOT_TARGET, new GantryGraphicsDisplay(this));

	}
	
	/**
	 * Forward network requests to devices processing
	 * @param req incoming request
	 */
	public void receiveData(Request req) {
		devices.get(req.getTarget()).receiveData(req);
	}
	
	/**
	 * Main method sets up the JFrame
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Client.setUpJFrame(frame, WINDOW_WIDTH, WINDOW_HEIGHT, "Lane Manager");
		
		LaneManager mngr = new LaneManager();
		frame.add(mngr);
		mngr.setVisible(true);
		frame.validate();
	}
	
	/**
	 * This function handles painting of graphics
	 */
	public void paintComponent(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
		
		for(DeviceGraphicsDisplay device : devices.values()) {
			device.draw(this, g);
		}
	}
	
	/**
	 * This function intercepts requests and drops them if the request is a "DONE" request
	 * @req Request to be sent.
	 */
	@Override
	public void sendData(Request req) {
		if (!req.getCommand().endsWith(Constants.DONE_SUFFIX)) {
			super.sendData(req);
		}
	}
	
	/**
	 * This function handles action events.
	 */
	public void actionPerformed(ActionEvent arg0) {
		repaint();
	}
}
