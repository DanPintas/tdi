package es.danpintas.tdi;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import es.danpintas.tdi.BaseInjector;
import es.danpintas.tdi.Binder;
import es.danpintas.tdi.Injector;
import junit.framework.Test;

/**
 * Runs the JSR 330 TCK against the library. Supports both static and private injection.
 * 
 * @author danpintas
 */
public class TckSuite {

  /**
   * Runs the test suite.
   * 
   * @return {@link Test} suite to execute.
   */
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

}
