package com.dah.cem.app.sc.worker.workers.report.actor;

import com.dah.cem.app.sc.worker.domain.target.BaseTarget;
import com.dah.cem.app.sc.worker.domain.target.BaseTargetMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Setter
public class GatewayActorManager implements InitializingBean {

    private static final int RELOAD_PERIOD_MINUTES = 10;

    private Map<String, GatewayActor> actorMap = new ConcurrentHashMap<>();

    @Autowired
    private BaseTargetMapper baseTargetMapper;

    @Autowired
    @Qualifier("actorTaskScheduler")
    private TaskScheduler taskScheduler;

    @Autowired
    private ReportActor reportActor;

    private Map<String, BaseTarget> contrastMap = new ConcurrentHashMap<>();

    public GatewayActor getActor(String gatewayCode) {
        return actorMap.get(gatewayCode);
    }

    @Override
    public void afterPropertiesSet() {
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                load();
            }
        }, RELOAD_PERIOD_MINUTES * 60 * 1000L);
    }

    private void load() {
        try {
            List<BaseTarget> baseTargets = baseTargetMapper.getAll();
            for (BaseTarget baseTarget : baseTargets) {
                contrastMap.put(baseTarget.getProbeCode(), baseTarget);
            }
            for (BaseTarget baseTarget : baseTargets) {
                GatewayActor actor = actorMap.get(baseTarget.getGatewayCode());
                // 创建
                if (actor == null) {
                    actor = new GatewayActor(contrastMap, baseTarget, reportActor, taskScheduler);
                    actor.initialize();
                    actorMap.put(baseTarget.getGatewayCode(), actor);
                }
            }
        } catch (Exception e) {
            log.error("构造或更新actors异常。", e);
        }
    }

}
