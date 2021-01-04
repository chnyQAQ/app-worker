package com.dah.cem.app.sc.worker.domain.sendunit;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

@Mapper
public interface UnitMapper {

    Unit getByUnitCode(@Param("unitCode") String unitCode);

    Page<Unit> getPage(@Param("search") String search, RowBounds rowBounds);

    int save(Unit unit);

    int update(Unit unit);

}
