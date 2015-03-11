rm -r .\bin
mkdir .\bin
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/stamp/math/UnsignedIntMath.java +U
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/stamp/math/IntegerMath.java +U
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/org/SpiderBot/io/SerialLCD.java +U
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/org/SpiderBot/sensors/Ping.java +U
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/org/SpiderBot/sensors/Compass.java +U
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/stamp/peripheral/servo/psc/psc.java +U
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/org/SpiderBot/core/Servos.java +U
jikes -depend -d .\bin -classpath "C:\spiderPoser\lib;C:\spiderPoser\bin" ./src/org/SpiderBot/PoserMain.java +U

