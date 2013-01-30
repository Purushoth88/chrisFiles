import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.AnyType;

@BTrace
class MethodCall {
	@OnMethod(clazz = "/org\\.eclipse\\.orion\\..*/", method = "/.*/", location = @Location(value = Kind.CALL, clazz = "/org\\.eclipse\\.jgit\\..*/", method = "/.*/"))
	static void logMethodCall(@TargetInstance Object targetInstance,
			@TargetMethodOrField String targetMethod,
			@ProbeClassName String probeClass,
			@ProbeMethodName String probeMethod, AnyType[] args) {
		print(Strings.strcat(Strings.strcat(probeClass, "."), probeMethod));
		print(" -> ");
		print(Strings.strcat(Strings.strcat("?", "."), targetMethod));
	}
}