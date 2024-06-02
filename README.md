## 抓取博客园数据

### 回顾spring的定时任务配置

#### 注解方式配置

````
1. spring配置文件中配置注解扫描包 可以配置多个扫描
    例如
        <context:component-scan base-package="com.et.task" />
        <context:component-scan base-package="com.et.service" />
2. 自定义类 添加如下注解
	@Component
	@EnableScheduling // 开启定时任务
3. 了解@Scheduled注解中参数的使用
````

##### application.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:jee="http://www.springframework.org/schema/jee"
       xmlns:tx="http://www.springframework.org/schema/tx" xmlns:task="http://www.springframework.org/schema/tool"
       xsi:schemaLocation="http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd
        http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee-4.0.xsd
        http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd http://www.springframework.org/schema/tool http://www.springframework.org/schema/tool/spring-tool.xsd">


    <context:component-scan base-package="com.et.task" />
    <!--数据源-->
    <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/db_blog0708"/>
        <property name="username" value="root"/>
        <property name="password" value="Karen@1234"/>
        <property name="maxActive" value="10"/>
        <property name="minIdle" value="5"/>
    </bean>

    <!--mybatis连接工厂-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <!-- 自动扫描mappers.xml文件 -->
        <property name="mapperLocations" value="classpath:com/et/mappers/*.xml"></property>
        <!-- mybatis配置文件 -->
        <property name="configLocation" value="classpath:mybatis-config.xml"></property>
    </bean>

    <!-- Mapper接口所在包名，Spring会自动查找其下的类 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.et.mapper" />
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"></property>
    </bean>

    <!-- (事务管理)transaction manager, use JtaTransactionManager for global tx -->
    <bean id="transactionManager"
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource" />
    </bean>

    <!-- 自定义Realm -->
    <bean id="myRealm" class="com.et.realm.MyRealm"/>

    <!-- 安全管理器 -->
    <bean id="securityManager" class="org.apache.shiro.web.mgt.DefaultWebSecurityManager">
        <property name="realm" ref="myRealm"/>
    </bean>

    <!-- Shiro过滤器 -->
    <bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
        <!-- Shiro的核心安全接口,这个属性是必须的 -->
        <property name="securityManager" ref="securityManager"/>
        <!-- 身份认证失败，则跳转到登录页面的配置 -->
        <property name="loginUrl" value="/login.jsp"/>
        <!-- Shiro连接约束配置,即过滤链的定义 -->
        <property name="filterChainDefinitions">
            <value>
                /login=anon
                /admin/**=authc
            </value>
        </property>
    </bean>

    <!-- 保证实现了Shiro内部lifecycle函数的bean执行 -->
    <bean id="lifecycleBeanPostProcessor" class="org.apache.shiro.spring.LifecycleBeanPostProcessor"/>
    <!-- 开启Shiro注解 -->
    <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator" depends-on="lifecycleBeanPostProcessor"/>
    <bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">
        <property name="securityManager" ref="securityManager"/>
    </bean>

    <!-- 配置事务通知属性 -->
    <tx:advice id="txAdvice" transaction-manager="transactionManager">
        <!-- 定义事务传播属性 -->
        <tx:attributes>
            <tx:method name="insert*" propagation="REQUIRED" />
            <tx:method name="update*" propagation="REQUIRED" />
            <tx:method name="edit*" propagation="REQUIRED" />
            <tx:method name="save*" propagation="REQUIRED" />
            <tx:method name="add*" propagation="REQUIRED" />
            <tx:method name="new*" propagation="REQUIRED" />
            <tx:method name="set*" propagation="REQUIRED" />
            <tx:method name="remove*" propagation="REQUIRED" />
            <tx:method name="delete*" propagation="REQUIRED" />
            <tx:method name="change*" propagation="REQUIRED" />
            <tx:method name="check*" propagation="REQUIRED" />
            <tx:method name="get*" propagation="REQUIRED" read-only="true" />
            <tx:method name="find*" propagation="REQUIRED" read-only="true" />
            <tx:method name="load*" propagation="REQUIRED" read-only="true" />
            <tx:method name="*" propagation="REQUIRED" read-only="true" />
        </tx:attributes>
    </tx:advice>

    <!-- 配置事务切面 -->
    <aop:config>
        <aop:pointcut id="serviceOperation"
                      expression="execution(* com.et.service.*.*(..))" />
        <aop:advisor advice-ref="txAdvice" pointcut-ref="serviceOperation" />
    </aop:config>

    <!-- 自动扫描 -->
    <context:component-scan base-package="com.et.service" />

</beans>
```

##### Sheduled注解参数

```
cron
zone
fixedDelay
fixedDelayString
fixedRate
fixedRateString
initialDelay
initialDelayString
```

###### cron

```
该参数接收一个cron表达式，cron表达式是一个字符串，字符串以5或6个空格隔开，分开共6或7个域，每一个域代表一个含义

cron表达式语法
	[秒] [分] [小时] [日] [月] [周] [年]
	注：[年]不是必须的域，可以省略[年]，则一共6个域

```

| #    | 说明   | 是否必须 | 允许的通配符  | 取值范围         |
| ---- | ------ | -------- | ------------- | ---------------- |
| 1    | second | Y        | , - * /       | 0-59             |
| 2    | minute | Y        | , - * /       | 0-59             |
| 3    | hour   | Y        | , - * /       | 0-23             |
| 4    | day    | Y        | , - * ? / L W | 1-31             |
| 5    | month  | Y        | , - * /       | 1-12 or  JAN-DEC |
| 6    | week   | Y        | , - * / L #   | 1-7 or SUN-SAT   |
| 7    | year   | N        | , - * /       | 1970-2099        |

###### cron通配符说明

```
1. *表示所有值。 例如:在分的字段上设置 *,表示每一分钟都会触发。

2. ? 表示不指定值。使用的场景为不需要关心当前设置这个字段的值。例如:要在每月的10号触发一个操作，但不关心是周几，所以需要周位置的那个字段设置为”?” 具体设置为 0 0 0 10 * ?

3. -表示区间。例如 在小时上设置 “10-12”,表示 10,11,12点都会触发

4. , 表示指定多个值，例如在周字段上设置 “MON,WED,FRI” 表示周一，周三和周五触发

5. / 用于递增触发。如在秒上面设置”5/15” 表示从5秒开始，每增15秒触发(5,20,35,50)。 在月字段上设置’1/3’所示每月1号开始，每隔三天触发一次

6. L 表示最后的意思。在日字段设置上，表示当月的最后一天(依据当前月份，如果是二月还会依据是否是润年[leap]), 在周字段上表示星期六，相当于”7”或”SAT”。如果在”L”前加上数字，则表示该数据的最后一个。例如在周字段上设置”6L”这样的格式,则表示“本月最后一个星期五”

7. W 表示离指定日期的最近那个工作日(周一至周五). 例如在日字段上置”15W”，表示离每月15号最近的那个工作日触发。如果15号正好是周六，则找最近的周五(14号)触发, 如果15号是周未，则找最近的下周一(16号)触发.如果15号正好在工作日(周一至周五)，则就在该天触发。如果指定格式为 “1W”,它则表示每月1号往后最近的工作日触发。如果1号正是周六，则将在3号下周一触发。(注，”W”前只能设置具体的数字,不允许区间”-“)

8. #序号(表示每月的第几个周几)，例如在周字段上设置”6#3”表示在每月的第三个周六.注意如果指定”#5”,正好第五周没有周六，则不会触发该配置(用在母亲节和父亲节再合适不过了) ；小提示：’L’和 ‘W’可以一组合使用。如果在日字段上设置”LW”,则表示在本月的最后一个工作日触发；周字段的设置，若使用英文字母是不区分大小写的，即MON与mon相同
```

cron示例

```
每隔5秒执行一次：*/5 * * * * ?
每隔1分钟执行一次：0 */1 * * * ?
每天23点执行一次：0 0 23 * * ?
每天凌晨1点执行一次：0 0 1 * * ?
每月1号凌晨1点执行一次：0 0 1 1 * ?
每月最后一天23点执行一次：0 0 23 L * ?
每周星期天凌晨1点实行一次：0 0 1 ? * L
在26分、29分、33分执行一次：0 26,29,33 * * * ?
每天的0点、13点、18点、21点都执行一次：0 0 0,13,18,21 * * ?
cron表达式使用占位符 另外，cron属性接收的cron表达式支持占位符。

