package com.dew.system.module;

import com.dew.DewCommon;
import com.dew.system.event.EventListener;
import com.dew.system.module.modules.combat.*;
import com.dew.system.module.modules.exploit.Disabler;
import com.dew.system.module.modules.movement.InvMove;
import com.dew.system.module.modules.movement.LongJump;
import com.dew.system.module.modules.movement.MoveFix;
import com.dew.system.module.modules.movement.NoSlow;
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
                new MoveFix(), new Animations(), new ClickGui(), new KillAura(),
                new FlightModule(), new Disabler(), new Hud(), new SpeedModule(),
                new Cape(), new NoFall(), new Rotations(), new InvMove(), new NoSlow(),
                new Scaffold(), new KeepSprint(), new NameTags(), new Chams(),
                new Velocity(), new CameraNoClip(), new AutoTool(), new Breaker(),
                new CaveFinder(), new Fullbright(), new Xray(), new NoBreakDelay(),
                new Manager(), new Stealer(), new Backtrack(), new AutoPot(),
                new ESP(), new NoHurtCam(), new NoFireOverlay(), new Wings(),
                new Sprint(), new TargetStrafe(), new AutoBlock(), new FastBow(),
                new FastUse(), new Teams(), new HitSelect(), new LongJump(),
                new Knockback()
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
