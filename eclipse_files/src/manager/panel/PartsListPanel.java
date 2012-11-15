package manager.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import manager.util.ClickablePanel;
import manager.util.ClickablePanelClickHandler;
import manager.util.OverlayPanel;
import manager.util.WhiteLabel;
import Utils.Constants;
import factory.PartType;

public class PartsListPanel extends OverlayPanel {
	PartsListPanelHandler handler;
	ArrayList<PartType> partTypes = new ArrayList<PartType>();
	
	public static final Border PADDING = BorderFactory.createEmptyBorder(20, 20, 20, 20);
	public static final Border FIELD_PADDING = BorderFactory.createEmptyBorder(5, 5, 5, 5);
	public static final Border MEDIUM_PADDING = BorderFactory.createEmptyBorder(10, 10, 10, 10);
	public static final Border BOTTOM_PADDING = BorderFactory.createEmptyBorder(0, 0, 20, 0);
	public static final Border TOP_PADDING = BorderFactory.createEmptyBorder(20, 0, 5, 0);
	public static final Border VERTICAL_PADDING = BorderFactory.createEmptyBorder(10, 0, 10, 0);
	
	public PartsListPanel(PartsListPanelHandler h) {
		super();
		handler = h;
		partTypes = (ArrayList<PartType>) Constants.DEFAULT_PARTTYPES.clone();
	
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setAlignmentX(LEFT_ALIGNMENT);
		setBorder(PADDING);
		
		parsePartTypes();
	}
	
	public void parsePartTypes() {
		
		removeAll();
		
		for(PartType pt : partTypes) {
			JPanel panel = new ClickablePanel(new EditClickHandler(pt));
			panel.setSize(350, 50);
			panel.setBorder(MEDIUM_PADDING);
			panel.setAlignmentX(0);
			
			add(panel);
			
			JLabel imageLabel = new JLabel(new ImageIcon(pt.getImage()));
			imageLabel.setMinimumSize(new Dimension(50, 50));
			imageLabel.setPreferredSize(new Dimension(50, 50));
			imageLabel.setMaximumSize(new Dimension(50, 50));
			panel.add(imageLabel);
			
			WhiteLabel nameLabel = new WhiteLabel("Part: " + pt.getName());
			nameLabel.setLabelSize(190, 50);
			panel.add(nameLabel);
			
			JButton deleteButton = new JButton("delete");
			deleteButton.addActionListener(new DeleteClickHandler(pt));
			panel.add(deleteButton);
			
			// add padding
			add(Box.createVerticalStrut(10));
		}
	}
	
	public void updatePartTypes(ArrayList<PartType> pt) {
		partTypes = pt;
		parsePartTypes();
	}
	
	public interface PartsListPanelHandler {
		public void editPart(PartType pt);
		public void deletePart(PartType pt);
	}
	
	private class EditClickHandler implements ClickablePanelClickHandler{
		PartType pt;
		public EditClickHandler(PartType pt) {
			this.pt = pt;
		}

		@Override
		public void mouseClicked() {
			handler.editPart(pt);
		}
	}
	
	private class DeleteClickHandler implements ActionListener{
		PartType pt;
		public DeleteClickHandler(PartType pt) {
			this.pt = pt;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			handler.deletePart(pt);
		}
	}
}