例如
	    @Scheduled(cron="${time.cron}")
        void testPlaceholder1() {
        	System.out.println("Execute at " + System.currentTimeMillis());
        }

        @Scheduled(cron="*/${time.interval} * * * * *")
            void testPlaceholder2() {
            System.out.println("Execute at " + System.currentTimeMillis());
        }

```

###### zone

```
时区，接收一个java.util.TimeZone#ID。cron表达式会基于该时区解析
默认是一个空字符串，即取服务器所在地的时区
比如我们一般使用的时区Asia/Shanghai。该字段我们一般留空
```

###### fixedDelay

```
上一次执行完毕时间点之后多长时间再执行
例如
	@Scheduled(fixedDelay = 5000) //上一次执行完毕时间点之后5秒再执行

```

###### fixedDelayString

```
与  fixedDelay 意思相同，只是使用字符串的形式 唯一不同的是支持占位符
例如
	@Scheduled(fixedDelayString = "5000") //上一次执行完毕时间点之后5秒再执行
	
占位符的使用(配置文件中有配置：time.fixedDelay=5000)
	    @Scheduled(fixedDelayString = "${time.fixedDelay}")
        void testFixedDelayString() {
            System.out.println("Execute at " + System.currentTimeMillis());
        }

```

###### fixedRate

```
上一次开始执行时间点之后多长时间再执行
例如
	@Scheduled(fixedRate = 5000) //上一次开始执行时间点之后5秒再执行
