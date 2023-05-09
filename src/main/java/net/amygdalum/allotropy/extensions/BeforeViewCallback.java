package net.amygdalum.allotropy.extensions;

import net.amygdalum.allotropy.Extension;
import net.amygdalum.allotropy.Registrations;

public interface BeforeViewCallback extends Extension {

    void beforeView(Class<?> viewCLass, Registrations registrations);

}
