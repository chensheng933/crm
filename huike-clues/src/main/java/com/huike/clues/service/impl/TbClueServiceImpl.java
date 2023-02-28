package com.huike.clues.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.huike.clues.domain.dto.ImportResultDTO;
import com.huike.clues.strategy.Rule;
import com.huike.common.core.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.huike.clues.domain.TbActivity;
import com.huike.clues.domain.TbAssignRecord;
import com.huike.clues.domain.TbClue;
import com.huike.clues.domain.TbClueTrackRecord;
import com.huike.clues.domain.TbRulePool;
import com.huike.clues.domain.vo.TbClueExcelVo;
import com.huike.clues.mapper.SysDictDataMapper;
import com.huike.clues.mapper.SysUserMapper;
import com.huike.clues.mapper.TbActivityMapper;
import com.huike.clues.mapper.TbAssignRecordMapper;
import com.huike.clues.mapper.TbClueMapper;
import com.huike.clues.mapper.TbClueTrackRecordMapper;
import com.huike.clues.service.ITbActivityService;
import com.huike.clues.service.ITbClueService;
import com.huike.clues.service.ITbRulePoolService;
import com.huike.clues.utils.HuiKeCrmDateUtils;
import com.huike.clues.utils.JobUtils;
import com.huike.common.annotation.DataScope;
import com.huike.common.constant.Constants;
import com.huike.common.core.domain.entity.SysUser;
import com.huike.common.exception.CustomException;
import com.huike.common.utils.DateUtils;
import com.huike.common.utils.SecurityUtils;
import com.huike.common.utils.StringUtils;
import com.huike.common.utils.bean.BeanUtils;

/**
 * 线索管理Service业务层处理
 * 
 * @date 2021-04-02
 */
@Service
public class TbClueServiceImpl implements ITbClueService {

	@Autowired
	private Rule rule;

	@Autowired
	private TbClueMapper tbClueMapper;

	@Autowired
	private TbAssignRecordMapper assignRecordMapper;

	@Autowired
	private TbClueTrackRecordMapper tbClueTrackRecordMapper;

	@Autowired
	private SysUserMapper userMapper;

	@Autowired
	ITbRulePoolService rulePoolService;

	@Autowired
	SysDictDataMapper sysDictDataMapper;

	@Autowired
	private TbActivityMapper tbActivityMapper;

	@Autowired
	private ITbActivityService activityService;

	@Autowired
	private ITbClueService tbClueService;

	@Autowired
	private RedisCache redisCache;

	/**
	 * 查询线索管理
	 * 
	 * @param id 线索管理ID
	 * @return 线索管理
	 */
	@Override
	public TbClue selectTbClueById(Long id) {
		TbClue tbClue = tbClueMapper.selectTbClueById(id);
		if (tbClue != null && tbClue.getActivityId() != null) {
			TbActivity activity = tbActivityMapper.selectTbActivityById(tbClue.getActivityId());
			if (activity != null) {
				tbClue.setActivityInfo(activity.getCode() + ":" + activity.getName() + ":" + activity.getInfo());
			}
		}
		// 查询专派遣信息
		TbAssignRecord assignRecord = assignRecordMapper.selectAssignRecordByAssignId(tbClue.getId(), "0");
		//证明是待分配数据
		if(assignRecord==null){
			return tbClue;
		}
		tbClue.setOwner(assignRecord.getUserName());
		tbClue.setOwnerTime(assignRecord.getCreateTime());
		return tbClue;
	}

	/**
	 * 查询线索管理列表
	 * 
	 * @param tbClue 线索管理
	 * @return 线索管理
	 */
	@Override
	@DataScope(deptAlias = "r", userAlias = "r")
	public List<TbClue> selectTbClueList(TbClue tbClue) {
		return tbClueMapper.selectTbClueList(tbClue);
	}

	@Override
	public List<TbClue> selectTbCluePool(TbClue tbClue) {
		List<TbClue> clueList = tbClueMapper.selectTbCluePoll(tbClue);
		clueList.forEach(d -> {
			if (d.getActivityId() != null) {
				TbActivity activity = tbActivityMapper.selectTbActivityById(d.getActivityId());
				if (activity != null) {
					d.setActivityName(activity.getName());
				}
			}
		});
		return clueList;
	}