```

###### fixedRateString

```
与fixedRate 意思相同，只是使用字符串的形式 
	@Scheduled(fixedRate = "5000") //上一次开始执行时间点之后5秒再执行
```

###### initialDelay

```
第一次延迟指定时间后再执行
@Scheduled(initialDelay=1000, fixedRate=5000) //第一次延迟1秒后执行，之后按fixedRate的规则每5秒执行一次
```

###### initialDelayString

```
与  initialDelay 意思相同，只是使用字符串的形式。唯一不同的是支持占位符
```

##### MyTask

```java
package com.et.task;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-05-29  20:56
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@EnableScheduling // 开启注解扫描
public class MyTask {

    int i = 0;
    @Scheduled(fixedDelay = 5000) //上一次执行完毕时间点之后5秒再执行
    public void test1(){
        System.out.println("1===================="+(i++));
    }


    @Scheduled(fixedRate = 5000) //上一次开始执行时间点之后5秒再执行
    public void test2(){
        System.out.println("2===================="+(i++));
    }

    // 启动时执行一次，之后每隔2秒执行一次
    @Scheduled(fixedRate = 1000*12)
    public void test3(){
        System.out.println("3===================="+(i++));
    }
}

```

#### 配置文件方式

```
在spring配置文件中直接配置调度时间及要执行的类和方法
```

##### application.xml

头信息

```xml
xmlns:task="http://www.springframework.org/schema/task"
xsi:schemaLocation：
	http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task-4.1.xsd

```

配置

```
<!--配置文件方式配置定时器-->
    <task:annotation-driven /> <!-- 定时器开关-->
    <bean id="myTask" class="com.et.task.MyTask"></bean>


    <task:scheduled-tasks>
        <!-- 这里表示的是每隔30分钟执行一次   0 0/30 * * * ?  -->
        <!--<task:scheduled ref="myTask" method="show" cron="0 0/30 * * * ? " /> -->
        <!-- 这里表示的是每隔10秒执行一次 print 指的MyTask类中要执行的方法名-->
        <task:scheduled ref="myTask" method="test1" cron="*/10 * * * * ?"/>
    </task:scheduled-tasks>
