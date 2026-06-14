The document "REDUNDANT-RULES.md" outlines common R8/ProGuard keep rules that are unnecessary in modern Android development. Because most modern libraries now include their own **consumer keep rules** embedded in their AAR files, manual configurations can often be deleted to allow for better code shrinking and obfuscation.

### 1. Global Optimization Disablers
**Redundant Rules:**
*   `-dontshrink`
*   `-dontobfuscate`
*   `-dontoptimize`

**The Fix:** Remove these rules entirely. They disable the core functionality of R8, leading to significantly larger and less efficient APKs.

### 2. Android Components and Libraries
**Redundant Rules:**
*   **Components:** Manual rules for `Activity`, `Service`, `View`, or `Fragment`. AAPT2 and R8 automatically detect these via the `AndroidManifest.xml` and layout files.
*   **Official Libraries:** Broad rules targeting `androidx.**`, `kotlin.**`, or `kotlinx.**`. These libraries ship with their own rules.

### 3. Library-Specific Redundancies

| Library | Redundant Rules to Delete | Requirements for Removal |
| :--- | :--- | :--- |
| **Gson** | Package-level model keeps, `TypeAdapter`, `TypeToken`, and `internal.**` rules. | Use **Gson v2.11.0+** and annotate data fields with `@SerializedName`. |
| **Retrofit** | Blanket rules for `retrofit2.**`, HTTP annotations, or `Response` wrappers. | Use **Retrofit v2.9.0+**. |
| **Coroutines** | Broad `kotlinx.coroutines.**` rules or manual continuation/dispatcher keeps. | Use **Coroutines v1.7.0+**. |
| **Room** | Manual keeps for DAO interfaces or generated `_Impl` classes. | Room generates its own rules automatically. |

### 4. Parcelable Implementation
**Redundant Rules:**
Manual keeps for `android.os.Parcelable$Creator` are common in legacy projects but no longer needed.

**The Fix:**
1. Use the `kotlin-parcelize` plugin.
2. Use the `@Parcelize` annotation on your data classes.
3. Remove all manual Parcelable keep rules from your ProGuard file.

```kotlin
// Example of modern Parcelable usage
@Parcelize
data class User(val firstName: String, val lastName: String) : Parcelable
```

### Summary Recommendation
If your project uses the library versions listed above, your `proguard-rules.pro` should ideally be empty of rules targeting those specific libraries. Relying on **consumer keep rules** ensures that R8 only keeps the code strictly necessary for your app to function, resulting in smaller and faster applications.
