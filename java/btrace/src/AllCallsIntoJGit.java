// import all BTrace annotations
import com.sun.btrace.annotations.*;
// import statics from BTraceUtils class
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.AnyType;

// @BTrace annotation tells that this is a BTrace program
@BTrace
class AllCallsIntoJGit  {
	@TLS
	int indent;

	// @OnMethod annotation tells where to probe.
	// In this example, we are interested in entry
	// into the Thread.start() method.
	@OnMethod(clazz = "/org\\.eclipse\\.jgit\\.api\\..*/", method = "/.*/")
	void callToJgit(@ProbeClassName String probeClass,
			@ProbeMethodName String probeMethod, AnyType[] args) {
		if (indent < 3) {
			indent();
			print(Strings.strcat(Strings.strcat(
					Strings.strcat(probeClass, "."), probeMethod), "("));
			printArray(args);
			println(") {");
		}
		indent++;
	}

	@OnMethod(clazz = "/org\\.eclipse\\.jgit\\.api\\..*/", method = "/.*/", location = @Location(Kind.RETURN))
	void returnFromJGit(AnyType[] args) {
		indent--;
		if (indent < 3) {
			indent();
			println("}");
		}
	}

	void indent() {
		switch (indent) {
		case 0:
			print("");
			break;
		case 1:
			print(" ");
			break;
		case 2:
			print("  ");
			break;
		case 3:
			print("   ");
			break;
		case 4:
			print("    ");
			break;
		case 5:
			print("     ");
			break;
		case 6:
			print("      ");
			break;
		case 7:
			print("       ");
			break;
		case 8:
			print("        ");
			break;
		case 9:
			print("         ");
			break;
		default:
			if (indent < 0)
				print(Strings.strcat("----", Strings.str(indent)));
			else if (indent > 9)
				print(Strings.strcat("+++", Strings.str(indent)));
			break;
		}
	}
}