```

完整配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:jee="http://www.springframework.org/schema/jee"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:task="http://www.springframework.org/schema/tool"
       xmlns:task="http://www.springframework.org/schema/task"
       xsi:schemaLocation="http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd
        http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee-4.0.xsd
        http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd http://www.springframework.org/schema/tool http://www.springframework.org/schema/tool/spring-tool.xsd
        http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task-4.1.xsd">


    <!--配置文件方式配置定时器-->
    <task:annotation-driven /> <!-- 定时器开关-->
    <bean id="myTask" class="com.et.task.MyTask"></bean>


    <task:scheduled-tasks>
        <!-- 这里表示的是每隔30分钟执行一次   0 0/30 * * * ?  -->
        <!--<task:scheduled ref="myTask" method="show" cron="0 0/30 * * * ? " /> -->
        <!-- 这里表示的是每隔10秒执行一次 print 指的MyTask类中要执行的方法名-->
        <task:scheduled ref="myTask" method="test1" cron="*/10 * * * * ?"/>
    </task:scheduled-tasks>
    <!--注解方式配置定时器-->
    <context:component-scan base-package="com.et.task" />
    <!--数据源-->
    <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/db_blog0708"/>
        <property name="username" value="root"/>
        <property name="password" value="Karen@1234"/>
        <property name="maxActive" value="10"/>
        <property name="minIdle" value="5"/>
    </bean>

    <!--mybatis连接工厂-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <!-- 自动扫描mappers.xml文件 -->
        <property name="mapperLocations" value="classpath:com/et/mappers/*.xml"></property>
        <!-- mybatis配置文件 -->
        <property name="configLocation" value="classpath:mybatis-config.xml"></property>
    </bean>

    <!-- Mapper接口所在包名，Spring会自动查找其下的类 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.et.mapper" />
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"></property>
    </bean>

    <!-- (事务管理)transaction manager, use JtaTransactionManager for global tx -->
    <bean id="transactionManager"
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource" />
    </bean>

    <!-- 自定义Realm -->
    <bean id="myRealm" class="com.et.realm.MyRealm"/>

    <!-- 安全管理器 -->
    <bean id="securityManager" class="org.apache.shiro.web.mgt.DefaultWebSecurityManager">
        <property name="realm" ref="myRealm"/>
    </bean>

    <!-- Shiro过滤器 -->
    <bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
        <!-- Shiro的核心安全接口,这个属性是必须的 -->
        <property name="securityManager" ref="securityManager"/>
        <!-- 身份认证失败，则跳转到登录页面的配置 -->
        <property name="loginUrl" value="/login.jsp"/>
        <!-- Shiro连接约束配置,即过滤链的定义 -->
        <property name="filterChainDefinitions">
            <value>
                /login=anon
                /admin/**=authc
            </value>
        </property>
    </bean>

    <!-- 保证实现了Shiro内部lifecycle函数的bean执行 -->
    <bean id="lifecycleBeanPostProcessor" class="org.apache.shiro.spring.LifecycleBeanPostProcessor"/>
    <!-- 开启Shiro注解 -->
    <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator" depends-on="lifecycleBeanPostProcessor"/>
    <bean class="org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor">
        <property name="securityManager" ref="securityManager"/>
    </bean>

    <!-- 配置事务通知属性 -->
    <tx:advice id="txAdvice" transaction-manager="transactionManager">
        <!-- 定义事务传播属性 -->
        <tx:attributes>
            <tx:method name="insert*" propagation="REQUIRED" />
            <tx:method name="update*" propagation="REQUIRED" />
            <tx:method name="edit*" propagation="REQUIRED" />
            <tx:method name="save*" propagation="REQUIRED" />
            <tx:method name="add*" propagation="REQUIRED" />
            <tx:method name="new*" propagation="REQUIRED" />
            <tx:method name="set*" propagation="REQUIRED" />
            <tx:method name="remove*" propagation="REQUIRED" />
            <tx:method name="delete*" propagation="REQUIRED" />
            <tx:method name="change*" propagation="REQUIRED" />
            <tx:method name="check*" propagation="REQUIRED" />
            <tx:method name="get*" propagation="REQUIRED" read-only="true" />
            <tx:method name="find*" propagation="REQUIRED" read-only="true" />
            <tx:method name="load*" propagation="REQUIRED" read-only="true" />
            <tx:method name="*" propagation="REQUIRED" read-only="true" />
        </tx:attributes>
    </tx:advice>

    <!-- 配置事务切面 -->
    <aop:config>
        <aop:pointcut id="serviceOperation"
                      expression="execution(* com.et.service.*.*(..))" />
        <aop:advisor advice-ref="txAdvice" pointcut-ref="serviceOperation" />
    </aop:config>

    <!-- 自动扫描 -->
    <context:component-scan base-package="com.et.service" />

</beans>
```

