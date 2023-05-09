package net.amygdalum.allotropy;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

public interface PrototypeTestdescriptor extends TestDescriptor {

    TestDescriptor create(UniqueId id);

}
