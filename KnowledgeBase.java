import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeBase {

	private static HashMap<String, List<Sentence>> kB = new HashMap<>();
	
	public static Map<String, List<Sentence>> populate(List<String> lines) {
		
		for(String text : lines){
			if(text.contains("=>"))
			{
				List<Sentence> value=new ArrayList<>();			
				List<Predicate> list=new ArrayList<Predicate>();
				Predicate  lhs_predicate;			
				
				String tokens[]=text.split("=>");
				String leftValue=tokens[0];			
				String rightValue=tokens[1];
				leftValue.trim();
				rightValue.trim();
				
				Predicate rhs=createPredicate(rightValue);
				
				if(leftValue.contains("^"))
				{
					String leftArgs[]=leftValue.split("\\^");				
					for(int i=0;i<leftArgs.length;i++)
					{
						lhs_predicate=createPredicate(leftArgs[i].trim());
						list.add(lhs_predicate);					
					}								
				}
				else
				{
					lhs_predicate=createPredicate(leftValue);					
					list.add(lhs_predicate);				
				}
					
				Sentence sentence=new Sentence();
				sentence.setLhs(list);
				sentence.setRhs(rhs);
				
				value.add(sentence);
				
				if(kB.containsKey(rhs.getName().trim()))
				{
					List<Sentence> existing=kB.get(rhs.getName());
					existing.add(sentence);
					kB.put(rhs.getName().trim(),existing );
				}
				else
				{
					kB.put(rhs.getName().trim(), value);
				}
				
			} 	//end if text contains =>	
			
			else	//text contains only fact
			{
				String tokens[]=text.trim().split("\\(");
				String predicate_name=tokens[0].trim();
				List<Sentence> list= new ArrayList<Sentence>();
				
				Predicate new_fact=createPredicate(text);
				Sentence sentence=new Sentence();
				sentence.setLhs(null);
				sentence.setRhs(new_fact);
					
				if(kB.containsKey(predicate_name.trim()))
				{
					list=kB.get(predicate_name);
					list.add(sentence);
				}
				else
				{
					list.add(sentence);					
				}
				
				kB.put(predicate_name.trim(), list);
			}
		}
		return kB;
	}

	

	public static HashMap<String, List<Sentence>> getkB() {
		return kB;
	}


	public static void setkB(HashMap<String, List<Sentence>> kB) {
		KnowledgeBase.kB = kB;
	}


	public static Predicate createPredicate(String text) {
		
		Predicate predicate=new Predicate();
		
		String tokens[]=text.split("\\(");
		
		predicate.setName(tokens[0].trim());		
		
		String arguments[]=tokens[1].split("\\)");		
		String argList[] = null;
		int i;
		
		List<String> temp= new ArrayList<String>();
		
		if( arguments[0].contains(",") )
		{
			argList=arguments[0].split(",");					
			for(i=0;i<argList.length;i++)
				temp.add(argList[i]);			
		}
		else
			temp.add(arguments[0].trim());
			
		predicate.setArgs(temp);
		return predicate;
	}

}
