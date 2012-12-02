package manager;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.Timer;

import manager.panel.FactoryProductionManagerPanel;
import DeviceGraphicsDisplay.ConveyorGraphicsDisplay;
import DeviceGraphicsDisplay.DeviceGraphicsDisplay;
import DeviceGraphicsDisplay.FeederGraphicsDisplay;
import DeviceGraphicsDisplay.GantryGraphicsDisplay;
import DeviceGraphicsDisplay.InspectionStandGraphicsDisplay;
import DeviceGraphicsDisplay.KitRobotGraphicsDisplay;
import DeviceGraphicsDisplay.LaneGraphicsDisplay;
import DeviceGraphicsDisplay.MessagingBoxGraphicsDisplay;
import DeviceGraphicsDisplay.NestGraphicsDisplay;
import DeviceGraphicsDisplay.PartsRobotDisplay;
import DeviceGraphicsDisplay.StandGraphicsDisplay;
import Networking.Client;
import Networking.Request;
import Utils.Constants;
import factory.KitConfig;
import factory.Order;

/**
 * This class processes new orders and animates the entire factory floor.
 * 
 * @author Shalynn Ho, Harry Trieu
 */
public class FactoryProductionManager extends Client implements ActionListener {
	// Window Dimensions
	private static final int WINDOW_WIDTH = 1200;
	private static final int WINDOW_HEIGHT = 700;

	// Create a new control panel for the FPM
	private FactoryProductionManagerPanel fpmPanel;

	private final FPMMusicAgent musicAgent;

	// Create a new timer
	private Timer timer;
	private final java.util.Timer musicTimer = new java.util.Timer();

	// Background music - Goldenrod City
	private Clip music, pokeflute, recovery, completed;

	/**
	 * Constructor
	 */
	public FactoryProductionManager() {
		super();
		clientName = Constants.FACTORY_PROD_MNGR_CLIENT;
		offset = 0;

		musicAgent = new FPMMusicAgent(this);
		musicAgent.startThread();

		initStreams();
		initGUI();
		initDevices();
		initMusic();

	}

	/**
	 * Initialize the GUI and start the timer.
	 */
	public void initGUI() {
		fpmPanel = new FactoryProductionManagerPanel(this, WINDOW_HEIGHT);

		add(fpmPanel, BorderLayout.EAST);
		fpmPanel.setVisible(true);

		timer = new Timer(Constants.TIMER_DELAY, this);
		timer.start();
	}

	/**
	 * Initialize the devices
	 */
	public void initDevices() {

		addDevice(Constants.STAND_TARGET + 0, new InspectionStandGraphicsDisplay(this));

		for (int i = 1; i < Constants.STAND_COUNT; i++) {
			addDevice(Constants.STAND_TARGET + i, new StandGraphicsDisplay(this, i));
		}

		addDevice(Constants.CONVEYOR_TARGET, new ConveyorGraphicsDisplay(this));
		addDevice(Constants.KIT_ROBOT_TARGET, new KitRobotGraphicsDisplay(this));
		addDevice(Constants.GANTRY_ROBOT_TARGET, new GantryGraphicsDisplay(this));

		for (int i = 0; i < Constants.LANE_COUNT; i++) {
			addDevice(Constants.LANE_TARGET + i, new LaneGraphicsDisplay(this, i));
		}

		for (int i = 0; i < Constants.NEST_COUNT; i++) {
			addDevice(Constants.NEST_TARGET + i, new NestGraphicsDisplay(this, i));
		}

		addDevice(Constants.PARTS_ROBOT_TARGET, new PartsRobotDisplay(this));

		for (int i = 0; i < Constants.FEEDER_COUNT; i++) {
			addDevice(Constants.FEEDER_TARGET + i, new FeederGraphicsDisplay(this, i));
		}

		addDevice(Constants.MESSAGING_BOX_TARGET, new MessagingBoxGraphicsDisplay(this));

	}

	private void initMusic() {
	}

	@Override
	public void stopMusic() {
		if (music.isRunning()) {
			music.stop();
		}
	}

	@Override
	public void startMusic() {
		// stopCompleted();
		// stopPokeflute();
		// stopRecovery();

		if (music != null) {
			music.loop(Clip.LOOP_CONTINUOUSLY);
		}
	}

	@Override
	public void startPokeflute() {
		musicAgent.startPokeflute();
	}

	@Override
	public void stopPokeflute() {
		if (pokeflute.isRunning()) {
			pokeflute.stop();
		}
	}

	@Override
	public void startRecovery() {
		musicAgent.msgStartRecovery();
	}

	@Override
	public void stopRecovery() {
		if (recovery.isRunning()) {
			recovery.stop();
		}
	}

	public void setConveyorExitTrue() {
		((ConveyorGraphicsDisplay) devices.get(Constants.CONVEYOR_TARGET)).setExit(true);
	}

	public void startCompleted() {
		musicAgent.msgStartCompleted();
	}

	public void stopCompleted() {
		if (completed.isRunning()) {
			completed.stop();
		}
	}

	/**
	 * Forward network requests to devices or panel for processing
	 * 
	 * @param req
	 *            incoming request
	 */
	@Override
	public void receiveData(Request req) {
		if (req.getTarget().equals(Constants.ALL_TARGET)) {
			if (req.getCommand().equals(Constants.FCS_UPDATE_KITS)) {
				fpmPanel.updateKitConfigs((ArrayList<KitConfig>) req.getData());
			} else if (req.getCommand().equals(Constants.FCS_UPDATE_ORDERS)) {
				ArrayList<Order> o = (ArrayList<Order>) req.getData();
				fpmPanel.updateOrders(o);
				if(o.size() == 0) {
					startCompleted();
				}
			} else if (req.getCommand().equals(Constants.FCS_SHIPPED_KIT)) {
				fpmPanel.decreaseCurrentKitCount();
				((ConveyorGraphicsDisplay) devices.get(Constants.CONVEYOR_TARGET)).setExit(true);
			}
		} else {
			synchronized (devices) {
				devices.get(req.getTarget()).receiveData(req);
			}
		}
	}

	/**
	 * Send a new order to the FCS for processing
	 * 
	 * @param o
	 *            order
	 */
	public void createOrder(Order o) {
		this.sendData(new Request(Constants.FCS_ADD_ORDER, Constants.FCS_TARGET, o));
	}

	/**
	 * Main method sets up the JFrame
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Client.setUpJFrame(frame, WINDOW_WIDTH, WINDOW_HEIGHT, "Factory Production Manager");

		FactoryProductionManager mngr = new FactoryProductionManager();
		frame.add(mngr);
		mngr.setVisible(true);
		frame.validate();
	}

	/**
	 * This function handles painting of graphics
	 */
	@Override
	public void paintComponent(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);

		synchronized (devices) {
			for (DeviceGraphicsDisplay device : devices.values()) {
				device.draw(this, g);
			}
		}
	}

	/**
	 * This function handles action events.
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		repaint();
	}
}
