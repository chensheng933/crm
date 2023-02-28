package com.huike.business.strategy.impl;

import com.huike.business.domain.TbBusiness;
import com.huike.business.strategy.Rule;
import com.huike.clues.domain.TbAssignRecord;
import com.huike.clues.mapper.SysDictDataMapper;
import com.huike.clues.mapper.SysUserMapper;
import com.huike.clues.mapper.TbAssignRecordMapper;
import com.huike.clues.service.ITbCourseService;
import com.huike.common.core.domain.entity.SysDictData;
import com.huike.common.core.domain.entity.SysUser;
import com.huike.common.utils.DateUtils;
import com.huike.common.utils.DictUtils;
import com.huike.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 *  rule 处理策略
 *  根据规则系统自动分配对应的商机专员来处理
 *  1.意向学科是java的分配给lisi商机专员
 *  2.意向学科是前端的分配给lisi1商机专员
 */
@ConditionalOnProperty(name = "rule.transfor", havingValue = "rule")
@Service("BussinessruleStrategy")
public class RuleStrategy implements Rule {

    @Autowired
    private TbAssignRecordMapper assignRecordMapper;

    @Autowired
    private SysUserMapper userMapper;


    private static SysUser lisi = new SysUser();

    private static SysUser lisi1 = new SysUser();

    //内存中JAVA学科的内容--提前预加载在内存中
    private static SysDictData subjectJAVA = new SysDictData();

    //内存中前端学科的内容--提前预加载在内存中
    private static SysDictData subjectHtml = new SysDictData();

    @PostConstruct
    public void init() {
        //空间换时间的方式将数据库中的学科读取到内存中
        //预加载学科数据到内存中
        List<SysDictData> course_subject = DictUtils.getDictCache("course_subject");
        for (SysDictData index: course_subject) {
            //找到java和前端两个学科对应的数值
            if(index.getDictLabel().equals("Java")){
                subjectJAVA = index;
            }
            if(index.getDictLabel().equals("前端")){
                subjectHtml = index;
            }
        }
        //预加载lisi和lisi1的数据到内存中
        lisi = userMapper.selectUserByName("lisi");
        lisi1 = userMapper.selectUserByName("lisi1");
    }

    /**
     * 定义一些规则来自动分配
     *
     * 1.意向学科是java的分配给lisi商机专员
     * 2.意向学科是前端的分配给lisi1商机专员
     * 3.如果没有匹配到规则则不分配等待管理员和主管来进行分配
     * @param tbBusiness
     * @return
     */
    @Override
    public Integer transforBusiness(TbBusiness tbBusiness) {
        //注意处理空指针的问题
        if(subjectJAVA.getDictValue().equals(tbBusiness.getSubject())){
            //如果意向学科是java--分配给lisi
            return distribute(tbBusiness,lisi);
        }else if(subjectHtml.getDictValue().equals(tbBusiness.getSubject())){
            //如果意向学科是前端--分配给lisi1
            return distribute(tbBusiness,lisi1);
        }else{
            //不进行分配，直接添加-----即待分配状态
            return 1;
        }
    }


    /**
     * 分配商机给具体用户的方法
     * 这部分代码学生不用研究，未预制bug
     * @param business
     * @param user
     * @return
     */
    private int distribute(TbBusiness business,SysUser user){
        TbAssignRecord tbAssignRecord =new TbAssignRecord();
        tbAssignRecord.setAssignId(business.getId());
        tbAssignRecord.setUserId(user.getUserId());
        tbAssignRecord.setUserName(user.getUserName());
        tbAssignRecord.setDeptId(user.getDeptId());
        tbAssignRecord.setCreateBy(SecurityUtils.getUsername());
        tbAssignRecord.setCreateTime(DateUtils.getNowDate());
        tbAssignRecord.setType(TbAssignRecord.RecordType.BUSNIESS.getValue());
        business.setNextTime(null);
        return assignRecordMapper.insertAssignRecord(tbAssignRecord);
    }

}
