package com.oim.core.common.box;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: XiaHui
 * @date: 2017年5月26日 下午3:05:58
 */
public class TestPMDOne {

	static Map<String,Map<String,String>> map=new HashMap<String,Map<String,String>>();
	static{
		
	}
	public static String getValueByName(String property,String name){
		String value=null;
		Map<String,String> vm=map.get(property);
		if(null!=vm){
		}
	}

     public void sampleEmptySynchronizedBlock(){
        synchronized (this) { //高危：EmptySynchronizedBlock
            // empty!
        }
    }

    public static Map<String, Field> getBeanFieldMap(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        if (beaninfoMap == null) {
            synchronized (BeanAccessor.class) {
                if (beaninfoMap == null) {
                    beaninfoMap = new HashMap<Class<?>, Map<String, Field>>();
                }
            }
        }

        if (beaninfoMap == null) {
            return null;
        }

        Map<String, Field> fieldMap = beaninfoMap.get(clazz);
        if (fieldMap == null) {
            synchronized (clazz) {
                if ((fieldMap = beaninfoMap.get(clazz)) == null) {
                    Field[] fields = clazz.getDeclaredFields();
                    fieldMap = new LinkedHashMap<String, Field>(fields.length);
                    for (Field field : fields) {
                        fieldMap.put(field.getName(), field);
                    }
                    beaninfoMap.put(clazz, fieldMap);
                }
            }
        }
        return fieldMap;
    }

    public static long getBeanFieldOffset(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return -1;
        }
        if (beanFieldOffsetMap == null) {
            synchronized (BeanAccessor.class) {
                if (beanFieldOffsetMap == null) {
                    beanFieldOffsetMap = new HashMap<Class<?>, Map<String, Long>>();
                }
            }
        }

        Map<String, Long> offsetMap = beanFieldOffsetMap.get(clazz);
        if (offsetMap == null) {
            synchronized (clazz) {
                if ((offsetMap = beanFieldOffsetMap.get(clazz)) == null) {
                    Map<String, Field> fieldMap = getBeanFieldMap(clazz);
                    offsetMap = new HashMap<String, Long>(fieldMap.size());
                    for (String key : fieldMap.keySet()) {
                        Field field = fieldMap.get(key);
                        String modifiers = Modifier.toString(field.getModifiers());
                        if (field.getName().equals(SERIAL_VERSION_UID) || modifiers.contains(FINAL) || modifiers.contains(STATIC)) {
                            continue;
                        }
                        long offset = getUnsafeInstance().objectFieldOffset(field);
                        offsetMap.put(key, offset);
                    }
                    beanFieldOffsetMap.put(clazz, offsetMap);
                }
            }
        }

        Long result = offsetMap == null ? null : offsetMap.get(fieldName);
        return result == null ? -1 : result;
    }

    public static Object getBeanValue(Object bean, String fieldName) {
        if (bean == null || fieldName == null) {
            return null;
        }
        Class<?> clazz = bean.getClass();

        Map<String, Field> fieldMap = getBeanFieldMap(clazz);
        Field field = fieldMap.get(fieldName);
        Unsafe unsafe = getUnsafeInstance();
        if (unsafe == null) {
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field.get(bean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long offset = getBeanFieldOffset(clazz, fieldName);
        if (offset == -1) {
            return null;
        }

        Class<?> type = field.getType();
        if (type == boolean.class) {
            return unsafe.getBoolean(bean, offset);
        } else if (type == byte.class) {
            return unsafe.getByte(bean, offset);
        } else if (type == short.class) {
            return unsafe.getShort(bean, offset);
        } else if (type == char.class) {
            return unsafe.getChar(bean, offset);
        } else if (type == int.class) {
            return unsafe.getInt(bean, offset);
        } else if (type == long.class) {
            return unsafe.getLong(bean, offset);
        } else if (type == float.class) {
            return unsafe.getFloat(bean, offset);
        } 
        return unsafe.getObject(bean, offset);

    }

    public static void setBeanValue(Object bean, String fieldName, Object data) {
        if (bean == null || fieldName == null) {
            return;
        }

        Class<?> clazz = bean.getClass();

        Map<String, Field> fieldMap = getBeanFieldMap(clazz);
        Field field = fieldMap.get(fieldName);
        Unsafe unsafe = getUnsafeInstance();
        if (unsafe == null) {
            try {
                field.set(bean, data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long offset = getBeanFieldOffset(clazz, fieldName);
        if (offset == -1) {
            return;
        }

        Class<?> type = field.getType();
        if (type == boolean.class) {
            unsafe.putBoolean(bean, offset, ((Boolean) data).booleanValue());
        } else {
            unsafe.putObject(bean, offset, data);
        }
    }

}
