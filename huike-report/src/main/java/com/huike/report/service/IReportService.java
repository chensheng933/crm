package com.huike.report.service;

import java.util.List;
import java.util.Map;

import com.huike.report.domain.vo.*;
import org.apache.ibatis.annotations.Param;

import com.huike.clues.domain.TbActivity;
import com.huike.clues.domain.TbClue;
import com.huike.clues.domain.vo.IndexStatisticsVo;
import com.huike.contract.domain.TbContract;

public interface IReportService {

    /**
     *新增客户统计
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public LineChartVo contractStatistics(String beginCreateTime, String endCreateTime);


    /**
     * 学科分布统计
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public List<Map<String, Object>> subjectStatistics(String beginCreateTime, String endCreateTime);

    /**
     * 客户统计报表
     * @param tbContract
     * @return
     */
    public  List<TbContract> contractReportList(TbContract tbContract);

    /**
     * 销售统计
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public LineChartVo salesStatistics(String beginCreateTime, String endCreateTime);



    /**
     * 销售统计部门报表
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public List<Map<String, Object>> deptStatisticsList(String beginCreateTime, String endCreateTime);

    /**
     * 销售统计渠道报表
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public List<Map<String, Object>> channelStatisticsList(String beginCreateTime, String endCreateTime);
    /**
     * 销售统计归属人报表
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public List<Map<String, Object>> ownerShipStatisticsList(String beginCreateTime, String endCreateTime);

    /**
     * 线索统计
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public LineChartVo cluesStatistics(String beginCreateTime, String endCreateTime);


    /**
     * 渠道统计
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public List<Map<String, Object>> chanelStatistics(String beginCreateTime, String endCreateTime);


    /**
     * 活动统计
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    public List<Map<String, Object>> activityStatistics(String beginCreateTime, String endCreateTime);



    public List<TbClue> cluesStatisticsList(TbClue clue);

    /**
     * 活动渠道统计
     * @param activity
     * @return
     */
    public List<ActivityStatisticsVo> activityStatisticsList(TbActivity activity);

    //漏斗统计
    public VulnerabilityMapVo getVulnerabilityMap(String beginCreateTime, String endCreateTime);


    public IndexVo getIndex(IndexStatisticsVo request);


    public List<Map<String,Object>> businessChangeStatisticsForIndex(IndexStatisticsVo request);


    public List<Map<String, Object>> clueChangeStatisticsForIndex(IndexStatisticsVo request);


    /**
     * 首页基本数据展示
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    IndexBaseInfoVO getBaseInfo(String beginCreateTime, String endCreateTime);


    /**
     * 获取今日简报数据
     * @param today
     * @return
     */
    IndexTodayInfoVO getTodayInfo(String today);


    /**
     * 获取待办数据
     * @param beginCreateTime
     * @param endCreateTime
     * @return
     */
    IndexTodoInfoVO getTodoInfo(String beginCreateTime, String endCreateTime);

}
