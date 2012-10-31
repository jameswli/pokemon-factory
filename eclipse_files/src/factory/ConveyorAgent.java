package factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import agent.Agent;
import factory.data.Kit;
import factory.interfaces.Conveyor;
import factory.interfaces.KitRobot;

/**
 * Conveyor brings empty kits into and takes completed (i.e. assembled and
 * inspected) kits out of the kitting cell. Interacts with the Factory Control
 * System (FCS) and the Kit Robot.
 * @author dpaje
 */
public class ConveyorAgent extends Agent implements Conveyor {

	private final List<MyKit> kitsOnConveyor = Collections
			.synchronizedList(new ArrayList<MyKit>());
	private final int numKitsToDeliver;

	// Used to prevent animations from overlapping
	Semaphore animation = new Semaphore(1);

	// References to other agents
	private KitRobot kitrobot;
	private FCS fcs;
	private ConveyorGraphics conveyorGraphics;

	// Name of the conveyor
	private final String name;

	/**
	 * Inner class encapsulates kit and adds states relevant to the conveyor
	 * @author dpaje
	 */
	private class MyKit {
		public Kit kit;
		public KitStatus KS;

		public MyKit(Kit k) {
			this.kit = k;
			this.KS = KitStatus.MovingIn;
		}
	}

	public enum KitStatus {
		MovingIn, AwaitingPickup, PickedUp, AwaitingDropOff, MovingOut, Delivered
	};

	/**
	 * Constructor for ConveyorAgent class
	 * @param name name of the conveyor
	 */
	public ConveyorAgent(String name) {
		super();

		this.name = name;
		this.numKitsToDeliver = 0;
	}

	/*
	 * Messages.
	 */

	@Override
	public void msgNeedKit() {
		numKitsToDeliver++;
		stateChanged();
	}

	@Override
	public void msgTakeKitAway(Kit k) {
		MyKit mk = new MyKit(k);
		mk.KS = KitStatus.MovingOut;
		kitsOnConveyor.add(mk);
		stateChanged();
	}

	@Override
	public void msgBringEmptyKitDone() {
		animation.release();
		stateChanged();
	}

	@Override
	public void msgGiveKitToKitRobotDone() {
		animation.release();
		stateChanged();
	}

	@Override
	public void msgReceiveKitDone() {
		animation.release();
		stateChanged();
	}

	/*
	 * Scheduler
	 * @see agent.Agent#pickAndExecuteAnAction()
	 */

	@Override
	public boolean pickAndExecuteAnAction() {

		synchronized (kitsOnConveyor) {
			// Send the kit if it has reached the "stop" position on the
			// conveyor where the kit robot can pick it up.
			for (MyKit mk : kitsOnConveyor) {
				if (mk.KS == KitStatus.AwaitingPickup) {
					sendKit(mk.kit);
					return true;
				}
			}

			// Place kit onto conveyor and start moving it out of the cell if
			// the kit robot has dropped off a completed kit
			for (MyKit mk : kitsOnConveyor) {
				if (mk.KS == KitStatus.AwaitingDropOff) {
					deliverKit(mk.kit);
					return true;
				}
			}

			// Place kit onto conveyor and start moving it into the cell if a
			// new kit was requested by the kit robot
			for (MyKit mk : kitsOnConveyor) {
				// Default KS for MyKit is AwaitingPickup
				if (numKitsToDeliver > 0) {
					prepareKit();
					return true;
				}
			}

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
	 * Generate a new kit to move into the kitting cell.
	 */
	private void prepareKit() {
		Kit k = new Kit();
		kitsOnConveyor.add(new MyKit(k));
		animation.acquire();
		conveyorGraphics.msgBringEmptyKit(k);
		stateChanged();
	}

	/**
	 * Send an empty kit to the kitrobot
	 * @param k the kit being sent.
	 */
	private void sendKit(Kit k) {
		numKitsToDeliver--;
		kitrobot.msgHereIsKit(k);
		animation.acquire();
		conveyorGraphics.msgGiveKitToKitRobot(k);
		stateChanged();
	}

	/**
	 * Send a finished kit out of the cell.
	 * @param k the kit being delivered.
	 */
	private void deliverKit(Kit k) {
		animation.acquire();
		conveyorGraphics.msgReceiveKit(k);
		stateChanged();
	}

	/**
	 * GUI Hack to set the reference to the kitrobot.
	 * @param kr the kitrobot
	 */
	public void setPartsRobot(KitRobot kr) {
		this.kitrobot = kr;
		stateChanged();
	}

	/**
	 * GUI Hack to set the reference to the FCS.
	 * @param fcs the FCS
	 */
	public void setFCS(FCS fcs) {
		this.FCS = fcs;
		stateChanged();
	}

	/**
	 * GUI Hack to set the reference to this class' gui component
	 * @param gc the gui representation of conveyor
	 */
	public void setGraphicalRepresentation(ConveyorGraphics cg) {
		this.conveyorGraphics = cg;
		stateChanged();
	}

}
