package de.alphahelix.alphalibary.storage.sql2.special;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import de.alphahelix.alphalibary.core.AlphaLibary;
import de.alphahelix.alphalibary.core.utils.JSONUtil;
import de.alphahelix.alphalibary.storage.ReflectionHelper;
import de.alphahelix.alphalibary.storage.sql2.DatabaseType;
import de.alphahelix.alphalibary.storage.sql2.SQLCache;
import de.alphahelix.alphalibary.storage.sql2.SQLConstraints;
import de.alphahelix.alphalibary.storage.sql2.SQLDatabaseHandler;
import de.alphahelix.alphalibary.storage.sql2.mysql.MySQLDataType;
import de.alphahelix.alphalibary.storage.sql2.sqlite.SQLiteDataType;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleDatabaseMap<K, V> implements Map<K, V> {
	
	private static final JsonParser PARSER = new JsonParser();
	
	private final List<String> fieldNames = new ArrayList<>();
	private final String keyColumnName;
	private final Class<V> valueClazz;
	
	private final SQLDatabaseHandler handler;
	private final SQLCache cache = new SQLCache();
	
	public SimpleDatabaseMap(String table, String database, Class<K> keyClazz, Class<V> valueClazz, DatabaseType type) {
		this.handler = new SQLDatabaseHandler(table, database);
		
		this.keyColumnName = keyClazz.getSimpleName().toLowerCase();
		this.valueClazz = valueClazz;
		
		List<String> columnNames = new ArrayList<>();
		
		
		if(type == DatabaseType.MYSQL) {
			columnNames.add(SQLDatabaseHandler.createMySQLColumn(keyColumnName, MySQLDataType.VARCHAR, 767, SQLConstraints.PRIMARY_KEY));
			
			for(ReflectionHelper.SaveField valueFields : ReflectionHelper.findFieldsNotAnnotatedWith(Expose.class, valueClazz)) {
				columnNames.add(SQLDatabaseHandler.createMySQLColumn(
						valueFields.field().getName(), MySQLDataType.TEXT, 5000));
				
				fieldNames.add(valueFields.field().getName());
			}
		} else if(type == DatabaseType.SQLITE) {
			columnNames.add(SQLDatabaseHandler.createSQLiteColumn(keyColumnName, SQLiteDataType.TEXT, SQLConstraints.PRIMARY_KEY));
			
			for(ReflectionHelper.SaveField sf : ReflectionHelper.findFieldsNotAnnotatedWith(Expose.class, valueClazz)) {
				columnNames.add(SQLDatabaseHandler.createSQLiteColumn(
						sf.field().getName(), SQLiteDataType.TEXT));
				
				fieldNames.add(sf.field().getName());
			}
		}
		
		handler.create(columnNames.toArray(new String[columnNames.size()]));
	}
	
	public void addDefaultValue(K key, V value) {
		hasValue(key, res -> {
			if(!res) {
				addValue(key, value);
			}
		});
	}
	
	public void hasValue(K key, Consumer<Boolean> callback) {
		hasValue(key, callback, false);
	}
	
	public void hasValue(K key, Consumer<Boolean> callback, boolean cached) {
		hasValue(key.toString(), callback, cached);
	}
	
	public void hasValue(String key, Consumer<Boolean> callback) {
		hasValue(key, callback, false);
	}
	
	public void hasValue(String key, Consumer<Boolean> callback, boolean cached) {
		if(cached) {
			getKeys(vs -> callback.accept(vs.contains(key)), true);
			return;
		}
		handler.getList(keyColumnName, objects -> {
			if(objects == null) {
				callback.accept(false);
				return;
			}
			callback.accept(objects.contains(key));
		});
	}
	
	public void getKeys(Consumer<List<Object>> callback, boolean cached) {
		getKeys(-1, callback, cached);
	}
	
	public void getKeys(int limit, Consumer<List<Object>> callback, boolean cached) {
		if(cached) {
			callback.accept(new ArrayList<>(cache.getCache().keySet()));
			return;
		}
		handler.getList(keyColumnName, limit, callback);
	}
	
	public void getValue(K key, Consumer<V> callback) {
		getValue(key.toString(), callback);
	}
	
	public boolean hasSyncValue(K key) {
		return hasSyncValue(key, false);
	}
	
	public boolean hasSyncValue(K key, boolean cached) {
		return hasSyncValue(key.toString(), cached);
	}
	
	public boolean hasSyncValue(String key, boolean cached) {
		if(cached)
			return getSyncKeys(true).contains(key);
		
		List<Object> keys = getSyncKeys();
		
		if(keys == null)
			return false;
		
		return keys.contains(key);
	}
	
	public List<Object> getSyncKeys(boolean cached) {
		return getSyncKeys(-1, cached);
	}
	
	public List<Object> getSyncKeys() {
		return getSyncKeys(-1, false);
	}
	
	public List<Object> getSyncKeys(int limit, boolean cached) {
		if(cached)
			return new ArrayList<>(cache.getCache().keySet());
		
		return handler.getSyncList(keyColumnName, limit);
	}
	
	public void getValue(String key, Consumer<V> callback) {
		getValue(key, callback, false);
	}
	
	public void getKeys(Consumer<List<Object>> callback) {
		getKeys(-1, callback, false);
	}
	
	public boolean hasSyncValue(String key) {
		return hasSyncValue(key, false);
	}
	
	public void getValue(String key, Consumer<V> callback, boolean cached) {
		Bukkit.getScheduler().runTaskAsynchronously(AlphaLibary.getInstance(), () -> {
			if(cached) {
				if(cache.getObject(key).isPresent()) {
					callback.accept((V) cache.getObject(key).get());
					return;
				} else
					getValue(key, callback, false);
			}
			cache.save(key, getJSONValue(key));
			handler.syncedCallback(getJSONValue(key), callback);
		});
	}
	
	public void getValue(K key, Consumer<V> callback, boolean cached) {
		getValue(key.toString(), callback, cached);
	}
	
	public void getValues(Consumer<List<V>> callback) {
		getValues(callback, false);
	}
	
	public void getValues(Consumer<List<V>> callback, boolean cached) {
		if(cached) {
			callback.accept(new ArrayList<>(cache.getCache().values()));
			return;
		}
		
		getKeys(keys -> Bukkit.getScheduler().runTaskAsynchronously(AlphaLibary.getInstance(), () -> {
			List<V> values = new ArrayList<>();
			
			if(keys == null) {
				handler.syncedCallback(values, callback);
				return;
			}
			
			cache.getCache().clear();
			
			for(Object key : keys) {
				values.add(getJSONValue(key.toString()));
				cache.save(key.toString(), getJSONValue(key.toString()));
			}
			
			handler.syncedCallback(values, callback);
		}));
	}
	
	public V getSyncValue(K key) {
		return getSyncValue(key.toString(), false);
	}
	
	private V getJSONValue(String key) {
		JsonObject obj = new JsonObject();
		
		for(String fN : fieldNames) {
			JsonElement el = PARSER.parse(handler.getResult(keyColumnName, key, fN).toString());
			
			obj.add(fN, el);
		}
		
		return JSONUtil.getGson().fromJson(obj, valueClazz);
	}
	
	public V getSyncValue(String key) {
		return getSyncValue(key, false);
	}
	
	public V getSyncValue(K key, boolean cached) {
		return getSyncValue(key.toString(), cached);
	}
	
	public void getKeys(int limit, Consumer<List<Object>> callback) {
		getKeys(limit, callback, false);
	}
	
	public V getSyncValue(String key, boolean cached) {
		if(cached) {
			if(cache.getObject(key).isPresent())
				return (V) cache.getObject(key).get();
			else
				return getSyncValue(key, false);
		}
		
		cache.save(key, getJSONValue(key));
		return getJSONValue(key);
	}
	
	public List<Object> getSyncKeys(int limit) {
		return getSyncKeys(limit, false);
	}
	
	@Override
	public String toString() {
		return "SimpleDatabaseMap{" +
				"fieldNames=" + fieldNames +
				", keyColumnName='" + keyColumnName + '\'' +
				", valueClazz=" + valueClazz +
				", handler=" + handler +
				", cache=" + cache +
				'}';
	}
	
	@Override
	public int size() {
		return getSyncValues().size();
	}
	
	@Override
	public boolean isEmpty() {
		return getSyncKeys().size() == 0;
	}
	
	public List<V> getSyncValues() {
		return getSyncValues(false);
	}
	
	public List<V> getSyncValues(boolean cached) {
		if(cached)
			return new ArrayList<>(cache.getCache().values());
		
		List<V> values = new ArrayList<>();
		List<Object> keys = getSyncKeys();
		
		if(keys == null)
			return values;
		
		cache.getCache().clear();
		
		for(Object key : keys) {
			values.add(getJSONValue(key.toString()));
			cache.save(key.toString(), getJSONValue(key.toString()));
		}
		
		return values;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return getSyncKeys().contains(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return getSyncValues().contains(value);
	}
	
	@Override
	public V get(Object key) {
		return getSyncValue((K) key);
	}
	
	@Override
	public V put(K key, V value) {
		return addValue(key, value);
	}
	
	@Override
	public V remove(Object key) {
		return remove(key);
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
			addValue(entry.getKey(), entry.getValue());
		}
	}
	
	public V addValue(K key, V value) {
		Map<String, String> values = new LinkedHashMap<>();
		
		cache.save(key.toString(), value);
		
		values.put(keyColumnName, key.toString());
		
		for(String fieldName : fieldNames) {
			ReflectionHelper.SaveField sf = ReflectionHelper.getDeclaredField(fieldName, valueClazz);
			
			values.put(fieldName, JSONUtil.toJson(sf.get(value)));
		}
		
		this.handler.contains(keyColumnName, key.toString(), res -> {
			if(res) {
				for(String fieldName : values.keySet()) {
					this.handler.update(keyColumnName, key.toString(), fieldName, values.get(fieldName));
				}
			} else {
				this.handler.insert(values.values().toArray(new String[values.values().size()]));
			}
		});
		return value;
	}
	
	@Override
	public void clear() {
		handler.empty();
	}
	
	@Override
	public Set<K> keySet() {
		return new HashSet<>((Collection<? extends K>) getSyncKeys());
	}
	
	@Override
	public Collection<V> values() {
		return getSyncValues();
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> entries = new HashSet<>();
		
		for(int i = 0; i < keySet().size(); i++) {
			int finalI = i;
			entries.add(new Entry<K, V>() {
				@Override
				public K getKey() {
					return (K) getSyncKeys().get(finalI);
				}
				
				@Override
				public V getValue() {
					return getSyncValues().get(finalI);
				}
				
				@Override
				public V setValue(V value) {
					return value;
				}
			});
		}
		
		return entries;
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return containsKey(key) ? get(key) : defaultValue;
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		entrySet().forEach(kvEntry -> action.accept(kvEntry.getKey(), kvEntry.getValue()));
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		for(int i = 0; i < keySet().size(); i++) {
			function.apply((K) getSyncKeys().get(i), getSyncValues().get(i));
		}
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		return containsKey(key) ? value : put(key, value);
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		removeValue((K) key);
		return true;
	}
	
	public void removeValue(K key) {
		handler.remove(keyColumnName, key.toString());
		cache.remove(key.toString());
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		replace(key, newValue);
		return true;
	}
	
	@Override
	public V replace(K key, V value) {
		Map<String, String> values = new LinkedHashMap<>();
		
		cache.save(key.toString(), value);
		
		values.put(keyColumnName, key.toString());
		
		for(String fieldName : fieldNames) {
			ReflectionHelper.SaveField sf = ReflectionHelper.getDeclaredField(fieldName, valueClazz);
			
			values.put(fieldName, JSONUtil.toJson(sf.get(value)));
		}
		
		for(String fieldName : values.keySet())
			this.handler.update(keyColumnName, key.toString(), fieldName, values.get(fieldName));
		
		return value;
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return null;
	}
	
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return null;
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return null;
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return null;
	}
}
