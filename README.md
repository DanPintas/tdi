# tdi
Turbo Dependency Injection

###Features
 - Lightweight: the core framework contains 26 source files, with all but one being under 250 lines
 - JSR 330 compliant: TCK Tests pass supporting both private and static injection
 - Minimal dependencies: only depends on javax.inject API for JSR compliance
 - Prototypes by default: avoids the Spring anti-pattern 
 - Guice inspired: declare dependency bindings in modules
   + Reusable and modularized binding groups
   + You can see where the injection happens in a concise way
   + Compile time check of the bindings, preventing typos and some invalid declarations

###How to use 

####Base example

```java
    public static Test suite() {
        Injector injector = new BaseInjector(TckSuite::bind);
        Car car = injector.getInstance(Car.class);
        return Tck.testsFor(car, true, true);
    }
    
    private static void bind(Binder binder) {
        binder.bind(Cupholder.class);
        binder.bind(Seat.class);
        binder.bind(Seat.class).annotated(Drivers.class).to(DriversSeat.class);
        binder.bind(FuelTank.class);
        binder.bind(Tire.class);
        binder.bind(Tire.class).named("spare").to(SpareTire.class);
        binder.bind(SpareTire.class);
        binder.bind(Engine.class).to(V8Engine.class);
        binder.bind(Car.class).to(Convertible.class);
    }
```

####Injector
An ```Injector``` must be declared with its ```Module``` list in load order.

The default BaseInjector supports both Prototype and Singleton scopes, and registers a shutdown hook.
Both AbstractInjector and BaseInjector can be extended if you additional scopes or custom behavior.

####Module and Binder
A module is a functional interface using a ```Binder``` so you can declare your dependencies.
 - The ```bind``` method will bind a dependency to its own type 
 - For an interface-to-implementation binding, you ```bind(Service.class).to(ServiceImpl.class)```
 - For named beans, you ```bind(Tire.class).named("spare").to(SpareTire.class)```
 - For other annotated beans, you ```bind(Seat.class).annotated(Drivers.class).to(DriversSeat.class)```
 - You can also bind to a ```Provider``` or an instance straight away
 
####Generic bindings

```java
        Type type = Types.listOf(Integer.class);
        TypeData<List<Integer>> typeData = TypeData.get(type);
        Module module = binder -> binder.bind(typeData).to(Arrays.asList(1,2,3));
        Injector injector = new BaseInjector(module);
        List<Integer> list = injector.getInstance(typeData);
```

Generic types can be obtained via ```Types.parameterizedType(GenericType.class, GenericArg.class)```

Shortcut methods exist for common generics, such as ```List``` or ```Map```

You can use the type to generate a  ```TypeData``` used for both declaring the binding and retrieving the instance

####Embed into main

If you want something similar to a ```SpringApplication.run(App.class, args)``` you can do the following 

```java
    public static void main(String... args) {
        new BaseInjector(App::bind).getInstance(App.class).run(args);
    }
    
    private static void bind(Binder binder) {
        // whatever bindings you need
    }
    
    private void run(String... args) {
        // whatever your run method does
    }
```