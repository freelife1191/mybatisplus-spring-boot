package com.baomidou.springboot.excel.util;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.TypeUtils;


/**
 * 반사 도구
 * @author lisuo
 */
public abstract class ReflectUtil {

    /**
     * 상수 값 가져 오기
     * @param clazz 상수
     * @param constName 상수
     * @return 상수는 값에 해당합니다.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getConstValue(Class<?> clazz,String constName){
        Field field = ReflectUtil.getField(clazz, constName);
        if(field!=null){
            field.setAccessible(true);
            try {
                Object object = field.get(null);
                if(object != null){
                    return (T) object;
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     *String 유형 특성을 trim
     * @param model 빈 공간 모델 (엔터티 클래스) 제거
     * @param propNames 속성의 이름을 제거합니다. String 유형 만 적용됩니다.
     */
    public static void trimFields(Object model, String... propNames){
        Assert.notEmpty(propNames, "이동하기 전후의 물건 이름을 지정하십시오.");
        for(String propName : propNames){
            Object val = ReflectUtil.getProperty(model, propName);
            if(val instanceof String){
                String valStr = (String)val;
                valStr = valStr.trim();
                ReflectUtil.setProperty(model, propName, valStr);
            }
        }
    }


    /**
     * static, final 필드를 제외하고, 지정된 클래스의 모든 필드를 가져옵니다.
     * @param clazz 종류
     * @return List<필드>
     */
    public static List<Field> getFields(Class<?> clazz){
        List<Field> fieldResult = new ArrayList<Field>();
        while(clazz!=Object.class){
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field:fields) {
                    int modifiers = field.getModifiers();
                    //static 또는 final 필드 필터링
                    if(Modifier.isStatic(modifiers)||Modifier.isFinal(modifiers)){
                        continue;
                    }
                    fieldResult.add(field);
                }
            } catch (Exception ignore) {}
            clazz = clazz.getSuperclass();
        }
        return fieldResult;
    }

    /**
     * static, final 필드를 제외 해, 지정된 클래스의 모든 필드 명을 가져옵니다.
     * @param clazz 종류
     * @return List<입력란 이름>
     */
    public static List<String> getFieldNames(Class<?> clazz){
        List<Field> fields = getFields(clazz);
        List<String> fieldNames = new ArrayList<String>(fields.size());
        for(Field field:fields){
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    /**
     * 리플렉션에 의해 클래스를 정의 할 때 선언 된 부모 클래스의 제네릭 매개 변수 유형 가져 오기
     * 예 : public EmployeeDao extends BaseDao <Employee, String>
     * @param clazz
     * @param index
     * @return
     */
    public static Class<?> getSuperClassGenricType(Class<?> clazz, int index){
        Type genType = clazz.getGenericSuperclass();

        if(!(genType instanceof ParameterizedType)){
            return Object.class;
        }
        Type [] params = ((ParameterizedType)genType).getActualTypeArguments();
        if(index >= params.length || index < 0){
            return Object.class;
        }
        if(!(params[index] instanceof Class)){
            return Object.class;
        }
        return (Class<?>) params[index];
    }

    /**
     * 리플렉션을 통해 클래스 정의에 선언 된 상위의 제네릭 매개 변수 유형을 가져옵니다.
     * 예 : public EmployeeDao extends BaseDao<Employee, String>
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static<T> Class<T> getSuperGenericType(Class<?> clazz){
        return (Class<T>) getSuperClassGenricType(clazz, 0);
    }

    /**
     * 속성 복사
     *
     * @param source
     * @param target
     * @param ignoreProps 무시 된 속성
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void copyProps(Object source, Object target, String... ignoreProps) {
        if (source instanceof Map) {
            Map sourceMap = (Map) source;
            Set<String> ignorPropsSet = new HashSet<String>();
            if (ArrayUtils.isNotEmpty(ignoreProps)) {
                for (String prop : ignoreProps) {
                    ignorPropsSet.add(prop);
                }
            }
            Set<Entry<Object, Object>> entrySet = sourceMap.entrySet();
            for (Entry<Object, Object> e : entrySet) {
                if (ignorPropsSet.isEmpty()) {
                    setProperty(target, e.getKey().toString(), e.getValue());
                } else if (!ignorPropsSet.contains(e.getKey())) {
                    setProperty(target, e.getKey().toString(), e.getValue());
                }
            }

        } else if (source instanceof List) {
            List sourceList = (List) source;
            if (target instanceof List) {
                List targetList = (List) target;
                for (Object obj : sourceList) {
                    targetList.add(obj);
                }
            }

        } else {
            BeanUtils.copyProperties(source, target, ignoreProps);
        }
    }

    /**
     * 비어 있지 않은 속성 복사, copy 속성 지정
     * @param source 리소스
     * @param target 목표
     * @param incProps 속성을 포함합니다.
     */
    public static void copyPropsInc(Object source, Object target, String...incProps) {
        if(ArrayUtils.isNotEmpty(incProps)){
            Set<String> incPropsSet = new HashSet<String>(incProps.length);
            for(String prop : incProps){
                incPropsSet.add(prop);
            }
            List<String> fieldNames = getFieldNames(source.getClass());
            for(String fieldName : fieldNames){
                if(!incPropsSet.contains(fieldName)){
                    continue;
                }
                Object value = getProperty(source, fieldName);
                setProperty(target, fieldName, value);
            }
        }
    }

    /**
     * java bean Map 루프
     * @param bean
     * @param propNames map에 넣을 속성의 이름
     * @return
     */
    public static Map<String,Object> beanToMap(Object bean, String...propNames) {
        Map<String,Object> rtn = new HashMap<String,Object>();
        if(ArrayUtils.isEmpty(propNames)){
            List<String> fieldNames = getFieldNames(bean.getClass());
            for (String fieldName: fieldNames) {
                Object value = getProperty(bean, fieldName);
                rtn.put(fieldName, value);
            }
        }else{
            for(String propName: propNames){
                Object value = getProperty(bean, propName);
                rtn.put(propName, value);
            }
        }
        return rtn;
    }

    /**
     * Map JavaBean 루프
     * @param map
     * @param clazz
     * @return
     */
    public static <T> T mapToBean(Map<String,?> map,Class<T> clazz){
        T bean = newInstance(clazz);
        for(Entry<String, ?> me:map.entrySet()){
            setProperty(bean, me.getKey(), me.getValue(), true);
        }
        return bean;
    }

    /**
     * 객체를 생성하기위한 매개 변수없이 반영
     * @param clazz
     * @return
     */
    public static <T> T newInstance(Class<T> clazz){
        return BeanUtils.instantiate(clazz);
    }

    /**
     * 개인 속성Value 얻기
     * @param bean
     * @param name
     * @return
     */
    public static Object getPrivateProperty(Object bean, String name){
        try {
            Field field = getField(bean.getClass(), name);
            if(field!=null){
                field.setAccessible(true);
                return field.get(bean);
            }else{
                throw new RuntimeException("The field [ "+field+"] in ["+bean.getClass().getName()+"] not exists");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 개인 속성Value 설정
     * @param bean
     * @param name
     * @param value
     */
    public static void setPrivateProperty(Object bean, String name, Object value){
        try {
            Field field = getField(bean.getClass(), name);
            if(field!=null){
                field.setAccessible(true);
                field.set(bean, value);
            }else{
                throw new RuntimeException("The field [ "+field+"] in ["+bean.getClass().getName()+"] not exists");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 필드 가져 오기
     * @param clazz
     * @param name
     * @return
     * @throws Exception
     */
    public static Field getField(Class<?> clazz,String name){
        return ReflectionUtils.findField(clazz, name);

    }

    /**
     * 필드 형의 취득
     * @param clazz
     * @param name
     * @return
     * @throws Exception
     */
    public static Class<?> getFieldType(Class<?> clazz,String name) {
        Field field = getField(clazz, name);
        if(field!=null){
            return field.getType();
        }else{
            throw new RuntimeException("Cannot locate field " + name + " on " + clazz);
        }
    }

    /**
     * @see #setProperty(Object, String, Object, boolean)
     * @param bean
     * @param name
     * @param value
     */
    public static void setProperty(Object bean, String name, Object value){
        setProperty(bean, name, value, false);
    }

    /**
     * Bean 속성 세트, 자동 변환 유형, 날짜 변환의 경우 long형 또는 long문자열 만
     * @param bean
     * @param name
     * @param value
     * @param ignoreError 무시 속성 오류를 찾을 수 없습니다.
     * @throws Exception
     */
    public static void setProperty(Object bean, String name, Object value, boolean ignoreError) {
        if (value != null) {
            try {
                Class<?> type = getPropertyType(bean, name);
                if (type != null) {
                    if (!value.getClass().equals(type)) {
                        if (TypeUtils.isAssignable(Date.class, type) && value instanceof String) {
                            try {
                                value = new Date(Long.parseLong((String) value));
                            } catch (NumberFormatException ignore) {}
                        } else {
                            //문자열을 숫자 값으로 변환하는 경우 숫자 값을 제거하십시오.,
                            if(TypeUtils.isAssignable(Number.class, type)){
                                value = StringUtils.deleteAny(value.toString(), ",");
                            }
                            value = ConvertUtils.convert(value, type);
                        }
                    }
                }
                PropertyUtils.setProperty(bean, name, value);
            } catch (NestedNullException e) {
                createNestedBean(bean, name);
                setProperty(bean, name, value, ignoreError);
            } catch (NoSuchMethodException e) {
                if (!ignoreError)
                    throw new RuntimeException(e);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * NestedNullException을 무시하고 속성 가져 오기
     *
     * @param bean
     * @param name
     * @return
     */
    public static Object getProperty(Object bean, String name){
        try {
            return PropertyUtils.getProperty(bean, name);
        } catch (NestedNullException ignore) {
            return null;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * NestedNullException를 무시하고 속성 유형을 가져옵니다.
     *
     * @param bean
     * @param name
     * @return
     */
    public static Class<?> getPropertyType(Object bean, String name) {
        try {
            return PropertyUtils.getPropertyType(bean, name);
        } catch (NestedNullException ignore) {
            return null;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 포함 된 개체 만들기
     *
     * @param bean
     * @param name
     */
    private static void createNestedBean(Object bean, String name){
        String[] names = name.split("[.]");
        if (names.length == 1)
            return;
        try {
            StringBuilder nestedName = new StringBuilder();
            for (int i = 0; i < names.length - 1; i++) {
                String n = names[i];
                if (i > 0) {
                    nestedName.append("." + n);
                } else {
                    nestedName.append(n);
                }
                Object val = PropertyUtils.getProperty(bean, nestedName.toString());
                if (val == null) {
                    PropertyUtils.setProperty(bean, nestedName.toString(),
                            PropertyUtils.getPropertyType(bean, nestedName.toString()).newInstance());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * n 개의 클래스, 동일한 부모 클래스 유형, 동일한 부모 클래스 중 둘 이상인 경우 가장 가까운 클래스를 가져오고,
     * 건네받은 Object가 Object.class를 직접 포함하고있는 경우는 null를 돌려줍니다.
     * @param clazzs
     * @return 같은 부모 클래스
     */
    public static Class<?> getEqSuperClass(Class<?> ...clazzs){
        Validate.notEmpty(clazzs);
        List<List<Class<?>>> container = new ArrayList<List<Class<?>>>(clazzs.length);
        for(Class<?>clazz :clazzs){
            if(clazz==Object.class)return null;
            List<Class<?>> superClazz = new ArrayList<Class<?>>(5);
            for(clazz=clazz.getSuperclass();clazz!=Object.class;clazz=clazz.getSuperclass()){
                superClazz.add(clazz);
            }
            container.add(superClazz);
        }
        List<Class<?>> result = new ArrayList<Class<?>>(5);
        Iterator<List<Class<?>>> it = container.iterator();
        int len =0;
        while(it.hasNext()){
            if(len == 0){
                result.addAll(it.next());
            }else{
                result.retainAll(it.next());
                if(CollectionUtils.isEmpty(result)){
                    break;
                }
            }
            len++;
        }
        //같은 부모에 관계없이 가장 가까운 것을 반환하는 몇 가지가 있습니다.
        if(CollectionUtils.isNotEmpty(result)){
            return result.get(0);
        }
        return Object.class;
    }

}
