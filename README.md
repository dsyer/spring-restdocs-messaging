This project shows a proof of concept of how to use Spring RESTdocs with Spring Cloud Stream (the same techniques probably work equally well with Spring Integration).

The basic idea is that you install a `MessageDocumentationInterceptor` that inspects all the messages flowing. There is also a RESTdocs generator with methods that accept `Snippets`. The default behaviour is to output the message headers and payload for channels which are selected by the user. E.g.

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureRestDocs
public class RestdocsTestApplicationTests {

	@Autowired
	@Qualifier(Processor.INPUT)
	private MessageChannel input;

	@Autowired
	private MessageDocumentation messages;

	@Autowired
	private MessageCollector collector;

	@Test
	public void bar() throws Exception {
		input.send(MessageBuilder.withPayload(new Foo("bar")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
		messages.document(input);
	}

}
```

Results in an Asciidoctor snippet in `target/generated-snippets/bar/input-message.adoc`:

```
[source,options="nowrap"]
----
id: e85c0363-628d-d763-c69e-83861a42c3af
contentType: application/json
timestamp: 1516897094835

{"value":"bar"}
----
```

You can add other snippets (e.g. to document fields in the message payload) using the public method in `MessageDocumentation`.

A snippet and public methods in `MessageDocumentation` are provided to record a stubs for messaging with Spring Cloud Contract. The contract can then be used to construct a stub of the app that consumers (drivers) can use to do integration testing. There is a sample showing the generation and use of such stubs.