### 抓取博客

#### 技术栈

```
使用技术栈
    Ehcache 缓存
    HttpClient 执行http请求
    Jsoup 解析网页内容
	
```

#### 流程

```
1. HttpClient发送http请求 返回response对象
	该对象封装了响应的头信息及页面主体内容等
2. jsoup解析返回的主体信息 获取指定的连接
	获取到连接后添加到缓存 避免重复抓取
3. 重复第一步操作 获取每个连接对应的博客数据
4. 解析文章内容 获取有效数据
	文章中图片需要下载到本地 同时将文章中的图片地址替换为本地路径
5. 保存到数据库
6. 关闭请求释放资源
```

#### 依赖

```xml
<!-- 添加Httpclient支持 -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.2</version>
</dependency>

<!-- 添加ehcache支持 -->
<dependency>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <version>2.10.3</version>
</dependency>
修改commons-io版本为2.5或以上
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.5</version>
</dependency>
处理特殊符号的依赖 因为如果文章中存在特殊符号 数据库可能会保存失败
<dependency>
    <groupId>com.vdurmont</groupId>
    <artifactId>emoji-java</artifactId>
    <version>4.0.0</version>
</dependency>
```

#### 工具类封装

```
HttpUtil	
	发送请求获取response对象
	关闭http请求 释放资源
  
CacheUtil
	创建缓存
	添加缓存
	释放资源
PropertiesUtil
	读取配置文件工具类
	
DateUtil
	生成日期格式路径
		例如 2024/06/01
		
DownloadImgUtil
	图片下载工具类 用来下载博客内容中的图片到本地
	返回Map 
		Map中key 图片的原始路径 value需要替换的路径
```

##### HttpUtil

```java
package com.et.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  17:59
 * @Description: TODO http请求相关工具类
 * @Version: 1.0
 */
public class HttpUtil {

    /**
     * 发送请求 返回网页内容
     * @param url
     * @return
     */
    public static String send(String url){
        // 定义网页内容
        String webContent = "";
        // 1. 创建HttpClient对象 用来发送http请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 2. 创建HttpGet对象 用来发送get请求
        HttpGet httpGet = new HttpGet(url);
        // 3. 配置请求消息
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000) // 连接超时时间 单位毫秒
                .setSocketTimeout(1 * 1000 * 10) // 读取超时时间 单位毫秒
                .build();
        httpGet.setConfig(config);

        // 4. 创建response对象 用来接收返回的数据
        CloseableHttpResponse response = null;
        try {
            // 执行get请求 获取数据
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            System.out.println("请求 ["+url+"] 时发生了异常 "+e.getMessage());
            throw new RuntimeException(e);
        }
        // 5. 通过response获取网页实体
        if(response!=null){
            // 获取返回实体
            HttpEntity entity = response.getEntity();
            // 判断返回状态是否是200
            if(response.getStatusLine().getStatusCode()==200) {

                try {
                    // 获取网页中的内容
                    webContent = EntityUtils.toString(entity, "utf-8");
                } catch (ParseException | IOException e) {
                    System.out.println("解析链接["+url+"]返回的实体异常 "+e.getMessage());
                    e.printStackTrace();
                }
            }else{
                System.out.println("获取链接["+url+"]返回的状态是  "+response.getStatusLine().getStatusCode());
            }
        }else{
            System.out.println("请求 ["+url+"] 时 链接超时");
        }
        // 释放资源
        closeHttp(response,httpClient);
        return webContent;
    }

    private static void closeHttp(CloseableHttpResponse response, CloseableHttpClient httpClient){
        try {
            if(response!=null)
                response.close();
            if(httpClient!=null)
                httpClient.close();
        } catch (IOException e) {
            System.out.println("释放资源发生错误 ");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String s = send("https://www.cnblogs.com/");
        System.out.println(s);
    }
}

```

