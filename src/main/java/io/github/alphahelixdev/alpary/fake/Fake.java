package io.github.alphahelixdev.alpary.fake;

import com.mojang.authlib.GameProfile;
import io.github.alphahelixdev.alpary.Alpary;
import io.github.alphahelixdev.alpary.fake.entities.*;
import io.github.alphahelixdev.alpary.fake.events.FakeEntityClickEvent;
import io.github.alphahelixdev.alpary.fake.exceptions.NoSuchFakeEntityException;
import io.github.alphahelixdev.alpary.reflection.nms.enums.REnumAction;
import io.github.alphahelixdev.alpary.reflection.nms.enums.REnumHand;
import io.github.alphahelixdev.alpary.reflection.nms.nettyinjection.NettyInjector;
import io.github.alphahelixdev.alpary.reflection.nms.nettyinjection.handler.PacketHandler;
import io.github.alphahelixdev.alpary.reflection.nms.nettyinjection.handler.PacketOptions;
import io.github.alphahelixdev.alpary.reflection.nms.nettyinjection.handler.ReceivedPacket;
import io.github.alphahelixdev.alpary.reflection.nms.nettyinjection.handler.SentPacket;
import io.github.alphahelixdev.alpary.reflection.nms.wrappers.PlayerInfoDataWrapper;
import io.github.alphahelixdev.alpary.utils.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Fake {

    private static final EntityHandler ENTITY_HANDLER = new EntityHandler();
	private static final List<String> NPCS = new ArrayList<>();
	private static final Map<Integer, UUID> ENTITY_ID_MAP = new HashMap<>();
	private static final Map<Class<? extends FakeEntity>, EntityStorage<? extends FakeEntity>> STORAGE = new HashMap<>();
	private static boolean enabled = false;
	
	public Fake(JavaPlugin plugin) {
		storage().put(FakeArmorstand.class,
				new EntityStorage<>(plugin, "armorstands", FakeArmorstand.class));
		storage().put(FakeBigItem.class,
				new EntityStorage<>(plugin, "bigItems", FakeBigItem.class));
		storage().put(FakeMob.class,
				new EntityStorage<>(plugin, "mobs", FakeMob.class));
		storage().put(FakeEndercrystal.class,
				new EntityStorage<>(plugin, "endercrystals", FakeEndercrystal.class));
		storage().put(FakeItem.class,
				new EntityStorage<>(plugin, "items", FakeItem.class));
		storage().put(FakePlayer.class,
				new EntityStorage<>(plugin, "players", FakePlayer.class));
		storage().put(FakeXPOrb.class,
				new EntityStorage<>(plugin, "xpOrbs", FakeXPOrb.class));
	}
	
	public static Map<Class<? extends FakeEntity>, EntityStorage<? extends FakeEntity>> storage() {
		return Fake.STORAGE;
	}
	
	public static Map<Integer, UUID> entityIDMap() {
		return Fake.ENTITY_ID_MAP;
	}
	
	public static <T extends FakeEntity> EntityStorage<T> storage(Class<T> entity) {
		return (EntityStorage<T>) storage().get(entity);
	}
	
	public void enable() {
		if(enabled)
			return;
		NettyInjector.addPacketHandler(new PacketHandler(Alpary.getInstance()) {
			@PacketOptions(forcePlayer = true)
			public void onSend(SentPacket packet) {
				if(packet.getPacketName().equals("PacketPlayOutPlayerInfo")) {
					Object enumAction = packet.getPacketValue("a");
					
					if(!enumAction.toString().equals("ADD_PLAYER")) return;
					
					ArrayList<Object> newPlayerInfo = new ArrayList<>();
					
					for(Object playerInfo : (ArrayList) packet.getPacketValue("b")) {
						
						if(PlayerInfoDataWrapper.isUnknown(playerInfo)) {
							newPlayerInfo.add(playerInfo);
							continue;
						}
						
						PlayerInfoDataWrapper wrapper = PlayerInfoDataWrapper.getPlayerInfo(playerInfo);
						OfflinePlayer p = Bukkit.getOfflinePlayer(wrapper.getProfile().getId());
						
						if(Fake.npcs().contains(wrapper.getName())) {
							NMSUtil.getReflections().getDeclaredField("name", GameProfile.class).set(wrapper.getProfile(), wrapper.getName(), true);
							
							newPlayerInfo.add(new PlayerInfoDataWrapper(
									wrapper.getProfile(), wrapper.getPing(), wrapper.getGameMode(), p.getName(), packet.getPacket()
							).getPlayerInfoData());
						} else {
							newPlayerInfo.add(playerInfo);
						}
						
					}
					packet.setPacketValue("b", newPlayerInfo);
				}
			}
			
			@PacketOptions(forcePlayer = true)
			public void onReceive(ReceivedPacket packet) {
				if(packet.getPacketName().equals("PacketPlayInUseEntity")) {
					Player p = packet.getPlayer();
					int id = (int) packet.getPacketValue("a");

					try {
						FakeEntity clicked = Fake.getEntityHandler().getFakeEntityByID(p, id);

						REnumAction action = REnumAction.valueOf(packet.getPacketValue("action").toString());
						REnumHand hand = REnumHand.MAIN_HAND;
						
						if(packet.getPacketValue("d") != null)
							hand = REnumHand.values()[NMSUtil.getReflections().getEnumConstantID(packet.getPacketValue("d"))];

						if(action != REnumAction.INTERACT_AT)
							Bukkit.getPluginManager().callEvent(new FakeEntityClickEvent(p, clicked, action, hand));
					} catch(NoSuchFakeEntityException ignore) {
					}
				}
			}
		});
		enabled = true;
	}
	
	public static List<String> npcs() {
		return Fake.NPCS;
	}
	
	public static EntityHandler getEntityHandler() {
        return ENTITY_HANDLER;
    }
}
