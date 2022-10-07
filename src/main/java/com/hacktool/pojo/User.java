package com.hacktool.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hacktool.Annotation.Sensitive;
import com.hacktool.enums.SensitiveStrategy;
import lombok.Data;

@Data
public class User {
    private int id;
    @TableField(value = "userName")
    @Sensitive(SensitiveStrategy.USERNAME)
    private String userName;
    @Sensitive(SensitiveStrategy.PHONE)
    private String phone;
    @Sensitive(SensitiveStrategy.EMAIL)
    private String email;
    @TableField(value = "idCard")
    @Sensitive(SensitiveStrategy.ID_CARD)
    private String idCard;
}
