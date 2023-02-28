package com.huike.business.service;

import java.util.List;

import com.huike.business.domain.TbBusiness;
import com.huike.business.domain.TbBusinessTrackRecord;

/**
 * 商机跟进记录Service接口
 * @date 2021-04-28
 */
public interface ITbBusinessTrackRecordService {


    /**
     * 新增商机跟进记录
     * @param tbBusiness
     * @param tbBusinessTrackRecord
     * @return
     */
    public int insertTbBusinessTrackRecord(TbBusiness tbBusiness,TbBusinessTrackRecord tbBusinessTrackRecord);


    /**
     * 跟进商机id查询商机跟进记录
     * @param id
     * @return
     */
	public List<TbBusinessTrackRecord> selectTbBusinessTrackRecordList(Long id);
}
