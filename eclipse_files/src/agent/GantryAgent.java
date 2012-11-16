package agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import DeviceGraphics.DeviceGraphics;
import GraphicsInterfaces.GantryGraphics;
import agent.data.Bin;
import agent.data.Bin.BinStatus;
import agent.interfaces.Gantry;
import factory.PartType;

/**
 * Gantry delivers parts to the feeder
 * @author Arjun Bhargava
 */
public class GantryAgent extends Agent implements Gantry {

	public List<Bin> binList = Collections
			.synchronizedList(new ArrayList<Bin>());
	public List<MyFeeder> feeders = Collections
			.synchronizedList(new ArrayList<MyFeeder>());

	private GantryGraphics GUIGantry;

	private final String name;

	private boolean waitForDrop = false;

	public class MyFeeder {
		public FeederAgent feeder;
		public PartType requestedType;
		public FeederStatus FS;

		public MyFeeder(FeederAgent feeder) {
			this.feeder = feeder;
		}

		public MyFeeder(FeederAgent feeder, PartType type) {
			this.feeder = feeder;
			this.requestedType = type;
		}

		public FeederAgent getFeeder() {
			return feeder;
		}

		public PartType getRequestedType() {
			return requestedType;
		}
	}

	private enum FeederStatus {
		IDLE, FEEDING, BIN_REQUESTED, WAITING_FOR_BIN
	};

	public Semaphore animation = new Semaphore(0, true);

	public GantryAgent(String name) {
		super();
		this.name = name;
	}

	@Override
	public void msgHereIsBin(Bin bin) {
		print("Received msgHereIsBinConfig");
		binList.add(bin);
		// stateChanged();
	}

	@Override
	public void msgINeedParts(PartType type, FeederAgent feeder) {
		print("Received msgINeedParts");
		boolean temp = true;
		for (MyFeeder currentFeeder : feeders) {
			if (currentFeeder.getFeeder() == feeder) {
				currentFeeder.requestedType = type;
				temp = false;
				break;
			}
		}
		if (temp == true) {
			MyFeeder currentFeeder = new MyFeeder(feeder, type);
			feeders.add(currentFeeder);
		}

		stateChanged();
	}

	@Override
	public void msgReceiveBinDone(Bin bin) {
		print("Received msgReceiveBinDone from graphics");
		bin.binState = BinStatus.OVER_FEEDER;
		animation.release();
		stateChanged();
	}

	@Override
	public void msgDropBinDone(Bin bin) {
		print("Received msgdropBingDone from graphics");
		bin.binState = BinStatus.EMPTY;
		animation.release();
		waitForDrop = false;
		stateChanged();
	}

	@Override
	public void msgRemoveBinDone(Bin bin) {
		print("Received msgremoveBinDone from graphics");
		binList.remove(bin);
		animation.release();
		stateChanged();
	}

	// SCHEDULER
	@Override
	public boolean pickAndExecuteAnAction() {
		synchronized (binList) {
			for (Bin bin : binList) {
				if (bin.binState == BinStatus.PENDING) {
					addBinToGraphics(bin);
					return true;
				}
			}
		}
		synchronized (feeders) {
			if (waitForDrop == false) {
				for (MyFeeder currentFeeder : feeders) {
					for (Bin bin : binList) {
						if (bin.part.type.equals(currentFeeder
								.getRequestedType())
								&& bin.binState == BinStatus.FULL) {
							print("Moving to feeder");
							moveToFeeder(bin, currentFeeder.getFeeder());
							return true;
						}
					}
				}
			}
			if (waitForDrop) {
				for (MyFeeder currentFeeder : feeders) {
					for (Bin bin : binList) {
						if (bin.part.type.equals(currentFeeder
								.getRequestedType())
								&& bin.binState == BinStatus.OVER_FEEDER) {
							fillFeeder(bin, currentFeeder.getFeeder());
							return true;
						}
					}
				}
			}
			if (!waitForDrop) {
				for (MyFeeder currentFeeder : feeders) {
					for (Bin bin : binList) {
						if (bin.part.type.equals(currentFeeder
								.getRequestedType())
								&& bin.binState == BinStatus.EMPTY) {
							discardBin(bin);
							return true;
						}
					}
				}
			}
		}
		// print("I'm returning false");
		return false;
	}

	// ACTIONS
	@Override
	public void moveToFeeder(Bin bin, FeederAgent feeder) {
		print("Moving bin to over feeder");
		waitForDrop = true;
		bin.binState = BinStatus.MOVING;

		GUIGantry.receiveBin(bin, feeder);
		try {
			animation.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stateChanged();
	}

	@Override
	public void fillFeeder(Bin bin, FeederAgent feeder) {
		print("Placing bin in feeder and filling feeder");
		waitForDrop = false;
		bin.binState = BinStatus.FILLING_FEEDER;
		GUIGantry.dropBin(bin, feeder);

		try {
			animation.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		feeder.msgHereAreParts(bin.part.type, bin);

		stateChanged();
	}

	@Override
	public void discardBin(Bin bin) {
		print("Discarding bin");
		bin.binState = BinStatus.DISCARDING;

		GUIGantry.removeBin(bin);
		try {
			animation.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stateChanged();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setGraphicalRepresentation(DeviceGraphics dg) {
		this.GUIGantry = (GantryGraphics) dg;
	}

	public void addBinToGraphics(Bin bin) {
		if (GUIGantry != null) {
			GUIGantry.hereIsNewBin(bin);
		}
		bin.binState = BinStatus.FULL;
		stateChanged();
	}

}
