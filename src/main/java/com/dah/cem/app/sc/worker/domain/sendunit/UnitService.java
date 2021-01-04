package com.dah.cem.app.sc.worker.domain.sendunit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Service
@Validated
public class UnitService {

    @Autowired
    private UnitMapper unitMapper;

    public void saveOrUpdate(@Valid Unit unit) {
        if (unitMapper.getByUnitCode(unit.getUnitCode()) != null) {
            unitMapper.update(unit);
        } else {
            unitMapper.save(unit);
        }
    }

}
