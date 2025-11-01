package com.dew.system.module;

import com.dew.DewCommon;
import com.dew.system.event.EventListener;
import com.dew.system.module.modules.combat.*;
import com.dew.system.module.modules.exploit.*;
import com.dew.system.module.modules.ghost.*;
import com.dew.system.module.modules.other.*;
import com.dew.system.module.modules.movement.*;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.player.*;
import com.dew.system.module.modules.visual.*;
import com.dew.utils.LogUtil;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ModuleManager implements EventListener {
    private final Queue<Module> modules = new ConcurrentLinkedQueue<>();

    public ModuleManager() {
        this.modules.addAll(Arrays.asList(
                new MoveFix(),
                new Animations(),
                new ClickGui(),
                new Aura(),
                new FlightModule(),
                new Disabler(),
                new Hud(),
                new SpeedModule(),
                new MurdererDetector(),
                new NoFall(),
                new SilentView(),
                new InvMove(),
                new NoSlow(),
                new Scaffold(),
                new KeepSprint(),
                new NameTags(),
                new Chams(),
                new Velocity(),
                new CameraNoClip(),
                new AutoTool(),
                new Breaker(),
                new ShaderESP(),
                new Fullbright(),
                new Xray(),
                new NoBreakDelay(),
                new Manager(),
                new Stealer(),
                new PingReach(),
                new AutoPot(),
                new Phase(),
                new FreeLook(),
                new LongJump(),
                new Sprint(),
                new TargetStrafe(),
                new AutoBlock(),
                new FastBow(),
                new FastUse(),
                new Teams(),
                new HitSelect(),
                new NoHitDelay(),
                new Knockback(),
                new BloxdPhysics(),
                new Step(),
                new ItemPhysics(),
                new Ambience(),
                new NoJumpDelay(),
                new SafetySwitchv2000(),
                new AntiExploit(),
                new FastPlace(),
                new BridgeAssist(),
                new Clicker(),
                new FakeLag(),
                new SilentAimAssist(),
                new Freecam(),
                new Trajectories(),
                new PacketBlaster(),
                new PixelPartyAssist(),
                new CivBreak(),
                new LightningDetector(),
                new Spider(),
                new BarrierVision(),
                new Trails(),
                new AutoAuth(),
                new AutoExtinguish(),
                new QuakeAura(),
                new AutoAimPractice(),
                new Jesus(),
                new Canceller(),
                new AntiFalseFlag(),
                new Highlighter(),
                new RotRandomizer(),
                new HighJump(),
                new Timer(),
                new ViaCollision(),
                new Plugins(),
                new Promoter(),
                new BlockOutline(),
                new Streamer(),
                new RawInput(),
                new Blink(),
                new Backtrack(),
                new TickBase()
        ));

        DewCommon.eventManager.register(this);

        LogUtil.infoLog("init moduleManager");
    }

    public Queue<Module> getModules() {
        return modules;
    }

    public <T extends Module> T getModule(Class<T> moduleClass) {
        return modules.stream()
                .filter(m -> m.getClass() == moduleClass)
                .map(moduleClass::cast)
                .findFirst()
                .orElse(null);
    }
}
