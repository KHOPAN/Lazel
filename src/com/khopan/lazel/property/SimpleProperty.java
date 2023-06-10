package com.khopan.lazel.property;

public class SimpleProperty<T, R> implements  Property<T, R> {
	private final Getter<T> getter;
	private final SimpleSetter<T> setter;
	private Updater<T> updater;
	private boolean isSet;
	private boolean nullable;
	private T nullSafety;
	private R returner;

	public SimpleProperty(Getter<T> getter, SimpleSetter<T> setter, R returner) {
		this(getter, setter, null, returner);
	}

	public SimpleProperty(Getter<T> getter, SimpleSetter<T> setter, Updater<T> updater, R returner) {
		if(getter == null) {
			throw new NullPointerException("'getter' cannot be null.");
		}

		if(setter == null) {
			throw new NullPointerException("'setter' cannot be null.");
		}

		this.getter = getter;
		this.setter = setter;
		this.updater = updater;
		this.returner = returner;
	}

	public SimpleProperty<T, R> updater(Updater<T> updater) {
		this.updater = updater;
		return this;
	}

	public SimpleProperty<T, R> nullable() {
		this.isSet = true;
		this.nullable = true;
		return this;
	}

	public SimpleProperty<T, R> requireNonNull() {
		this.isSet = true;
		this.nullable = false;
		return this;
	}

	public SimpleProperty<T, R> whenNull(T nullSafety) {
		this.nullSafety = nullSafety;
		return this;
	}

	@Override
	public T get() {
		return this.getter.get();
	}

	@Override
	public R set(T value) {
		if(this.isSet && value == null) {
			if(this.nullable) {
				if(this.nullSafety != null) {
					value = this.nullSafety;
				}
			} else {
				throw new NullPointerException("'value' is a non-null argument.");
			}
		}

		this.setter.set(value);

		if(this.updater != null) {
			this.updater.valueUpdated(value);
		}

		return this.returner;
	}
}
