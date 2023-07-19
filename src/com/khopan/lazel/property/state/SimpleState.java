package com.khopan.lazel.property.state;

import com.khopan.lazel.property.Getter;
import com.khopan.lazel.property.SimpleSetter;
import com.khopan.lazel.property.Updater;

public class SimpleState<R> implements State<R> {
	private final Getter<Boolean> getter;
	private final SimpleSetter<Boolean> setter;
	private Updater<Boolean> updater;
	private R returner;

	public SimpleState(Getter<Boolean> getter, SimpleSetter<Boolean> setter, R returner) {
		this(getter, setter, null, returner);
	}

	public SimpleState(Getter<Boolean> getter, SimpleSetter<Boolean> setter, Updater<Boolean> updater, R returner) {
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

	public SimpleState<R> updater(Updater<Boolean> updater) {
		this.updater = updater;
		return this;
	}

	@Override
	public R setState(boolean state) {
		this.setter.set(state);

		if(this.updater != null) {
			this.updater.valueUpdated(state);
		}

		return this.returner;
	}

	@Override
	public R enable() {
		this.setState(true);
		return this.returner;
	}

	@Override
	public R disable() {
		this.setState(false);
		return this.returner;
	}

	@Override
	public boolean isEnable() {
		return this.getter.get();
	}

	@Override
	public boolean isDisable() {
		return !this.isEnable();
	}

	@Override
	public boolean getState() {
		return this.isEnable();
	}

	@Override
	public R toggle() {
		return this.setState(!this.getState());
	}
}
