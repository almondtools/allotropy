package net.amygdalum.allotropy;

import static net.amygdalum.allotropy.OnlyAllotropyTests.onlyAlllotropyTests;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

public class AllotropyTestEngine implements TestEngine {

    public static final String ENGINE = "engine";
    public static final String ENGINE_ID = "allotropy";

    public AllotropyTestEngine() {
    }

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
        AllotropyEngineDescriptor engineDescriptor = new AllotropyEngineDescriptor(UniqueId.forEngine(getId()), "Allotropy Test Engine", new AllotropyConfiguration(request.getConfigurationParameters()));

        EngineDiscoveryRequestResolver<AllotropyEngineDescriptor> resolver = EngineDiscoveryRequestResolver.<AllotropyEngineDescriptor> builder()
            .addClassContainerSelectorResolver(onlyAlllotropyTests())
            .addSelectorResolver(new FromClassesResolver())
            .addSelectorResolver(new FromMethodsResolver())
            .addTestDescriptorVisitor(new GroupByView())
            .build();
        resolver.resolve(request, engineDescriptor);
        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest request) {
        TestInterpreter interpreter = new TestInterpreter(request.getEngineExecutionListener());
        InterpretableTestDescriptor rootTestDescriptor = (InterpretableTestDescriptor) request.getRootTestDescriptor();
        rootTestDescriptor.accept(interpreter);
        interpreter.close();
    }

}
