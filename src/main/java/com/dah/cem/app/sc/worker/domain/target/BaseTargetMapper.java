package com.dah.cem.app.sc.worker.domain.target;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface BaseTargetMapper {

    BaseTarget getByProbeCode(@Param("probeCode") String probeCode);

    BaseTarget getByTargetCode(@Param("targetCode") String targetCode);

    List<BaseTarget> getAll();

    Page<BaseTarget> getPage(@Param("search") String search, RowBounds rowBounds);

    int save(BaseTarget baseTarget);

    int update(BaseTarget baseTarget);

    int remove(@Param("probeCode") String probeCode);

    int removeByUnitCode(@Param("unitCode") String unitCode);

}
