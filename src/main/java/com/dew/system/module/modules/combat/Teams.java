package com.dew.system.module.modules.combat;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;

public class Teams extends Module {

    public Teams() {
        super("Teams", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final BooleanValue scoreboard = new BooleanValue("Scoreboard", true);
    private static final BooleanValue nameColor = new BooleanValue("Name Color", false);
    private static final BooleanValue armorColor = new BooleanValue("Armor Color", false);

    public boolean isInYourTeam(EntityLivingBase entity) {
        if (mc.thePlayer == null || !this.isEnabled()) return false;

        if (scoreboard.get() && mc.thePlayer.getTeam() != null && entity.getTeam() != null) {
            if (mc.thePlayer.getTeam().isSameTeam(entity.getTeam())) {
                return true;
            }
        }

        IChatComponent displayName = mc.thePlayer.getDisplayName();

        if (nameColor.get() && displayName != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = displayName.getFormattedText().replace("§r", "");
            if (clientName.length() > 1 && targetName.startsWith("§" + clientName.charAt(1))) {
                return true;
            }
        }

        if (armorColor.get()) {
            for (int i = 0; i <= 3; i++) {
                ItemStack playerArmor = mc.thePlayer.getCurrentArmor(i);
                ItemStack entityArmor = entity.getCurrentArmor(i);

                if (playerArmor == null || entityArmor == null) continue;

                if (playerArmor.getItem() instanceof ItemArmor && entityArmor.getItem() instanceof ItemArmor) {
                    ItemArmor playerItem = (ItemArmor) playerArmor.getItem();
                    ItemArmor entityItem = (ItemArmor) entityArmor.getItem();

                    if (playerItem.getColor(playerArmor) == entityItem.getColor(entityArmor) && entityItem.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
