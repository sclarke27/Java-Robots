rm -r .\bin
mkdir .\bin
jikes -depend -d .\bin -classpath "C:\spiderTooth\lib;C:\spiderTooth\bin" ./src/org/SpiderBot/io/SerialLCD.java +U
jikes -depend -d .\bin -classpath "C:\spiderTooth\lib;C:\spiderTooth\bin" ./src/stamp/math/UnsignedIntMath.java +U
jikes -depend -d .\bin -classpath "C:\spiderTooth\lib;C:\spiderTooth\bin" ./src/stamp/peripheral/servo/psc/psc.java +U
jikes -depend -d .\bin -classpath "C:\spiderTooth\lib;C:\spiderTooth\bin" ./src/stamp/peripheral/wireless/eb500/eb500.java
jikes -depend -d .\bin -classpath "C:\spiderTooth\lib;C:\spiderTooth\bin" ./src/org/SpiderBot/core/Servos.java +U
jikes -depend -d .\bin -classpath "C:\spiderTooth\lib;C:\spiderTooth\bin" ./src/org/SpiderBot/MainTooth.java +U

