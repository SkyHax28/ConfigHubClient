package net.minecraft.util;
import com.dew.DewCommon;
import com.dew.system.event.events.MoveInputEvent;
import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState() {
        final float lastForward = this.moveForward;
        final float lastStrafe = this.moveStrafe;

        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            ++this.moveForward;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            --this.moveForward;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();
        final MoveInputEvent event = new MoveInputEvent(this.moveForward, this.moveStrafe, lastForward, lastStrafe, this.jump, this.sneak, 0.3D);

        DewCommon.eventManager.call(event);
        this.moveForward = event.forward;
        this.moveStrafe = event.strafe;
        this.jump = event.jump;
        this.sneak = event.sneak;

        final double sneakMultiplier = event.sneakMultiplier;

        if (this.sneak) {
            this.moveStrafe = (float) (this.moveStrafe * sneakMultiplier);
            this.moveForward = (float) (this.moveForward * sneakMultiplier);
        }
    }
}
