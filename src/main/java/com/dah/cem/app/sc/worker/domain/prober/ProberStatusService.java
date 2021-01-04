package com.dah.cem.app.sc.worker.domain.prober;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.UUID;

@Validated
@Service
public class ProberStatusService {

    @Autowired
    ProberStatusMapper proberStatusMapper;

    public synchronized int save(@Valid ProberStatus proberStatus) {
        proberStatus.setId(UUID.randomUUID().toString());
        return proberStatusMapper.save(proberStatus);
    }

    public synchronized int remove(String id) {
        return proberStatusMapper.remove(id);
    }

    public synchronized int removeLast(int limit) {
        return proberStatusMapper.removeLast(limit);
    }

}
