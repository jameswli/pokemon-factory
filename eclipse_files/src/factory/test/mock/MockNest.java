package factory.test.mock;

import factory.data.Part;
import factory.data.PartType;
import factory.interfaces.Nest;

public class MockNest extends MockAgent implements Nest{

	public EventLog log;
	public MockNest(String name) {
		super(name, new EventLog());
		this.log = super.getLog();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void msgHereIsPartType(PartType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsPart(Part p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgTakingPart(Part p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDoneTakingParts() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgReceivePartDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgGivePartToPartsRobotDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPurgingDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getParts(PartType requestedType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveToPosition(Part part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nestFull() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateParts() {
		// TODO Auto-generated method stub
		
	}

}
