package org.junit.contrib.java.lang.system;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.contrib.java.lang.system.TextFromStandardInputStream.emptyStandardInputStream;
import static org.junit.rules.ExpectedException.none;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runners.model.Statement;

public class TextFromStandardInputStreamTest {
	@Rule
	public final Timeout timeout = new Timeout(1000);

	@Rule
	public final ExpectedException thrown = none();

	private final TextFromStandardInputStream systemInMock = emptyStandardInputStream();

	@Test
	public void provideText() throws Throwable {
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideText("arbitrary text");
				Scanner scanner = new Scanner(System.in);
				String textFromSystemIn = scanner.nextLine();
				assertThat(textFromSystemIn, is(equalTo("arbitrary text")));
			}
		});
	}

	@Test
	public void providesMultipleTexts() throws Throwable {
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideText("first text\n", "second text\n");
				Scanner firstScanner = new Scanner(System.in);
				firstScanner.nextLine();
				Scanner secondScanner = new Scanner(System.in);
				String textFromSystemIn = secondScanner.nextLine();
				assertThat(textFromSystemIn, is(equalTo("second text")));
			}
		});
	}

	@Test
	public void doesNotFailForNoProvidedText() throws Throwable {
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideText();
				int character = System.in.read();
				assertThat(character, is(-1));
			}
		});
	}

	@Test
	public void providesMultipleLines() throws Throwable {
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideLines("first text", "second text");
				Scanner firstScanner = new Scanner(System.in);
				firstScanner.nextLine();
				Scanner secondScanner = new Scanner(System.in);
				String textFromSystemIn = secondScanner.nextLine();
				assertThat(textFromSystemIn, is(equalTo("second text")));
			}
		});
	}

	@Test
	public void doesNotFailForNoProvidedLine() throws Throwable {
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideLines();
				int character = System.in.read();
				assertThat(character, is(-1));
			}
		});
	}

	@Test
	public void throwsRequestedIoException() throws Throwable {
		thrown.expect(IOException.class);
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.throwExceptionOnInputEnd(new IOException());
			}
		});
	}

	@Test
	public void providesTextBeforeRequestedIoExceptionIsThrown() {
		thrown.expect(IOException.class);
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideText("arbitrary text");
				systemInMock.throwExceptionOnInputEnd(new IOException());
				assertSystemInProvidesText("arbitrary text");
				System.in.read(); //throws the exception.
			}
		});
	}

	@Test
	public void providesTextAfterRequestedIoExceptionIsThrown() {
		thrown.expect(IOException.class);
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideText("arbitrary text");
				systemInMock.throwExceptionOnInputEnd(new IOException());
				assertSystemInProvidesText("arbitrary text");
				System.in.read(); //throws the exception.
			}
		});
	}

	@Test
	public void throwsRequestedRuntimeException() throws Throwable {
		thrown.expect(RuntimeException.class);
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.throwExceptionOnInputEnd(new RuntimeException());
			}
		});
	}

	@Test
	public void providesTextBeforeRequestedRuntimeExceptionIsThrown() {
		thrown.expect(RuntimeException.class);
		executeRuleWithStatement(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				systemInMock.provideText("arbitrary text");
				systemInMock.throwExceptionOnInputEnd(new RuntimeException());
				assertSystemInProvidesText("arbitrary text");
				System.in.read(); //throws the exception.
			}
		});
	}

	@Test
	public void restoreSystemIn() throws Throwable {
		InputStream originalSystemIn = System.in;
		executeRuleWithStatement(new EmptyStatement());
		assertThat(System.in, is(sameInstance(originalSystemIn)));
	}

	private void executeRuleWithStatement(Statement statement) throws Throwable {
		systemInMock.apply(statement, null).evaluate();
	}

	private void assertSystemInProvidesText(String text) throws IOException {
		for (char c : text.toCharArray())
			assertThat((char) System.in.read(), is(c));
	}
}
