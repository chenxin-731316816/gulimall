package com.iflytek.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@TableName("oms_city")
public class CityEntity  implements Serializable {
    /**
     * 城市id
     */
    @TableId
    private Long cityId;

    /**
     * 上级节点id
     */

    private Long cityPid;

    /**
     * 城市名称
     */

    private String cityName;

    /**
     * 创建时间
     */

    private String createTime;

    /**
     * 更新时间
     */

    private String updateTime;


    /**
     * 子级数据
     */
    @TableField(exist = false)
    private List<CityEntity> childs;

}
