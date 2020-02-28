package com.java.secondskill.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义校验手机号格式的注解
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER}) //描述注解的使用范围
@Retention(RetentionPolicy.RUNTIME)  //描述注解的生命周期
@Documented //添加注释
@Constraint(validatedBy = {IsMobileValidator.class}) //引进校验器
public @interface IsMobile {

    boolean required() default true;//默认不能为空

    String message() default "手机号格式不正确"; //校验不通过输出信息

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
