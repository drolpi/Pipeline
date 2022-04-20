package de.natrox.pipeline.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A document is a wrapper to persistence data or read data in the heap or easy into the following implementation format
 * of this interface.
 */
public interface Document<R extends Document<R>> extends Serializable, Readable, Persistable, Iterable<String>, Cloneable {

    @NotNull Collection<String> keys();

    int size();

    @NotNull R clear();

    @NotNull R remove(@NotNull String key);

    boolean contains(@NotNull String key);

    @NotNull R append(@NotNull String key, @Nullable Object value);

    @NotNull R append(@NotNull String key, @Nullable Number value);

    @NotNull R append(@NotNull String key, @Nullable Boolean value);

    @NotNull R append(@NotNull String key, @Nullable String value);

    @NotNull R append(@NotNull String key, @Nullable Character value);

    @NotNull R append(@NotNull String key, @Nullable R value);

    @NotNull R append(@Nullable R t);

    @NotNull R appendNull(@NotNull String key);

    @NotNull R getDocument(@NotNull String key);

    default int getInt(@NotNull String key) {
        return this.getInt(key, 0);
    }

    default double getDouble(@NotNull String key) {
        return this.getDouble(key, 0);
    }

    default float getFloat(@NotNull String key) {
        return this.getFloat(key, 0);
    }

    default byte getByte(@NotNull String key) {
        return this.getByte(key, (byte) 0);
    }

    default short getShort(@NotNull String key) {
        return this.getShort(key, (short) 0);
    }

    default long getLong(@NotNull String key) {
        return this.getLong(key, 0);
    }

    default boolean getBoolean(@NotNull String key) {
        return this.getBoolean(key, false);
    }

    default @UnknownNullability String getString(@NotNull String key) {
        return this.getString(key, null);
    }

    default char getChar(@NotNull String key) {
        return this.getChar(key, (char) 0);
    }

    default @UnknownNullability Object get(@NotNull String key) {
        return this.get(key, (Object) null);
    }

    default @UnknownNullability <T> T get(@NotNull String key, @NotNull Class<T> clazz) {
        return this.get(key, clazz, null);
    }

    default @UnknownNullability <T> T get(@NotNull String key, @NotNull Type type) {
        return this.get(key, type, null);
    }

    @UnknownNullability R getDocument(@NotNull String key, @Nullable R def);

    int getInt(@NotNull String key, int def);

    double getDouble(@NotNull String key, double def);

    float getFloat(@NotNull String key, float def);

    byte getByte(@NotNull String key, byte def);

    short getShort(@NotNull String key, short def);

    long getLong(@NotNull String key, long def);

    boolean getBoolean(@NotNull String key, boolean def);

    @UnknownNullability String getString(@NotNull String key, @Nullable String def);

    char getChar(@NotNull String key, char def);

    @UnknownNullability Object get(@NotNull String key, @Nullable Object def);

    @UnknownNullability <T> T get(@NotNull String key, @NotNull Class<T> clazz, @Nullable T def);

    @UnknownNullability <T> T get(@NotNull String key, @NotNull Type type, @Nullable T def);

    default boolean empty() {
        return this.size() == 0;
    }

    default @NotNull Stream<String> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}
