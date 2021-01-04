package com.dah.cem.app.sc.worker.domain.target;

import com.dah.cem.app.sc.worker.infrastructure.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Service
@Validated
public class BaseTargetService {

    @Autowired
    private BaseTargetMapper baseTargetMapper;

    public void save(@Valid BaseTarget baseTarget) {
        if (baseTargetMapper.getByProbeCode(baseTarget.getProbeCode()) != null) {
            throw new ServiceException("操作失败，探测器编码已存在！");
        }
        if (baseTargetMapper.getByTargetCode(baseTarget.getTargetCode()) != null) {
            throw new ServiceException("操作失败，指标编码已存在！");
        }
        baseTargetMapper.save(baseTarget);
    }

    public void update(String probeCode, @Valid BaseTarget baseTarget) {
        BaseTarget saved = baseTargetMapper.getByTargetCode(baseTarget.getTargetCode());
        if (saved != null && !saved.getProbeCode().equals(probeCode)) {
            throw new ServiceException("操作失败，指标编码已存在！");
        }
        baseTarget.setProbeCode(probeCode);
        baseTargetMapper.update(baseTarget);
    }


    public void remove(String probeCode) {
        baseTargetMapper.remove(probeCode);
    }

    public void removeByUnitCode(String unitCode) {
        baseTargetMapper.removeByUnitCode(unitCode);
    }

}
