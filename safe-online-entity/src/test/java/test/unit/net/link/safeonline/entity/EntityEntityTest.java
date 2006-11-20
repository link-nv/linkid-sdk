package test.unit.net.link.safeonline.entity;

import junit.framework.TestCase;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.test.util.TestEntityManager;

public class EntityEntityTest extends TestCase {

	private TestEntityManager testEntityManager;

	protected void setUp() throws Exception {
		super.setUp();
		this.testEntityManager = new TestEntityManager();
		this.testEntityManager.setUp(EntityEntity.class);
	}

	protected void tearDown() throws Exception {
		this.testEntityManager.tearDown();
		super.tearDown();
	}

	public void testAnnotationCorrectness() throws Exception {
		// empty
	}
}
