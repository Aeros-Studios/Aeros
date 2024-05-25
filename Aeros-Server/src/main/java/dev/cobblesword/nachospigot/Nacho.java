package dev.cobblesword.nachospigot;

import java.util.Set;

import dev.aeros.Aeros;
import dev.aeros.protocol.MovementListener;
import dev.aeros.protocol.PacketListener;

@Deprecated
public class Nacho {

	private static Nacho INSTANCE;

	public Nacho() {
		INSTANCE = this;
	}

	public static Nacho get() {
		return INSTANCE == null ? new Nacho() : INSTANCE;
	}

	public void registerCommands() {

	}

	public void registerPacketListener(PacketListener packetListener) {
		Aeros.getInstance().registerPacketListener(packetListener);
	}

	public void unregisterPacketListener(PacketListener packetListener) {
		Aeros.getInstance().unregisterPacketListener(packetListener);
	}

	public Set<PacketListener> getPacketListeners() {
		return Aeros.getInstance().getPacketListeners();
	}

	public void registerMovementListener(MovementListener movementListener) {
		Aeros.getInstance().registerMovementListener(movementListener);
	}

	public void unregisterMovementListener(MovementListener movementListener) {
		Aeros.getInstance().unregisterMovementListener(movementListener);
	}

	public Set<MovementListener> getMovementListeners() {
		return Aeros.getInstance().getMovementListeners();
	}

}
