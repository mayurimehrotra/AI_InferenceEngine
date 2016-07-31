import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class inference {

	private static FileReader fileReader=null;
	private static String fileName=null;
	private static BufferedReader bufferedReader=null;
	private static FileWriter fileWriter=null;
	private static BufferedWriter bufferedWriter=null;
	private static int no_of_queries;
	private static int size_of_kb;
	private static List<Predicate> queries=new ArrayList<Predicate>();	
	private static int std_counter=1;
	private static Map<String,List<Predicate>> loop_check_list=new HashMap<String,List<Predicate>>(); 
	
	public static void main(String[] args) {
		
		if(args[0].equals("-i"))
			fileName=args[1];
		
		try {
			fileReader=new FileReader(fileName);
			bufferedReader=new BufferedReader(fileReader);
			fileWriter=new FileWriter("output.txt");
			bufferedWriter= new BufferedWriter(fileWriter);		
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		String text;
		int i;
		List<String> input_sentences = new ArrayList<String>();
		HashMap<String,List<Sentence>> KB = new HashMap<>();
		try {		
			
			no_of_queries= Integer.parseInt(bufferedReader.readLine()); 
			
			for(i=0;i<no_of_queries;i++)
			{					
				Predicate query=KnowledgeBase.createPredicate(bufferedReader.readLine());		
				queries.add(query);
			}
			
			size_of_kb= Integer.parseInt(bufferedReader.readLine());			

			
			for(i=0;i<size_of_kb;i++)
				input_sentences.add(bufferedReader.readLine());
						
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean query_exists=false;
		for(i=0;i<no_of_queries;i++)
		{
			KnowledgeBase.populate(input_sentences);
			KB = KnowledgeBase.getkB();
			List<Sentence> ans= null;
		
			
			query_exists= FOL_BC_ASK(queries.get(i), KB);			
			System.out.println(query_exists);			
			try {
				bufferedWriter.write(Boolean.toString(query_exists).toUpperCase());
				bufferedWriter.newLine();
			} catch (IOException e) {			
				e.printStackTrace();
			}
		}			
			
		try {
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		KnowledgeBase.populate(input_sentences);
		KB = KnowledgeBase.getkB();	
	}//end of main
	
	
	public static boolean FOL_BC_ASK(Predicate query, HashMap<String, List<Sentence>> kB2)
	{				
		loop_check_list=new HashMap<String,List<Predicate>>();
		std_counter=1;
		
		List<HashMap<String,String>> ans = FOL_BC_OR(query,new HashMap<String,String>(),kB2);				
		if(ans==null)
			return false;
		
		return true;			
	}

	private static Sentence standardise(Sentence sentence1) {
		
		//copy sentence1 values to sentence
		Sentence sentence=new Sentence();
		
		if(sentence1.getLhs()!= null)
		{
			List<Predicate> orig_sent_list=sentence1.getLhs();
			List<Predicate> list=new ArrayList<Predicate>();
			for(int i=0;i<orig_sent_list.size();i++)
			{
				Predicate p= new Predicate();
				String name= orig_sent_list.get(i).getName().trim();
				List<String> args= new ArrayList<String>();
				for(int x=0;x<orig_sent_list.get(i).getArgs().size();x++)
					args.add(orig_sent_list.get(i).getArgs().get(x).trim());
				
				p.setName(name);
				p.setArgs(args);
				list.add(p);			
			}			
			sentence.setLhs(list);		
		}
		
		Predicate sent_rhs=sentence1.getRhs();
		Predicate p= new Predicate();		
		List<String> args= new ArrayList<String>();
		for(int x=0;x<sentence1.getRhs().getArgs().size();x++)
			args.add(sentence1.getRhs().getArgs().get(x).trim());
		
		String rhs_name=sentence1.getRhs().getName().trim();
		p.setName(rhs_name);
		p.setArgs(args);
		sentence.setRhs(p);
		List<Predicate> lhs=sentence.getLhs();
		Predicate rhs=sentence.getRhs();
		
		if(lhs!=null)			//not a fact
		{
			for(int i=0;i<lhs.size();i++)
			{
				List<String> arg_list=lhs.get(i).getArgs();
				for(int j=0;j<arg_list.size();j++)
				{
					if(checkVariable(arg_list.get(j)))
					{
						arg_list.set(j, arg_list.get(j)+std_counter);										
					}				
				}
				sentence.getLhs().get(i).setArgs(arg_list);
			}
		}
		
		List<String> rhs_arg_list=rhs.getArgs();
		for(int j=0;j<rhs_arg_list.size();j++)
		{
			if(checkVariable(rhs_arg_list.get(j)))
			{
				rhs_arg_list.set(j, rhs_arg_list.get(j)+std_counter);										
			}
		}
		sentence.getRhs().setArgs(rhs_arg_list);
		
		std_counter++;		
		return sentence;
	}

	
	private static List<HashMap<String, String>> FOL_BC_OR(Predicate goal, HashMap<String,String> theta, HashMap<String, List<Sentence>> kB2)
	{		
		List<HashMap<String,String>> or_return_list=new ArrayList<HashMap<String,String>>();			
		int i,flag=0;
		
		if(isPresentInLoopList(goal))
			return null;
		else
			add(goal);	
		
		List<Sentence> valid_sent_list= getValidSentences(goal,kB2);	
		
		if( valid_sent_list == null )				
				return null;
		
		int count=valid_sent_list.size();
		if(count!=0){
		for (Sentence sentence : valid_sent_list) 
		{		
			Sentence sentence1 = standardise(sentence);
			System.out.println("\nSent after std" + sentence1);
			count--;			
						
			String rhs_string="";
			for(int x=0;x<sentence1.getRhs().getArgs().size();x++)
				rhs_string+=sentence1.getRhs().getArgs().get(x)+",";
						
			String goal_string="";
			for(int x=0;x<goal.getArgs().size();x++)
				goal_string+=goal.getArgs().get(x)+",";

			
			HashMap<String, String> theta_copy=new HashMap<String,String>();
			for(String s:theta.keySet())
			{
				String val=theta.get(s);
				theta_copy.put(s, val);
			}
			
			
			HashMap<String, String> theta_value=unify(rhs_string,goal_string,theta_copy);
			List<HashMap<String, String>> theta_dash= FOL_BC_AND(sentence1.getLhs(),theta_value,kB2);
							
			if(theta_dash!=null)	
			{
				//add to final return list
				for (HashMap<String, String> h : theta_dash) 
				{										
					HashMap<String , String> new_map =new HashMap<String,String>();
					
					for(String s:h.keySet())
						new_map.put(s,h.get(s));
					
					or_return_list.add(new_map);
				}
			}		
		}
		}// end if count!=0
		
		if(or_return_list.size()==0)
			return null;
		return or_return_list;
	}



	private static List<HashMap<String, String>> FOL_BC_AND(List<Predicate> goal, HashMap<String,String> theta, HashMap<String, List<Sentence>> kB2) {
				
		List<HashMap<String, String>> return_and_list=new ArrayList<HashMap<String,String>>();
		
		
		if(theta==null )
			return null;
		if(goal==null || goal.size()==0 || goal.isEmpty())
		{
			return_and_list.add(theta);
			return return_and_list;
		}

		Predicate first=new Predicate();
		first.setName(goal.get(0).getName().trim());
		List<String> first_list=new ArrayList<>();
		
		for(int x=0;x<goal.get(0).getArgs().size();x++)
		{
			first_list.add(goal.get(0).getArgs().get(x));
		}
		first.setArgs(first_list);
		
		List<Predicate> rest= new ArrayList<Predicate>();		
		int i;
		for(i=1;i<goal.size();i++)
			rest.add(goal.get(i));	
		
		Predicate goal_for_subs= substitute(first,theta);

		List<HashMap<String, String>> theta_dash= FOL_BC_OR(goal_for_subs, theta,kB2);
		
		//perform and for all of theta_dash
		if(theta_dash!=null)
		{
			
			for (HashMap<String, String> hashMap : theta_dash) {
				
				List<HashMap<String, String>> and_list= FOL_BC_AND(rest,hashMap,kB2);
				
				if(and_list!=null)
				{
					for (HashMap<String, String> h : and_list) 
					{								
						HashMap<String , String> new_map =new HashMap<String,String>();			
						for(String s:h.keySet())
							new_map.put(s,h.get(s));
						
						return_and_list.add(new_map);
					}
				}							
			}
		}	
		
		if(return_and_list.size()==0)
			return null;
		
		return return_and_list;		
	}

	private static boolean hasOnlyConstants(Predicate goal_for_subs) {

		List<String> list=goal_for_subs.getArgs();
		for (String string : list) {
			if(checkNewVariable(string))
				return false;				
		}
		return true;
	}
	private static boolean isSame(Predicate p, Predicate q) {


		List<String> p_arg_list=p.getArgs();
		List<String> q_arg_list=q.getArgs();
		int cnt=0;
		for(int i=0;i<p_arg_list.size();i++)
		{
			if( checkNewVariable(p_arg_list.get(i))  && checkNewVariable(q_arg_list.get(i)) )
			{
				if(p_arg_list.get(i).charAt(0) == q_arg_list.get(i).charAt(0))				
					cnt++;
				else 
					return false;
			}
			else
			{
				if( p_arg_list.get(i).equals(q_arg_list.get(i)))	//constants not matching
					cnt++;
				else
					return false;
			}										
		}
		if(cnt==p_arg_list.size())
			return true;
		return false;
	}


	private static void add(Predicate newPredicate) {
		
		//make copy of newPredicate
		Predicate p=new Predicate();
		String name=newPredicate.getName().trim();
		List<String> temp=new ArrayList<String>();
		List<String> arg_list=newPredicate.getArgs();
		for(int i=0;i<arg_list.size();i++)
		{
			String x=arg_list.get(i);
			temp.add(x);
		}
		p.setName(name);
		p.setArgs(temp);
		
		List<Predicate> list=new ArrayList<Predicate>();			
		list.add(p);
		loop_check_list.put(p.getName(),list);	
		System.out.println("Adding to loop " + p.toString());		
	}

	private static boolean isPresentInLoopList(Predicate goal_for_subs) {

		List<String> goal_arg_list=goal_for_subs.getArgs();
		if(loop_check_list.containsKey(goal_for_subs.getName()))
		{
			int flag=0;
			List<Predicate> list=loop_check_list.get(goal_for_subs.getName().trim());
			
			for(int i=0;i<list.size();i++)
			{
				if(isSame(list.get(i), goal_for_subs))
				{
					System.out.println("Loop detected ");
					return true;
				}
			}
		}
		return false;
	}
	private static Predicate substitute(Predicate first_original,HashMap<String , String> theta) {
		
		List<String> args_list=first_original.getArgs();
		List<String> return_args_list=new ArrayList<String>();

		for(int x=0;x<args_list.size();x++)
		{
			return_args_list.add(args_list.get(x));
		}
		String x=first_original.getName().trim();
		Predicate return_pred=new Predicate();
		return_pred.setName(x);
		return_pred.setArgs(return_args_list);
		
		int i;
		for (i=0;i<args_list.size();i++) {
			if(checkNewVariable(args_list.get(i)))
			{
				if(theta.containsKey(args_list.get(i)))
				{
					String value=theta.get(args_list.get(i));
					return_args_list.set(i, value);					
				}					
			}
		}
		return_pred.setArgs(return_args_list);
		return return_pred;
		
	}

	
	private static HashMap<String, String> unify(String rhs_string,String goal_string, HashMap<String,String> theta) {
		
		String[] rhs_tokens;
		if(rhs_string.contains(","))
			rhs_tokens=rhs_string.split(",");
		else
			rhs_tokens=rhs_string.split("");
		
		
		String[] goal_tokens;
		if(goal_string.contains(","))
			goal_tokens=goal_string.split(",");
		else
			goal_tokens=goal_string.split("");
		
		if(theta==null)
			return null;		
		
		if(rhs_string.equals(goal_string))
			return theta;
				
		else if ( checkNewVariable(rhs_string))
			return Unify_Var(rhs_string,goal_string,theta);
		
		else if ( checkNewVariable(goal_string))
			return Unify_Var(goal_string,rhs_string,theta);
		
		else if(rhs_tokens.length>1  && goal_tokens.length>1)
		{
			String x_rest="";
			for(int i=1;i<rhs_tokens.length;i++)
				x_rest+=rhs_tokens[i]+ ",";
			
			String y_rest="";
			for(int i=1;i<goal_tokens.length;i++)
				y_rest+=goal_tokens[i]+ ",";
			
			return unify(x_rest,y_rest, unify(rhs_tokens[0],goal_tokens[0],theta));
		}
						
		return null;
	}
	
	
	private static HashMap<String, String> Unify_Var(String var, String x,HashMap<String, String> theta) {

		String var_copy="";
		var_copy=" " + var + " ";	
		var_copy = var.trim().replace(",", "");
		
		String x_copy="";
		x_copy=" " + x+ " ";	
		x_copy =x.trim().replace(",", "");
		
		if(theta.containsKey(var_copy))
			return unify(theta.get(var_copy),x, theta);
		else if(theta.containsKey(x_copy))
			return unify(var,theta.get(x_copy), theta);
		else
		{
			theta.put(var_copy, x_copy);
			return theta;
		}
	}


	private static boolean check_arg_lists(String rhs_string, String goal_string)
	{
		String[] rhs_tokens=rhs_string.split(",");
		String[] goal_tokens=goal_string.split(",");
		
		for(int i=0;i<rhs_tokens.length;i++)
		{
			if ( !checkNewVariable(rhs_tokens[i]) && !checkNewVariable(goal_tokens[i]) )
			{
				if( !rhs_tokens[i].equals(goal_tokens[i]))
					return false;
			}				
			else if( checkNewVariable(rhs_tokens[i]) && checkNewVariable(goal_tokens[i]) )
			{
				if(rhs_tokens[i].charAt(0)!=goal_tokens[i].charAt(0))
					return false;
			}
			else if( checkNewVariable(rhs_tokens[i]) && !checkNewVariable(goal_tokens[i]) )
				return false;
			else if ( !checkNewVariable(rhs_tokens[i]) && checkNewVariable(goal_tokens[i]) )
				return false;
		}
					
		return true;
	}

	
	private static boolean Check_facts(Predicate goal,HashMap<String,List<Sentence>> kB2 ) {

		int i=0;
		if( kB2.containsKey( goal.getName().trim()))
		{	int flag=0;	
			List<Sentence> fact_sentences=kB2.get(goal.getName());
			for (Sentence sentence : fact_sentences) {
				{
					flag=0;
					Predicate predicate=sentence.getRhs();
					List<String> arg_list=predicate.getArgs();
					List<String> goal_list=goal.getArgs();
					for(i=0;i<arg_list.size();i++)
					{
						if (! (arg_list.get(i).trim().equals(goal_list.get(i).trim())) )
						{
							flag=1;
							break;
						}
					}
					if(flag==0)
						return true;
				}
			}			
			if(flag==0)
				return true;
		}
		
	return false;
	}

	private static List<Sentence> getValidSentences( Predicate goal,HashMap<String,List<Sentence>> kB2 ) {		
		
		if(kB2.containsKey(goal.getName().trim()))
			return kB2.get(goal.getName().trim());
		else 
			return null;
			
	}
		
	private static boolean checkVariable(String str) {
		
	    String regex = "[a-z]";
	    boolean result;

	    Pattern pattern1 = Pattern.compile(regex);
	    Matcher matcher1 = pattern1.matcher(str);
	    result = matcher1.matches();
	    return result;	
	}

	static boolean checkNewVariable(String str) {
		
		String str_copy="";

		str_copy=" " + str + " ";
		str_copy=str_copy.trim();
		if(str_copy.contains(","))
			str_copy=str_copy.replace(",", "");
		
	    String regex = "[a-z][0-9]+";
	    boolean result;

	    Pattern pattern1 = Pattern.compile(regex);
	    Matcher matcher1 = pattern1.matcher(str_copy);
	    result = matcher1.matches();
	    return result;	
	}

}//end of class
