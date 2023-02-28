package com.huike.clues.service;

import java.util.List;

import com.huike.clues.domain.TbClue;
import com.huike.clues.domain.TbClueTrackRecord;

/**
 * 线索跟进记录Service接口
 * 
 * @author ruoyi
 * @date 2021-04-19
 */
public interface ITbClueTrackRecordService {

    /**
     * 添加线索跟进记录
     * @param tbClue
     * @param tbClueTrackRecord
     * @return
     */
    public int insertTbClueTrackRecord(TbClue tbClue, TbClueTrackRecord tbClueTrackRecord);

    /**
     * 跟进线索id查询线索跟进记录
     * @param clueId
     * @return
     */
	public List<TbClueTrackRecord> selectTbClueTrackRecordList(Long clueId);
}
