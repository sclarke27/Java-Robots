rm -r .\bin
mkdir .\bin
jikes -depend -d .\bin -classpath "H:\spiderbot backup\RC Car 1.0\lib;H:\spiderbot backup\RC Car 1.0\bin" ./src/stamp/peripheral/appmod/LcdTerminal.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\RC Car 1.0\lib;H:\spiderbot backup\RC Car 1.0\bin" ./src/stamp/math/UnsignedIntMath.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\RC Car 1.0\lib;H:\spiderbot backup\RC Car 1.0\bin" ./src/stamp/peripheral/servo/psc/psc.java +U
jikes -depend -d .\bin -classpath "H:\spiderbot backup\RC Car 1.0\lib;H:\spiderbot backup\RC Car 1.0\bin" ./src/org/RCCar/Main.java +U +OLDCSO