	/**
	 * 新增线索管理
	 * 
	 * @param tbClue 线索管理
	 * @return 结果
	 */
	@Override
	@Transactional
	public int insertTbClue(TbClue tbClue) {
		tbClue.setCreateBy(SecurityUtils.getUsername());
		Date now = DateUtils.getNowDate();
		tbClue.setCreateTime(now);
		tbClueMapper.insertTbClue(tbClue);
		TbAssignRecord tbAssignRecord = new TbAssignRecord();
		tbAssignRecord.setAssignId(tbClue.getId());
		tbAssignRecord.setUserId(SecurityUtils.getUserId());
		tbAssignRecord.setUserName(SecurityUtils.getUsername());
		tbAssignRecord.setDeptId(SecurityUtils.getDeptId());
		tbAssignRecord.setCreateBy(SecurityUtils.getUsername());
		tbAssignRecord.setCreateTime(now);
		int rows = assignRecordMapper.insertAssignRecord(tbAssignRecord);
		// 线索池规则支持
		Date endDate = HuiKeCrmDateUtils.getEndDateByRule(tbAssignRecord);
		tbClueMapper.updateClueEndTimeById(tbClue.getId(), endDate);
		return rows;
	}

	/**
	 * 修改线索管理
	 * 
	 * @param tbClue 线索管理
	 * @return 结果
	 */
	@Override
	public int updateTbClue(TbClue tbClue) {
		return tbClueMapper.updateTbClue(tbClue);
	}

	/**
	 * 批量删除线索管理
	 * 
	 * @param ids 需要删除的线索管理ID
	 * @return 结果
	 */
	@Override
	public int deleteTbClueByIds(Long[] ids) {
		return tbClueMapper.deleteTbClueByIds(ids);
	}

	/**
	 * 删除线索管理信息
	 * 
	 * @param id 线索管理ID
	 * @return 结果
	 */
	@Override
	public int deleteTbClueById(Long id) {
		return tbClueMapper.deleteTbClueById(id);
	}

	@Override
	@Transactional
	public Map<String, Integer> importClues(List<TbClue> clueList) {
		if (StringUtils.isNull(clueList) || clueList.size() == 0) {
			throw new CustomException("导入用户数据不能为空！");
		}
		Map<String, Integer> map = new HashMap<>();
		List<TbClue> toAssignlist = new ArrayList<>();
		int successNum = 0;
		int failureNum = 0;
		for (TbClue clue : clueList) {
			try {
				if (StringUtils.isBlank(clue.getPhone())) {
					failureNum++;
					continue;
				}
				if (StringUtils.isBlank(clue.getChannel())) {
					failureNum++;
					continue;
				}

				// 验证是否存在这个用户
				TbClue dbcule = tbClueMapper.selectTbClueByPhone(clue.getPhone());
				if (dbcule == null) {
					// 特殊字段处理
					String channel = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.CHANNEL.getDictType(),
							clue.getChannel());
					clue.setChannel(channel);

					if (StringUtils.isNoneBlank(clue.getSubject())) {
						String subject = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.SUBJECT.getDictType(),
								clue.getSubject());
						clue.setSubject(subject);
					}

					if (StringUtils.isNoneBlank(clue.getLevel())) {
						String level = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.LEVEL.getDictType(),
								clue.getLevel());
						clue.setLevel(level);
					}

					if (StringUtils.isNoneBlank(clue.getSex())) {
						String sex = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.SEX.getDictType(),
								clue.getSex());
						clue.setSex(sex);
					}

