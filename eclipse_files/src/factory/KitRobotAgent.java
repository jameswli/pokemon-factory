package factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import agent.Agent;
import factory.data.Kit;
import factory.interfaces.Camera;
import factory.interfaces.Conveyor;
import factory.interfaces.KitRobot;
import factory.interfaces.Stand;

/**
 * Kit Robot brings moves kits to and from the conveyor and arranges kits on the
 * kitting stand. It is responsible for moving the assembled kits on the stand
 * into the inspection area for the Camera. Interacts with the Parts Robot,
 * Conveyor and Camera.
 * @author dpaje
 */
public class KitRobotAgent extends Agent implements KitRobot {

	private final List<MyKit> myKits = Collections
			.synchronizedList(new ArrayList<MyKit>());

	// Tracks stand positions and whether or not they are open
	Map<Integer, Boolean> standPositions = Collections
			.synchronizedMap(new TreeMap<Integer, Boolean>());

	int numKitsToRequest;

	// Used to prevent animations from overlapping
	Semaphore animation = new Semaphore(1);

	// References to other agents
	private Stand stand;
	private Conveyor conveyor;
	private Camera camera;
	private KitRobotGraphics kitrobotGraphics;

	private final String name;

	/**
	 * Inner class encapsulates kit and adds states relevant to the stand
	 * @author dpaje
	 */
	private class MyKit {
		public Kit kit;
		public KitStatus KS;

		public MyKit(Kit k) {
			this.kit = k;
			this.KS = KitStatus.PickedUp;
		}
	}

	public enum KitStatus {
		PickedUp, OnStand, MarkedForInspection, AwaitingInspection, Inspected;
	};

	/**
	 * Constructor for KitRobotAgent class
	 * @param name name of the kitrobot
	 */
	public KitRobotAgent(String name) {
		super();

		this.name = name;
		numKitsToRequest = 0;

		// Don't assume stand is empty
		standPositions.put(0, false);
		standPositions.put(1, false);
		standPositions.put(2, false);
	}

	/*
	 * Messages
	 */

	@Override
	public void msgHereIsKit(Kit k) {
		MyKit mk = new MyKit(k);
		myKits.add(mk);
		numKitsToRequest--;
		stateChanged();
	}

	@Override
	public void msgNeedKit(int standLocation) {
		standPositions.put(standLocation, true);
		numKitsToRequest++;
		stateChanged();
	}

	@Override
	public void msgMoveKitToInspectionArea(Kit k) {
		for (MyKit mk : myKits) {
			if (mk.kit == k) {
				mk.KS = KitStatus.MarkedForInspection;
				break;
			}
		}
		stateChanged();
	}

	@Override
	public void msgKitPassedInspection() {
		for (MyKit mk : myKits) {
			if (mk.KS == KitStatus.AwaitingInspection) {
				mk.KS = KitStatus.Inspected;
				break;
			}
		}
		stateChanged();
	}

	@Override
	public void msgPlaceKitOnConveyorDone() {
		animation.release();
		stateChanged();
	}

	@Override
	public void msgPlaceKitInInspectionAreaDone() {
		animation.release();
		stateChanged();
	}

	@Override
	public void msgPlaceKitOnStandDone() {
		animation.release();
		stateChanged();
	}

	/*
	 * Scheduler
	 * @see agent.Agent#pickAndExecuteAnAction()
	 */
	@Override
	public boolean pickAndExecuteAnAction() {

		synchronized (myKits) {
			// Picked up a kit from conveyor
			for (MyKit mk : myKits) {
				if (mk.KS == KitStatus.PickedUp) {
					placeKitOnStand(mk);
					return true;
				}
			}

			// Kit needs to be inspected
			for (MyKit mk : myKits) {
				if (mk.KS == KitStatus.MarkedForInspection) {
					placeKitInInspectionArea(mk);
					return true;
				}
			}

			// Kit needs to be shipped out of the kitting cell
			for (MyKit mk : myKits) {
				if (mk.KS == KitStatus.Inspected) {
					shipKit(mk.kit);
					return true;
				}
			}
		}

		// If other rules fail and there's a spot on the stand, request a new
		// kit
		if (myKits.size() < 3) {
			requestKit();
		}

		/*
		 * Tried all rules and found no actions to fire. Return false to the
		 * main loop of abstract base class Agent and wait.
		 */
		return false;
	}

	/*
	 * Actions
	 */

	/**
	 * Requests a kit from the conveyor.
	 */
	private void requestKit() {
		conveyor.msgNeedKit();
		stateChanged();
	}

	/**
	 * Takes a kit from the conveyor and place it on the stand.
	 */
	private void placeKitOnStand(MyKit mk) {
		for (int loc : standPositions.keySet()) {
			if (standPositions.get(loc) == true) {
				animation.acquire();
				kitrobotGraphics.msgPlaceKitOnStand(loc);
				standPositions.put(loc, false);
				mk.KS = KitStatus.OnStand;
				stand.msgHereIsKit(mk.kit, loc);
				break;
			}
		}
		stateChanged();
	}

	/**
	 * Places an assembled kit on the stand into the inspection area (also on
	 * the stand).
	 * @param k the kit being placed.
	 */
	private void placeKitInInspectionArea(MyKit mk) {
		animation.acquire();
		mk.KS = KitStatus.AwaitingInspection;
		kitrobotGraphics.msgPlaceKitInInspectionArea(mk.kit);
		camera.msgInspectKit(mk.kit);
		stateChanged();
	}

	/**
	 * Places a completed kit on the conveyor for removal from the kitting cell.
	 * @param k the kit being shipped out of the kitting cell.
	 */
	private void shipKit(Kit k) {
		animation.acquire();
		kitrobotGraphics.msgPlaceKitOnConveyor();
		conveyor.msgTakeKitAway(k);
		stand.msgShippedKit();
		myKits.remove(k);
		stateChanged();
	}

	/**
	 * GUI Hack to set the reference to the stand.
	 * @param s the stand
	 */
	public void setConveyor(Stand s) {
		this.stand = s;
		stateChanged();
	}

	/**
	 * GUI Hack to set the reference to the conveyor.
	 * @param co the conveyor
	 */
	public void setConveyor(Conveyor co) {
		this.conveyor = co;
		stateChanged();
	}

	/**
	 * GUI Hack to set the reference to the camera.
	 * @param ca the camera
	 */
	public void setCamera(Camera ca) {
		this.camera = ca;
		stateChanged();
	}

	/**
	 * GUI Hack to set the reference to this class' gui component
	 * @param gc the gui representation of kit robot
	 */
	public void setGraphicalRepresentation(KitRobotGraphics krg) {
		this.kitrobotGraphics = krg;
		stateChanged();
	}

}
