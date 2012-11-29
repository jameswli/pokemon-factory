package manager.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import manager.PartsManager;
import manager.util.OverlayPanel;
import manager.util.WhiteLabel;
import Utils.Constants;
import factory.PartType;

/**
 * Prettified PartsManagerPanel, powers PartsManager. Uses PartsListPanel to display parts.
 * 
 * @author Peter Zhang
 */
public class PartsManagerPanelV2 extends JPanel {

    private JPanel panels;
    private OverlayPanel leftPanel;
    private JScrollPane jsp;
    private PartsListPanel rightPanel;

    private WhiteLabel leftTitle;
    private JTextField nameField;
    private WhiteLabel numField;
    private JTextArea descField;
    private JButton submitButton;

    private JSlider badChanceScroller;
    private static final int CHANCE_MIN = 0;
    private static final int CHANCE_MAX = 100;
    private static final int CHANCE_INIT = 0;

    private PartsManager manager;

    private boolean isEditing;
    private boolean isDeleting;

    public PartsManagerPanelV2(PartsManager mngr) {
	manager = mngr;

	setLayout(new BorderLayout());
	setBorder(Constants.PADDING);

	JLabel title = new WhiteLabel("Parts Manager");
	title.setFont(new Font("Arial", Font.BOLD, 30));
	title.setBorder(Constants.VERTICAL_PADDING);
	add(title, BorderLayout.NORTH);

	panels = new JPanel(new GridLayout(1, 2));
	panels.setOpaque(false);
	panels.setVisible(true);
	add(panels);

	leftPanel = new OverlayPanel();
	leftPanel.setVisible(true);
	panels.add(leftPanel);

	rightPanel = new PartsListPanel(new PartsListPanel.PartsListPanelHandler() {
	    @Override
	    public void panelClicked(PartType pt) {
		startEditing(pt);
	    }

	    @Override
	    public void buttonClicked(PartType pt) {
		startDeleting(pt);
	    }
	});
	rightPanel.setVisible(true);
	rightPanel.setBackground(new Color(0, 0, 0, 30));

	jsp = new JScrollPane(rightPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	jsp.setOpaque(false);
	jsp.getViewport().setOpaque(false);
	panels.add(jsp);

	setUpLeftPanel();
    }

    public void setUpLeftPanel() {
	leftPanel.removeAll();

	leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
	leftPanel.setAlignmentX(LEFT_ALIGNMENT);
	leftPanel.setBorder(Constants.PADDING);

	leftTitle = new WhiteLabel("Create a New Part");
	leftTitle.setFont(new Font("Arial", Font.PLAIN, 20));
	leftTitle.setLabelSize(300, 40);
	leftTitle.setAlignmentX(0);
	leftPanel.add(leftTitle);

	JPanel namePanel = new JPanel();
	namePanel.setBorder(Constants.TOP_PADDING);
	namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
	namePanel.setOpaque(false);
	namePanel.setVisible(true);
	namePanel.setAlignmentX(0);
	leftPanel.add(namePanel);

	WhiteLabel nameLabel = new WhiteLabel("Name");
	nameLabel.setLabelSize(100, 25);
	namePanel.add(nameLabel);

	nameField = new JTextField("name");

	nameField.setMaximumSize(new Dimension(200, 25));
	nameField.setBorder(Constants.FIELD_PADDING);
	namePanel.add(nameField);

	JPanel numPanel = new JPanel();
	numPanel.setBorder(Constants.TOP_PADDING);
	numPanel.setLayout(new BoxLayout(numPanel, BoxLayout.LINE_AXIS));
	numPanel.setOpaque(false);
	numPanel.setVisible(true);
	numPanel.setAlignmentX(0);
	leftPanel.add(numPanel);

	WhiteLabel numLabel = new WhiteLabel("Part no.");
	numLabel.setLabelSize(100, 25);
	numPanel.add(numLabel);

	numField = new WhiteLabel("" + (rightPanel.getItemList().size() + 1));
	numField.setMaximumSize(new Dimension(200, 25));
	numField.setBorder(Constants.FIELD_PADDING);
	numPanel.add(numField);

	JPanel descPanel = new JPanel();
	descPanel.setBorder(Constants.TOP_PADDING);
	descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.LINE_AXIS));
	descPanel.setOpaque(false);
	descPanel.setVisible(true);
	descPanel.setAlignmentX(0);
	leftPanel.add(descPanel);

	WhiteLabel descLabel = new WhiteLabel("Description");
	descLabel.setLabelSize(100, 25);
	descPanel.add(descLabel);

	descField = new JTextArea("Description...");
	descField.setMinimumSize(new Dimension(200, 100));
	descField.setMaximumSize(new Dimension(200, 100));
	descField.setPreferredSize(new Dimension(200, 100));
	descField.setBorder(Constants.FIELD_PADDING);
	descPanel.add(descField);

