package net.amygdalum.allotropy;

import static net.amygdalum.allotropy.SelectorMode.STRICT;

import org.junit.platform.engine.ConfigurationParameters;

public class AllotropyConfiguration  {

    private static final String ALLOTROPY_SELECTORS = "allotropy.selectors";
    
    private ConfigurationParameters configurationParameters;

    public AllotropyConfiguration(ConfigurationParameters configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    public SelectorMode selectors() {
        return configurationParameters.get(ALLOTROPY_SELECTORS)
            .map(String::toUpperCase)
            .map(SelectorMode::valueOf)
            .orElse(STRICT);
    }
}