##### PropertiesUtil

```java
package com.et.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
* 配置文件读取工具类
* */
public class PropertiesUtil {

    /*
     * 根据key获取指定的value
     * */
    public static String getValue(String key){
        // 1. 创建Properties对象
        Properties prop = new Properties();
        // 2. 获取文件流
        InputStream is = new PropertiesUtil().getClass().getResourceAsStream("/sys.properties");
        try {
            // 3. 加载到Properties
            prop.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (String) prop.get(key);
    }

    public static void main(String[] args) {
        System.out.println(getValue("salt"));
    }
}

```

##### CacheUtil

```java
package com.et.crawler;

import com.et.util.PropertiesUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  18:15
 * @Description: TODO ehcache相关工具类
 * @Version: 1.0
 */
public class CacheUtil {

    // 定义缓存管理器
    private static CacheManager manager;
    // 缓存对象
    private static Cache cache;

    /**
     * 将内容添加到缓存中
     * @param str
     */
    public static void openCache(String str){
        // 初始化缓存对象
        manager = CacheManager.create(PropertiesUtil.getValue("cacheFilePath"));
        // 获取cache对象
        cache = manager.getCache("cnblog");
        Element element = new Element(str,str); // key和value都给同样值
        cache.put(element);// 添加到缓存中

        // 刷新缓存才会被写入到文件中
        cache.flush();
    }


    /**
     * 在ehcache中获取缓存对象
     * @param key
     */
    public static Element getCache(String key){
        if (cache!=null){
            return cache.get(key);
        }
        return null;
    }


    /*
    * 关闭缓存
    * */
    private static void shutdown(){
        if (manager!=null){
            manager.shutdown();
        }
    }

    public static void main(String[] args) {
        openCache("http://www.etjava.com");
        Element element = getCache("http://www.etjava.com");
        System.out.println(element);
    }
}

```

##### sys.properties

```properties
imageUrl=http://localhost:8080/static/blogImages/
cacheFilePath=C://ehcache.xml
CNBLOGURI=https://www.cnblogs.com/
lucenePath1=C://lucene
lucenePath2=C://crawlerlucene
# login
salt=et
# page size
pageSize = 5
```



##### ehcache.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>

<ehcache>
   <!-- 
         磁盘存储:将缓存中暂时不使用的对象,转移到硬盘,类似于Windows系统的虚拟内存
          path:指定在硬盘上存储对象的路径
   -->
   <diskStore path="C:\blogehcache" />
   
   <!-- 
        defaultCache:默认的缓存配置信息,如果不加特殊说明,则所有对象按照此配置项处理
        maxElementsInMemory:设置了缓存的上限,最多存储多少个记录对象
        eternal:代表对象是否永不过期
        overflowToDisk:当内存中Element数量达到maxElementsInMemory时，Ehcache将会Element写到磁盘中
   -->
   <defaultCache
      maxElementsInMemory="1"
      eternal="true"
      overflowToDisk="true"/>

    <cache 
      name="cnblog"
      maxElementsInMemory="1"
      diskPersistent="true"
      eternal="true"
      overflowToDisk="true"/>
      
</ehcache>

```

##### DateUtil

```java
package com.et.crawler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  17:57
 * @Description: TODO 日期相关工具类
 * @Version: 1.0
 */
