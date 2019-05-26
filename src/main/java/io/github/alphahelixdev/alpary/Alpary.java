package io.github.alphahelixdev.alpary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.alphahelixdev.alpary.addons.AddonCore;
import io.github.alphahelixdev.alpary.annotations.AnnotationHandler;
import io.github.alphahelixdev.alpary.fake.Fake;
import io.github.alphahelixdev.alpary.reflection.nms.nettyinjection.NettyInjector;
import io.github.alphahelixdev.alpary.utilities.GameProfileFetcher;
import io.github.alphahelixdev.alpary.utilities.UUIDFetcher;
import io.github.alphahelixdev.alpary.utilities.json.MaterialTypeAdapter;
import io.github.whoisalphahelix.helix.Helix;
import io.github.whoisalphahelix.helix.IHelix;
import io.github.whoisalphahelix.helix.handlers.CacheHandler;
import io.github.whoisalphahelix.helix.handlers.IOHandler;
import io.github.whoisalphahelix.helix.handlers.NettyHandler;
import io.github.whoisalphahelix.helix.reflection.Reflection;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.logging.Logger;

public class Alpary extends JavaPlugin implements IHelix {
	
	private static final GsonBuilder GSON_BUILDER = new GsonBuilder().registerTypeAdapter(Material.class,
			new MaterialTypeAdapter());
	@Getter
	private static Alpary instance;
	
	private final NettyInjector nettyInjector = new NettyInjector();
	private final Helix helix = Helix.helix();
	private final AnnotationHandler annotationHandler = new AnnotationHandler(helix);
	
	private UUIDFetcher uuidFetcher;
	private GameProfileFetcher gameProfileFetcher;
	
	@Override
	public void onLoad() {
		nettyInjector.load();
	}
	
	@Override
	public void onDisable() {
		this.nettyInjector.disable();
	}
	
	@Override
	public void onEnable() {
		Alpary.instance = this;

		ioHandler().createFolder(getDataFolder());
		
		this.uuidFetcher = new UUIDFetcher();
		try {
			this.gameProfileFetcher = new GameProfileFetcher();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new AddonCore().enable();
		
		this.annotationHandler.registerListeners();
		this.annotationHandler.createSingletons();

		this.nettyInjector.enable(this);
		new Fake(this).enable();

		cacheHandler().startCacheClearing();
	}
	
	@Override
	public Reflection reflection() {
		return this.helix.reflection();
	}
	
	@Override
	public Reflections reflections() {
		return this.helix.reflections();
	}
	
	@Override
	public Logger logger() {
		return this.helix.logger();
	}
	
	@Override
	public AnnotationHandler annotationHandler() {
		return this.annotationHandler;
	}
	
	@Override
	public CacheHandler cacheHandler() {
		return this.helix.cacheHandler();
	}
	
	public GameProfileFetcher gameProfileFetcher() {
		return this.gameProfileFetcher;
	}
	
	@Override
	public IOHandler ioHandler() {
		return this.helix.ioHandler();
	}
	
	public NettyInjector nettyInjector() {
		return nettyInjector;
	}
	
	@Override
	public NettyHandler nettyHandler() {
		return this.helix.nettyHandler();
	}
	
	public Gson gson() {
		return gsonBuilder().create();
	}
	
	public GsonBuilder gsonBuilder() {
		return GSON_BUILDER;
	}
	
	public UUIDFetcher uuidFetcher() {
		return this.uuidFetcher;
	}
	
	public Helix helix() {
		return this.helix;
	}
}