	JPanel chancePanel = new JPanel();
	chancePanel.setBorder(Constants.TOP_PADDING);
	chancePanel.setLayout(new BoxLayout(chancePanel, BoxLayout.LINE_AXIS));
	chancePanel.setOpaque(false);
	chancePanel.setVisible(true);
	chancePanel.setAlignmentX(0);
	leftPanel.add(chancePanel);

	WhiteLabel chanceLabel = new WhiteLabel("Defect Chance");
	chanceLabel.setLabelSize(100, 25);
	chancePanel.add(chanceLabel);

	badChanceScroller = new JSlider(JSlider.HORIZONTAL, CHANCE_MIN, CHANCE_MAX, CHANCE_INIT);
	badChanceScroller.setMajorTickSpacing(50);
	badChanceScroller.setMinorTickSpacing(5);
	badChanceScroller.setPaintTicks(true);
	badChanceScroller.setPaintLabels(true);
	badChanceScroller.setFont(new Font("Arial", Font.PLAIN, 14));
	badChanceScroller.setForeground(Color.WHITE);
	badChanceScroller.setOpaque(false);
	chancePanel.add(badChanceScroller);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setBorder(Constants.TOP_PADDING);
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
	buttonPanel.setOpaque(false);
	buttonPanel.setVisible(true);
	buttonPanel.setAlignmentX(0);
	leftPanel.add(buttonPanel);

	if (isEditing || isDeleting) {
	    JButton cancelButton = new JButton("Cancel");
	    cancelButton.setMinimumSize(new Dimension(100, 25));
	    cancelButton.setMaximumSize(new Dimension(100, 25));
	    cancelButton.setPreferredSize(new Dimension(100, 25));
	    cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    restoreLeftPanel();
		}
	    });
	    buttonPanel.add(cancelButton);
	} else {
	    WhiteLabel fakeLabel = new WhiteLabel("");
	    fakeLabel.setLabelSize(100, 25);
	    buttonPanel.add(fakeLabel);
	}

	submitButton = new JButton("Submit >");
	submitButton.setMinimumSize(new Dimension(200, 25));
	submitButton.setMaximumSize(new Dimension(200, 25));
	submitButton.setPreferredSize(new Dimension(200, 25));
	submitButton.setAlignmentX(0);
	buttonPanel.add(submitButton);

	removeAllActionListener(submitButton);
	submitButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		// TODO try to remove this hack
		float chance = (float) badChanceScroller.getValue();

		manager.createPart(new PartType(nameField.getText(), Integer.parseInt(numField.getText()), descField
			.getText(), chance / 100));
		restoreLeftPanel();
	    }
	});
    }

    public void updatePartTypes(ArrayList<PartType> pt) {
	if (pt != null) {
	    rightPanel.updateList(pt);
	    jsp.validate();
	}
    }

    public void startEditing(final PartType pt) {
	isEditing = true;
	isDeleting = false;
	setUpLeftPanel();

	leftTitle.setText("Editing a Part");
	nameField.setText(pt.getName());
	numField.setText(String.valueOf(pt.getPartNum()));
	descField.setText(pt.getDescription());
	badChanceScroller.setValue((int) (pt.getBadChance() * 100));
	submitButton.setText("Edit >");

	removeAllActionListener(submitButton);
	submitButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		// TODO try and get rid of this hack
		float newChance = (float) badChanceScroller.getValue();

		System.out.println("Edit name: " + nameField.getText());
		pt.setName(nameField.getText());
		pt.setPartNum(Integer.parseInt(numField.getText()));
		pt.setDescription(descField.getText());
		pt.setBadChance(newChance / 100);

		manager.editPart(pt);
		restoreLeftPanel();
	    }
	});
    }

    public void startDeleting(final PartType pt) {
	isEditing = false;
	isDeleting = true;
	setUpLeftPanel();

	leftTitle.setText("Deleting a Part");
	nameField.setEnabled(false);
	numField.setEnabled(false);
	descField.setEnabled(false);
	submitButton.setText("Delete >");

	removeAllActionListener(submitButton);
	submitButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		manager.deletePart(pt);
		restoreLeftPanel();
	    }
	});
    }

    public void restoreLeftPanel() {
	isEditing = false;
	isDeleting = false;
	setUpLeftPanel();

	rightPanel.restoreColors();

	validate();
    }

    @Override
    public void paintComponent(Graphics gg) {
	Graphics2D g = (Graphics2D) gg;
	g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
    }

    /**
     * Removes all action listeners from a button. TODO consider moving this out, or starting own JButton subclass.
     */
    public void removeAllActionListener(JButton button) {
	for (ActionListener al : button.getActionListeners()) {
	    button.removeActionListener(al);
	}
    }

}
