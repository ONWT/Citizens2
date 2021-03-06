package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Location;

public class CurrentLocation extends Trait {
    @Persist(value = "", required = true)
    private Location location = new Location(null, 0, 0, 0);

    public CurrentLocation() {
        super("location");
    }

    public Location getLocation() {
        return location.getWorld() == null ? null : location;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        location = npc.getBukkitEntity().getLocation(location);
    }

    public void setLocation(Location loc) {
        this.location = loc;
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + location + "}";
    }
}