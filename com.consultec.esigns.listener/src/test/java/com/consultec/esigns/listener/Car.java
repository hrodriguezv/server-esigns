package com.consultec.esigns.listener;
class Car {
	enum Type{
		SEDAN, SPORT, SUB	
	}
	private String color;
	private Type type;

	Car() {
	}

	Car(String c, Type t) {
		color = c;
		type = t;
	}
	
	// standard getters setters

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type t) {
		this.type = t;
	}

}
