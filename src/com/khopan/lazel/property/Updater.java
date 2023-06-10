package com.khopan.lazel.property;

@FunctionalInterface
public interface Updater<T> {
	public void valueUpdated(T value);
}