					if (StringUtils.isNoneBlank(clue.getActivityName())) {
						String sex = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.SEX.getDictType(),
								clue.getSex());
						clue.setSex(sex);
					}
					clue.setStatus(TbClue.StatusType.UNFOLLOWED.getValue());
					tbClueMapper.insertTbClue(clue);
					// 默认分配超级管理员
					//如果线索添加成功，利用策略将线索分配给具体的人
					rule.loadRule(clue);
					successNum++;
					toAssignlist.add(clue);
				} else {
					failureNum++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				failureNum++;
			}
		}

		map.put("successNum", successNum);
		map.put("failureNum", failureNum);
		return map;
	}

	@Override
	public String assign(Long[] clueIds, Long userId) {
		TbRulePool rulePool = rulePoolService.selectTbRulePoolByType(Constants.rule_type_clue);
		// TbAssignRecord tbAssignRecord =new TbAssignRecord();
		// 统计当前分配人所有线索
		int assignRecords = assignRecordMapper.countAssignCluesByUser(userId);
		if (assignRecords >= rulePool.getMaxNunmber()) {
			throw new CustomException("分配失败！最大保有量(" + rulePool.getMaxNunmber() + ")，剩余可以分配"
					+ (rulePool.getMaxNunmber() - assignRecords) + "条线索");
		}
		for (int i = 0; i < clueIds.length; i++) {
			Long clueId = clueIds[i];

			// 超过最大保有量
			if (assignRecords + i >= rulePool.getMaxNunmber()) {
				// return "超过当前用户最大保有量，部分分配成功";
				throw new CustomException("分配失败！保有量达到上线，最多选择" + rulePool.getMaxNunmber() + "条线索");
			}

			// 从新分配
			updateStatus(clueId, TbClue.StatusType.UNFOLLOWED.getValue());
			TbAssignRecord tbAssignRecord = addNewRecord(clueId, userId);
			Date endDate = HuiKeCrmDateUtils.getEndDateByRule(tbAssignRecord);
			tbClueMapper.updateClueEndTimeById(clueId, endDate);
		}
		return "全部分配";
	}

	// 重复捞取时间限制
	@Override
	public String gain(Long[] clueIds, Long userId) {
		// 是否批量捞取
		boolean isBatch = clueIds.length > 1 ? true : false;
		TbRulePool rulePool = rulePoolService.selectTbRulePoolByType(TbRulePool.RuleType.CLUES.getValue());
		// 统计当前分配人所有线索
		int asignRecords = assignRecordMapper.countAssignCluesByUser(userId);
		if (asignRecords >= rulePool.getMaxNunmber()) {
			throw new CustomException("捞取失败！最大保有量(" + rulePool.getMaxNunmber() + ")，剩余可以捞取0条线索");
		}
		for (int i = 0; i < clueIds.length; i++) {
			Long clueId = clueIds[i];

			// 超过最大保有量
			if (asignRecords + i >= rulePool.getMaxNunmber()) {
				throw new CustomException("捞取失败！保有量达到上线，最多选择" + rulePool.getMaxNunmber() + "条线索");
			}
			// 最近捞取记录
			TbAssignRecord assignRecord = assignRecordMapper.selectAssignRecordByAssignId(clueId,
					TbAssignRecord.RecordType.CLUES.getValue());
			if (assignRecord != null && assignRecord.getUserId().equals(userId)) {
				Date repeatGetTime = JobUtils.getDate(rulePool.getRepeatGetTime().intValue(), rulePool.getRepeatType(),
						assignRecord.getCreateTime());
				// 捞取限制时间内，不让捞取
				if (DateUtils.getNowDate().before(repeatGetTime)) {
					// 批量捞取跳过
					if (isBatch) {
						continue;
					} else {
						throw new CustomException("捞取失败！需要在 " + DateUtils.dateTimeHm(repeatGetTime) + " 后捞取");
					}
				}
			}
			// 捞取后下次跟进时间，及状态重置
			tbClueMapper.resetNextTimeAndStatus(clueId, TbClue.StatusType.UNFOLLOWED.getValue());
			// 新建分配记录
			TbAssignRecord tbAssignRecord = addNewRecord(clueId, userId);

			Date endDate = HuiKeCrmDateUtils.getEndDateByRule(tbAssignRecord);
			tbClueMapper.updateClueEndTimeById(clueId, endDate);
		}
		return "全部捞取成功";
	}

	public TbAssignRecord addNewRecord(Long id, Long userId) {
		// 保留上一条分配记录
		assignRecordMapper.updateLatest(id, TbAssignRecord.RecordType.CLUES.getValue());
		// 新建分配记录
		TbAssignRecord tbAssignRecord = new TbAssignRecord();
		tbAssignRecord.setAssignId(id);
		SysUser sysUser = userMapper.selectUserById(userId);
		tbAssignRecord.setUserId(userId);
		tbAssignRecord.setDeptId(sysUser.getDeptId());
		tbAssignRecord.setUserName(sysUser.getUserName());
		Date now = DateUtils.getNowDate();
		tbAssignRecord.setCreateTime(now);
		tbAssignRecord.setCreateBy(SecurityUtils.getUsername());
		tbAssignRecord.setType(TbAssignRecord.RecordType.CLUES.getValue());
		assignRecordMapper.insertAssignRecord(tbAssignRecord);
		return tbAssignRecord;
	}

	@Override
	@Transactional
	public int falseClue(Long id, String reason, String remark) {
		TbClue tbClue = tbClueMapper.selectTbClueById(id);
		int falseCount = tbClue.getFalseCount();
		// 上报超过三次删除
		if (falseCount >= 2) {
			// 删除这条线索
			return tbClueMapper.removeClueByFalseClue(id);
		}
		// 少于三次入线索池
		tbClue.setFalseCount(tbClue.getFalseCount() + 1);
		tbClue.setStatus(TbClue.StatusType.FALSE.getValue());
		updateTbClue(tbClue);
		updateStatus(tbClue.getId(), TbClue.StatusType.FALSE.getValue());
		// 伪线索原因
		TbClueTrackRecord trackRecord = new TbClueTrackRecord();
		trackRecord.setCreateBy(SecurityUtils.getUsername());
		trackRecord.setFalseReason(reason);
		trackRecord.setRecord(remark);
		trackRecord.setClueId(id);
		trackRecord.setType("1");
		trackRecord.setCreateTime(DateUtils.getNowDate());
		return tbClueTrackRecordMapper.insertTbClueTrackRecord(trackRecord);
	}

	@Override
	@Transactional
	public int updateStatus(Long clueId, String status) {
		return tbClueMapper.resetNextTimeAndStatus(clueId, status);
	}

	/**
	 * 批量导入
	 */
	@Override
	public Map<String, Integer> addTbClue(List<TbClueExcelVo> cluevoList) {
		List<TbClue> clueList = cluevoList.stream().map(vo -> {
			TbClue tbClue = new TbClue();
			BeanUtils.copyProperties(vo, tbClue);
			tbClue.setCreateBy(SecurityUtils.getUsername());
			tbClue.setCreateTime(DateUtils.getNowDate());
			String activityCode = vo.getActivityCode();
			// 关联活动
			if (StringUtils.isNoneBlank(activityCode)) {
				TbActivity activity = activityService.selectTbActivityByCode(activityCode);
				if (activity != null) {
					tbClue.setActivityId(activity.getId());
				}
			}
			return tbClue;
		}).collect(Collectors.toList());
		return tbClueService.importClues(clueList);
	}

	/**
	 * 校验线索手机号是否存在
	 */
	@Override
	public boolean checkCluePhoneExis(String phone) {
		// 验证是否存在这个用户
		TbClue dbcule = tbClueMapper.selectTbClueByPhone(phone);
		if (dbcule == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 线索数据添加入库
	 * @param data
	 * @return
	 */
	@Override
	public ImportResultDTO importCluesData(TbClueExcelVo data) {
		//===============校验线索数据======================
		//===============封装属性==============
		TbClue clue = new TbClue();
		BeanUtils.copyProperties(data, clue);
		clue.setCreateBy(SecurityUtils.getUsername());
		clue.setCreateTime(DateUtils.getNowDate());
		String activityCode = data.getActivityCode();
		//1 判断活动编号对应的活动是否存在
		if (StringUtils.isNoneBlank(activityCode)) {
			//利用空间换时间将缓存中的活动编号查询出来
			Set<String> codeSets =  redisCache.getCacheSet(Constants.ACT_CODE_KEY);
			//如果活动编号不在系统中则不进行录入
			if(!codeSets.contains(activityCode)){
				return ImportResultDTO.error();
			}
		}
		try {
			if (StringUtils.isBlank(clue.getPhone())) {
				return ImportResultDTO.error();
			}
			if (StringUtils.isBlank(clue.getChannel())) {
				return ImportResultDTO.error();
			}
			// 验证是否存在这个用户
			TbClue dbcule = tbClueMapper.selectTbClueByPhone(clue.getPhone());
			if (dbcule == null) {
				// 特殊字段处理
				String channel = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.CHANNEL.getDictType(),
						clue.getChannel());
				clue.setChannel(channel);

				if (StringUtils.isNoneBlank(clue.getSubject())) {
					String subject = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.SUBJECT.getDictType(),
							clue.getSubject());
					clue.setSubject(subject);
				}

				if (StringUtils.isNoneBlank(clue.getLevel())) {
					String level = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.LEVEL.getDictType(),
							clue.getLevel());
					clue.setLevel(level);
				}

				if (StringUtils.isNoneBlank(clue.getSex())) {
					String sex = sysDictDataMapper.selectDictValue(TbClue.ImportDictType.SEX.getDictType(),
							clue.getSex());
					clue.setSex(sex);
				}
				clue.setStatus(TbClue.StatusType.UNFOLLOWED.getValue());
				tbClueMapper.insertTbClue(clue);
				// 默认分配超级管理员
				//如果线索添加成功，利用策略将线索分配给具体的人
				rule.loadRule(clue);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("数据导入失败：导入失败的id编号为:"+clue.getId());
		}
		return ImportResultDTO.success();
	}
}
