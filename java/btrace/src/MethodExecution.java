import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.AnyType;

@BTrace
class MethodExecution {
	@TLS
	int indent = 0;
	@TLS
	int nr = 0;

	@OnMethod(clazz = "/org\\.eclipse\\.jgit\\..*/", method = "/.*/")
	// @OnMethod(clazz = "/org\\.eclipse\\.(orion|jgit)\\..*/", method = "/.*/")
	// @OnMethod(clazz="/org\\.eclipse\\.jgit\\.api\\..*/", method="/.*/")
	// @OnMethod(clazz="/org\\.eclipse\\.jgit\\..*/", method="/.*/")
	void logMethodExecution(@ProbeClassName String probeClass,
			@ProbeMethodName String probeMethod, AnyType[] args) {
		print(str(nr++));
		print(": ");
		print(name(currentThread()));
		print("@");
		print(threadId(currentThread()));
		print(", lvl:");
		print(str(indent++));
		print(" ");
		println(Strings.strcat(Strings.strcat(probeClass, "."),
				Strings.strcat(probeMethod, "() {")));
	}

	// @OnMethod(clazz="/org\\.eclipse\\.jgit\\.api\\..*/", method="/.*/",
	// location=@Location(value=Kind.RETURN))
	// @OnMethod(clazz="/org\\.eclipse\\.jgit\\..*/", method="/.*/",
	// location=@Location(value=Kind.RETURN))
	@OnMethod(clazz = "/org\\.eclipse\\.jgit\\..*/", method = "/.*/", location = @Location(value = Kind.RETURN))
	void logMethodReturn(@ProbeMethodName(fqn = true) String probeMethod,
			@Duration long duration) {
		print(str(nr++));
		print(": ");
		print(name(currentThread()));
		print("@");
		print(threadId(currentThread()));
		print(", lvl:");
		print(str(--indent));
		println(" }");
	}
}
