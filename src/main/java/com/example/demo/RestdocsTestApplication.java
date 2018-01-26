package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@EnableBinding(Processor.class)
public class RestdocsTestApplication {

	@Autowired
	@Output(Processor.OUTPUT)
	private MessageChannel output;

	@StreamListener(Processor.INPUT)
	public void process(Foo foo) {
		List<Bar> bars = new ArrayList<>();
		if (foo.getValue().startsWith("bar")) {
			for (int i = 0; i < 2; i++) {
				bars.add(new Bar(foo.getValue() + i));
			}
		}
		else {
			bars.add(new Bar(foo.getValue()));
		}
		for (Bar bar : bars) {
			output.send(MessageBuilder.withPayload(bar).build());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(RestdocsTestApplication.class, args);
	}
}

class Foo {

	private String value;

	Foo() {
	}

	public String lowercase() {
		return value.toLowerCase();
	}

	public Foo(String value) {
		this.value = value;
	}

	public String uppercase() {
		return value.toUpperCase();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

class Bar {

	private String value;

	Bar() {
	}

	public Bar(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}