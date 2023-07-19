package com.khopan.lazel.property.state;

public interface State<R> {
	public R setState(boolean state);
	public R enable();
	public R disable();
	public boolean isEnable();
	public boolean isDisable();
	public boolean getState();
	public R toggle();
}
