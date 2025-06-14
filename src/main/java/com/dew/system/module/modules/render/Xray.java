package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Xray extends Module {

    public Xray() {
        super("Xray", ModuleCategory.RENDER, Keyboard.KEY_F7, false, true, true);
    }

    private final List<Block> XRAY_BLOCKS = new ArrayList<>(Arrays.asList(
            // 鉱石
            Blocks.coal_ore,
            Blocks.iron_ore,
            Blocks.gold_ore,
            Blocks.redstone_ore,
            Blocks.lapis_ore,
            Blocks.diamond_ore,
            Blocks.emerald_ore,
            Blocks.quartz_ore,

            // レア・便利
            Blocks.coal_block,
            Blocks.iron_block,
            Blocks.gold_block,
            Blocks.redstone_block,
            Blocks.lapis_block,
            Blocks.diamond_block,
            Blocks.emerald_block,

            // 探索補助
            Blocks.clay,
            Blocks.glowstone,
            Blocks.fire,
            Blocks.tnt,
            Blocks.torch,
            Blocks.ladder,
            Blocks.crafting_table,
            Blocks.furnace,
            Blocks.lit_furnace,

            // ストレージ
            Blocks.chest,
            Blocks.trapped_chest,
            Blocks.ender_chest,
            Blocks.command_block,

            // 構造物
            Blocks.bookshelf,
            Blocks.enchanting_table,
            Blocks.mob_spawner,
            Blocks.end_portal_frame,
            Blocks.mossy_cobblestone,

            // 液体
            Blocks.water,
            Blocks.flowing_water,
            Blocks.lava,
            Blocks.flowing_lava,

            // その他
            Blocks.beacon,
            Blocks.anvil,
            Blocks.redstone_torch,
            Blocks.rail,
            Blocks.detector_rail,
            Blocks.activator_rail,
            Blocks.golden_rail
    ));

    public List<Block> getXrayBlocks() {
        return this.XRAY_BLOCKS;
    }

    @Override
    public void onEnable() {
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        mc.renderGlobal.loadRenderers();
    }
}