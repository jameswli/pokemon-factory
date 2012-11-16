package manager.panel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import manager.FactoryProductionManager;
import manager.util.OverlayPanel;
import Utils.Constants;
import factory.KitConfig;
import factory.Order;

/**
*
* @author Shalynn Ho, Harry Trieu and Matt Zecchini
*/

public class FactoryProductionManagerPanel extends OverlayPanel implements ActionListener {
	// Width of the JPanel
	private static final int PANEL_WIDTH = 300;
	
	// A reference to the FactoryProductionManager client
	private FactoryProductionManager fpmClient;
	
	// Stores kits currently available for order
	private ArrayList<KitConfig> kitConfigs = new ArrayList<KitConfig>();
	// Stores current list of orders
	private ArrayList<Order> queue = new ArrayList<Order>();
	// Stores the selected kitConfig for a new order
	private KitConfig selectedKit;
	
	/** JComponents **/
	// Displays kits currently available for order
	private JComboBox kitComboBox;
	// Displays current schedule of orders
	private JTextArea orderScheduleTextArea;
	private SpinnerNumberModel spinnerModel;
	private JSpinner quantitySpinner;
	private DefaultComboBoxModel defaultComboBox;
	private JButton orderButton;
	private JScrollPane orderScrollPane;
	
	/**
	 * Constructor
	 * @param f a reference to the FactoryProductionManager client.
	 * @param height of the JFrame
	 */
	public FactoryProductionManagerPanel(FactoryProductionManager f, int height) {
		super();
		fpmClient = f;
		setPreferredSize(new Dimension(PANEL_WIDTH, height));
		setMinimumSize(new Dimension(PANEL_WIDTH, height));
		setMaximumSize(new Dimension(PANEL_WIDTH, height));
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
	
		kitComboBox = new JComboBox();
		defaultComboBox = (DefaultComboBoxModel)kitComboBox.getModel();
		
		// TODO: REMOVE - FOR TESTING ONLY
		kitConfigs = (ArrayList<KitConfig>) Constants.DEFAULT_KITCONFIG.clone();
		
		for (int i = 0; i < kitConfigs.size(); i++) {
			kitComboBox.addItem(kitConfigs.get(i).getName());
		}
		
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.PAGE_START;
		add(kitComboBox, c);
		
		spinnerModel = new SpinnerNumberModel(0, 0, 1000, 1);
	    quantitySpinner = new JSpinner(spinnerModel);
		c.gridx = 0;
		c.gridy = 1;
		// Top padding
		c.insets = new Insets(50,0,0,0);
		c.anchor = GridBagConstraints.CENTER;
		add(quantitySpinner, c);
		
		orderButton = new JButton("ORDER KITS");
		orderButton.addActionListener(this);
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.PAGE_END;
		add(orderButton, c);
				
		orderScheduleTextArea = new JTextArea();
		orderScheduleTextArea.setColumns(20);
		orderScheduleTextArea.setRows(20);
		orderScheduleTextArea.setLineWrap(true);
		orderScheduleTextArea.setEditable(false);
		orderScheduleTextArea.setWrapStyleWord(true);
		
		orderScrollPane = new JScrollPane(orderScheduleTextArea);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.PAGE_END;
		add(orderScrollPane, c);
	}

	/**
	 * Handle action events.
	 * @param ae action event
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == orderButton) {			
			// match kitComboBox selection with list of kitConfigs
			for (int i = 0; i < kitConfigs.size(); i++) {
				if (kitConfigs.get(i).getName().equals(kitComboBox.getSelectedItem())) {
					selectedKit = kitConfigs.get(i);
				}
			}
			
			//Set variable quantityToMake equal to number user enters in spinnerModel
			int quantityToMake = spinnerModel.getNumber().intValue();
			
			//Creates new Order and passes it the kit the User selects and the quantity to make
			Order newOrder = new Order(selectedKit, quantityToMake);
			
			//sends message to FCS with order info
			fpmClient.createOrder(newOrder);
		}
	}
		
	
	/**
	 * This function is called by FactoryProductionManager whenever KitConfigs are updated.
	 * @param kc ArrayList of current KitConfigs
	 */
	public void updateKitConfigs(ArrayList<KitConfig> kc) {
		kitConfigs = kc;
		
		// Clear the JComboBox
		defaultComboBox.removeAllElements();
		
		// Populate the JComboBox with the new kitConfigs
		for (int i = 0; i < kitConfigs.size(); i++) {
			kitComboBox.addItem(kitConfigs.get(i).getName());
		}
	}
	
	/**
	 * This function is called by FactoryProductionManager whenever orders are updated.
	 * @param o ArrayList of orders
	 */
	public void updateOrders(ArrayList<Order> o) {
		queue = o;
		
		// Clear the contents of the JTextArea
		orderScheduleTextArea.setText("");
		
		System.out.println("Queue size: " + queue.size());
		
		// Populate the JTextArea with current orders
		for (int i = 0; i < queue.size(); i++) {
			// System.out.println("--------- Orders in the Queue ----------");
			// System.out.println("Kit name: " + queue.get(i).getConfig().getName() + " // Quantity: " + queue.get(i).getNumKits());
			
			orderScheduleTextArea.append("Kit Type: " + queue.get(i).getConfig().getName() + "  |  Quantity: " + queue.get(i).getNumKits() + "\n");
		}
	}
}
