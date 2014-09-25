##Fixed point arithmetic for Java

This repository provides classes to perform fixed point arithmetic, i.e. arithmetic using the Java long primitive,
with an assumed number of fractional digits.  The total precision of course is limited to 18 digits, but this is
acceptable in most financial applications. 

The benefit to use fixed point arithmetic compared to BigDecimal is the much faster execution (about 4 times),
but even more the reduced GC overhead.

The challenging part in this implementation is the use of 128 bit intermediate result for multiplication and
later rounding. On Linux systems supporting gcc, JNI is used. As a fallback, an implementation using BigDecimal
is provided.

The structure is an abstract base class which implements most of the functionality, and some specific
derived classes for a fixed number of decimals (Units, Tenths, Hundreds, Millis, Micros, Nanos, Picos, Femtos),
but also a class with a variable number of decimals (VariableUnits), which stores an explicit number for the scale.

If xtend is used in the application, operator overloading is provided, which allows to write code such as

```java
val myMicros = 130.micros
val z = myMicros / 3
```

As an example application, a class for money amounts is provided. With xtend you can then write code such as

```java
        val net = 120.Euro
        val tax = #[ 19.percent ]
        val gross = net + tax
        println('''A net amount of «net» plus «tax» VAT gives «gross» total''')
```

This project uses the jpaw project for some utility classes.



###Building

The parent pom is located in fixedpoint-base:

    (cd fixedpoint-base && mvn install)


