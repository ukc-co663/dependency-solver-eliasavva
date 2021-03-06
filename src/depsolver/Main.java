package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.print.DocFlavor.STRING;

import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
class Package {
  private String name;
  private String version;
  private Integer size;
  private List<List<String>> depends = new ArrayList<>();
  private List<String> conflicts = new ArrayList<>();

  public String getName() { return name; }
  public String getVersion() { return version; }
  public Integer getSize() { return size; }
  public List<List<String>> getDepends() { return depends; }
  public List<String> getConflicts() { return conflicts; }
  public void setName(String name) { this.name = name; }
  public void setVersion(String version) { this.version = version; }
  public void setSize(Integer size) { this.size = size; }
  public void setDepends(List<List<String>> depends) { this.depends = depends; }
  public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }
}

public class Main {
	public ArrayList<String> state = new ArrayList<String>();
  public static void main(String[] args) throws IOException {
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {};
    List<Package> repo = JSON.parseObject(readFile(args[0]), repoType);
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {};
    List<String> initial = JSON.parseObject(readFile(args[1]), strListType);
    List<String> constraints = JSON.parseObject(readFile(args[2]), strListType);

    // CHANGE CODE BELOW:
    // using repo, initial and constraints, compute a solution and print the answer

    //needs to deal with temporarily installing dependecies and unistalling later on

    
   Main main = new Main(repo, initial, constraints, args);
    
  }   

  
  public List<Package> sortRepo (List<Package> repo) {
	 int[] sizes = new int[repo.size()];
	  for (int i = 0;i< repo.size();i++) {
		  sizes[i] = repo.get(i).getSize();
		 
	  }
	  
	  Arrays.sort(sizes);
	  
	  ArrayList<Package> newRepo = new ArrayList<Package>();
	  for (int j = 0;j< sizes.length;j++) {
		  int l = 0;
		  boolean end = false;
		  while (!end && l < repo.size()) {
			  if (sizes[j] == repo.get(l).getSize()) {
				  newRepo.add(repo.get(l));
				  repo.remove(repo.get(l));
				  end = true;
			  }
			  l++;
		  }
	  }
	  return newRepo;
  }
  
