public class CastsAndInheritence {
	Vehicle v = new Vehicle();
	Car c = new Car();
	BMW b = new BMW();
	
	public static void main(String[] args) {
		Vehicle v = new Vehicle();
		Car c = new Car();
		BMW b = new BMW();

		System.out.println("v.sayHello: " + v.sayHello());
		System.out.println("c.sayHello: " + c.sayHello());
		System.out.println("b.sayHello: " + b.sayHello());

		System.out
				.println("((Vehicle)c).sayHello: " + ((Vehicle) c).sayHello());
		System.out
				.println("((Vehicle)b).sayHello: " + ((Vehicle) b).sayHello());

		System.out.println("((Car)b).sayHello: " + ((Car) b).sayHello());
		
		Vehicle v2;
		
		v2=v;
		f(v2);
		v2=c;
		f(v2);
		v2=b;
		f(v2);
		
		CastsAndInheritence cai = new CastsAndInheritence();
		
		v2=v;
		cai.f2(v2);
		v2=c;
		cai.f2(v2);
		v2=b;
		cai.f2(v2);
		
		cai.doit();
	}
	
	private void doit() {
		f2(getVehicle(0));
		f2(getVehicle(1));
		f2(getVehicle(2));
	}
	
	private Vehicle getVehicle(int i) {
		if (i==0)
			return new Vehicle();
		if (i==1)
			return new Car();
		if (i==2)
			return new BMW();
		return null;
	}

	public static void f(Vehicle v) {
		System.out.println("f(Vehicle v) called. v=" + v.toString());
	}

	public static void f(Car c) {
		System.out.println("f(Car c) called. c=" + c.toString());
	}

	public static void f(BMW b) {
		System.out.println("f(BMW b) called. b=" + b.toString());
	}
	
	public void f2(Vehicle v) {
		System.out.println("f2(Vehicle v) called. v=" + v.toString());
	}

	public void f2(Car c) {
		System.out.println("f2(Car c) called. c=" + c.toString());
	}

	public void f2(BMW b) {
		System.out.println("f2(BMW b) called. b=" + b.toString());
	}
}

class Vehicle {
	public String sayHello() {
		return "hello! from Vehicle";
	}
}

class Car extends Vehicle {
	public String sayHello() {
		return "hello! from Car";
	}
}

class BMW extends Car {
	public String sayHello() {
		return "hello! from BMW";
	}
}
