package es.cristichi.fnac.io;

import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.*;
import org.yaml.snakeyaml.util.PlatformFeatureDetector;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Edit for {@link PropertyUtils} that makes sure that properties are ordered so that the {@link Settings} file
 * is always ordered the same way, for better human reading.
 */
public class OrderedPropertyUtils extends PropertyUtils {
    private final Map<Class<?>, Map<String, Property>> propertiesCache = new HashMap<>();
    private final Map<Class<?>, Set<Property>> readableProperties = new HashMap<>();
    private BeanAccess beanAccess = BeanAccess.DEFAULT;
    private boolean allowReadOnlyProperties = false;
    private boolean skipMissingProperties = false;

    private final PlatformFeatureDetector platformFeatureDetector;
    
    /**
     * Creates the {@link PropertyUtils} with a new instance of {@link PlatformFeatureDetector}.
     */
    public OrderedPropertyUtils() {
        this(new PlatformFeatureDetector());
    }

    OrderedPropertyUtils(PlatformFeatureDetector platformFeatureDetector) {
        this.platformFeatureDetector = platformFeatureDetector;

        /*
         * Android lacks much of java.beans (including the Introspector class, used here), because java.beans classes tend to rely on java.awt, which isn't
         * supported in the Android SDK. That means we have to fall back on FIELD access only when SnakeYAML is running on the Android Runtime.
         */
        if (platformFeatureDetector.isRunningOnAndroid()) {
            beanAccess = BeanAccess.FIELD;
        }
    }

    protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) {
        if (propertiesCache.containsKey(type)) {
            return propertiesCache.get(type);
        }

        Map<String, Property> properties = new LinkedHashMap<>();
        boolean inaccessableFieldsExist = false;
        if (Objects.requireNonNull(bAccess) == BeanAccess.FIELD) {
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)
                            && !properties.containsKey(field.getName())) {
                        properties.put(field.getName(), new FieldProperty(field));
                    }
                }
            }
        } else {// add JavaBean properties
            try {
                for (PropertyDescriptor property : Introspector.getBeanInfo(type)
                        .getPropertyDescriptors()) {
                    Method readMethod = property.getReadMethod();
                    if ((readMethod == null || !readMethod.getName().equals("getClass"))
                            && !isTransient(property)) {
                        properties.put(property.getName(), new MethodProperty(property));
                    }
                }
            } catch (IntrospectionException e) {
                throw new YAMLException(e);
            }

            // add public fields
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                        if (Modifier.isPublic(modifiers)) {
                            properties.put(field.getName(), new FieldProperty(field));
                        } else {
                            inaccessableFieldsExist = true;
                        }
                    }
                }
            }
        }
        if (properties.isEmpty() && inaccessableFieldsExist) {
            throw new YAMLException("No JavaBean properties found in " + type.getName());
        }
        propertiesCache.put(type, properties);
        return properties;
    }

    private static final String TRANSIENT = "transient";

    private boolean isTransient(FeatureDescriptor fd) {
        return Boolean.TRUE.equals(fd.getValue(TRANSIENT));
    }

    public Set<Property> getProperties(Class<?> type) {
        return getProperties(type, beanAccess);
    }

    public Set<Property> getProperties(Class<?> type, BeanAccess bAccess) {
        if (readableProperties.containsKey(type)) {
            return readableProperties.get(type);
        }
        Set<Property> properties = createPropertySet(type, bAccess);
        readableProperties.put(type, properties);
        return properties;
    }

    protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
        Set<Property> properties = new LinkedHashSet<>();
        Collection<Property> props = getPropertiesMap(type, bAccess).values();
        for (Property property : props) {
            if (property.isReadable() && (allowReadOnlyProperties || property.isWritable())) {
                properties.add(property);
            }
        }
        return properties;
    }

    public Property getProperty(Class<?> type, String name) {
        return getProperty(type, name, beanAccess);
    }

    public Property getProperty(Class<?> type, String name, BeanAccess bAccess) {
        Map<String, Property> properties = getPropertiesMap(type, bAccess);
        Property property = properties.get(name);
        if (property == null && skipMissingProperties) {
            property = new MissingProperty(name);
        }
        if (property == null) {
            throw new YAMLException(
                    "Unable to find property '" + name + "' on class: " + type.getName());
        }
        return property;
    }

    public void setBeanAccess(BeanAccess beanAccess) {
        if (platformFeatureDetector.isRunningOnAndroid() && beanAccess != BeanAccess.FIELD) {
            throw new IllegalArgumentException(
                    "JVM is Android - only BeanAccess.FIELD is available");
        }

        if (this.beanAccess != beanAccess) {
            this.beanAccess = beanAccess;
            propertiesCache.clear();
            readableProperties.clear();
        }
    }

    public void setAllowReadOnlyProperties(boolean allowReadOnlyProperties) {
        if (this.allowReadOnlyProperties != allowReadOnlyProperties) {
            this.allowReadOnlyProperties = allowReadOnlyProperties;
            readableProperties.clear();
        }
    }

    public boolean isAllowReadOnlyProperties() {
        return allowReadOnlyProperties;
    }

    /**
     * Skip properties that are missing during deserialization of YAML to a Java
     * object. The default is false.
     *
     * @param skipMissingProperties true if missing properties should be skipped, false otherwise.
     */
    public void setSkipMissingProperties(boolean skipMissingProperties) {
        if (this.skipMissingProperties != skipMissingProperties) {
            this.skipMissingProperties = skipMissingProperties;
            readableProperties.clear();
        }
    }

    public boolean isSkipMissingProperties() {
        return skipMissingProperties;
    }
}