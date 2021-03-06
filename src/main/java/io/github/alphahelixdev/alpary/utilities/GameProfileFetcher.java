package io.github.alphahelixdev.alpary.utilities;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import io.github.alphahelixdev.alpary.Alpary;
import io.github.alphahelixdev.alpary.utils.Utils;
import io.github.whoisalphahelix.helix.Cache;
import io.github.whoisalphahelix.helix.io.HonFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

@Getter
@EqualsAndHashCode
@ToString
public class GameProfileFetcher {
	
	private static final String SERVICE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
	private static final String JSON_SKIN = "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"isPublic\":true,\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}";
	private static final String JSON_CAPE = "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"isPublic\":true,\"textures\":{\"SKIN\":{\"url\":\"%s\"},\"CAPE\":{\"url\":\"%s\"}}}";
	
	private final GameProfileCache cache;

    public GameProfileFetcher() throws IOException {
		this.cache = new GameProfileCache();
        Alpary.getInstance().cacheHandler().addCache(this.cache);
	}
	
	public static GameProfile buildProfile(String name, String skinUrl, String capeUrl) {
		UUID id = UUID.randomUUID();
		GameProfile profile = new GameProfile(id, name);
		boolean cape = capeUrl != null && !capeUrl.isEmpty();
		
		List<Object> args = new ArrayList<>();
		args.add(System.currentTimeMillis());
		args.add(UUIDTypeAdapter.fromUUID(id));
		args.add(name);
		args.add(skinUrl);
		if (cape)
			args.add(capeUrl);
		
		profile.getProperties().clear();
		profile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString(String.format(cape ? JSON_CAPE : JSON_SKIN, args.toArray()))));
		return profile;
	}
	
	public void fetch(UUID id, Consumer<GameProfile> profile) {
		fetch(id, false, profile);
	}
	
	public void fetch(UUID id, boolean forceNew, Consumer<GameProfile> profile) {
		if (!forceNew && getCache().getProfiles().containsKey(id))
			profile.accept(getCache().getProfiles().get(id));
		else
			Utils.schedules().runAsync(() -> profile.accept(fetch(id, forceNew)));
	}
	
	public GameProfile fetch(UUID id, boolean forceNew) {
		if (!forceNew && getCache().getProfiles().containsKey(id))
			return getCache().getProfiles().get(id);
		
		HttpURLConnection connection = null;
		
		try {
			connection = (HttpURLConnection) new URL(String.format(SERVICE_URL, UUIDTypeAdapter.fromUUID(id))).openConnection();
		} catch (IOException ignored) {
		}
		
		if (connection == null)
            return new GameProfile(UUID.randomUUID(), Utils.strings().generateRandomString(15));
		
		connection.setReadTimeout(5000);
		
		try {
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String json = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
				
				GameProfile result = Alpary.getInstance().gson().fromJson(json, GameProfile.class);
				
				getCache().getProfiles().put(id, result);
				
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

        return new GameProfile(UUID.randomUUID(), Utils.strings().generateRandomString(15));
	}
	
	@Getter
	@EqualsAndHashCode
	@ToString
	public class GameProfileCache implements Cache {
		
		private final Map<UUID, GameProfile> profiles = new HashMap<>();
        private final HonFile honFile;

        GameProfileCache() throws IOException {
            honFile = new HonFile(Alpary.getInstance().getDataFolder(), "profiles.hon", Alpary.getInstance());
		}
		
		@Override
        public boolean clear() {
			profiles.clear();
            return true;
        }

		@Override
        public String log() {
            return "Gameprofile Cache cleared";
        }

		@Override
		public void save() {
            profiles.forEach((uuid, gameProfile) -> {
                honFile.getHon().set(uuid.toString(), gameProfile);
                honFile.update();
            });
		}
	}
}
