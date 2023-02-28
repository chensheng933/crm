package com.huike.clues.service.impl;

import java.util.List;

import com.huike.clues.domain.TbClue;
import com.huike.clues.mapper.TbClueMapper;
import com.huike.common.utils.DateUtils;
import com.huike.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.huike.clues.mapper.TbClueTrackRecordMapper;
import com.huike.clues.domain.TbClueTrackRecord;
import com.huike.clues.service.ITbClueTrackRecordService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 线索跟进记录Service业务层处理
 * @date 2021-04-22
 */
@Service
public class TbClueTrackRecordServiceImpl implements ITbClueTrackRecordService 
{
    @Autowired
    private TbClueTrackRecordMapper tbClueTrackRecordMapper;

    @Autowired
    private TbClueMapper tbClueMapper;


    /**
     * 添加线索跟进记录
     * @param tbClue
     * @param tbClueTrackRecord
     * @return
     */
    @Override
    @Transactional
    public int insertTbClueTrackRecord(TbClue tbClue, TbClueTrackRecord tbClueTrackRecord) {
        tbClueMapper.updateTbClue(tbClue);
        return tbClueTrackRecordMapper.insertTbClueTrackRecord(tbClueTrackRecord);
    }

    /**
     * 根据线索id查询线索跟进记录
     */
	@Override
	public List<TbClueTrackRecord> selectTbClueTrackRecordList(Long clueId) {
		return tbClueTrackRecordMapper.selectTbClueTrackRecordListByClueId(clueId);
	}
}
