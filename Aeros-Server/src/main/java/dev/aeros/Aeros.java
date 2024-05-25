package dev.aeros;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import dev.aeros.async.AsyncUtil;
import dev.aeros.async.pathsearch.SearchHandler;
import dev.aeros.async.thread.CombatThread;
import dev.aeros.commands.KnockbackCommand;
import dev.aeros.commands.MobAICommand;
import dev.aeros.commands.PingCommand;
import dev.aeros.commands.SetMaxSlotCommand;
import dev.aeros.commands.SpawnMobCommand;
import dev.aeros.config.AerosConfig;
import dev.aeros.hitdetection.LagCompensator;
import dev.aeros.protocol.MovementListener;
import dev.aeros.protocol.PacketListener;
import dev.aeros.statistics.StatisticsClient;
import net.minecraft.server.MinecraftServer;
import dev.amargos.anticrash.AntiCrash;
import dev.amargos.async.AsyncExplosions;

public class Aeros {

	private StatisticsClient client;

	public static final Logger LOGGER = LogManager.getLogger();
	private static final Logger DEBUG_LOGGER = LogManager.getLogger();
	private static Aeros INSTANCE;

	private CombatThread knockbackThread;

	private final Executor statisticsExecutor = Executors
			.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Aeros Statistics Thread")
					.build());

	private volatile boolean statisticsEnabled = false;

	private LagCompensator lagCompensator;

	private final Set<PacketListener> packetListeners = Sets.newConcurrentHashSet();
	private final Set<MovementListener> movementListeners = Sets.newConcurrentHashSet();

	public Aeros() {
		INSTANCE = this;
		this.init();
	}

	public void reload() {
		this.init();
	}

	private void initCmds() {

		SimpleCommandMap commandMap = MinecraftServer.getServer().server.getCommandMap();

		if (AerosConfig.mobAiCmd) {
			MobAICommand mobAiCommand = new MobAICommand("mobai");
			commandMap.register(mobAiCommand.getName(), "", mobAiCommand);
		}

		if (AerosConfig.pingCmd) {
			PingCommand pingCommand = new PingCommand("ping");
			commandMap.register(pingCommand.getName(), "", pingCommand);
		}

		// NachoSpigot commands
		// TODO: add configuration for all of these
		SetMaxSlotCommand setMaxSlotCommand = new SetMaxSlotCommand("sms"); // [Nacho-0021] Add setMaxPlayers within
																			// Bukkit.getServer() and SetMaxSlot Command
		commandMap.register(setMaxSlotCommand.getName(), "ns", setMaxSlotCommand);

		SpawnMobCommand spawnMobCommand = new SpawnMobCommand("spawnmob");
		commandMap.register(spawnMobCommand.getName(), "ns", spawnMobCommand);

		KnockbackCommand knockbackCommand = new KnockbackCommand("kb");
		commandMap.register(knockbackCommand.getName(), "ns", knockbackCommand);
	}

	private void initStatistics() {
		if (AerosConfig.statistics && !statisticsEnabled) {
			Runnable statisticsRunnable = (() -> {
				client = new StatisticsClient();
				try {
					statisticsEnabled = true;

					if (!client.isConnected) {
						// Connect to the statistics server and notify that there is a new server
						client.start("150.230.35.78", 500);
						client.sendMessage("new server");

						while (true) {
							// Keep alive, this tells the statistics server that this server
							// is still online
							client.sendMessage("keep alive packet");

							// Online players, this tells the statistics server how many players
							// are on
							client.sendMessage("player count packet " + Bukkit.getOnlinePlayers().size());

							// Statistics are sent every 40 secs.
							TimeUnit.SECONDS.sleep(40);
						}

					}
				} catch (Exception ignored) {
				}
			});
			AsyncUtil.run(statisticsRunnable, statisticsExecutor);
		}
	}

	private void init() {
		initCmds();
		initStatistics();

		// We do not want to initialize this again after a reload
		if (AerosConfig.asyncPathSearches && SearchHandler.getInstance() == null) {
			new SearchHandler();
		}

		if (AerosConfig.asyncKnockback) {
			knockbackThread = new CombatThread("Knockback Thread");
		}
		lagCompensator = new LagCompensator();
		if (AerosConfig.asyncTnt) {
			AsyncExplosions.initExecutor(AerosConfig.fixedPoolSize);
		}
		if (AerosConfig.enableAntiCrash) {
			registerPacketListener(new AntiCrash());
		}
	}

	public StatisticsClient getClient() {
		return this.client;
	}

	public CombatThread getKnockbackThread() {
		return knockbackThread;
	}

	public LagCompensator getLagCompensator() {
		return lagCompensator;
	}

	public static void debug(String msg) {
		if (AerosConfig.debugMode)
			DEBUG_LOGGER.info(msg);
	}

	public void registerPacketListener(PacketListener packetListener) {
		this.packetListeners.add(packetListener);
	}

	public void unregisterPacketListener(PacketListener packetListener) {
		this.packetListeners.remove(packetListener);
	}

	public Set<PacketListener> getPacketListeners() {
		return this.packetListeners;
	}

	public void registerMovementListener(MovementListener movementListener) {
		this.movementListeners.add(movementListener);
	}

	public void unregisterMovementListener(MovementListener movementListener) {
		this.movementListeners.remove(movementListener);
	}

	public Set<MovementListener> getMovementListeners() {
		return this.movementListeners;
	}

	public static Aeros getInstance() {
		return INSTANCE;
	}
}
