package cn.whu.geois.modules.rssample.enums;

import lombok.Getter;

/**
 * @Author WuSiKun
 * @Date 2021-12-28 15:42
 * @Description
 */
@Getter
public enum TaskTypeEnum {
    SC("sc","场景检索"),
    OD("od","目标识别"),
    LC("lc","地物分类"),
    CD("cd","变化检测"),
    TD("td","多视三维");

    private final String type;
    private final String desc;

    TaskTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 根据type获取枚举
     * @param type 枚举类型值
     * @return MetaDataEunm
     */
    public static TaskTypeEnum getEnumByType(String type) {
        TaskTypeEnum[] values = TaskTypeEnum.values();
        for (TaskTypeEnum taskTypeEnum : values) {
            if (taskTypeEnum.getType().equals(type)) {
                return taskTypeEnum;
            }
        }
        throw new IllegalArgumentException("Invalid Enum type:" + type);
    }
}
