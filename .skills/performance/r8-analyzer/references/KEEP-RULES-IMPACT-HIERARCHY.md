Based on the documentation provided, keep rules for R8 are ranked by their impact on code optimization. Broad rules prevent R8 from shrinking, obfuscating, or optimizing your code effectively, while narrow rules allow for better performance and smaller APK sizes.

Here is the hierarchy of keep rules from most impactful (broadest) to least impactful (most narrow):

### 1. Package-Wide Wildcards (Most Restrictive)
These prevent all optimizations for entire packages and should be avoided.
*   `-keep class com.example.package.** { *; }`: Affects the package and all subpackages.
*   `-keep class com.example.package.* { *; }`: Affects all classes in the specific package.
*   **Recommendation:** Refine these to target specific classes.

### 2. Inversion Operator (`!`)
Using the exclamation mark can unintentionally impact the entire application.
*   `-keep class !com.example.MyClass { *; }`: This keeps the **entire app** except for `MyClass`.
*   **Recommendation:** Remove these in favor of specific, positive keep rules.

### 3. Keep Rules for Both Class and Members
Rules using the wildcard `*` inside braces force R8 to retain the class and every member exactly as defined.
*   `-keep class com.example.MyClass { *; }`

### 4. Keep Class Members Only
Forces R8 to retain all members within a class.
*   `-keepclassmembers class com.example.MyClass { *; }`

### 5. Modifiers with Keep Specification
Retains members but allows specific optimizations like obfuscation or shrinking.
*   `-keep,allowobfuscation class com.example.MyClass { *; }`
*   `-keep,allowshrinking class com.example.MyClass { *; }`

### 6. Specific Method Preservation
Keeps the class and a specific method, but disables optimizations for that method.
*   `-keep class com.example.MyClass { void myMethod(); }`

### 7. Class-Name Only Preservation
Only the class name is preserved. R8 can still remove unused methods and fields.
*   `-keep class com.example.MyClass`

### 8. Modifiers without Member Specification
Keeps the class entry point while allowing specific optimizations (like access modification) and does not force member retention.
*   `-keep,allowobfuscation class com.example.MyClass`
*   `-keep,allowshrinking class com.example.MyClass`
*   `-keep,allowaccessmodification class com.example.MyClass`

### 9. Conditional Keep Rules (Most Optimized)
These are the most narrow rules and only trigger if specific conditions are met, making them the most optimization-friendly.
*   `-keepclassmembers class com.example.MyClass { <init>(); }`
*   `-keepclasseswithmembers class * { native <methods>; }`

### Best Practices for Performance
To ensure R8 can optimize your app effectively:
1.  **Be Specific:** Target the narrowest scope possible (Level 9 vs Level 1).
2.  **Avoid Wildcards:** Only use `{ *; }` if you absolutely need to keep every single member (e.g., for reflection).
3.  **Use Modifiers:** Use `allowobfuscation` if you need to keep a class but don't care if its name is changed.
