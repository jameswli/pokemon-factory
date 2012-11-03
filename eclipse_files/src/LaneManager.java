import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import DeviceGraphicsDisplay.FeederGraphicsDisplay;
import GUI.NetworkingButtonListener;
import GUI.OverlayPanel;
import Networking.Client;
import Networking.Request;
import Utils.Constants;


public class LaneManager extends Client {
	// Temp values. Feel free to change
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 600;
	
	private ArrayList<FeederGraphicsDisplay> feeders = new ArrayList<FeederGraphicsDisplay>();
	
	public LaneManager() {
		super();
		clientName = Constants.LANE_MNGR_CLIENT;
		// initStreams();
		
		JLabel label = new JLabel("Lane Manager");
		label.setForeground(Color.WHITE);
		label.setFont(new Font("SansSerif", Font.PLAIN, 40));
		label.setHorizontalAlignment(JLabel.CENTER);
		add(label);
		
		OverlayPanel panel = new OverlayPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setVisible(true);
		
		JButton testButton = new JButton("Test button");
		testButton.addActionListener(new NetworkingButtonListener("Testing", Constants.SERVER_TARGET, writer));
		panel.add(testButton);
	}
	
	@Override
	public void receiveData(Request req) {
		if(req.getTarget().contains(Constants.FEEDER_TARGET)) {
			int targetID = Integer.parseInt(req.getTarget().substring(req.getTarget().indexOf(':')+1));
			feeders.get(targetID).receiveData(req);
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Client.setUpJFrame(frame, WINDOW_WIDTH, WINDOW_HEIGHT);
		
		LaneManager mngr = new LaneManager();
		frame.add(mngr);
		mngr.setVisible(true);
		frame.validate();
	}
	
	@Override
	public void paintComponent(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		
		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
	}
}