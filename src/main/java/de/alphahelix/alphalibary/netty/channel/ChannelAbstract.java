package de.alphahelix.alphalibary.netty.channel;

import de.alphahelix.alphalibary.netty.IPacketListener;
import de.alphahelix.alphalibary.reflection.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class ChannelAbstract {

    static final Class<?> ENTITY_PLAYER_CLASS = ReflectionUtil.getNmsClass("ENTITY_PLAYER_CLASS");
    static final Class<?> PLAYER_CONNECTION_CLASS = ReflectionUtil.getNmsClass("PLAYER_CONNECTION_CLASS");
    static final Class<?> PACKET_CLASS = ReflectionUtil.getNmsClass("PACKET_CLASS");
    static final Class<?> SERVER_CONNECTION_CLASS = ReflectionUtil.getNmsClass("SERVER_CONNECTION_CLASS");
    static final Class<?> MINECRAFT_SERVER_CLASS = ReflectionUtil.getNmsClass("MINECRAFT_SERVER_CLASS");

    static final ReflectionUtil.SaveField
            NETWORK_MANAGER = ReflectionUtil.getDeclaredField("NETWORK_MANAGER", PLAYER_CONNECTION_CLASS);

    static final ReflectionUtil.SaveField
            PLAYER_CONNECTION = ReflectionUtil.getDeclaredField("PLAYER_CONNECTION", ENTITY_PLAYER_CLASS);

    static final ReflectionUtil.SaveField
            SERVER_CONNECTION = ReflectionUtil.getFirstType(SERVER_CONNECTION_CLASS, MINECRAFT_SERVER_CLASS);

    static final ReflectionUtil.SaveField
            CONNECTION_LIST = ReflectionUtil.getLastType(List.class, SERVER_CONNECTION_CLASS);

    static final ReflectionUtil.SaveMethod
            GET_SERVER = ReflectionUtil.getDeclaredMethod("GET_SERVER", Bukkit.getServer().getClass());

    static final String KEY_HANDLER = "packet_handler";
    static final String KEY_PLAYER = "packet_listener_player";
    static final String KEY_SERVER = "packet_listener_server";

    final Executor addChannelExecutor = Executors.newSingleThreadExecutor();
    final Executor removeChannelExecutor = Executors.newSingleThreadExecutor();
    private IPacketListener iPacketListener;

    public ChannelAbstract(IPacketListener iPacketListener) {
        this.iPacketListener = iPacketListener;
    }

    public abstract void addChannel(Player player);

    public abstract void removeChannel(Player player);

    public void addServerChannel() {
        try {
            Object dedicatedServer = GET_SERVER.invoke(Bukkit.getServer(), false);
            if (dedicatedServer == null) return;

            Object serverConnection = ChannelAbstract.SERVER_CONNECTION.get(dedicatedServer);
            if (serverConnection == null) return;

            List currentList = (List) CONNECTION_LIST.get(serverConnection);
            ReflectionUtil.SaveField superListField = new ReflectionUtil.SaveField(currentList.getClass().getSuperclass().getDeclaredField("list"));
            Object list = superListField.get(currentList);
            if (IListenerList.class.isAssignableFrom(list.getClass())) return;

            List newList = Collections.synchronizedList(newListenerList());
            for (Object o : currentList) {
                newList.add(o);
            }

            CONNECTION_LIST.set(serverConnection, newList, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract IListenerList newListenerList();

    protected final Object onPacketSend(Object receiver, Object packet, Cancellable cancellable) {
        return iPacketListener.onPacketSend(receiver, packet, cancellable);
    }

    protected final Object onPacketReceive(Object sender, Object packet, Cancellable cancellable) {
        return iPacketListener.onPacketReceive(sender, packet, cancellable);
    }

    interface IListenerList<E> extends List<E> {
    }

    interface IChannelHandler {
    }

    interface IChannelWrapper {
    }
}
