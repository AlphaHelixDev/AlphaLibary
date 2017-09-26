package de.alphahelix.alphalibary.storage.file;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import de.alphahelix.alphalibary.core.utils.JSONUtil;
import de.alphahelix.alphalibary.reflection.ReflectionUtil;

public interface ConfigValue<T> {

    String name();

    default T type() {
        return (T) this;
    }

    default JsonObject save() {
        JsonObject obj = new JsonObject();

        for (ReflectionUtil.SaveField f : ReflectionUtil.findFieldsNotAnnotatedWith(Expose.class, type().getClass())) {
            obj.add(f.field().getName(), JSONUtil.getGson().toJsonTree(f.get(this)));
        }

        return obj;
    }
}
