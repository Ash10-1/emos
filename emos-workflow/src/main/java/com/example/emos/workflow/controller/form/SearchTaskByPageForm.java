package com.example.emos.workflow.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.*;

@Data
public class SearchTaskByPageForm {

    @NotNull(message = "userId不能为空")
    @Min(value = 1, message = "userId不能小于1")
    private Integer userId;

    @NotEmpty(message = "role不能为空")
    private String[] role;

    @NotNull(message = "page不能为空")
    @Min(value = 1, message = "page不能小于1")
    private Integer page;

    @NotNull(message = "length不能为空")
    @Range(min = 10, max = 100, message = "length必须在10~100之间")
    private Integer length;

    @Pattern(regexp = "^员工请假$|^会议申请$|^报销申请$", message = "type内容不正确")
    private String type;

    @NotBlank(message = "status不能为空")
    @Pattern(regexp = "^待审批$|^已审批$|^已结束$", message = "status内容不正确")
    private String status;

    @Pattern(regexp = "^[\\e4e00-\\u9fa5]{2,20}$", message = "creatorName内容不正确")
    private String creatorName;

    @Pattern(regexp = "^[0-9A-Za-z\\-]{36}$", message = "instanceId内容不正确")
    private String instanceId;

    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "tcode不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "tcode必须是6位数字")
    private String tcode;

}
