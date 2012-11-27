package GraphicsInterfaces;

import DeviceGraphics.KitGraphics;
import DeviceGraphics.PartGraphics;

public interface PartsRobotGraphics {

	public abstract void pickUpPart(PartGraphics part);
	public abstract void givePartToKit(PartGraphics part, KitGraphics kit);

    public abstract void dropPartFromArm(PartGraphics part);
	
}
