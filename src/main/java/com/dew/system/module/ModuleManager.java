package com.dew.system.module;

import com.dew.DewCommon;
import com.dew.system.event.EventListener;
import com.dew.system.module.modules.combat.*;
import com.dew.system.module.modules.exploit.*;
import com.dew.system.module.modules.ghost.*;
import com.dew.system.module.modules.movement.*;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.player.*;
import com.dew.system.module.modules.render.*;
import com.dew.utils.LogUtil;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ModuleManager implements EventListener {
    private final Queue<Module> modules = new ConcurrentLinkedQueue<>();

    public ModuleManager() {
        this.modules.addAll(Arrays.asList(
                new MoveFix(), new Animations(), new ClickGui(), new Aura(),
                new FlightModule(), new Disabler(), new Hud(), new SpeedModule(),
                new MurdererDetector(), new NoFall(), new SilentView(), new InvMove(), new NoSlow(),
                new Scaffold(), new KeepSprint(), new NameTags(), new Chams(),
                new Velocity(), new CameraNoClip(), new AutoTool(), new Breaker(),
                new ESP(), new Fullbright(), new Xray(), new NoBreakDelay(),
                new Manager(), new Stealer(), new Backtrack(), new AutoPot(),
                new AutoWalkAI(), new NoHurtCam(), new NoFireOverlay(), new Wings(),
                new Sprint(), new TargetStrafe(), new AutoBlock(), new FastBow(),
                new FastUse(), new Teams(), new HitSelect(), new NoHitDelay(),
                new Knockback(), new BloxdPhysics(), new Step(), new ItemPhysics(),
                new Ambience(), new NoJumpDelay(), new SafetySwitchv2000(), new AntiExploit(),
                new FastPlace(), new BridgeAssist(), new Clicker(), new FakeLag(),
                new SilentAimAssist(), new Freecam(), new Trajectories(), new PacketBlaster(),
                new ColorBlindnessAssistant(), new Scaffold2(), new CivBreak(), new LightningDetector(),
                new Spider(), new BarrierVision(), new Breadcrumbs(), new AutoAuth(),
                new AutoExtinguish()
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
