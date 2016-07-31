import java.util.List;

public class Predicate {

	private  String name;
	private  List<String> args=null;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getArgs() {
		return args;
	}
	public void setArgs(List<String> args) {
		this.args = args;
	}
	@Override
	public String toString() {
		return "Predicate = " + name + "-->" + args.toString();
	}
}
