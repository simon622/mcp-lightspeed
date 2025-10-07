package org.slj.lightspeed.mcp.utilities;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slj.lightspeed.mcp.model.McpTool;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Scans the classpath for methods annotated with @McpTool.
 */
public final class McpToolScanner {

    private McpToolScanner() {
        // static utility
    }

    /**
     * Scans all packages on the classpath for @McpTool methods.
     */
    public static List<McpToolMethod> findAll() {
        return findAll("");
    }

    /**
     * Scans a specific base package (and subpackages) for @McpTool methods.
     *
     * @param basePackage the root package to start scanning (e.g. "com.example")
     * @return list of discovered MCP tool methods
     */
    public static List<McpToolMethod> findAll(String basePackage) {
        Reflections reflections = new Reflections(basePackage, Scanners.MethodsAnnotated);
        Set<Method> methods = reflections.getMethodsAnnotatedWith(McpTool.class);

        List<McpToolMethod> list = new ArrayList<>();
        for (Method method : methods) {
            McpTool ann = method.getAnnotation(McpTool.class);
            list.add(new McpToolMethod(
                    ann.name(),
                    ann.description(),
                    ann.category(),
                    ann.enabled(),
                    method.getDeclaringClass(),
                    method
            ));
        }
        return list;
    }

    /**
     * Simple record-like holder for discovered method metadata.
     */
    public static class McpToolMethod {
        public final String name;
        public final String description;
        public final String category;
        public final boolean enabled;
        public final Class<?> declaringClass;
        public final Method method;

        public McpToolMethod(String name, String description, String category,
                             boolean enabled, Class<?> declaringClass, Method method) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.enabled = enabled;
            this.declaringClass = declaringClass;
            this.method = method;
        }

        @Override
        public String toString() {
            return String.format("%s.%s()  [name='%s', desc='%s']",
                    declaringClass.getSimpleName(), method.getName(), name, description);
        }
    }
}