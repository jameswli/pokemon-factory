package agent.interfaces;

import factory.PartType;
import agent.data.Part;
import DeviceGraphics.PartGraphics;
import GraphicsInterfaces.LaneGraphics;

public interface Lane {

	public abstract void msgINeedPart(PartType type);

	public abstract void msgHereIsPart(Part p);
	
	public abstract void msgPurgeParts();
	
	public abstract void msgGiveMePart();

	public abstract void msgReceivePartDone(PartGraphics part);

	public abstract void msgGivePartToNestDone(PartGraphics part);
	
	public abstract void msgPurgeDone();

	public abstract boolean pickAndExecuteAnAction();

	public abstract void getParts(PartType requestedType);

	public abstract void giveToNest(Part part);

}