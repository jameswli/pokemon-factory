package agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import DeviceGraphics.DeviceGraphics;
import DeviceGraphics.PartGraphics;
import GraphicsInterfaces.PartsRobotGraphics;
import agent.data.Kit;
import agent.data.Part;
import agent.interfaces.Nest;
import agent.interfaces.PartsRobot;
import agent.interfaces.Stand;
import factory.KitConfig;
import factory.PartType;

/**
 * Parts robot picks parts from nests and places them in kits
 * @author Ross Newman, Michael Gendotti, Daniel Paje
 */
public class PartsRobotAgent extends Agent implements PartsRobot {

	private String name;

	private KitConfig Kitconfig;
	private final List<MyKit> MyKits = Collections
			.synchronizedList(new ArrayList<MyKit>());
	private Map<Nest, List<Part>> GoodParts = new ConcurrentHashMap<Nest, List<Part>>();
	private List<Arm> Arms = Collections.synchronizedList(new ArrayList<Arm>());

	private final Date time;

	private int kitsNum = 0;
	private final Timer timer;

	private List<Kit> KitsOnStand;

	private Stand stand;
	private PartsRobotGraphics partsRobotGraphics;

	public class MyKit {
		public Kit kit;
		public MyKitStatus MKS;

		public MyKit(Kit k) {
			kit = k;
			MKS = MyKitStatus.NOT_DONE;
		}
	}

	public enum MyKitStatus {
		NOT_DONE, DONE
	};

	public class Arm {
		Part part;
		ArmStatus AS;

		public Arm() {
			part = null;
			AS = ArmStatus.EMPTY;
		}
	}

	private enum ArmStatus {
		EMPTY, FULL, EMPTYING
	};

	private PartsRobotStatus status;

	private enum PartsRobotStatus {
		IDLE, PICKING_UP, PLACING
	};

	public Semaphore animation = new Semaphore(0, true);

	// public Semaphore accessKit = new Semaphore(0, true);

	public PartsRobotAgent(String name) {
		super();

		this.name = name;
		time = new Date();
		timer = new Timer();

		// Add arms
		for (int i = 0; i < 4; i++) {
			this.Arms.add(new Arm());
		}

		status = PartsRobotStatus.IDLE;

		timer.scheduleAtFixedRate(new TimerTask() {
			// Fires every 3.001 seconds.
			@Override
			public void run() {
				print("Waking up");
				stateChanged();
			}
		}, System.currentTimeMillis(), 3000);

		time.setTime(System.currentTimeMillis());
	}

	/*
	 * Messages
	 */

	/**
	 * Changes the configuration for the kits From FCS
	 */
	@Override
	public void msgHereIsKitConfiguration(KitConfig config) {
		print("Received msgHereIsKitConfiguration");
		Kitconfig = config;
		kitsNum = 0;
		GoodParts = new ConcurrentHashMap<Nest, List<Part>>();
		// stateChanged();
	}

	/**
	 * From Camera
	 */
	@Override
	public void msgHereAreGoodParts(Nest n, List<Part> goodParts) {
		print("Received msgHereAreGoodParts of type "
				+ goodParts.get(0).type.getName());
		GoodParts.put(n, goodParts);
		print("I have " + MyKits.size() + " kits and " + GoodParts.size()
				+ " nests");
		stateChanged();
	}

	/**
	 * From Stand
	 */
	@Override
	public void msgUseThisKit(final Kit k) {
		print("Received msgUseThisKit");

		MyKit mk = new MyKit(k);
		MyKits.add(mk);
		print("I have " + MyKits.size() + " kits and " + GoodParts.size()
				+ " nests");
		stateChanged();

	}

	/**
	 * Releases animation semaphore after a part is picked up, so that a new
	 * animation may be run by graphics. From graphics
	 */
	@Override
	public void msgPickUpPartDone() {
		print("Received msgPickUpPartDone from graphics");
		animation.release();
		stateChanged();
	}

	/**
	 * Releases animation semaphore after a part is given to kit, so that a new
	 * animation may be run by graphics. From graphics
	 */
	@Override
	public void msgGivePartToKitDone() {
		print("Received msgGivePartToKitDone from graphics");
		animation.release();
		stateChanged();
	}

