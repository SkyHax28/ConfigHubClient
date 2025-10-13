package com.dew.system.module.modules.other;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;

public class MurdererDetector extends Module {

    private static final ArrayList<EntityPlayer> murderers = new ArrayList<>();
    private final ArrayList<Item> items = new ArrayList<>(Arrays.asList( // updated items 2025/08/11 0:58
            Items.iron_sword,
            Items.stone_sword,
            Items.iron_shovel,
            Items.stick,
            Items.wooden_axe,
            Items.wooden_sword,
            Item.getItemFromBlock(Blocks.deadbush),
            Items.reeds,
            Items.stone_shovel,
            Items.blaze_rod,
            Items.diamond_shovel,
            Items.quartz,
            Items.pumpkin_pie,
            Items.golden_pickaxe,
            Items.leather,
            Items.name_tag,
            Items.coal,
            Items.flint,
            Items.bone,
            Items.golden_carrot,
            Items.cookie,
            Items.diamond_axe,
            Item.getItemFromBlock(Blocks.double_plant),
            Items.prismarine_shard,
            Items.cooked_beef,
            Items.netherbrick,
            Items.cooked_chicken,
            Items.record_blocks,
            Items.golden_hoe,
            Items.dye,
            Items.golden_sword,
            Items.diamond_sword,
            Items.diamond_hoe,
            Items.shears,
            Items.fish,
            Items.bread,
            Items.boat,
            Items.speckled_melon,
            Items.book,
            Item.getItemFromBlock(Blocks.sapling),
            Items.golden_axe,
            Items.diamond_pickaxe,
            Items.golden_shovel
    ));

    public MurdererDetector() {
        super("Murderer Detector", ModuleCategory.OTHER, Keyboard.KEY_NONE, false, true, true);
    }

    public ArrayList<EntityPlayer> getMurderers() {
        return murderers;
    }

    @Override
    public void onDisable() {
        murderers.clear();
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        murderers.clear();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (!player.getName().isEmpty() && player.getHeldItem() != null && (!murderers.contains(player) && items.contains(player.getHeldItem().getItem()))) {
                murderers.add(player);
                LogUtil.printChat("Murderer " + player.getName() + " was detected!");
            }
        }
    }
}
