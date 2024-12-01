package com.thatoneaiguy.init;

import com.thatoneaiguy.item.BombardItem;
import com.thatoneaiguy.item.CrateCracker;
import com.thatoneaiguy.item.TrackerCompassItem;
import com.thatoneaiguy.item.TrackerItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.thatoneaiguy.Paroxysm.MODID;

public class ParoxysmItems {
    //public static final Item ITEM_NAME = createItem(Rarity.COMMON, 64);

    public static final Item BOMBARD = new BombardItem(new FabricItemSettings().maxCount(1).rarity(Rarity.COMMON));

    public static final Item CRATE_CRACKER = new CrateCracker(new FabricItemSettings().maxCount(1).rarity(Rarity.COMMON));

    public static final Item TRACKER = new TrackerItem(new FabricItemSettings().maxCount(1).rarity(Rarity.COMMON));
    public static final Item TRACKER_COMPASS = new TrackerCompassItem(new FabricItemSettings().maxCount(1).rarity(Rarity.COMMON));

    public static Item createItem(Rarity rarity, int maxCount) {
        return new Item(new FabricItemSettings().maxCount(maxCount).rarity(rarity));
    }

    static Map<String, Object> ITEMS = Stream.of(new Object[][]{
            {"bombard", BOMBARD},
            {"crate_cracker", CRATE_CRACKER},
            {"tracker", TRACKER},
            {"tracker_compass", TRACKER_COMPASS}
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));


    public static void registerAll() {
        for (Map.Entry<String, Object> entry : ITEMS.entrySet()) {
            String key = entry.getKey();
            Item value = (Item) entry.getValue();

            registerItem(key, value);

            //registerToolItem("tool_name", TOOL_NAME);
        }

    }

    private static void registerItem(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(MODID, name), item);
    }

    private static void registerToolItem(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(MODID, name), item);
    }
}
