package cn.com.dbsync.util;

/**
 * JAVA BEAN 的工具类,ADDED BY LHY 200703211710
 */
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 */
public class JavaBeanClass {

    private static transient final Log log =
            LogFactory.getLog(JavaBeanClass.class.getName());
    private static boolean cacheEnabled = true;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Set SIMPLE_TYPE_SET = new HashSet();
    private static final Map CLASS_INFO_MAP = Collections.synchronizedMap(new
            HashMap());
    private static final Object[] NO_ARGUMENTS = new Object[0];
    private String className;
    private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
    private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;
    private HashMap setMethods = new HashMap();
    private HashMap getMethods = new HashMap();
    private HashMap setTypes = new HashMap();
    private HashMap getTypes = new HashMap();

    static {
        SIMPLE_TYPE_SET.add(String.class);
        SIMPLE_TYPE_SET.add(Byte.class);
        SIMPLE_TYPE_SET.add(Short.class);
        SIMPLE_TYPE_SET.add(Character.class);
        SIMPLE_TYPE_SET.add(Integer.class);
        SIMPLE_TYPE_SET.add(Long.class);
        SIMPLE_TYPE_SET.add(Float.class);
        SIMPLE_TYPE_SET.add(Double.class);
        SIMPLE_TYPE_SET.add(Boolean.class);
        SIMPLE_TYPE_SET.add(Date.class);
        SIMPLE_TYPE_SET.add(Class.class);
        SIMPLE_TYPE_SET.add(BigInteger.class);
        SIMPLE_TYPE_SET.add(BigDecimal.class);

        SIMPLE_TYPE_SET.add(Collection.class);
        SIMPLE_TYPE_SET.add(Set.class);
        SIMPLE_TYPE_SET.add(Map.class);
        SIMPLE_TYPE_SET.add(List.class);
        SIMPLE_TYPE_SET.add(HashMap.class);
        SIMPLE_TYPE_SET.add(TreeMap.class);
        SIMPLE_TYPE_SET.add(ArrayList.class);
        SIMPLE_TYPE_SET.add(LinkedList.class);
        SIMPLE_TYPE_SET.add(HashSet.class);
        SIMPLE_TYPE_SET.add(TreeSet.class);
        SIMPLE_TYPE_SET.add(Vector.class);
        SIMPLE_TYPE_SET.add(Hashtable.class);
        SIMPLE_TYPE_SET.add(Enumeration.class);
    }

    private JavaBeanClass(Class clazz) {
        className = clazz.getName();
        addMethods(clazz);
        readablePropertyNames = (String[]) getMethods.keySet().toArray(new String[
                getMethods.keySet().size()]);
        writeablePropertyNames = (String[]) setMethods.keySet().toArray(new String[
                setMethods.keySet().size()]);
    }

