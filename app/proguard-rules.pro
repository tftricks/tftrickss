# kotlinx.serialization — keep generated serializers for the app's models.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.tftricks.app.domain.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.tftricks.app.domain.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