  public Main(List<Package> repo, List<String> initial,List<String> constraints, String[] args)  throws IOException  {
	  repo = sortRepo(repo);
	  ArrayList<String> commands = new ArrayList<String>();
	    ArrayList<String> posError= new ArrayList<String>();
	    ArrayList<String> blacklist= new ArrayList<String>();
	    ArrayList<String> conflicts= new ArrayList<String>();
	    int j = 0;
	    initial.remove("");
	    List<String> originalInitial = initial;
	    state.addAll(initial);
	    while ( j < constraints.size()) {
	    	String p = constraints.get(j);
	    	if (p.substring(0,1).equals("+")) {
	    		
	    		if (install(p.substring(1), commands, posError, repo, initial, blacklist, conflicts, false, new ArrayList<String>()) == false || checkCommands(commands, conflicts) == true) {
	    			
	    			blacklist.add(posError.get(posError.size()-1));
	    			posError.clear();;
	    			state.clear();
	    			commands.clear(); 
	    			conflicts.clear();
	    			initial = originalInitial;
	    			j = 0;
	    		} else {	
	    			j++;
	    		}
	    	} else {
	    		commands.add(p);
	    		j++;
	    	}
	    }
	    
	    System.out.println(JSON.toJSONString(commands));
	   
	    
  }
  
  
  public boolean install(String p, ArrayList<String> commands, ArrayList<String> posError, List<Package> repo, List<String> initial, ArrayList<String> blacklist, ArrayList<String> conflicts, boolean wasOption, ArrayList<String> pending) {

	
		//splits for comparing purposes
	  String v = "";
	  String type = "";
	  if (p.contains(">=")) {
		   v = p.substring(p.indexOf(">=")+2);
		   type = ">=";
		   p = p.substring(0, p.indexOf(">=") );
	  } else if (p.contains("<=")) {
		   v = p.substring(p.indexOf("<=")+2);
		   type = "<=";
		   p = p.substring(0, p.indexOf("<=") );
	  } else if (p.contains("=")) {
		   v = p.substring(p.indexOf("=")+1);
		   type = "=";
		   p = p.substring(0, p.indexOf("=") );
	  } else if (p.contains("<")) {
		   v = p.substring(p.indexOf("<")+1);
		   type = "<";
		   p = p.substring(0, p.indexOf("<") );
	  } else if (p.contains(">")) {
		   v = p.substring(p.indexOf(">")+1);
		   type = ">";
		   p = p.substring(0, p.indexOf(">") );
	  } else {
		type = "any";
	  }
	  	boolean done = false;
		int i = 0;
		int end = 0;
		
		//looks through repo to find packages that match version and name
		while (i < repo.size() && end < 3) {
			Package thePackage = repo.get(i);
			if (thePackage.getName().equals(p)){
				//checks if conflicts or was already tried and failed
				if (doesConflict(p, thePackage.getVersion(), blacklist, conflicts) ) {
				
						end = -1;
					
				} else {
					//checks what operator si used 
					if (type.equals("=" )){
						if (compare(thePackage.getVersion(), type, v)) {
						if (end !=1) {
							if (wasOption) {
								posError.add(p + "=" + thePackage.getVersion());
							  }
							pending.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
							List<List<String>> dependencies = thePackage.getDepends();
							//System.out.println(dependencies.toString());
							//sorts dependencies form shortest to longest to reduce errors
							Collections.sort(dependencies, new Comparator<List>(){
							    public int compare(List a1, List a2) {
							        return a2.size() - a1.size(); // assumes you want biggest to smallest
							    }
							});
							Collections.reverse(dependencies);
							//System.out.println(dependencies.toString());
							conflicts.addAll(thePackage.getConflicts());
							conflicts.remove("");
							//checks if the conflicts conflict with the initial
							ArrayList<String> toRemove = checkInitial(initial, conflicts);
							
							if (toRemove.size() == 1) {
								commands.add(toRemove.get(0));
								updateState(commands);
								initial.remove(toRemove.get(0).substring(1));
								
							} else {
								for (String remove : toRemove) {
									commands.add(remove);
									updateState(commands);
									initial.remove(remove.substring(1));
								}
							}
							//attempt at removing things that conflict, such as in seen-6!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
							 ArrayList<String> maybeUninstall = checkInitial(state, conflicts);
								
								for (String remove : maybeUninstall) {
									if (canUninstall(remove, repo)) {
									commands.add("-" + remove.substring(1));
									initial.remove(remove.substring(1));
									}
								}
						
							boolean noErrors = true;
							int j = 0;
							boolean toContinue = true;
					//goes through dependencies
							while (j < dependencies.size() && toContinue == true) {
								List<String> s = dependencies.get(j);
								
								boolean theBool = false;
									if (s.size() > 1) {
										theBool = true;
									}
									int looper = 0;
									boolean ifContinue = true;
									while (looper < s.size() && ifContinue == true) {
										if (checkCommands(pending, s) == true) {
											noErrors = false;
										}
										if ( checkCommands(state, s) == false) {
											if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts, theBool, pending) == true) {														
												end = 1;
												noErrors = true;
												ifContinue = false;
											}else {
												noErrors = false;
											}
										}else {
											ifContinue = false;
										}
										looper++;
									}
								j++;
							}
							pending.remove("+" + thePackage.getName() + "=" + thePackage.getVersion());
							if (noErrors == true) {
								commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
								updateState(commands);
								end = 3;
								return true;
							} else {
								
								end = 2;
								
								conflicts.removeAll(thePackage.getConflicts());
								return false;
									
							}
						} 
						}else {
							end =2;
						}
					} else if (type.equals("any")) {
						//similar to above
						if (end !=1) {
							if (wasOption) {
								posError.add(p + "=" + thePackage.getVersion());
							  }
							pending.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
							List<List<String>> dependencies = thePackage.getDepends();
							//System.out.println(dependencies.toString());
							Collections.sort(dependencies, new Comparator<List>(){
							    public int compare(List a1, List a2) {
							        return a2.size() - a1.size(); // assumes you want biggest to smallest
							    }
							});
							Collections.reverse(dependencies);
							//System.out.println(dependencies.toString());
							dependencies.remove("");
							conflicts.addAll(thePackage.getConflicts());
							conflicts.remove("");
							ArrayList<String> toRemove = checkInitial(initial, conflicts);
							if (toRemove.size() == 1) {
								commands.add(toRemove.get(0));
								updateState(commands);
								initial.remove(toRemove.get(0).substring(1));
								
							} else {
								for (String remove : toRemove) {
									commands.add(remove);
									updateState(commands);
									initial.remove(remove.substring(1));
								}
							}
							
							 ArrayList<String> maybeUninstall = checkInitial(state, conflicts);
								
								for (String remove : maybeUninstall) {
									if (canUninstall(remove, repo)) {
									commands.add("-" + remove.substring(1));
									initial.remove(remove.substring(1));
									}
								}
							boolean noErrors = true;
							int j = 0;
							boolean toContinue = true;
							while (j < dependencies.size() && toContinue == true) {
								List<String> s = dependencies.get(j);
								boolean theBool = false;
									if (s.size() > 1) {
										theBool = true;
									}
									int looper = 0;
									boolean ifContinue = true;
									while (looper < s.size() && ifContinue == true) {
										if (checkCommands(pending, s) == true) {
											return false;
										}
										if ( checkCommands(state, s) == false) {
											if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts, theBool, pending) == true) {
												end = 1;
												noErrors = true;
												ifContinue = false;
											}else {
												noErrors = false;
											}
										} else {
											ifContinue = false;
										}
										looper++;
									}
								j++;
							}
							pending.remove("+" + thePackage.getName() + "=" + thePackage.getVersion());
							if (noErrors == true) {
								commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
								updateState(commands);
								end = 3;
								return true;
							} else {
								
								end = 2;
								conflicts.removeAll(thePackage.getConflicts());
								return false;
							}
						} 
					
						} else {
							if (compare(thePackage.getVersion(), type, v)) {
								if (end !=1) {
									if (wasOption) {
										posError.add(p + "=" + thePackage.getVersion());
									}
									pending.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
									List<List<String>> dependencies = thePackage.getDepends();
									//System.out.println(dependencies.toString());
									Collections.sort(dependencies, new Comparator<List>(){
									    public int compare(List a1, List a2) {
									        return a2.size() - a1.size(); // assumes you want biggest to smallest
									    }
									});
									Collections.reverse(dependencies);
									//System.out.println(dependencies.toString());
									conflicts.addAll(thePackage.getConflicts());
									conflicts.remove("");
									ArrayList<String> toRemove = checkInitial(initial, conflicts);
									if (toRemove.size() == 1) {
										commands.add(toRemove.get(0));
										updateState(commands);
										initial.remove(toRemove.get(0).substring(1));
										
									} else {
										for (String remove : toRemove) {
											commands.add(remove);
											updateState(commands);
											initial.remove(remove.substring(1));
										}
									}
									 ArrayList<String> maybeUninstall = checkInitial(state, conflicts);
										
										for (String remove : maybeUninstall) {
											if (canUninstall(remove, repo)) {
											commands.add("-" + remove.substring(1));
											initial.remove(remove.substring(1));
											}
										}
									boolean noErrors = true;
									int j = 0;
									boolean toContinue = true;
									while (j < dependencies.size() && toContinue == true) {
										List<String> s = dependencies.get(j);
										boolean theBool = false;
											if (s.size() > 1) {
												theBool = true;
											}
											int looper = 0;
											boolean ifContinue = true;
											while (looper < s.size() && ifContinue == true) {
												if (checkCommands(pending, s) == true) {
													return false;
												}
												if ( checkCommands(state, s) == false) {
													if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts, theBool, pending) == true) {
														end = 1;
														noErrors = true;
														ifContinue = false;
													}else {
														noErrors = false;
													}
												}else {
													ifContinue = false;
												}
												looper++;
											}
										j++;
									}
									pending.remove("+" + thePackage.getName() + "=" + thePackage.getVersion());
									if (noErrors == true) {
										commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
										updateState(commands);
										end = 3;
										return true;
									} else {
										 
										end = 2;
										conflicts.removeAll(thePackage.getConflicts());
										return false;
									}
								} 
							}else {
								end = 2;
							}
						}
					
				}
			} else if ( end == 1 ) {
				end = 3;
			}
			i++;
		}
		
		if (end == 3 || end ==1) {
			return true;
		} else {
			
			blacklist = new ArrayList<String>();
			return false;
		}
  }
  
  public boolean doesConflict(String p, String v, ArrayList<String> blacklist, List<String> conflicts) {
	//System.out.println(blacklist);
	  //checks if there is a conflict
	 for (String s : blacklist) {
		 
		  if (s.equals(p + "=" + v)) {
			
			  return true;
		  }
	 }
	 for (String s : conflicts) {
		  String v2 = "";
		  String type2 = "";
		  if (s.contains(">=")) {
			   v2 = s.substring(s.indexOf(">=")+2);
			   type2 = ">=";
			   s = s.substring(0, s.indexOf(">=") );
		  } else if (s.contains("<=")) {
			   v2 = s.substring(s.indexOf("<=")+2);
			   type2 = "<=";
			   s = s.substring(0, s.indexOf("<=") );
		  } else if (s.contains("<")) {
			   v2 = s.substring(s.indexOf("<")+1);
			   type2 = "<";
			   s = s.substring(0, s.indexOf("<") );
		  } else if (s.contains(">")) {
			   v2 = s.substring(s.indexOf(">")+1);
			   type2 = ">";
			   s = s.substring(0, s.indexOf(">") );
		  } else if (s.contains("=")) {
			   v2 = s.substring(s.indexOf("=")+1);
			   type2 = "=";
			   s = s.substring(0, s.indexOf("=") );
		  } else {
			type2 = "any";
		  }
		  if (p.equals(s)) {
			  return compare(v, type2, v2);
		  }
	 }
	 
	 
	return false;	 
  }
  
  public boolean compare(String v1,String type, String v2) {
	//compares two versions using the operator specified
		  if (type.equals("=")) {
			  if (v1.equals(v2) ) {
				  return true;
			  }
	  	  } else if (type.equals("<=")) {
	  		  if (v1.equals(v2) ) {
				  return true;
			  }
	  		
	  			 for (int i =0; i< v1.length();i++) {
	  				 if (v1.compareTo(v2) < 0) {
	  					 return true;
	  				 }
	  		  }
	  	  } else if (type.equals("<")) {
	  		 
	  			 for (int i =0; i< v1.length();i++) {
	  				 if (v1.compareTo(v2) < 0) {
	  					 return true;
	  				 }
	  		  }
	  	  } else if (type.equals(">=")) {
	  		 if (v1.equals(v2) ) {
				  return true;
			  }
	  		
	  			 for (int i =0; i< v1.length();i++) {
	  				 if (v1.compareTo(v2) > 0) {
	  					 
	  					 return true;
	  				 }
	  		  }
		  }  else if (type.equals(">")) {
	  		
	  			 for (int i =0; i< v1.length();i++) {
	  				 if (v1.compareTo(v2) > 0) {
	  					 return true;
	  				 }
	  		  }
		  } else {
			  return true;
		  }
		
	  return false;
  }
  
  public boolean checkCommands (List<String> commands, List<String> conflicts) {
	 //System.out.println(commands);
	 //checks if the commands conflict
	  if (commands.isEmpty() || conflicts.isEmpty()) {
		  return false;
	  }
	  for (String command : commands) {
		  boolean negative = false;
		  String v = "";
		  String type = "";
		  if (command.contains(">=")) {
			   v = command.substring(command.indexOf(">=")+2);
			   type = ">=";
			   command = command.substring(1, command.indexOf(">=") );
		  } else if (command.contains("<=")) {
			   v = command.substring(command.indexOf("<=")+2);
			   type = "<=";
			   command = command.substring(1, command.indexOf("<=") );
		  } else if (command.contains("<")) {
			   v = command.substring(command.indexOf("<")+1);
			   type = "<";
			   command = command.substring(1, command.indexOf("<") );
		  } else if (command.contains(">")) {
			   v = command.substring(command.indexOf(">")+1);
			   type = ">";
			   command = command.substring(1, command.indexOf("<=") );
		  } else if (command.contains("=")) {
			   v = command.substring(command.indexOf("=")+1);
			   type = "=";
			   if (command.substring(0,1).equals("+")){
				   command = command.substring(1, command.indexOf("=") );
			   } else if (command.substring(0,1).equals("-")){
				   negative = true;
				   command = command.substring(1, command.indexOf("=") );
			   }else {
				   command = command.substring(0, command.indexOf("=") );
			   }
			   
		  
		  }
		  //System.out.println(conflicts);
		 
		  for(String conflict : conflicts) {
			  String v2 = "";
			  String type2 = "";
			  if (conflict.contains(">=")) {
				   v2 = conflict.substring(conflict.indexOf(">=")+2);
				   type2 = ">=";
				   conflict = conflict.substring(0, conflict.indexOf(">=") );
			  } else if (conflict.contains("<=")) {
				   v2 = conflict.substring(conflict.indexOf("<=")+2);
				   type2 = "<=";
				   conflict = conflict.substring(0, conflict.indexOf("<=") );
			  } else if (conflict.contains("<")) {
				   v2 = conflict.substring(conflict.indexOf("<")+1);
				   type2 = "<";
				   conflict = conflict.substring(0, conflict.indexOf("<") );
			  } else if (conflict.contains(">")) {
				   v2 = conflict.substring(conflict.indexOf(">")+1);
				   type2 = ">";
				   conflict = conflict.substring(0, conflict.indexOf(">") );
			  } else if (conflict.contains("=")) {
				   v2 = conflict.substring(conflict.indexOf("=")+1);
				   type2 = "=";
				   conflict = conflict.substring(0, conflict.indexOf("=") );
			  } else {
				  
				type2 = "any";
				
			  }
			 
			  if (command.equals(conflict)) {
				  if (compare(v, type2, v2) == true) {
					  //System.out.println(v + type2 + v2);
					  if (negative == true) {
						  return false;
					  } else {
						 return true; 
					  }
					  
				  }
			  }
			  
		  }
	  }
	  return false;
  }
  
  ArrayList<String> checkInitial(List<String> initial, List<String> conflicts) {
	  //System.out.println(commands);
	  
	  //checks if the initial conflicts
		ArrayList<String> toRemove = new ArrayList<String>();
	  for (String ins : initial) {
		  String v = "";
		  String type = "";
		 
		  v = ins.substring(ins.indexOf("=")+1);
		  type = "=";
		  ins = ins.substring(0, ins.indexOf("=") );		   
		 
		 
		  for(String conflict : conflicts) {
			  String v2 = "";
			  String type2 = "";
			  if (conflict.contains(">=")) {
				   v2 = conflict.substring(conflict.indexOf(">=")+2);
				   type2 = ">=";
				   conflict = conflict.substring(0, conflict.indexOf(">=") );
			  } else if (conflict.contains("<=")) {
				   v2 = conflict.substring(conflict.indexOf("<=")+2);
				   type2 = "<=";
				   conflict = conflict.substring(0, conflict.indexOf("<=") );
			  } else if (conflict.contains("<")) {
				   v2 = conflict.substring(conflict.indexOf("<")+1);
				   type2 = "<";
				   conflict = conflict.substring(0, conflict.indexOf("<") );
			  } else if (conflict.contains(">")) {
				   v2 = conflict.substring(conflict.indexOf(">")+1);
				   type2 = ">";
				   conflict = conflict.substring(0, conflict.indexOf(">") );
			  } else if (conflict.contains("=")) {
				   v2 = conflict.substring(conflict.indexOf("=")+1);
				   type2 = "=";
				   conflict = conflict.substring(0, conflict.indexOf("=") );
			  } else {
				  
				type2 = "any";
				
			  }
			 
			  if (ins.equals(conflict)) {
				  if (compare(v, type2, v2) == true) {
					toRemove.add("-" + ins + "=" + v);	
					//System.out.println(toRemove);
				  }	
			  }
		  }
	  }
	  return toRemove;
  }
  
  public void updateState(ArrayList<String> commands) {
	  //System.out.println(state);
	  
	  //updates the current state
	  for (String s : commands) {
		  if (s.substring(0,1).equals("-")) {
			  state.remove(s.substring(1));
		  } else {
			  state.add(s.substring(1));
		  }
	  }
  }
  
  public boolean canUninstall(String p, List<Package> repo) {
	  //p = p.substring(1);
//suppose to check is when somethig is removed if the state is still valid i.e seen-6!!!!!!!!!!!!!!!!!!!!!!!!!!
	  	int i = 0;
		int end = 0;
		ArrayList<String> tempState = state;
		tempState.remove(p);
		for (String s : tempState) {
			while (i < repo.size() && end < 3) {
				Package thePackage = repo.get(i);
				if (thePackage.getName().equals(s.substring(0, s.indexOf('='))) && thePackage.getVersion().equals(s.substring(s.indexOf('=') + 1))) {
					List<List<String>> dependencies = thePackage.getDepends();
					
					boolean noErrors = false;
					int j = 0;
					while (j < dependencies.size()) {
						List<String> d = dependencies.get(j);
						
							int looper = 0;
							
							while (looper < d.size() ) {
								
								if (checkCommands(tempState, d) == true) {
									noErrors = true;
								}
								
								looper++;
							}
							if (noErrors == false) {
								//System.out.println(p);
								return false;
							}
						j++;
					}
					
				}
				i++;
			}
		}
		return true;
  }
  
  static String readFile(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    StringBuilder sb = new StringBuilder();
    br.lines().forEach(line -> sb.append(line));
    return sb.toString();
  }
}
