package com.minister.framework.api.validate.aop;

import org.apache.commons.lang3.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 枚举校验
 *
 * @author QIUCHANGQING620
 * @date 2020-03-08 10:35
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Repeatable(EnumValue.List.class)
@Documented
@Constraint(validatedBy = {EnumValue.EnumValueValidator.class})
public @interface EnumValue {

    String message() default "{com.pingan.smartcity.framework.api.validate.aop.EnumValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();

    /**
     * enumClass 中校验方法名
     */
    String checkMethod();

    /**
     * Defines several {@link EnumValue} annotations on the same element.
     *
     * @see com.minister.framework.api.validate.aop.EnumValue
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {

        EnumValue[] value();

    }

    /**
     * 枚举校验注解实现
     */
    class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

        /**
         * 枚举类
         */
        private Class<? extends Enum<?>> enumClass;

        /**
         * 校验方法名
         */
        private String checkMethod;

        @Override
        public void initialize(EnumValue enumValue) {
            checkMethod = enumValue.checkMethod();
            enumClass = enumValue.enumClass();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
            if (value == null) {
                return Boolean.TRUE;
            }
            if (enumClass == null || StringUtils.isBlank(checkMethod)) {
                return Boolean.TRUE;
            }
            Class<?> valueClass = value.getClass();
            try {
                Method method = enumClass.getMethod(checkMethod, valueClass);
                // 校验方法返回值是否为 Boolean 或者 boolean
                if (!Boolean.TYPE.equals(method.getReturnType()) && !Boolean.class.equals(method.getReturnType())) {
                    throw new RuntimeException(String.format("%s method return is not boolean type in the %s class", checkMethod, enumClass));
                }
                // 校验方法是否为静态方法
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new RuntimeException(String.format("%s method is not static method in the %s class", checkMethod, enumClass));
                }

                Boolean result = (Boolean) method.invoke(null, value);
                return result == null ? false : result;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(String.format("This %s(%s) method does not exist in the %s", checkMethod, valueClass, enumClass), e);
            }
        }

    }

}
