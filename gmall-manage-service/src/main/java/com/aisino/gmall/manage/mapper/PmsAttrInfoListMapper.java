package com.aisino.gmall.manage.mapper;

import com.aisino.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsAttrInfoListMapper extends Mapper<PmsBaseAttrInfo>{


    List<PmsBaseAttrInfo> selectAttrValueListByValueId(@Param("valueIdStr") String valueIdStr);
}
