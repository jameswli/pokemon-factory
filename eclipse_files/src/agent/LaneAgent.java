package agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import DeviceGraphics.DeviceGraphics;
import DeviceGraphics.PartGraphics;
import GraphicsInterfaces.LaneGraphics;
import agent.data.Part;
import agent.interfaces.Lane;
import factory.PartType;

/**
 * Lane delivers parts to the nest
 * @author Arjun Bhargava
 */
public class LaneAgent extends Agent implements Lane {

	public List<PartType> requestList = Collections
			.synchronizedList(new ArrayList<PartType>());
	public List<MyPart> currentParts = Collections
			.synchronizedList(new ArrayList<MyPart>());

	public int topLimit = 8;
	public int lowerThreshold = 3;

	public LaneStatus state;

	String name;

	public Semaphore animation = new Semaphore(0, true);

	public class MyPart {
		public Part part;
		PartStatus status;

		public MyPart(Part p) {
			part = p;
			status = PartStatus.BEGINNING_LANE;
		}
	}

	public enum PartStatus {
		BEGINNING_LANE, IN_LANE, END_LANE
	};

	public enum LaneStatus {
		FILLING, DONE_FILLING, PURGING
	};

	FeederAgent feeder;
	NestAgent nest;
	LaneGraphics laneGUI;

	public LaneAgent(String name) {
		super();

		this.name = name;
		state = LaneStatus.FILLING;
	}

	@Override
	public void msgINeedPart(PartType type) {
		print("Received msgINeedPart");
		requestList.add(type);
		stateChanged();
	}

	@Override
	public void msgPurgeParts() {
		print("Received msgPurgeParts");
		state = LaneStatus.PURGING;
		stateChanged();
	}

	@Override
	public void msgHereIsPart(Part p) {
		print("Received msgHereIsPart of type " + p.type.getName());
		currentParts.add(new MyPart(p));
		if (laneGUI != null) {
			laneGUI.receivePart(p.partGraphics);
		}

		stateChanged();
	}

	@Override
	public void msgReceivePartDone(PartGraphics part) {
		print("Received msgReceivePartDone from graphics");
		synchronized (currentParts) {
			for (MyPart p : currentParts) {
				if (p.status == PartStatus.BEGINNING_LANE) {
					p.status = PartStatus.END_LANE;
					break;
				}
			}
		}
		// animation.release();
		stateChanged();
	}

	@Override
	public void msgPurgeDone() {
		print("Received msgPurgeDone");
		animation.release();
		stateChanged();
	}

	@Override
	public void msgGivePartToNestDone(PartGraphics part) {
		print("Received msgGivePartToNestDone from graphics");
		animation.release();
		stateChanged();
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		// print("In the Scheduler");

		if (state == LaneStatus.PURGING) {
			purgeSelf();
			return true;
		}

		if (state == LaneStatus.FILLING) {
			synchronized (requestList) {
				for (PartType requestedType : requestList) {
					getParts(requestedType);
					return true;
				}
			}
		}

		synchronized (currentParts) {
			for (MyPart part : currentParts) {
				if (part.status == PartStatus.END_LANE) {
					giveToNest(part.part);
					return true;
				}
			}
		}
		return false;
	}

	public void purgeSelf() {
		print("Purging self");
		requestList = Collections.synchronizedList(new ArrayList<PartType>());
		currentParts = Collections.synchronizedList(new ArrayList<MyPart>());
		if (laneGUI != null) {
			laneGUI.purge();
			try {
				animation.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		nest.msgLanePurgeDone();
		state = LaneStatus.FILLING;
		stateChanged();
	}

	@Override
	public void getParts(final PartType requestedType) {
		print("Telling Feeder that it needs a part");
		requestList.remove(requestedType);
		feeder.msgINeedPart(requestedType, this);
		stateChanged();
	}

	@Override
	public void giveToNest(Part part) {
		print("Giving part to Nest of type " + part.type.getName());
		if (laneGUI != null) {
			laneGUI.givePartToNest(part.partGraphics);
		}
		try {
			animation.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		nest.msgHereIsPart(part);
		synchronized (currentParts) {
			for (MyPart currentPart : currentParts) {
				if (currentPart.part == part) {
					currentParts.remove(currentPart);
					break;
				}
			}
		}
		stateChanged();
	}

	@Override
	public void setGraphicalRepresentation(DeviceGraphics lane) {
		this.laneGUI = (LaneGraphics) lane;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setFeeder(FeederAgent feeder) {
		this.feeder = feeder;
	}

	public void setNest(NestAgent nest) {
		this.nest = nest;
	}

	public void thisFeederAgent(FeederAgent feeder) {
		this.feeder = feeder;
	}

	@Override
	public void msgGiveMePart() {
		// TODO Auto-generated method stub

	}

}
