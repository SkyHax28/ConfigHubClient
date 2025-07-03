package com.dew.system.module.modules.combat;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;

public class Teams extends Module {

    public Teams() {
        super("Teams", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final BooleanValue scoreboard = new BooleanValue("Scoreboard", true);
    private static final BooleanValue color = new BooleanValue("Color", false);
    private static final BooleanValue nameColor = new BooleanValue("Name Color", false);
    private static final BooleanValue armorColor = new BooleanValue("Armor Color", false);
    private static final NumberValue armorIndex = new NumberValue("Armor Index", 3.0, 0.0, 3.0, 1.0, armorColor::get);

    public boolean isInYourTeam(EntityLivingBase entity) {
        if (mc.thePlayer == null || !this.isEnabled()) return false;

        if (scoreboard.get() && mc.thePlayer.getTeam() != null && entity.getTeam() != null && mc.thePlayer.getTeam().isSameTeam(entity.getTeam())) {
            return true;
        }

        if (color.get() && mc.thePlayer.getDisplayName() != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");

            if (clientName.length() > 1) {
                return targetName.startsWith("§" + clientName.charAt(1));
            }
        }

        if (armorColor.get()) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer) entity;

                ItemStack myHead = mc.thePlayer.inventory.armorInventory[armorIndex.get().intValue()];
                ItemStack entityHead = entityPlayer.inventory.armorInventory[armorIndex.get().intValue()];

                if (myHead != null && entityHead != null) {
                    ItemArmor myItemArmor = (ItemArmor) myHead.getItem();
                    ItemArmor entityItemArmor = (ItemArmor) entityHead.getItem();

                    return myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead);
                }
            }
        }

        if (nameColor.get() && mc.thePlayer.getDisplayName() != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");

            if (targetName.startsWith("T") && clientName.startsWith("T")) {
                if (targetName.length() > 1 && clientName.length() > 1 && Character.isDigit(targetName.charAt(1)) && Character.isDigit(clientName.charAt(1))) {
                    return targetName.charAt(1) == clientName.charAt(1);
                }
            }
        }

        return false;
    }
}
