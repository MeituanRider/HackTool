## 第一步 先定义需要的注解

~~~java
/**

* 脱敏注解
  *
  */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  @JacksonAnnotationsInside //这个注解用来标记Jackson复合注解,当你使用多个Jackson注解组合成一个自定义注解时会用到它
  @JsonSerialize(using = SensitiveJsonSerializer.class) //指定使用自定义的序列化器
  public @interface Sensitive {
  SensitiveStrategy value();   //该自定义注解需要的参数   strategy-参数名称    SensitiveStrategy-参数类型
  }
~~~

 @Retention(RetentionPolicy.RUNTIME) 和 @Target(ElementType.FIELD) 这两个是元注解,用来标注该注解的使用信息

@Retention(RetentionPolicy.RUNTIME) 表示该注解在运行时生效

@Target(ElementType.FIELD) 表示注解的作用目标 ElementType.FIELD表示注解作用于字段上

@JacksonAnnotationsInside 这个注解用来标记Jackson复合注解,当你使用多个Jackson注解组合成一个自定义注解时会用到它

@JsonSerialize(using = SensitiveJsonSerializer.class) 指定使用自定义的序列化器

SensitiveStrategy strategy(); 该自定义注解需要的参数 strategy-参数名称 SensitiveStrategy-参数类型

## 第二步 编写脱敏的策略的枚举


```java 
package com.hacktool.enums;

import java.util.function.Function;

/**
 * 校验数据类型枚举
 *
 */
public enum SensitiveStrategy {
    /**
     * Username sensitive strategy.  $1 替换为正则的第一组  $2 替换为正则的第二组
     */
    USERNAME(s -> s.replaceAll("(\\S)\\S(\\S*)", "$1*$2")),
    /**
     * Id card sensitive type.
     */
    ID_CARD(s -> s.replaceAll("(\\d{3})\\d{13}(\\w{2})", "$1****$2")),
    /**
     * Phone sensitive type.
     */
    PHONE(s -> s.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2")),
    /**
     * Address sensitive type.
     */
    ADDRESS(s -> s.replaceAll("(\\S{3})\\S{2}(\\S*)\\S{2}", "$1****$2****")),

    /**
     * Email sensitive type.
     */
    EMAIL(s -> s.replaceAll("(\\w+)\\w{5}@(\\w+)","$1***@$2"));



    private final Function<String, String> desensitizer;

    /**
     * 定义构造函数，传入一个函数
     */
    SensitiveStrategy(Function<String, String> desensitizer) {
        this.desensitizer = desensitizer;
    }

    /**
     * getter方法
     */
    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}

```
这个类似一个工厂类,里面放置需要的脱敏策略,需要注意的是这个枚举返回的是一个函数Function

该函数就是我们定义的脱敏函数,该函数会在后面的序列化时被使用,该枚举类的注解我写的很详细,这里就不一一赘述了

## 第三步 实现我们的自定义脱敏序列化器

~~~java
package com.hacktool.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.hacktool.Annotation.Sensitive;
import com.hacktool.enums.SensitiveStrategy;

import java.io.IOException;
import java.util.Objects;

/**
 * 自定义数据脱敏
 *
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {
    private SensitiveStrategy strategy;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        //strategy.desensitizer() 返一个Function
        // Function.apply(value) 执行枚举里面定义的脱敏方法
        gen.writeString(strategy.desensitizer().apply(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            this.strategy = annotation.value();
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}

~~~



JsonSerializer 是需要继承的序列化方法

ContextualSerializer 是获取前后文的方法

## 第四步 使用注解

在需要脱敏的字段上加上注解@Sensitive(strategy = SensitiveStrategy.ID_CARD) 并指定脱敏策略

~~~java
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

~~~



执行流程分析:

我添加了输出语句,来分析他的执行流程

```java 
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {
    private SensitiveStrategy strategy;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        //strategy.desensitizer() 返一个Function
        // Function.apply(value) 执行枚举里面定义的脱敏方法
        gen.writeString(strategy.desensitizer().apply(value));
        System.out.println(4);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        System.out.println(1);
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            this.strategy = annotation.strategy();
            System.out.println(2);
            return this;
        }
        System.out.println(3);
        return prov.findValueSerializer(property.getType(), property);
    }
}
```
![image](https://img2022.cnblogs.com/blog/1895037/202204/1895037-20220408153808602-451079211.png)

说明在进行序列化的时候,框架先扫描到了实体类的该注解 @Sensitive(strategy = SensitiveStrategy.ID_CARD)

然后根据该注解里面的 @JsonSerialize(using = SensitiveJsonSerializer.class) 使用了我们自定义的序列化器

先执行了createContextual方法,来获取上下文(获取注解里面的参数 SensitiveStrategy.ID_CARD)

然后执行序列化方法serialize,该方法会获取前面的createContextual方法返回的参数 (这里就是 value)

strategy.desensitizer() 返回的是一个函数

.apply(value) 使用的是jdk8 的Function.apply() 会执行strategy.desensitizer()返回的函数

gen.writeString(strategy.desensitizer().apply(value)) 然后把函数的返回值设置给序列化的对象

**结语 :**

(1)整个的执行流程如上所示,需要更加深刻了解的可以在代码里面进行debug,跟踪他执行的每一步,进行理解

(2)该方法时基于springboot默认的Jackson进行的,如果序列化框架是fastjson的话,需要进行修改(待补充)
