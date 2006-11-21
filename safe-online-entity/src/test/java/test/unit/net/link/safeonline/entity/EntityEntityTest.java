package test.unit.net.link.safeonline.entity;

import junit.framework.TestCase;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.test.util.EntityTestManager;

public class EntityEntityTest extends TestCase {

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(EntityEntity.class);
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testAnnotationCorrectness() throws Exception {
		// empty
	}
}
