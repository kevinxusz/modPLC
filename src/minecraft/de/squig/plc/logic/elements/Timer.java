package de.squig.plc.logic.elements;

import de.squig.plc.client.gui.controller.LogicTextureTile;
import de.squig.plc.logic.Circuit;
import de.squig.plc.logic.elements.functions.ElementFunction;


public class Timer extends CircuitElement {
	public Timer(Circuit circuit, int mapX, int mapY) {
		super(circuit, mapX, mapY, ElementFunction.TIMEROUTPUT);
		functions.add(ElementFunction.TIMERRESET);
		functions.add(ElementFunction.TIMERSTOP);
		functions.add(ElementFunction.TIMEROUTPUT);
		setTexture(LogicTextureTile.LOGIC_TIMER);
		name = "Timer";
	}
	public static String getDisplayName() {
		return "Timer (Sends Pulses based on Time) [T]";
	}
	public static int getDisplayTextureId() {
		return 229;
	}
	
	
	
}
