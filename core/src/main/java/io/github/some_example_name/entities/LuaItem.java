package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * Item/resource placed on the Lua map.
 * The type is kept here so future missions can identify and collect resources.
 */
public class LuaItem {

    public enum Type {
        FOOD,
        O2_TANK,
        ICE
    }

    private final Type type;
    private final Rectangle hitbox;

    public LuaItem(Type type, float x, float y, float width, float height) {
        this.type = type;
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public Type getType() {
        return type;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getX() {
        return hitbox.x;
    }

    public float getY() {
        return hitbox.y;
    }

    public float getWidth() {
        return hitbox.width;
    }

    public float getHeight() {
        return hitbox.height;
    }
}