public class DateUtil {

    /**
     * 日期格式路径
     * 例如：2024/06/01
     * @return
     */
    public static String datePath(){
        SimpleDateFormat fmt = new SimpleDateFormat("YYYY/MM/DD");
        return fmt.format(new Date());
    }
}

```

##### DownloadImgUtil

- 下载文章中的图片到本地 
- 返回Map集合
  - key 原始路径  value本地存放图片的路径

```java
package com.et.crawler;

import com.et.util.DateUtil;
import com.et.util.PropertiesUtil;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  19:09
 * @Description: TODO 下载图片工具类
 * @Version: 1.0
 */
public class DownloadImgUtil {

    /**
     * 下载图片到本地
     * @param imageList 存放的是网页中获取到的图片地址
     * @param realPath 图片本地保存的路径
     * @return
     */
    public static Map<String,Object> download(List<String> imageList,String realPath){
        Map<String,Object> map = new HashMap<>();
        for (String link : imageList) {
            // 剔除小图标的抓取
            String s = link.substring(link.length()-4, link.length());
            if(s.contains("gif")) {
                continue;
            }
            try {
                // 每次下载间隔1秒 防止访问过快被封禁
                Thread.sleep(1000);
                // 执行请求
                CloseableHttpResponse response = HttpUtil.getResponse(link);
                if(response!=null) {
                    // 获取返回实体
                    HttpEntity entity = response.getEntity();
                    // 判断返回状态是否是200
                    if(response.getStatusLine().getStatusCode()==200) {
                        try {
                            // 将图片转为InputStream
                            InputStream is = entity.getContent();
                            // 文件类型 例如 image/jpg   image/png
                            String imageType = entity.getContentType().getValue();
                            String b = imageType.split("/")[1];

                            // common-io.FileUtils .copyToFile 直接将文件下载到本地指定目录
                            String uuid = UUID.randomUUID().toString();
                            // 图片在项目中的位置
                            String fileURI = DateUtil.currentDatePath()+"/"+uuid+"."+b;
                            // 图片保存的地址
                            FileUtils.copyToFile(is,new File(realPath+"/static/blogImages/"+fileURI));
                            // 封装图片信息返回map
                            map.put(link, PropertiesUtil.getValue("imageUrl")+fileURI);
                        } catch (UnsupportedOperationException | IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        System.out.println("获取链接["+link+"]返回的状态是  "+response.getStatusLine().getStatusCode());
                    }
                }else{
                    System.out.println("请求 ["+link+"] 时 链接超时");
                }
                // 关闭资源
                try {
                    if(response!=null)
                        response.close();
                } catch (IOException e) {
                    System.out.println("释放资源发生错误 ");
                    e.printStackTrace();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("https://img2024.cnblogs.com/blog/2462804/202406/2462804-20240602140202065-601606452.png");
        Map<String, Object> map = download(list, "d:/");
        System.out.println(map);
    }
}

```

#### 爬取信息

```java
package com.et.crawler;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  18:35
 * @Description: TODO 抓取CNBlog中的文章信息
 * @Version: 1.0
 */
public class CNBlog {

    // 首页地址
    private static final String URL = "https://www.cnblogs.com/";

    // 博客中图片存放的位置
    private static String realPath="C:\\Users\\etjav\\Desktop\\blog\\target\\blog";

    public static void main(String[] args) {
        parseHome();
    }
    /**
     * 抓取首页内容
     */
    public static void parseHome(){
        // 发送http请求 获取首页内容
        String homeContent = HttpUtil.send(URL);
        // 解析首页内容 获取指定连接
        List<String> linkList = parseHomeLink(homeContent);
        // 判断该连接是否抓取过 如果没有则进行抓取 同时放到缓存中 避免重复抓取
        for (String link : linkList) {
            if(CacheUtil.getCache(link)!=null) {
                System.out.println("已抓取当前链接博客信息 " + URL);
                continue;
            }else {
                // 放到缓存中 避免二次抓取
               // CacheUtil.openCache(link);
                // 根据链接地址解析博客内容
                String blogContent = HttpUtil.send(link);
                // 解析博客内容获取有效数据
                parseContent(blogContent,link);
            }
        }
    }

    /**
     * 解析博客内容 获取有效数据 返回替换后的文章内容
     * @param blogContent
     */
    private static void parseContent(String blogContent,String url) {
        if(StringUtils.isEmpty(blogContent)){
            return ;
        }
        // 解析内容获取Document对象
        Document document = Jsoup.parse(blogContent);
        // 获取标题
        Elements titles = document.select("#cb_post_title_url span");
        // 如果标题没有获取到 则舍弃当前文章
        if(titles.size()==0) {
            System.out.println(url + " - 未获取到标题内容");
            return ;
        }
        // 获取博客标题
        String title = titles.get(0).text();
        // 获取博客内容  - 包含标签的文章内容
        Elements contents = document.select("#cnblogs_post_body");
        List<String> imgList = new ArrayList<>();
        // 如果内容为空则舍弃
        if(contents.size()==0) {
            System.out.println(url + " - 未获取到博客内容");
            return ;
        }
        // 获取带有html标签的文章内容
        String content = contents.get(0).html();
        // 移除博客内容中的emojo符号
        String content2 = EmojiParser.removeAllEmojis(content);
        // 查找文章中所有img标签 将图片保存到本地指定目录
        Elements images = contents.select("img");

        for(int i=0;i<images.size();i++) {
            String imgUrl = images.get(i).attr("src");
            imgList.add(imgUrl);
        }

        // 替换文章中的图片路径
        if(imgList.size()>0) {
            // 下载图片到本地 返回 原始地址-新地址 map集合   后边需要替换到文章中的img src属性中
            Map<String, Object> imageMap = DownloadImgUtil.download(imgList,realPath);
            // 替换文章中图片地址 返被修改好之后的文章内容
            content2 = replaceContentImages(imageMap,content2);

            // 保存数据库中
            System.out.println("文章标题："+title);
            System.out.println("文章内容："+content2);
        }
    }

    /**
     * 替换文章中原因的图片路径为本地图片路径
     * @param imageMap 图片路径集合 key原始路径  value本地路径
     * @param content2 需要被替换的文章内容
     * @return
     */
    private static String replaceContentImages(Map<String, Object> imageMap, String content2) {
        for(String url:imageMap.keySet()) {
            String newPath = (String) imageMap.get(url);// 根据原始地址获取新的地址
            content2 = content2.replace(url, newPath);
        }
        return content2;
    }

    /*
    * 解析首页内容 获取指定的连接
    * */
    private static List<String> parseHomeLink(String homeContent) {
        if (StringUtils.isEmpty(homeContent)) {
            return null;
        }
        List<String> list = new LinkedList<>();
        // 解析首页内容获取document对象
        Document document = Jsoup.parse(homeContent);
        // 获取指定的链接地址  因为存在子的a标签 因此这里直接使用clss定位到需要的链接标签中
        Elements links = document.select("#post_list .post-item .post-item-body .post-item-text .post-item-title");
        for (int i = 0; i < links.size(); i++) {
            Element link = links.get(i);
            String url = link.attr("href");
            list.add(url);
            //System.out.println("获取到的连接地址：" + url);
        }
        return list;
    }

}

```

### 定时器配置

- 定时执行抓取任务

```java
package com.et.task;

import com.et.crawler.CNBlog;
import com.et.util.PropertiesUtil;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-05-29  20:56
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@EnableScheduling
public class MyTask {

    int i = 0;
    //@Scheduled(fixedDelay = 5000) //上一次执行完毕时间点之后5秒再执行
    @Scheduled(cron = "0 19 20 * * ?") //每天20:19分执行
    public void test1(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        ServletContext servletContext = webApplicationContext.getServletContext();
        String realPath = servletContext.getRealPath(File.separator);
        new CNBlog(PropertiesUtil.getValue("CNBLOGURI"),realPath);
        System.out.println(realPath);
    }
}

```







