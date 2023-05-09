package net.amygdalum.allotropy;

public sealed interface InjectionKey extends Comparable<InjectionKey> permits ByName, ByType {

}
