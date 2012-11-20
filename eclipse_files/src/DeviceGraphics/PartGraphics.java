package DeviceGraphics;

import java.io.Serializable;

import Utils.Location;
import factory.PartType;

public class PartGraphics implements Serializable {
	PartType partType;
	Location partLocation;

	public PartGraphics(PartType type) {
		partType = type;
	}

	public PartGraphics(PartGraphics partGraphics) {
		this.partLocation = partGraphics.partLocation;
		this.partType = partGraphics.partType;
	}

	public void setLocation(Location newLocation) {
		partLocation = newLocation;
	}

	public Location getLocation() {
		return partLocation;
	}

	public PartType getPartType() {
		return partType;
	}

}
