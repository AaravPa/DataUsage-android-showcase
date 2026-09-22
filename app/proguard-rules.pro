# ORMLite discovers table mappings and DAO methods through reflection.
-keep class com.j256.** { *; }
-keep class com.sigterm.entities.** { *; }
-keep class com.sigterm.orm.sqlite.DatabaseHelper { *; }

# Preference XML instantiates this custom Preference by class name.
-keep class com.sigterm.preference.QuotaPreference { *; }

-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,InnerClasses,EnclosingMethod
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ORMLite ships optional desktop/JPA logging backends that are not part of the
# Android runtime and are never selected by this app.
-dontwarn javax.persistence.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.slf4j.**