	/*
	 * Scheduler
	 * @see agent.Agent#pickAndExecuteAnAction()
	 */

	@Override
	public boolean pickAndExecuteAnAction() {

		// Checks if a kit is done and inspects it if it is
		synchronized (MyKits) {
			if (MyKits.size() > 0) {
				for (MyKit mk : MyKits) {
					if (mk.MKS == MyKitStatus.DONE) {
						RequestInspection(mk);
						return true;
					}
				}
			}
		}

		// Checks if any arm is holding a part and places it if there is one
		if (status == PartsRobotStatus.PLACING) {
			synchronized (Arms) {
				for (Arm arm : Arms) {
					if (arm.AS == ArmStatus.FULL) {
						print("Arm holding: " + arm.part.type.toString());
						PlacePart(arm);
						return true;
					}
				}
			}

			if (allArmsEmpty()) {
				status = PartsRobotStatus.PICKING_UP;
				return true;
			}
		}

		// Checks if there is an empty arm, if there is it fills it with a
		// good part that the kit needs

		if (IsAnyArmEmpty()) {
			status = PartsRobotStatus.PICKING_UP;
			time.setTime(System.currentTimeMillis());
			synchronized (GoodParts) {
				for (Nest nest : GoodParts.keySet()) {
					// Going through all the good parts
					for (Part part : GoodParts.get(nest)) {
						synchronized (MyKits) {
							// print("Size of MyKits: " +
							// MyKits.size());
							for (MyKit mk : MyKits) {
								// Checking if the good part is needed by
								// either kit
								// print("Kit needs: " +
								// mk.kit.partsExpected.getConfig().toString());
								if (NumTotalPartsNeeded(part) > NumPartsInHand(part)) {
									print("Found a part I need of type "
											+ part.type.getName() + " for kit "
											+ MyKits.indexOf(mk) + " "
											+ mk.kit.PartsStillNeeded());
									synchronized (Arms) {
										for (Arm arm : Arms) {
											if (arm.AS == ArmStatus.EMPTY) {
												// Find the empty arm
												PickUpPart(arm, part, nest);
												return true;
											}
										}

									}
								}
							} // Why is this so awful
						}
					}
				}
			}
		} else if (System.currentTimeMillis() - time.getTime() > 3000) {
			// Last rule is to place parts if the parts robot has been idle
			// too long
			print("Assessing...");
			if (!allArmsEmpty()) {
				status = PartsRobotStatus.PLACING;
			}
			time.setTime(System.currentTimeMillis());
			return true;
		} else {
			status = PartsRobotStatus.PLACING;
			return true;
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

	private void PickUpPart(Arm arm, Part part, Nest nest) {

		print("Picking up part" + getPartTypesInArms());
		// synchronized (Arms) {

		GoodParts.remove(nest);
		arm.AS = ArmStatus.FULL;
		arm.part = part;
		// Tells the graphics to pickup the part
		if (partsRobotGraphics != null) {
			PartGraphics pg = new PartGraphics(arm.part.partGraphics);
			partsRobotGraphics.pickUpPart(pg);
			try {
				// print("Blocking");
				animation.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// print("Got permit");
		}

		// Only takes 1 part from a nest at a time
		nest.msgTakingPart(part);
		nest.msgDoneTakingParts();

		stateChanged();
		// }
	}

	private void PlacePart(Arm arm) {
		print("Placing part");
		synchronized (MyKits) {
			for (MyKit mk : MyKits) {
				if (mk.kit.needPart(arm.part) > 0) {

					if (partsRobotGraphics != null) {
						PartGraphics pg = new PartGraphics(
								arm.part.partGraphics);
						partsRobotGraphics
								.givePartToKit(pg, mk.kit.kitGraphics);
						try {
							// print("Blocking");
							animation.acquire();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// print("Got permit");
					}
					// Tells the kit it has the part now
					mk.kit.parts.add(arm.part);
					arm.part = null;
					arm.AS = ArmStatus.EMPTY;

					// Checks if the kit is done
					CheckMyKit(mk);
					break;
				}
			}
			stateChanged();
		}
	}

	private void CheckMyKit(MyKit mk) {
		int size = 0;

		for (PartType type : mk.kit.partsExpected.getConfig().keySet()) {
			for (int i = 0; i < mk.kit.partsExpected.getConfig().get(type); i++) {
				size++;
			}
		}

		print("Need " + (size - mk.kit.parts.size())
				+ " more part(s) to finish kit (kit: " + mk.toString());
		if (size - mk.kit.parts.size() == 0) {
			mk.MKS = MyKitStatus.DONE;
		}
		// stateChanged();
	}

	private void RequestInspection(MyKit mk) {
		print("Requesting inspection for kit " + mk.toString());
		MyKits.remove(mk);
		stand.msgKitAssembled(mk.kit);
		kitsNum++;
		print("I have " + MyKits.size() + " kits and " + GoodParts.size()
				+ " nests");
		print("I have " + MyKits.size() + " kits on the stand and I have made "
				+ kitsNum + " kits");
		stateChanged();
	}

	// Helper methods

	private int NumPartsInHand(Part part) {
		synchronized (Arms) {
			int count = 0;
			for (Arm a : Arms) {
				if (a.part != null) {
					if (a.part.type == part.type) {
						count++;
					}
				}
			}
			return count;
		}
	}

	// Checks if any of the arms are empty
	private boolean IsAnyArmEmpty() {
		synchronized (Arms) {
			for (Arm a : Arms) {
				if (a.AS == ArmStatus.EMPTY) {
					return true;
				}
			}
			return false;
		}
	}

	private int NumTotalPartsNeeded(Part p) {
		int temp = 0;

		synchronized (MyKits) {
			for (MyKit mk : MyKits) {
				temp += mk.kit.needPart(p);
			}
		}

		return temp;
	}

	/**
	 * Check if all arms are empty
	 * @return true if all arms empty
	 * @author Daniel Paje
	 */
	private boolean allArmsEmpty() {
		synchronized (Arms) {
			for (Arm a : Arms) {
				if (a.AS == ArmStatus.FULL || a.AS == ArmStatus.EMPTYING) {
					return false;
				}
			}
		}
		return true;
	}

	private String getPartTypesInArms() {
		String temp = new String();
		synchronized (Arms) {
			for (Arm a : Arms) {
				if (a.part != null) {
					temp = temp.concat(" " + a.part.type.getName());
				}
			}
		}
		return temp;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public KitConfig getKitConfig() {
		return Kitconfig;
	}

	public void setKitConfig(KitConfig kitConfig) {
		Kitconfig = kitConfig;
	}

	public Map<Nest, List<Part>> getGoodParts() {
		return GoodParts;
	}

	public void setGoodParts(Map<Nest, List<Part>> goodParts) {
		GoodParts = goodParts;
	}

	public List<Arm> getArms() {
		return Arms;
	}

	public void setArms(List<Arm> arms) {
		Arms = arms;
	}

	public List<Kit> getKitsOnStand() {
		return KitsOnStand;
	}

	public void setKitsOnStand(List<Kit> kitsOnStand) {
		KitsOnStand = kitsOnStand;
	}

	/*
	 * public List<Nest> getNests() { return nests; } public void
	 * setNests(List<Nest> nests) { this.nests = nests; }
	 */

	public Stand getStand() {
		return stand;
	}

	public void setStand(Stand stand) {
		this.stand = stand;
	}

	public PartsRobotGraphics getPartsrobotGraphics() {
		return partsRobotGraphics;
	}

	@Override
	public void setGraphicalRepresentation(DeviceGraphics partsrobotGraphics) {
		this.partsRobotGraphics = (PartsRobotGraphics) partsrobotGraphics;
	}

	public Semaphore getAnimation() {
		return animation;
	}

	public void setAnimation(Semaphore animation) {
		this.animation = animation;
	}

	public List<MyKit> getMyKits() {
		return MyKits;
	}

	// Initialize Arms
	public void InitializeArms() {
		for (int i = 0; i < 4; i++) {
			Arms.add(new Arm());
		}
	}
}
