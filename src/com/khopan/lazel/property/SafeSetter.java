package com.khopan.lazel.property;

public class SafeSetter<T, R> implements Setter<T, R> {
	private final SimpleSetter<T> setter;
	private Updater<T> updater;
	private boolean isSet;
	private boolean nullable;
	private T nullSafety;
	private R returner;

	public SafeSetter(SimpleSetter<T> setter, R returner) {
		this(setter, null, returner);
	}

	public SafeSetter(SimpleSetter<T> setter, Updater<T> updater, R returner) {
		if(setter == null) {
			throw new NullPointerException("'setter' cannot be null.");
		}

		this.setter = setter;
		this.updater = updater;
		this.returner = returner;
	}

	public SafeSetter<T, R> updater(Updater<T> updater) {
		this.updater = updater;
		return this;
	}

	public SafeSetter<T, R> nullable() {
		this.isSet = true;
		this.nullable = true;
		return this;
	}

	public SafeSetter<T, R> requireNonNull() {
		this.isSet = true;
		this.nullable = false;
		return this;
	}

	public SafeSetter<T, R> whenNull(T nullSafety) {
		this.nullSafety = nullSafety;
		return this;
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
