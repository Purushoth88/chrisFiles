public class CastsAndInheritence {

	public static void main(String[] args) {
		Vehicle v = new Vehicle();
		Car c = new Car();
		BMW b = new BMW();
		
		System.out.println("v.sayHello: "+v.sayHello());
		System.out.println("c.sayHello: "+c.sayHello());
		System.out.println("b.sayHello: "+b.sayHello());
		
		System.out.println("((Vehicle)c).sayHello: "+((Vehicle)c).sayHello());
		System.out.println("((Vehicle)b).sayHello: "+((Vehicle)b).sayHello());

		System.out.println("((Car)b).sayHello: "+((Car)b).sayHello());
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
