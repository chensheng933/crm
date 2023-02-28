package com.huike.clues.mapper;

import java.util.List;
import com.huike.clues.domain.TbClueTrackRecord;

/**
 * 线索跟进记录Mapper接口
 * @date 2021-04-19
 */
public interface TbClueTrackRecordMapper {

    /**
     * 新增线索跟进记录
     * 
     * @param tbClueTrackRecord 线索跟进记录
     * @return 结果
     */
    public int insertTbClueTrackRecord(TbClueTrackRecord tbClueTrackRecord);


    /**
     * 根据线索id查询线索跟进记录
     * @param clueId
     * @return
     */
	public List<TbClueTrackRecord> selectTbClueTrackRecordListByClueId(Long clueId);
    
    
}
