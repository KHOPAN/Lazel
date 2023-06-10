package com.khopan.lazel.property;

@FunctionalInterface
public interface Setter<T, R> {
	public R set(T value);
}
