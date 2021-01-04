package com.dah.cem.app.sc.worker.domain.prober;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProberStatusMapper {

    List<ProberStatus> getLast(int limit);

    int getCount();

    int save(ProberStatus proberStatus);

    int remove(@Param("id") String id);

    int removeLast(int limit);

}
