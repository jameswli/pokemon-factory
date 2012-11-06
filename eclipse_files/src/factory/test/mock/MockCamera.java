package factory.test.mock;

import factory.NestAgent;
import factory.data.Kit;
import factory.interfaces.Camera;
import factory.interfaces.Nest;

/**
 * Mock camera. Messages received simply add an entry to the mock agent's log.
 * @author Daniel Paje
 */
public class MockCamera extends MockAgent implements Camera {

	public EventLog log;

	public MockCamera(String name) {
		super(name, new EventLog());
		this.log = super.getLog();
	}

	@Override
	public void msgInspectKit(Kit kit) {
		log.add(new LoggedEvent("Received message msgInspectKit"));

	}

	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void msgTakePictureNestDone(NestAgent nest) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgTakePictureKitDone(Kit kit, boolean done) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgIAmFull(Nest nest) {
		// TODO Auto-generated method stub

	}

}
