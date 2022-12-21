package dependency_injection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

/**
 * TODO you should complete the class
 */
public class BeanFactoryImpl implements BeanFactory {

    private final Map<String, String> valueMap = new HashMap<>();
    private final Map<Class<?>, Class<?>> injectMap = new HashMap<>();

    @Override
    public void loadInjectProperties(File file) {
        Properties properties = new Properties();
        injectMap.clear();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                injectMap.put(Class.forName((String) entry.getKey()), Class.forName((String) entry.getValue()));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadValueProperties(File file) {
        Properties properties = new Properties();
        valueMap.clear();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                valueMap.put((String) entry.getKey(), (String) entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T createInstance(Class<T> clazz) {
        try {
            Class<?> implementClazz = injectMap.getOrDefault(clazz, clazz);
            Constructor<?> constructor = getConstructorAnnotatedWithInject(implementClazz);
            Parameter[] parameters = constructor.getParameters();
            Object[] paramValues = new Object[parameters.length];
            initConstructorParameters(parameters, paramValues);
            Object instance = constructor.newInstance(paramValues);
            injectFields(instance);
            //noinspection unchecked
            return (T) instance;
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException ignored) {
        }
        return null;
    }

    private void initConstructorParameters(Parameter[] parameters, Object[] paramValues) throws ClassNotFoundException {
        for (int i = 0; i < paramValues.length; i++) {
            Value value = parameters[i].getAnnotation(Value.class);
            if (value != null) {
                Type[] genericTypes = null;
                try {
                    genericTypes = (((ParameterizedType) parameters[i].getParameterizedType()).getActualTypeArguments());
                } catch (ClassCastException ignored) {}
                paramValues[i] = injectValue(value, parameters[i].getType(), genericTypes);
            } else {
                paramValues[i] = createInstance(parameters[i].getType());
            }
        }
    }

    private Object injectValue(Value value, Class<?> type, Type[] genericType) throws ClassNotFoundException {
        String[] values = valueMap.getOrDefault(value.value(), value.value()).split(value.delimiter());
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].replaceAll("[\\[\\]{}]", "");
        }

        List<String> temp = Arrays.stream(values).filter(v -> !v.equals("")).toList();
        values = new String[temp.size()];
        temp.toArray(values);
        return injectValue(type, values, genericType, true);
    }

    private Object injectValue(Class<?> type, String[] values, Type[] genericType, boolean useDefault) throws ClassNotFoundException {
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return injectBoolean(values, useDefault);
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return injectInteger(values, useDefault);
        } else if (type.equals(String.class)) {
            return injectString(values);
        } else if (type.equals(boolean[].class) || type.equals(Boolean[].class)) {
            return injectBooleanArray(values);
        } else if (type.equals(int[].class) || type.equals(Integer[].class)) {
            return injectIntegerArray(values);
        } else if (type.equals(String[].class)) {
            return injectStringArray(values);
        } else if (type.equals(List.class)) {
            return injectList(values, Class.forName(genericType[0].getTypeName()));
        } else if (type.equals(Set.class)) {
            return injectSet(values, Class.forName(genericType[0].getTypeName()));
        } else if (type.equals(Map.class)) {
            return injectMap(values, Class.forName(genericType[0].getTypeName()), Class.forName(genericType[1].getTypeName()));
        } else {
            throw new RuntimeException(String.format("Unsupported injected value type %s", type.getTypeName()));
        }
    }

    private Constructor<?> getConstructorAnnotatedWithInject(Class<?> clazz) throws NoSuchMethodException {
        List<Constructor<?>> constructors = Arrays
                .stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .toList();
        return constructors.size() == 1 ? constructors.get(0) : clazz.getDeclaredConstructor();
    }

    private Object injectBoolean(String[] values, boolean useDefault) {
        for (String val : values) {
            if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false"))
                return java.lang.Boolean.parseBoolean(val);
        }
        if (useDefault) return false;
        return null;
    }

    private Object injectInteger(String[] values, boolean useDefault) {
        for (String val : values) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
            }
        }
        if (useDefault) return 0;
        return null;
    }

    private Object injectString(String[] values) {
        if (values.length == 0) return "";
        return values[0];
    }

    private boolean[] injectBooleanArray(String[] values) {
        List<Boolean> booleanList = new LinkedList<>();
        for (String ele : values) {
            Object bool = injectBoolean(new String[]{ele}, false);
            if (bool != null)
                booleanList.add((Boolean) bool);
        }
        boolean[] ret = new boolean[booleanList.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = booleanList.get(i);
        }
        return ret;
    }

    private int[] injectIntegerArray(String[] values) {
        List<Integer> integerList = new LinkedList<>();
        for (String ele : values) {
            Object integer = injectInteger(new String[]{ele}, false);
            if (integer != null)
                integerList.add((Integer) integer);
        }
        int[] ret = new int[integerList.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = integerList.get(i);
        }
        return ret;
    }

    private String[] injectStringArray(String[] values) {
        List<String> stringList = new LinkedList<>();
        for (String ele : values) {
            Object string = injectString(new String[]{ele});
            stringList.add((String) string);
        }
        String[] ret = new String[stringList.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = stringList.get(i);
        }
        return ret;
    }

    private Object injectList(String[] values, Class<?> genericType) throws ClassNotFoundException {
        List<Object> ret = new LinkedList<>();
        for (String value : values) {
            Object injectValue = injectValue(genericType, new String[]{value}, null, false);
            if (injectValue != null)
                ret.add(injectValue);
        }
        return ret;
    }

    private Object injectSet(String[] values, Class<?> genericType) throws ClassNotFoundException {
        Set<Object> ret = new HashSet<>();
        for (String value : values) {
            Object injectValue = injectValue(genericType, new String[]{value}, null, false);
            if (injectValue != null)
                ret.add(injectValue);
        }
        return ret;
    }

    private Object injectMap(String[] values, Class<?> keyType, Class<?> valueType) throws ClassNotFoundException {
        Map<Object, Object> ret = new HashMap<>();
        for (String value : values) {
            String[] keyAndValue = value.split(":");
            Object keyObj = injectValue(keyType, new String[]{keyAndValue[0]}, null, false);
            Object valueObj = injectValue(valueType, new String[]{keyAndValue[1]}, null, false);
            if (keyObj != null && valueObj != null)
                ret.put(keyObj, valueObj);
        }
        return ret;
    }

    private <T> void injectFields(T instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    field.set(instance, createInstance(field.getType()));
                    field.setAccessible(false);
                } else if (field.isAnnotationPresent(Value.class)) {
                    field.setAccessible(true);
                    Type[] genericTypes = null;
                    try {
                        genericTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                    } catch (ClassCastException ignored) {}
                    field.set(instance, injectValue(field.getAnnotation(Value.class), field.getType(), genericTypes));
                    field.setAccessible(false);
                }
            }
        } catch (IllegalAccessException | ClassNotFoundException ignored) {
        }
    }
}
