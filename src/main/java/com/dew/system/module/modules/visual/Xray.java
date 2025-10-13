package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Xray extends Module {

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
            Blocks.tnt,
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
            Blocks.end_portal_frame,
            Blocks.mossy_cobblestone,

            // その他
            Blocks.anvil
    ));

    public Xray() {
        super("Xray", ModuleCategory.VISUAL, Keyboard.KEY_F7, false, true, true);
    }

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