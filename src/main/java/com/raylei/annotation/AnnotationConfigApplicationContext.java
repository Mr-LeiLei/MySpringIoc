package com.raylei.annotation;

import com.raylei.util.MyUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 1、自定义一个AnnotationConfigApplicationContext，构造器中传入目标包。
 * 2、获取这个包下的所有类。
 * 3、遍历这些类，找出添加了 @Component 注解的类，获取对应的 beanName，beanClass，封装成一个BeanDefinition 对象，存入 Set 集合，这些就是 IoC 自动装载的原材料。
 * 4、遍历 Set 集合，通过反射机制创建对象，同时检测属性是否有 @Value 注解，有的话就赋值，没有就不赋值。
 * 5、将上面创建的 bean 以 k-v 的形式存入 cache Map
 * 6、提供 getBean 方法，通过 beanName 从缓存区取出对应的 bean
 */
public class AnnotationConfigApplicationContext {

    // 用来存储创建好的bean
    private Map<String,Object> cache = new HashMap<>();

    public AnnotationConfigApplicationContext(String basePackage){
        //遍历包，找到目标类
        Set<BeanDefinition> beanDefinitions = findBeanDefinition(basePackage);
        //根据原料创建bean
        createObject(beanDefinitions);
        //自动装载
        autowiredObject(beanDefinitions);
    }

    /**
     * 遍历包，找到目标类
     * @param basePackage 要扫描的包路径
     * @return 返回包装好的类
     */
    private Set<BeanDefinition> findBeanDefinition(String basePackage) {
        // 获取包下的所有Class
        Set<Class<?>> classes = MyUtil.getClasses(basePackage);
        // 遍历集合
        Iterator<Class<?>> iterator = classes.iterator();
        // 用来存放包装好的BeanDefinition
        Set<BeanDefinition> set = new HashSet<>();
        while (iterator.hasNext()){
            Class<?> clazz = iterator.next();
            // 判断该类是否添加了@Component注解
            Component component = clazz.getAnnotation(Component.class);
            if(component!=null){
                // 获取注解上的值
                String beanName = component.value();
                if ("".equals(beanName)){
                    // 注解值为空，自动给这个bean取一个名字，默认类名首字母小写
                    String packageName = clazz.getPackage().getName();
                    String className = clazz.getName();
                    beanName = className.replace(packageName+".","");
                    beanName = beanName.substring(0,1).toLowerCase()+beanName.substring(1);
                }
                // 添加到集合
                set.add(new BeanDefinition(beanName,clazz));
            }
        }
        return set;
    }

    /**
     * 创建bean对象
     * @param beanDefinitions 包装好的beanDefinitions
     */
    private void createObject(Set<BeanDefinition> beanDefinitions) {
        // 遍历集合
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            try {
                // 获取beanClass信息
                Class beanClass = beanDefinition.getBeanClass();
                // 创建当前bean的实例
                Object instance = beanClass.getConstructor(null).newInstance(null);
                // 给属性赋值
                Field[] fields = beanClass.getDeclaredFields();
                for (Field field : fields) {
                    // 判断当前属性是否添加了@Value注解
                    Value fieldAnnotation = field.getAnnotation(Value.class);
                    if(fieldAnnotation!=null){
                        // 有@Value注解，给当前属性赋值
                        // 获取注解上的值
                        String value = fieldAnnotation.value();
                        // 获得属性名字，并找到给当前属性赋值的方法setXxx()
                        String fieldName = field.getName();
                        String methodName = "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
                        Method method = beanClass.getMethod(methodName, field.getType());
                        // 得到属性的类型
                        String name = field.getType().getName();
                        Object val = null;
                        switch (name){
                            case "java.lang.Integer":
                                val = Integer.parseInt(value);
                                break;
                            case "java.lang.String":
                                val = value;
                                break;
                            case "java.lang.Double":
                                val = Double.parseDouble(value);
                                break;
                        }
                        // 通过反射给当前属性赋值
                        method.invoke(instance,val);
                    }
                }
                // 并将创建好的bean放在缓存中
                cache.put(beanDefinition.getBeanName(),instance);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 自动装载
     * @param beanDefinitions 包装好的beanDefinitions
     */
    private void autowiredObject(Set<BeanDefinition> beanDefinitions) {
        // 遍历集合
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            // 获得当前的类信息
            Class beanClass = beanDefinition.getBeanClass();
            // 获得当前类的所有属性，并遍历它
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                // 判断该属性上是否添加了@Autowired注解
                Autowired autowired = field.getAnnotation(Autowired.class);
                if(autowired!=null){
                    // 判断该属性上是否添加了@Qualifier注解
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    if(qualifier!=null){
                        // 根据名字进行装载
                        try {
                            // 获取@Qualifier注解上的value值
                            String value = qualifier.value();
                            // 得到缓存中的bean对象
                            Object obj = cache.get(value);
                            Object bean = cache.get(beanDefinition.getBeanName());
                            // 找到要注入的方法setXxx()
                            String name = field.getName();
                            String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                            Method method = beanClass.getMethod(methodName, field.getType());
                            // 通过反射注入对象
                            method.invoke(bean,obj);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 根据类型进行装载
                        try {
                            // 找到当前对象的类名字，首字母小写
                            Class<?> clazz = field.getType();
                            String packageName = clazz.getPackage().getName();
                            String className = clazz.getName();
                            className = className.replace(packageName+".","");
                            className = className.substring(0,1).toLowerCase()+className.substring(1);
                            // 去缓存中获取bean对象
                            Object obj = cache.get(className);
                            Object bean = cache.get(beanDefinition.getBeanName());
                            // 找到要注入的方法setXxx()
                            String name = field.getName();
                            String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                            Method method = beanClass.getMethod(methodName, field.getType());
                            // 通过反射注入对象
                            method.invoke(bean,obj);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 通过name获取bean
     * @param beanName
     * @return
     */
    public Object getBean(String beanName){
        return cache.get(beanName);
    }
}
