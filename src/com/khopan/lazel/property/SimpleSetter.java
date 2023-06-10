package com.khopan.lazel.property;

@FunctionalInterface
public interface SimpleSetter<T> {
	public void set(T value);
}
