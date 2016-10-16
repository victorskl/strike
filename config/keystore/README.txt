Generated Using JDK Keytool
---

keytool -genkey -keystore strike_keystore.jks -keyalg RSA



---
Do I need to do it on my local:
    Nop. Just simply use this.

But, how to:
    Please read below.

Where to find keytool:
    Command line program 'keytool' comes with JDK installation.
    The complete path on Windows is:
        C:\Program Files\Java\jdk1.8.0_102\bin\keytool.exe

Screen capture:
D:\Projects\unimelb\strike\config\keystore>keytool -genkey -keystore strike_keystore.jks -keyalg RSA
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  Strike
What is the name of your organizational unit?
  [Unknown]:  Strike
What is the name of your organization?
  [Unknown]:  Strike
What is the name of your City or Locality?
  [Unknown]:  Melbourne
What is the name of your State or Province?
  [Unknown]:  VIC
What is the two-letter country code for this unit?
  [Unknown]:  AU
Is CN=Strike, OU=Strike, O=Strike, L=Melbourne, ST=VIC, C=AU correct?
  [no]:  yes

Enter key password for <mykey>
        (RETURN if same as keystore password):