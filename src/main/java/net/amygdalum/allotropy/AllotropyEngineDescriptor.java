package net.amygdalum.allotropy;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

public class AllotropyEngineDescriptor extends EngineDescriptor implements InterpretableTestDescriptor {

    private AllotropyConfiguration configuration;

    public AllotropyEngineDescriptor(UniqueId uniqueId, String displayName, AllotropyConfiguration configuration) {
        super(uniqueId, displayName);
        this.configuration = configuration;
    }

    @Override
    public void accept(Interpreter interpreter) {
        interpreter.visitEngineDescriptor(this);
    }

    public AllotropyConfiguration getConfiguration() {
        return configuration;
    }

}
