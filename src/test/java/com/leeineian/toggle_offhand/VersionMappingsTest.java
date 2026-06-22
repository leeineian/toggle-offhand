package com.leeineian.toggle_offhand;

import com.leeineian.toggle_offhand.compat.VersionMappings;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VersionMappingsTest {
    @Test
    public void testReflectionMappings() {
        // Bootstrap Minecraft registries to avoid "Not bootstrapped" exception
        try {
            Class<?> sharedConstantsClass = Class.forName("net.minecraft.SharedConstants");
            java.lang.reflect.Method tryStart = sharedConstantsClass.getMethod("tryStartWithCrash");
            tryStart.invoke(null);
            
            Class<?> bootstrapClass = Class.forName("net.minecraft.server.Bootstrap");
            java.lang.reflect.Method bootStrap = bootstrapClass.getMethod("bootStrap");
            bootStrap.invoke(null);
        } catch (Throwable t) {
            try {
                Class<?> sharedConstantsClass = Class.forName("net.minecraft.class_151");
                java.lang.reflect.Method tryStart = sharedConstantsClass.getMethod("method_16673");
                tryStart.invoke(null);
                
                Class<?> bootstrapClass = Class.forName("net.minecraft.class_2966");
                java.lang.reflect.Method bootStrap = bootstrapClass.getMethod("method_12851");
                bootStrap.invoke(null);
            } catch (Throwable ignored) {
            }
        }

        // Initialize version mappings
        VersionMappings.init();
        
        // Assert that mappings did not report errors in the compile-time environment
        assertFalse(VersionMappings.hasErrors(), "One or more version mappings failed to resolve in the compile-time environment!");
    }
}
