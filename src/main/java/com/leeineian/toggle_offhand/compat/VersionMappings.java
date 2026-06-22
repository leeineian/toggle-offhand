package com.leeineian.toggle_offhand.compat;

import com.leeineian.toggle_offhand.ToggleOffhand;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class VersionMappings {
    // Classes
    public static Class<?> KeyMappingClass;
    public static Class<?> InputConstantsClass;
    public static Class<?> InputConstantsTypeClass;
    public static Class<?> BuiltInRegistriesClass;
    public static Class<?> LanguageClass;
    public static Class<?> ComponentClass;
    public static Class<?> MinecraftClass;
    
    // Methods
    public static Method GetKeyMethod; // InputConstants.getKey(int, int) or method_15984
    public static Method GetKeyFromStringMethod; // InputConstants.getKey(String) or method_15981
    public static Method GetItemRegistryKeyMethod; // Registry.getKey
    public static Method GetMainHandItemMethod; // Entity.getMainHandItem or method_6047
    public static Method GetItemStackItemMethod; // ItemStack.getItem or method_7909
    public static Method IsItemStackEmptyMethod; // ItemStack.isEmpty or method_7960
    public static Method GetMainArmMethod; // Entity.getMainArm or method_6068
    public static Method GetOppositeArmMethod; // Arm.getOpposite or method_5928
    public static Method IsEntityInvisibleMethod; // Entity.isInvisible or method_5767
    
    // Fields
    public static Field ItemRegistryField; // BuiltInRegistries.ITEM or field_41178
    
    private static boolean initialized = false;
    private static boolean hasErrors = false;

    public static boolean hasErrors() {
        return hasErrors;
    }

    public static void init() {
        if (initialized) return;
        ToggleOffhand.LOGGER.info("Initializing version-safe mappings registry...");
        
        try {
            // Resolve Classes
            KeyMappingClass = findClass("net.minecraft.client.KeyMapping", "net.minecraft.class_304");
            
            InputConstantsClass = findClass("com.mojang.blaze3d.platform.InputConstants", "net.minecraft.class_3675");
            InputConstantsTypeClass = findClass("com.mojang.blaze3d.platform.InputConstants$Type", "net.minecraft.class_3675$class_307");
            
            BuiltInRegistriesClass = findClass("net.minecraft.core.registries.BuiltInRegistries", "net.minecraft.class_7923", "net.minecraft.class_7910");
            
            LanguageClass = findClass("net.minecraft.locale.Language", "net.minecraft.class_2477");
            ComponentClass = findClass("net.minecraft.network.chat.Component", "net.minecraft.class_2561");
            MinecraftClass = findClass("net.minecraft.client.Minecraft", "net.minecraft.class_310");

            // Resolve Methods & Fields
            if (InputConstantsClass != null) {
                GetKeyMethod = findMethod(InputConstantsClass, true, new String[]{"getKey", "method_15985", "m_84827_"}, int.class, int.class);
                GetKeyFromStringMethod = findMethod(InputConstantsClass, true, new String[]{"getKey", "method_15981", "m_84851_"}, String.class);
            }
            
            if (BuiltInRegistriesClass != null) {
                ItemRegistryField = findField(BuiltInRegistriesClass, true, new String[]{"ITEM", "field_41178", "f_256735_"});
                if (ItemRegistryField == null) {
                    // Try dynamic lookup by scanning fields of BuiltInRegistries
                    Class<?> itemClass = findClass("net.minecraft.world.item.Item", "net.minecraft.class_1792");
                    Class<?> resourceLocationClass = findClass("net.minecraft.resources.ResourceLocation", "net.minecraft.class_2960");
                    if (itemClass != null && resourceLocationClass != null) {
                        try {
                            Object airLocation = null;
                            try {
                                Constructor<?> rlConst = resourceLocationClass.getConstructor(String.class);
                                airLocation = rlConst.newInstance("minecraft:air");
                            } catch (Exception e) {
                                // 1.21+ fallback: ResourceLocation.parse(String)
                                Method parseMethod = resourceLocationClass.getMethod("parse", String.class);
                                airLocation = parseMethod.invoke(null, "minecraft:air");
                            }
                            
                            if (airLocation != null) {
                                for (Field f : BuiltInRegistriesClass.getDeclaredFields()) {
                                    if (Modifier.isStatic(f.getModifiers())) {
                                        f.setAccessible(true);
                                        Object registryObj = f.get(null);
                                        if (registryObj != null) {
                                            for (Method m : registryObj.getClass().getMethods()) {
                                                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == resourceLocationClass) {
                                                    try {
                                                        m.setAccessible(true);
                                                        Object got = m.invoke(registryObj, airLocation);
                                                        if (got != null && itemClass.isInstance(got)) {
                                                            ItemRegistryField = f;
                                                            ToggleOffhand.LOGGER.info("Dynamically resolved ItemRegistryField to field: " + f.getName());
                                                            break;
                                                        }
                                                    } catch (Exception ignored) {}
                                                }
                                            }
                                        }
                                    }
                                    if (ItemRegistryField != null) break;
                                }
                            }
                        } catch (Exception e) {
                            ToggleOffhand.LOGGER.error("Failed dynamic lookup of ITEM field: ", e);
                        }
                    }
                }
                if (ItemRegistryField == null) {
                    ToggleOffhand.LOGGER.warn("Failed to resolve field in net.minecraft.core.registries.BuiltInRegistries from targets: [ITEM, field_41178, f_256735_]");
                    hasErrors = true;
                }
            }

            Class<?> entityClass = findClass("net.minecraft.world.entity.LivingEntity", "net.minecraft.class_1309");
            if (entityClass != null) {
                GetMainHandItemMethod = findMethod(entityClass, new String[]{"getMainHandItem", "method_6047", "m_21205_"});
                GetMainArmMethod = findMethod(entityClass, new String[]{"getMainArm", "method_6068", "m_5737_"});
            }

            Class<?> itemStackClass = findClass("net.minecraft.world.item.ItemStack", "net.minecraft.class_1799");
            if (itemStackClass != null) {
                GetItemStackItemMethod = findMethod(itemStackClass, new String[]{"getItem", "method_7909", "m_41720_"});
                IsItemStackEmptyMethod = findMethod(itemStackClass, new String[]{"isEmpty", "method_7960", "m_41619_"});
            }

            Class<?> armClass = findClass("net.minecraft.world.entity.HumanoidArm", "net.minecraft.class_1306");
            if (armClass != null) {
                GetOppositeArmMethod = findMethod(armClass, new String[]{"getOpposite", "method_5928", "m_20828_"});
            }

            Class<?> baseEntityClass = findClass("net.minecraft.world.entity.Entity", "net.minecraft.class_1297");
            if (baseEntityClass != null) {
                IsEntityInvisibleMethod = findMethod(baseEntityClass, new String[]{"isInvisible", "method_5767", "m_20145_"});
            }

            // Perform self-test validation
            validateMappings();

        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Fatal error during version mapping initialization: ", e);
            hasErrors = true;
        } finally {
            initialized = true;
        }
    }

    private static Class<?> findClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name, false, VersionMappings.class.getClassLoader());
            } catch (Throwable t) {
                // Ignore and try next
            }
        }
        ToggleOffhand.LOGGER.warn("Failed to resolve class from targets: " + java.util.Arrays.toString(names));
        hasErrors = true;
        return null;
    }

    private static Method findMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) {
        return findMethod(clazz, false, names, parameterTypes);
    }

    private static Method findMethod(Class<?> clazz, boolean isOptional, String[] names, Class<?>... parameterTypes) {
        if (clazz == null) return null;
        for (String name : names) {
            try {
                Method m = clazz.getMethod(name, parameterTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                try {
                    Method m = clazz.getDeclaredMethod(name, parameterTypes);
                    m.setAccessible(true);
                    return m;
                } catch (NoSuchMethodException ex) {
                    // Try next name
                }
            }
        }
        if (!isOptional) {
            ToggleOffhand.LOGGER.warn("Failed to resolve method in " + clazz.getName() + " from targets: " + java.util.Arrays.toString(names));
            try {
                ToggleOffhand.LOGGER.info("Available methods in " + clazz.getName() + ":");
                for (Method m : clazz.getDeclaredMethods()) {
                    ToggleOffhand.LOGGER.info("  " + m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
                }
            } catch (Throwable t) {}
            hasErrors = true;
        } else {
            ToggleOffhand.LOGGER.info("Optional method not found in " + clazz.getName() + ": " + java.util.Arrays.toString(names));
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String[] names) {
        return findField(clazz, false, names);
    }

    private static Field findField(Class<?> clazz, boolean isOptional, String[] names) {
        if (clazz == null) return null;
        for (String name : names) {
            try {
                Field f = clazz.getField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ex) {
                    // Try searching all declared fields for matching type or name
                }
            }
        }
        
        // Final fallback: match by name ignoring visibility
        for (Field f : clazz.getDeclaredFields()) {
            for (String name : names) {
                if (f.getName().equals(name)) {
                    f.setAccessible(true);
                    return f;
                }
            }
        }
        
        if (!isOptional) {
            ToggleOffhand.LOGGER.warn("Failed to resolve field in " + clazz.getName() + " from targets: " + java.util.Arrays.toString(names));
            hasErrors = true;
        } else {
            ToggleOffhand.LOGGER.info("Optional field not found in " + clazz.getName() + ": " + java.util.Arrays.toString(names));
        }
        return null;
    }

    private static void validateMappings() {
        if (hasErrors) {
            ToggleOffhand.LOGGER.error("=== WARNING: Toggle Offhand Registry Validation Failed! ===");
            ToggleOffhand.LOGGER.error("One or more version reflection targets could not be resolved.");
            ToggleOffhand.LOGGER.error("Mod features may fail or crash if triggered.");
            ToggleOffhand.LOGGER.error("=========================================================");
        } else {
            ToggleOffhand.LOGGER.info("All version reflection mappings resolved successfully!");
        }
    }
}
