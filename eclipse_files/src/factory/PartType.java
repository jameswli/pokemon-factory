package factory;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.Serializable;

import Utils.Constants;
import Utils.StringUtil;

public class PartType implements Serializable {
	private String name = "";
	private final String id;
	private int partNum;
	private String description = "";

	// private Image image;

	/**
	 * @param s - a number indicating part type
	 */
	@Deprecated
	public PartType(String s) {
		name = s;
		this.id = StringUtil.md5(name);
		// setImage();
	}

	public PartType(String s, int num, String desc) {
		name = s;
		partNum = num;
		description = desc;
		this.id = StringUtil.md5(name);
	}

	@Override
	public String toString() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getPartNum() {
		return partNum;
	}

	public String getDescription() {
		return description;
	}

	public String getID() {
		return id;
	}

	public Image getImage() {
		return Toolkit.getDefaultToolkit().getImage(
				Constants.PART_IMAGE_PATH + name + ".png");
	}

	public boolean equals(PartType pt) {
		return this.id.equals(pt.getID());
	}
}
