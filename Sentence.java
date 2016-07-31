import java.util.List;

public class Sentence {

	@Override
	public String toString() {
		return "\nSent lhs = " + lhs + " rhs= " + rhs ;
	}
	private  List<Predicate> lhs;
	private  Predicate rhs;
	
	public List<Predicate> getLhs() {
		return lhs;
	}
	public void setLhs(List<Predicate> lhs) {
		this.lhs = lhs;
	}
	public Predicate getRhs() {
		return rhs;
	}
	public void setRhs(Predicate rhs) {
		this.rhs = rhs;
	}
	
}
