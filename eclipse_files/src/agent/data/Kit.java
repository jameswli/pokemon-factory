package agent.data;

import java.util.ArrayList;

import DeviceGraphics.KitGraphics;
import Utils.Constants;
import factory.KitConfig;
import factory.PartType;

public class Kit {

	public KitGraphics kitGraphics;

	public String kitID;

	public KitConfig partsExpected;

	public ArrayList<Part> parts = new ArrayList<Part>();

	public Kit() {
		kitGraphics = new KitGraphics(null);
		partsExpected = new KitConfig("default",
				Constants.DEFAULT_PARTTYPES.get(0));
	}

	public Kit(KitConfig expected) {
		kitGraphics = new KitGraphics(null);
		partsExpected = expected;
	}

	public Kit(String kitID) {
		kitGraphics = new KitGraphics(null);
		this.kitID = kitID;
	}

	public int needPart(Part part) {
		int count = 0;
		for (PartType type : partsExpected.getConfig().keySet()) {
			if (type == part.type) {
				count += partsExpected.getConfig().get(type);
				break;
			}
		}
		for (Part tempPart : parts) {
			if (tempPart.type == part.type) {
				count--;
			}
		}
		return count > 0 ? count : 0;
	}

	public String PartsStillNeeded() {
		String temp = "Needs ";
		for (PartType inputtype : partsExpected.getConfig().keySet()) {
			int count = 0;
			for (PartType type : partsExpected.getConfig().keySet()) {
				if (type == inputtype) {
					count = partsExpected.getConfig().get(type);
					break;
				}
			}
			for (Part tempPart : parts) {
				if (tempPart.type == inputtype) {
					count--;
				}
			}
			if (count > 0) {
				temp = temp.concat("" + count + ":" + inputtype + " ");
			}
		}
		return temp;
	}

	public boolean equals(Kit k) {
		return k.kitGraphics.toString().equals(this.kitGraphics.toString());
	}

}
