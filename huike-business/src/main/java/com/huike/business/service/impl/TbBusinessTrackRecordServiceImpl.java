package com.huike.business.service.impl;

import java.util.List;

import com.huike.business.domain.TbBusiness;
import com.huike.business.mapper.TbBusinessMapper;
import com.huike.clues.service.ISysDictDataService;
import com.huike.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.huike.business.mapper.TbBusinessTrackRecordMapper;
import com.huike.business.domain.TbBusinessTrackRecord;
import com.huike.business.service.ITbBusinessTrackRecordService;

/**
 * 商机跟进记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2021-04-28
 */
@Service
public class TbBusinessTrackRecordServiceImpl implements ITbBusinessTrackRecordService {
    @Autowired
    private TbBusinessTrackRecordMapper tbBusinessTrackRecordMapper;

    @Autowired
    private TbBusinessMapper tbBusinessMapper;



    @Override
    public int insertTbBusinessTrackRecord(TbBusiness tbBusiness, TbBusinessTrackRecord tbBusinessTrackRecord) {
        tbBusinessMapper.updateTbBusiness(tbBusiness);
        return tbBusinessTrackRecordMapper.insertTbBusinessTrackRecord(tbBusinessTrackRecord);
    }

    /**
     * 跟进商机id查询商机跟进记录
     */
	@Override
	public List<TbBusinessTrackRecord> selectTbBusinessTrackRecordList(Long id) {
		return tbBusinessTrackRecordMapper.selectTbBusinessTrackRecordListByBusinessId(id);
	}
}
