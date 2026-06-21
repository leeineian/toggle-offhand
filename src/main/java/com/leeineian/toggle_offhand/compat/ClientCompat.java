package com.leeineian.toggle_offhand.compat;

import com.leeineian.toggle_offhand.ToggleOffhand;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ClientCompat {
    public static File gameDirectory = null;
    private static String cachedLangCode = null;
    private static final Map<String, String> cachedTranslations = new HashMap<>();

    public static Object createKeyMapping(String name, int keycode, String category) {
        try {
            Class<?> keyMappingClass;
            try {
                keyMappingClass = Class.forName("net.minecraft.client.KeyMapping");
            } catch (ClassNotFoundException e) {
                keyMappingClass = Class.forName("net.minecraft.class_304");
            }

            // Loop over all constructors to find a matching one
            for (java.lang.reflect.Constructor<?> ctor : keyMappingClass.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                
                // Case A: 3-parameter constructors
                if (params.length == 3 && params[0] == String.class) {
                    boolean secondIsInt = params[1] == int.class;
                    boolean secondIsKey = params[1].getName().contains("InputConstants$Key") || params[1].getName().contains("class_3675$class_306");
                    
                    if (secondIsInt || secondIsKey) {
                        Object keyArg = secondIsInt ? keycode : createKey(keycode);
                        if (keyArg != null) {
                            if (params[2] == String.class) {
                                ctor.setAccessible(true);
                                ToggleOffhand.LOGGER.info("Found (String, Key/int, String) constructor");
                                return ctor.newInstance(name, keyArg, category);
                            } else if (params[2].isEnum() || params[2].getName().contains("Category") || params[2].getName().contains("class_6603")) {
                                Object catArg = getCategoryEnum(params[2], category);
                                if (catArg != null) {
                                    ctor.setAccessible(true);
                                    ToggleOffhand.LOGGER.info("Found (String, Key/int, Category) constructor");
                                    return ctor.newInstance(name, keyArg, catArg);
                                }
                            }
                        }
                    }
                }
                
                // Case B: 4-parameter constructors
                if (params.length == 4 && params[0] == String.class && 
                    (params[1].getName().contains("InputConstants$Type") || params[1].getName().contains("class_3675$class_307")) && 
                    params[2] == int.class) {
                    
                    Object keysym = getKeysym(params[1]);
                    if (keysym != null) {
                        if (params[3] == String.class) {
                            ctor.setAccessible(true);
                            ToggleOffhand.LOGGER.info("Found (String, Type, int, String) constructor");
                            return ctor.newInstance(name, keysym, keycode, category);
                        } else if (params[3].isEnum() || params[3].getName().contains("Category") || params[3].getName().contains("class_6603")) {
                            Object catArg = getCategoryEnum(params[3], category);
                            if (catArg != null) {
                                ctor.setAccessible(true);
                                ToggleOffhand.LOGGER.info("Found (String, Type, int, Category) constructor");
                                return ctor.newInstance(name, keysym, keycode, catArg);
                            }
                        }
                    }
                }

                // Case C: 5-parameter constructors
                if (params.length == 5 && params[0] == String.class && 
                    (params[1].getName().contains("InputConstants$Type") || params[1].getName().contains("class_3675$class_307")) && 
                    params[2] == int.class) {
                    
                    Object keysym = getKeysym(params[1]);
                    if (keysym != null) {
                        if (params[3] == String.class) {
                            ctor.setAccessible(true);
                            ToggleOffhand.LOGGER.info("Found (String, Type, int, String, int) constructor");
                            return ctor.newInstance(name, keysym, keycode, category, 0);
                        } else if (params[3].isEnum() || params[3].getName().contains("Category") || params[3].getName().contains("class_6603")) {
                            Object catArg = getCategoryEnum(params[3], category);
                            if (catArg != null) {
                                ctor.setAccessible(true);
                                ToggleOffhand.LOGGER.info("Found (String, Type, int, Category, int) constructor");
                                return ctor.newInstance(name, keysym, keycode, catArg, 0);
                            }
                        }
                    }
                }
            }

            // Fallback try the old one
            Class<?> inputConstantsTypeClass;
            try {
                inputConstantsTypeClass = Class.forName("com.mojang.blaze3d.platform.InputConstants$Type");
            } catch (ClassNotFoundException e) {
                inputConstantsTypeClass = Class.forName("net.minecraft.class_3675$class_307");
            }
            Object keysym = getKeysym(inputConstantsTypeClass);
            java.lang.reflect.Constructor<?> ctor = keyMappingClass.getConstructor(String.class, inputConstantsTypeClass, int.class, String.class);
            return ctor.newInstance(name, keysym, keycode, category);
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to create KeyMapping reflectively: ", e);
            // Log constructors for debugging
            try {
                Class<?> keyMappingClass = Class.forName("net.minecraft.client.KeyMapping");
                ToggleOffhand.LOGGER.info("Available constructors in KeyMapping:");
                for (java.lang.reflect.Constructor<?> c : keyMappingClass.getConstructors()) {
                    ToggleOffhand.LOGGER.info("  " + c.toString());
                }
            } catch (Exception ex) {}
        }
        return null;
    }

    private static Object getCategoryEnum(Class<?> categoryClass, String categoryStr) {
        String targetName = "MISC";
        if (categoryStr.endsWith(".movement")) targetName = "MOVEMENT";
        else if (categoryStr.endsWith(".gameplay")) targetName = "GAMEPLAY";
        else if (categoryStr.endsWith(".inventory")) targetName = "INVENTORY";
        else if (categoryStr.endsWith(".creative")) targetName = "CREATIVE";
        else if (categoryStr.endsWith(".multiplayer")) targetName = "MULTIPLAYER";
        else if (categoryStr.endsWith(".ui")) targetName = "UI";
        else if (categoryStr.endsWith(".misc")) targetName = "MISC";

        // Try enum constants first
        try {
            Object[] enumConstants = categoryClass.getEnumConstants();
            if (enumConstants != null) {
                Object fallback = null;
                for (Object obj : enumConstants) {
                    if (fallback == null) fallback = obj;
                    if (obj.toString().equalsIgnoreCase(targetName) || (obj instanceof Enum && ((Enum<?>)obj).name().equalsIgnoreCase(targetName))) {
                        return obj;
                    }
                }
                return fallback;
            }
        } catch (Exception e) {
            // ignore
        }

        // If not an enum, look for public static fields of the same type
        try {
            Object fallback = null;
            for (java.lang.reflect.Field f : categoryClass.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && categoryClass.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Object val = f.get(null);
                    if (val != null) {
                        if (fallback == null) fallback = val;
                        if (f.getName().equalsIgnoreCase(targetName) || val.toString().equalsIgnoreCase(targetName)) {
                            return val;
                        }
                    }
                }
            }
            return fallback;
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get category from fields: ", e);
        }
        return null;
    }

    private static Object createKey(int keycode) {
        try {
            Class<?> inputConstantsClass;
            try {
                inputConstantsClass = Class.forName("com.mojang.blaze3d.platform.InputConstants");
            } catch (ClassNotFoundException e) {
                inputConstantsClass = Class.forName("net.minecraft.class_3675");
            }
            try {
                java.lang.reflect.Method getKeyMethod = inputConstantsClass.getMethod("getKey", int.class, int.class);
                return getKeyMethod.invoke(null, keycode, 0);
            } catch (NoSuchMethodException e) {
                java.lang.reflect.Method getKeyMethod = inputConstantsClass.getMethod("method_15984", int.class, int.class);
                return getKeyMethod.invoke(null, keycode, 0);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to create Key object reflectively: ", e);
        }
        return null;
    }

    private static Object getKeysym(Class<?> inputConstantsTypeClass) {
        for (Object obj : inputConstantsTypeClass.getEnumConstants()) {
            if (obj.toString().equals("KEYSYM")) {
                return obj;
            }
        }
        return null;
    }

    public static String getItemId(Object item) {
        if (item == null) return "";
        try {
            Class<?> registriesClass;
            try {
                registriesClass = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            } catch (ClassNotFoundException e) {
                try {
                    registriesClass = Class.forName("net.minecraft.class_7923");
                } catch (ClassNotFoundException ex) {
                    registriesClass = Class.forName("net.minecraft.class_7910");
                }
            }
            java.lang.reflect.Field itemField = null;
            try {
                itemField = registriesClass.getField("ITEM");
            } catch (NoSuchFieldException e) {
                try {
                    itemField = registriesClass.getField("field_41178");
                } catch (NoSuchFieldException ex) {
                    for (java.lang.reflect.Field f : registriesClass.getDeclaredFields()) {
                        if (f.getName().equals("ITEM") || f.getName().equals("field_41178")) {
                            itemField = f;
                            break;
                        }
                    }
                }
            }
            if (itemField != null) {
                itemField.setAccessible(true);
                Object registry = itemField.get(null);
                java.lang.reflect.Method getKeyMethod = null;
                Class<?> current = registry.getClass();
                while (current != null && current != Object.class) {
                    for (java.lang.reflect.Method m : current.getMethods()) {
                        if (m.getName().equals("getKey") || m.getName().equals("method_10221")) {
                            if (m.getParameterTypes().length == 1) {
                                getKeyMethod = m;
                                break;
                            }
                        }
                    }
                    if (getKeyMethod != null) break;
                    current = current.getSuperclass();
                }
                if (getKeyMethod != null) {
                    getKeyMethod.setAccessible(true);
                    Object loc = getKeyMethod.invoke(registry, item);
                    if (loc != null) {
                        return loc.toString();
                    }
                }
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get item ID reflectively: ", e);
        }
        return "";
    }

    public static boolean consumeClick(Object mapping) {
        if (mapping == null) return false;
        try {
            java.lang.reflect.Method m = mapping.getClass().getMethod("consumeClick");
            return (Boolean) m.invoke(mapping);
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method m = mapping.getClass().getMethod("method_1436");
                return (Boolean) m.invoke(mapping);
            } catch (Exception ex) {
                ToggleOffhand.LOGGER.error("Failed to invoke consumeClick/wasPressed reflectively: ", ex);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to invoke consumeClick reflectively: ", e);
        }
        return false;
    }

    public static File getGameDirectory(Object mc) {
        try {
            for (java.lang.reflect.Field f : mc.getClass().getDeclaredFields()) {
                if (f.getType() == File.class) {
                    f.setAccessible(true);
                    return (File) f.get(mc);
                }
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get gameDirectory reflectively: ", e);
        }
        return null;
    }

    public static Object getPlayer(Object mc) {
        try {
            for (java.lang.reflect.Field f : mc.getClass().getDeclaredFields()) {
                String typeName = f.getType().getName();
                if (typeName.endsWith(".LocalPlayer") || typeName.endsWith(".class_746")) {
                    f.setAccessible(true);
                    return f.get(mc);
                }
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get player reflectively: ", e);
        }
        return null;
    }

    public static Object createComponent(String text) {
        try {
            Class<?> componentClass;
            try {
                componentClass = Class.forName("net.minecraft.network.chat.Component");
            } catch (ClassNotFoundException e) {
                componentClass = Class.forName("net.minecraft.class_2561");
            }
            for (java.lang.reflect.Method m : componentClass.getMethods()) {
                if (m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == String.class) {
                    if (m.getName().equals("literal") || m.getName().equals("method_43470")) {
                        m.setAccessible(true);
                        return m.invoke(null, text);
                    }
                }
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to create component reflectively: ", e);
        }
        return null;
    }

    public static void sendOverlay(Object player, Object component) {
        for (java.lang.reflect.Method m : player.getClass().getMethods()) {
            Class<?>[] params = m.getParameterTypes();
            if (params.length == 1 && params[0].isAssignableFrom(component.getClass())) {
                if (m.getName().equals("sendOverlayMessage") || m.getName().equals("displayClientMessage")) {
                    try {
                        m.setAccessible(true);
                        m.invoke(player, component);
                        return;
                    } catch (Exception e) {
                        // ignore
                    }
                }
            } else if (params.length == 2 && params[0].isAssignableFrom(component.getClass()) && params[1] == boolean.class) {
                if (m.getName().equals("displayClientMessage") || m.getName().equals("sendMessage") || m.getName().equals("method_7353")) {
                    try {
                        m.setAccessible(true);
                        m.invoke(player, component, true);
                        return;
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    private static String getLanguageFromManager() {
        try {
            Class<?> mcClass;
            try {
                mcClass = Class.forName("net.minecraft.client.Minecraft");
            } catch (ClassNotFoundException e) {
                mcClass = Class.forName("net.minecraft.class_310");
            }
            java.lang.reflect.Method getInstanceMethod = mcClass.getMethod("getInstance");
            Object mc = getInstanceMethod.invoke(null);
            if (mc != null) {
                Object langManager = null;
                try {
                    java.lang.reflect.Method getLangManager = mcClass.getMethod("getLanguageManager");
                    langManager = getLangManager.invoke(mc);
                } catch (NoSuchMethodException e) {
                    try {
                        java.lang.reflect.Method getLangManager = mcClass.getMethod("method_1509");
                        langManager = getLangManager.invoke(mc);
                    } catch (NoSuchMethodException ex) {
                        for (java.lang.reflect.Field f : mcClass.getDeclaredFields()) {
                            String tName = f.getType().getName();
                            if (tName.endsWith(".LanguageManager") || tName.endsWith(".class_1076")) {
                                f.setAccessible(true);
                                langManager = f.get(mc);
                                break;
                            }
                        }
                    }
                }
                if (langManager != null) {
                    Object selected = null;
                    try {
                        java.lang.reflect.Method getSelected = langManager.getClass().getMethod("getSelected");
                        selected = getSelected.invoke(langManager);
                    } catch (NoSuchMethodException e) {
                        try {
                            java.lang.reflect.Method getSelected = langManager.getClass().getMethod("method_4669");
                            selected = getSelected.invoke(langManager);
                        } catch (NoSuchMethodException ex) {
                        }
                    }
                    if (selected != null) {
                        if (selected instanceof String) {
                            return ((String) selected).toLowerCase(java.util.Locale.ROOT);
                        } else {
                            try {
                                java.lang.reflect.Method getCode = selected.getClass().getMethod("getCode");
                                return ((String) getCode.invoke(selected)).toLowerCase(java.util.Locale.ROOT);
                            } catch (NoSuchMethodException e) {
                                try {
                                    java.lang.reflect.Method getCode = selected.getClass().getMethod("method_4684");
                                    return ((String) getCode.invoke(selected)).toLowerCase(java.util.Locale.ROOT);
                                } catch (NoSuchMethodException ex) {
                                    for (java.lang.reflect.Field f : selected.getClass().getDeclaredFields()) {
                                        if (f.getType() == String.class) {
                                            f.setAccessible(true);
                                            String val = (String) f.get(selected);
                                            if (val != null && val.contains("_")) {
                                                return val.toLowerCase(java.util.Locale.ROOT);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
        }
        return null;
    }

    private static String getLanguageFromOptions() {
        if (gameDirectory == null) {
            try {
                Class<?> mcClass;
                try {
                    mcClass = Class.forName("net.minecraft.client.Minecraft");
                } catch (ClassNotFoundException e) {
                    mcClass = Class.forName("net.minecraft.class_310");
                }
                java.lang.reflect.Method getInstanceMethod = mcClass.getMethod("getInstance");
                Object mc = getInstanceMethod.invoke(null);
                if (mc != null) {
                    gameDirectory = getGameDirectory(mc);
                }
            } catch (Throwable t) {
                // ignore
            }
        }
        
        if (gameDirectory != null) {
            File optionsFile = new File(gameDirectory, "options.txt");
            if (optionsFile.exists()) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(optionsFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("lang:")) {
                            return line.substring(5).trim().toLowerCase(java.util.Locale.ROOT);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return "en_us";
    }

    private static String getCurrentLanguage() {
        String lang = getLanguageFromManager();
        if (lang != null && !lang.isEmpty()) {
            return lang;
        }
        return getLanguageFromOptions();
    }

    public static String getTranslation(String key) {
        if ("key.toggle_offhand".equals(key)) {
            String currentLang = getCurrentLanguage();
            if (!currentLang.equals(cachedLangCode)) {
                injectTranslations();
            }
            String val = cachedTranslations.get(key);
            if (val != null) {
                return val;
            }
            return "Toggle Offhand";
        }

        // First try the loaded Language instance (so we get both vanilla keys and mod keys)
        try {
            Class<?> languageClass;
            try {
                languageClass = Class.forName("net.minecraft.locale.Language");
            } catch (ClassNotFoundException e) {
                languageClass = Class.forName("net.minecraft.class_2477");
            }
            java.lang.reflect.Method getInstanceMethod = null;
            try {
                getInstanceMethod = languageClass.getMethod("getInstance");
            } catch (NoSuchMethodException e) {
                try {
                    getInstanceMethod = languageClass.getMethod("method_10517");
                } catch (NoSuchMethodException ex) {
                    for (java.lang.reflect.Method m : languageClass.getMethods()) {
                        if (m.getParameterTypes().length == 0 && m.getReturnType() == languageClass && java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                            getInstanceMethod = m;
                            break;
                        }
                    }
                }
            }
            if (getInstanceMethod != null) {
                Object languageInstance = getInstanceMethod.invoke(null);
                if (languageInstance != null) {
                    java.lang.reflect.Method getMethod = null;
                    try {
                        getMethod = languageClass.getMethod("getOrDefault", String.class);
                    } catch (NoSuchMethodException e) {
                        try {
                            getMethod = languageClass.getMethod("method_4679", String.class, String.class);
                        } catch (NoSuchMethodException ex) {
                            try {
                                getMethod = languageClass.getMethod("method_48307", String.class);
                            } catch (NoSuchMethodException ex2) {
                                for (java.lang.reflect.Method m : languageClass.getMethods()) {
                                    if (m.getReturnType() == String.class && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == String.class) {
                                        getMethod = m;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (getMethod != null) {
                        Object val;
                        if (getMethod.getParameterTypes().length == 2) {
                            val = getMethod.invoke(languageInstance, key, key);
                        } else {
                            val = getMethod.invoke(languageInstance, key);
                        }
                        if (val instanceof String && !key.equals(val)) {
                            return (String) val;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            // ignore
        }

        String val = cachedTranslations.get(key);
        if (val != null) {
            return val;
        }
        if ("options.on".equals(key)) return "ON";
        if ("options.off".equals(key)) return "OFF";
        return key;
    }

    public static void injectTranslations() {
        injectTranslations(null);
    }

    public static void injectTranslations(Object languageInstance) {
        String currentLang = getCurrentLanguage();
        cachedLangCode = currentLang;
        cachedTranslations.clear();
        loadTranslationsFor(currentLang);
        if (!"en_us".equals(currentLang)) {
            loadTranslationsFor("en_us"); // Fallback
        }

        try {
            Class<?> languageClass;
            try {
                languageClass = Class.forName("net.minecraft.locale.Language");
            } catch (ClassNotFoundException e) {
                languageClass = Class.forName("net.minecraft.class_2477");
            }
            
            if (languageInstance == null) {
                for (java.lang.reflect.Field f : languageClass.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && languageClass.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        languageInstance = f.get(null);
                        if (languageInstance != null) break;
                    }
                }
                
                if (languageInstance == null) {
                    java.lang.reflect.Method getInstanceMethod = null;
                    try {
                        getInstanceMethod = languageClass.getMethod("getInstance");
                    } catch (NoSuchMethodException e) {
                        try {
                            getInstanceMethod = languageClass.getMethod("method_10517");
                        } catch (NoSuchMethodException ex) {
                            try {
                                getInstanceMethod = languageClass.getMethod("method_144");
                            } catch (NoSuchMethodException ex2) {
                                for (java.lang.reflect.Method m : languageClass.getMethods()) {
                                    if (m.getParameterTypes().length == 0 && m.getReturnType() == languageClass && java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                                        getInstanceMethod = m;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (getInstanceMethod != null) {
                        languageInstance = getInstanceMethod.invoke(null);
                    }
                }
            }
            
            if (languageInstance != null) {
                ToggleOffhand.LOGGER.info("Injecting into languageInstance of class: " + languageInstance.getClass().getName());
                java.lang.reflect.Field mapField = null;
                Class<?> current = languageInstance.getClass();
                while (current != null && current != Object.class) {
                    for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                        if (Map.class.isAssignableFrom(f.getType())) {
                            f.setAccessible(true);
                            mapField = f;
                            break;
                        }
                    }
                    if (mapField != null) break;
                    current = current.getSuperclass();
                }
                if (mapField != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> map = (Map<String, String>) mapField.get(languageInstance);
                    if (map != null) {
                        if (cachedTranslations.isEmpty()) {
                            cachedTranslations.put("key.toggle_offhand", "Toggle Offhand");
                            cachedTranslations.put("options.on", "ON");
                            cachedTranslations.put("options.off", "OFF");
                        }
                        
                        HashMap<String, String> newMap = new HashMap<>(map);
                        String title = cachedTranslations.get("key.toggle_offhand");
                        newMap.put("key.toggle_offhand", title != null ? title : "Toggle Offhand");
                        newMap.put("options.on", cachedTranslations.getOrDefault("options.on", "ON"));
                        newMap.put("options.off", cachedTranslations.getOrDefault("options.off", "OFF"));
                        
                        mapField.set(languageInstance, newMap);
                        ToggleOffhand.LOGGER.info("Successfully injected translations reflectively!");
                    }
                }
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to inject translations reflectively: ", e);
        }
    }

    private static void loadTranslationsFor(String langCode) {
        String path = "assets/toggle_offhand/lang/" + langCode + ".json";
        ToggleOffhand.LOGGER.info("Attempting to load translation for " + langCode + " at path: " + path);
        try (InputStream stream = ToggleOffhand.class.getClassLoader().getResourceAsStream(path)) {
            if (stream != null) {
                ToggleOffhand.LOGGER.info("Found translation resource stream for: " + langCode);
                com.google.gson.Gson gson = new com.google.gson.Gson();
                @SuppressWarnings("unchecked")
                Map<String, String> map = gson.fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType()
                );
                if (map != null) {
                    ToggleOffhand.LOGGER.info("Successfully loaded " + map.size() + " translations for " + langCode);
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        cachedTranslations.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                ToggleOffhand.LOGGER.warn("Translation resource stream is NULL for: " + path);
            }
        } catch (Throwable t) {
            ToggleOffhand.LOGGER.error("Failed to load translation file for " + langCode, t);
        }
    }

    public static Object getMainHandItem(Object entity) {
        if (entity == null) return null;
        try {
            java.lang.reflect.Method m = entity.getClass().getMethod("getMainHandItem");
            return m.invoke(entity);
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method m = entity.getClass().getMethod("method_6047");
                return m.invoke(entity);
            } catch (Exception ex) {
                ToggleOffhand.LOGGER.error("Failed to get main hand item reflectively: ", ex);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get main hand item reflectively: ", e);
        }
        return null;
    }

    public static Object getItemStackItem(Object stack) {
        if (stack == null) return null;
        try {
            java.lang.reflect.Method m = stack.getClass().getMethod("getItem");
            return m.invoke(stack);
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method m = stack.getClass().getMethod("method_7909");
                return m.invoke(stack);
            } catch (Exception ex) {
                ToggleOffhand.LOGGER.error("Failed to get item from stack reflectively: ", ex);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get item from stack reflectively: ", e);
        }
        return null;
    }

    public static boolean isItemStackEmpty(Object stack) {
        if (stack == null) return true;
        try {
            java.lang.reflect.Method m = stack.getClass().getMethod("isEmpty");
            return (Boolean) m.invoke(stack);
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method m = stack.getClass().getMethod("method_7960");
                return (Boolean) m.invoke(stack);
            } catch (Exception ex) {
                ToggleOffhand.LOGGER.error("Failed to check if item stack is empty reflectively: ", ex);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to check if item stack is empty reflectively: ", e);
        }
        return true;
    }

    public static Object getMainArm(Object entity) {
        if (entity == null) return null;
        try {
            java.lang.reflect.Method m = entity.getClass().getMethod("getMainArm");
            return m.invoke(entity);
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method m = entity.getClass().getMethod("method_6068");
                return m.invoke(entity);
            } catch (Exception ex) {
                ToggleOffhand.LOGGER.error("Failed to get main arm reflectively: ", ex);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get main arm reflectively: ", e);
        }
        return null;
    }

    public static Object getOppositeArm(Object arm) {
        if (arm == null) return null;
        try {
            java.lang.reflect.Method m = arm.getClass().getMethod("getOpposite");
            return m.invoke(arm);
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method m = arm.getClass().getMethod("method_5928");
                return m.invoke(arm);
            } catch (Exception ex) {
                ToggleOffhand.LOGGER.error("Failed to get opposite arm reflectively: ", ex);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to get opposite arm reflectively: ", e);
        }
        return null;
    }

    public static boolean isEntityInvisible(Object entity) {
        if (entity == null) return false;
        try {
            java.lang.reflect.Method m = entity.getClass().getMethod("isInvisible");
            return (Boolean) m.invoke(entity);
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method m = entity.getClass().getMethod("method_5767");
                return (Boolean) m.invoke(entity);
            } catch (Exception ex) {
                ToggleOffhand.LOGGER.error("Failed to check if entity is invisible reflectively: ", ex);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to check if entity is invisible reflectively: ", e);
        }
        return false;
    }
}