    private void addMethods(Class cls) {
        Method[] methods = getAllMethodsForClass(cls);
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            if (name.startsWith("set") && name.length() > 3) {
                if (methods[i].getParameterTypes().length == 1) {
                    name = dropCase(name);
                    if (setMethods.containsKey(name)) {
                        // TODO(JGB) - this should probably be a RuntimeException at some point???
                        log.error("Illegal overloaded setter method for property " + name +
                                " in class " + cls.getName() +
                                ".  This breaks the JavaBeans specification and can cause unpredicatble results.");
                    }
                    setMethods.put(name, methods[i]);
                    setTypes.put(name, methods[i].getParameterTypes()[0]);
                }
            }
            else if (name.startsWith("get") && name.length() > 3) {
                if (methods[i].getParameterTypes().length == 0) {
                    name = dropCase(name);
                    getMethods.put(name, methods[i]);
                    getTypes.put(name, methods[i].getReturnType());
                }
            }
            else if (name.startsWith("is") && name.length() > 2) {
                if (methods[i].getParameterTypes().length == 0) {
                    name = dropCase(name);
                    getMethods.put(name, methods[i]);
                    getTypes.put(name, methods[i].getReturnType());
                }
            }
            name = null;
        }
    }

    private Method[] getAllMethodsForClass(Class cls) {
        if (cls.isInterface()) {
            // interfaces only have public methods - so the
            // simple call is all we need (this will also get superinterface methods)
            return cls.getMethods();
        }
        else {
            // need to get all the declared methods in this class
            // and any super-class - then need to set access appropriatly
            // for private methods
            return getClassMethods(cls);
        }
    }

    /**
     * This method returns an array containing all methods
     * declared in this class and any superclass.
     * We use this method, instead of the simpler Class.getMethods(),
     * because we want to look for private methods as well.
     *
     * @param cls
     * @return
     */
    private Method[] getClassMethods(Class cls) {
        HashMap uniqueMethods = new HashMap();
        Class currentClass = cls;
        while (currentClass != null) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // we also need to look for interface methods -
            // because the class may be abstract
            Class[] interfaces = currentClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                addUniqueMethods(uniqueMethods, interfaces[i].getMethods());
            }

            currentClass = currentClass.getSuperclass();
        }

        Collection methods = uniqueMethods.values();

        return (Method[]) methods.toArray(new Method[methods.size()]);
    }

    private void addUniqueMethods(HashMap uniqueMethods, Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            Method currentMethod = methods[i];
            String signature = getSignature(currentMethod);
            // check to see if the method is already known
            // if it is known, then an extended class must have
            // overridden a method
            if (!uniqueMethods.containsKey(signature)) {
                if (canAccessPrivateMethods()) {
                    try {
                        currentMethod.setAccessible(true);
                    }
                    catch (Exception e) {
                        // Ignored. This is only a final precaution, nothing we can do.
                    }
                }

                uniqueMethods.put(signature, currentMethod);
            }
        }
    }

    private String getSignature(Method method) {
        StringBuffer sb = new StringBuffer();
        sb.append(method.getName());
        Class[] parameters = method.getParameterTypes();

        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(':');
            }
            else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }

        return sb.toString();
    }

    private boolean canAccessPrivateMethods() {
        try {
            System.getSecurityManager().checkPermission(new ReflectPermission(
                    "suppressAccessChecks"));
            return true;
        }
        catch (SecurityException e) {
            return false;
        }
        catch (NullPointerException e) {
            return true;
        }
    }

    private static String dropCase(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        }
        else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        }
        else {
            throw new RuntimeException("Error parsing property name '" + name +
                    "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (name.length() == 1 ||
                (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
        }

        return name;
    }

    /**
     * Gets the name of the class the instance provides information for
     *
     * @return The class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the setter for a property as a Method object
     *
     * @param propertyName - the property
     * @return The Method
     */
    public Method getSetter(String propertyName) {
        for (Iterator i = setMethods.keySet().iterator(); i.hasNext(); ) {
            String temp = (String) i.next();
            if (temp.equalsIgnoreCase(propertyName)) {
                return (Method) setMethods.get(temp);
            }
        }
        return null;
    }

    /**
     * Gets the getter for a property as a Method object
     *
     * @param propertyName - the property
     * @return The Method
     */
    public Method getGetter(String propertyName) {
        for (Iterator i = getMethods.keySet().iterator(); i.hasNext(); ) {
            String temp = (String) i.next();
            if (temp.equalsIgnoreCase(propertyName)) {
                return (Method) getMethods.get(temp);
            }
        }
        return null;
    }

    /**
     * Gets the type for a property setter
     *
     * @param propertyName - the name of the property
     * @return The Class of the propery setter
     */
    public Class getSetterType(String propertyName) {
        for (Iterator i = setTypes.keySet().iterator(); i.hasNext(); ) {
            String temp = (String) i.next();
            if (temp.equalsIgnoreCase(propertyName)) {
                return (Class) setTypes.get(temp);
            }
        }
        return null;
    }

    /**
     * Gets the type for a property getter
     *
     * @param propertyName - the name of the property
     * @return The Class of the propery getter
     */
    public Class getGetterType(String propertyName) {
        for (Iterator i = getTypes.keySet().iterator(); i.hasNext(); ) {
            String temp = (String) i.next();
            if (temp.equalsIgnoreCase(propertyName)) {
                return (Class) getTypes.get(temp);
            }
        }
        return null;
    }

    /**
     * Gets an array of the readable properties for an object
     *
     * @return The array
     */
    public String[] getReadablePropertyNames() {
        return readablePropertyNames;
    }

    /**
     * Gets an array of the writeable properties for an object
     *
     * @return The array
     */
    public String[] getWriteablePropertyNames() {
        return writeablePropertyNames;
    }

    /**
     * Check to see if a class has a writeable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a writeable property by the name
     */
    public boolean hasWritableProperty(String propertyName) {
        return setMethods.keySet().contains(propertyName);
    }

    /**
     * Check to see if a class has a readable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a readable property by the name
     */
    public boolean hasReadableProperty(String propertyName) {
        return getMethods.keySet().contains(propertyName);
    }

    /**
     * Tells us if the class passed in is a knwon common type
     *
     * @param clazz The class to check
     * @return True if the class is known
     */
    public static boolean isKnownType(Class clazz) {
        if (SIMPLE_TYPE_SET.contains(clazz)) {
            return true;
        }
        else if (Collection.class.isAssignableFrom(clazz)) {
            return true;
        }
        else if (Map.class.isAssignableFrom(clazz)) {
            return true;
        }
        else if (List.class.isAssignableFrom(clazz)) {
            return true;
        }
        else if (Set.class.isAssignableFrom(clazz)) {
            return true;
        }
        else if (Iterator.class.isAssignableFrom(clazz)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Gets an instance of ClassInfo for the specified class.
     *
     * @param clazz The class for which to lookup the method cache.
     * @return The method cache for the class
     */
    public static JavaBeanClass getInstance(Class clazz) {
        if (cacheEnabled) {
            synchronized (clazz) {
                JavaBeanClass cache = (JavaBeanClass) CLASS_INFO_MAP.get(clazz);
                if (cache == null) {
                    cache = new JavaBeanClass(clazz);
                    CLASS_INFO_MAP.put(clazz, cache);
                }
                return cache;
            }
        }
        else {
            return new JavaBeanClass(clazz);
        }
    }

    public static void setCacheEnabled(boolean cacheEnabled) {
        JavaBeanClass.cacheEnabled = cacheEnabled;
    }

    public static Object getProperty(Object object, String name) throws
            Exception {
        JavaBeanClass classCache = JavaBeanClass.getInstance(object.getClass());
        Object value = null;
        Method method = classCache.getGetter(name);
        if (method == null) {
            throw new NoSuchMethodException("No GET method for property " + name +
                    " on instance of " +
                    object.getClass().getName());
        }
        value = method.invoke(object, NO_ARGUMENTS);
        return value;
    }

    public static void setProperty(Object object, String name, Object value) throws
            Exception {
        JavaBeanClass classCache = JavaBeanClass.getInstance(object.getClass());
        Method method = classCache.getSetter(name);
        if (method == null) {
            throw new NoSuchMethodException("No SET method for property " +
                    name + " on instance of " +
                    object.getClass().getName());
        }
        Object[] params = new Object[1];
        Class trueType = classCache.getSetterType(name);
        params[0] = transToTrueType(trueType, value);
        method.invoke(object, params);
    }

    private static Object transToTrueType(Class clz, Object value) {
        if (clz.isArray()) {//特别是针对BLOB字段处理,是返回byte[]类型数据
            if (value instanceof byte[]) {
                return value;
            }else{
                return String.valueOf(value).getBytes();
            }
        }
        String clName = clz.getName();
        if (clName.equals("java.lang.String")) {
            return String.valueOf(value);
        }
        else if (clName.equalsIgnoreCase("int") || clName.equals("java.lang.Integer")) {
            return Integer.valueOf(String.valueOf(value));
        }
        else if (clName.equalsIgnoreCase("short") || clName.equals("java.lang.Short")) {
            return Short.valueOf(String.valueOf(value));
        }
        else if (clName.equalsIgnoreCase("long") || clName.equals("java.lang.Long")) {
            return Long.valueOf(String.valueOf(value));
        }
        else if (clName.equalsIgnoreCase("float") || clName.equals("java.lang.Float")) {
            return Float.valueOf(String.valueOf(value));
        }
        else if (clName.equalsIgnoreCase("double") || clName.equals("java.lang.Double")) {
            return Double.valueOf(String.valueOf(value));
        }
        else if (clName.equals("java.lang.BigInteger")) {
            new BigInteger(String.valueOf(value));
        }
        else if (clName.equals("java.lang.BigDecimal")) {
            new BigDecimal(String.valueOf(value));
        }
        else if (clName.equalsIgnoreCase("boolean") || clName.equals("java.lang.Boolean")) {
            return Boolean.valueOf(StringConverter.getBoolean(String.valueOf(value)));
        }
        return value;
    }



}
