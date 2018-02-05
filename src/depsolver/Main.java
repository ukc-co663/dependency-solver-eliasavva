package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
  public static void main(String[] args) throws IOException {
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {};
    List<Package> repo = JSON.parseObject(readFile(args[0]), repoType);
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {};
    List<String> initial = JSON.parseObject(readFile(args[1]), strListType);
    List<String> constraints = JSON.parseObject(readFile(args[2]), strListType);

    // CHANGE CODE BELOW:
    // using repo, initial and constraints, compute a solution and print the answer
//    for (Package p : repo) {
//      System.out.printf("package %s version %s\n", p.getName(), p.getVersion());
//      for (List<String> clause : p.getDepends()) {
//        System.out.printf("  dep:");
//        for (String q : clause) {
//          System.out.printf(" %s", q);
//        }
//        System.out.printf("\n");
//      }
//    }

    
    ArrayList<String> commands = new ArrayList<String>();
    ArrayList<String> posError= new ArrayList<String>();
    ArrayList<String> blacklist= new ArrayList<String>();
    ArrayList<String> conflicts= new ArrayList<String>();
    int j = 0;
    while ( j < constraints.size()) {
    	
    	String p = constraints.get(j);
    	if (p.substring(0,1).equals("+")) {
    		
    	
    	boolean result = install(p.substring(1), commands, posError, repo, initial, blacklist,conflicts);
    	if (result == false) {
    		blacklist.add(posError.get(posError.size()-1));
    		posError.remove(posError.size()-1);

    		posError = new ArrayList<String>();
			commands = new ArrayList<String>();
			conflicts = new ArrayList<String>();
    		j = 0;
    	} else {	
    		j++;
    	}
    }
    	
    }
    	System.out.println(commands.toString());
//    	for (String s : conflicts) {
//    		System.out.println(s.toString());
//    	}
 
    
    
    
  }
  
  
  public static boolean install(String p, ArrayList<String> commands, ArrayList<String> posError, List<Package> repo, List<String> initial, ArrayList<String> blacklist, ArrayList<String> conflicts) {
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
		   type = "=";
		   p = p.substring(0, p.indexOf("<") );
	  } else if (p.contains(">")) {
		   v = p.substring(p.indexOf(">")+1);
		   type = "=";
		   p = p.substring(0, p.indexOf(">") );
	  } else {
		type = "any";
	  }
		 
	  //System.out.println(p);
	  
		int i = 0;
		int end = 0;
		while (i < repo.size() && end < 3) {
			Package thePackage = repo.get(i);
			if (thePackage.getName().equals(p)){
				if (doesConflict(p,type, v, blacklist, conflicts) /* or conflicts*/) {
					end = 4;
				} else {
					if (type == "=" ){
						//System.out.println(thePackage.getVersion() +v);
						if (Float.parseFloat(thePackage.getVersion()) == Float.parseFloat(v)) {
					
							List<List<String>> dependencies = thePackage.getDepends();
							conflicts.addAll(thePackage.getConflicts());
							boolean noErrors = true;
							for (List<String> s : dependencies) {
								if (s.size() > 1) {
									
									
									int looper = 0;
									boolean ifContinue = true;
									while (looper < s.size() && ifContinue == true) {
										if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts) == true) {
											//System.out.println(s.get(looper));
											posError.add(s.get(looper));
											end = 1;
											ifContinue = false;
										}else {
											end = 2;
										}
										looper++;
									}
								} else {
									if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
										
										noErrors = true;									
									}
								}
							}
								if (noErrors == true) {

									//System.out.println("passed" + p);
									commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
									return true;
								} else {

									//System.out.println("failed" + p);
									return false;
								}
							
						}
					
					} else if(type == ">=") {
						if (Float.parseFloat(thePackage.getVersion()) >= Float.parseFloat(v) )  {
							
								if (end == 0) {
									List<List<String>> dependencies = thePackage.getDepends();
									conflicts.addAll(thePackage.getConflicts());
									boolean noErrors = true;
									for (List<String> s : dependencies) {
										if (s.size() > 1) {
											
											
											int looper = 0;
											boolean ifContinue = true;
											while (looper < s.size() && ifContinue == true) {
												if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts) == true) {
													//System.out.println(s.get(looper));
													posError.add(s.get(looper));
													end = 1;
													ifContinue = false;
												}else {
													end = 2;
												}
												looper++;
											}
										} else {
											if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
												noErrors = true;									
											}
										}
									}
										if (noErrors == true) {
											commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
											end = 1;
										} else {
											end = 2;
										}
										
								} else if (end == 1 ){
									posError.add("+" + repo.get(i).getName() + "=" + repo.get(i).getVersion());
									end = 2;
								}
							}else {
								end =2;
							}
					} else if(type == ">") {
						if (Float.parseFloat(thePackage.getVersion()) > Float.parseFloat(v) )  {
							
								if (end == 0) {
									List<List<String>> dependencies = thePackage.getDepends();
									conflicts.addAll(thePackage.getConflicts());
									boolean noErrors = true;
									for (List<String> s : dependencies) {
										if (s.size() > 1) {
											
											
											int looper = 0;
											boolean ifContinue = true;
											while (looper < s.size() && ifContinue == true) {
												if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts) == true) {
												//	System.out.println(s.get(looper));
													posError.add(s.get(looper));
													end = 1;
													ifContinue = false;
												}else {
													end = 2;
												}
												looper++;
											}
										} else {
											if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
												noErrors = true;									
											}
										}
									}
										if (noErrors == true) {
											commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
											end = 1;
										} else {
											end = 2;
										}
										
								} else if (end == 1 ){
									posError.add("+" + repo.get(i).getName() + "=" + repo.get(i).getVersion());
									end = 2;
								}
							}else {
								end =2;
						}
					} else if (type == "<=") {
						if (  Float.parseFloat(thePackage.getVersion()) <= Float.parseFloat(v) ) {
							
								if (end == 0) {
									List<List<String>> dependencies = thePackage.getDepends();
									conflicts.addAll(thePackage.getConflicts());
									boolean noErrors = true;
									for (List<String> s : dependencies) {
										if (s.size() > 1) {
											
											
											int looper = 0;
											boolean ifContinue = true;
											while (looper < s.size() && ifContinue == true) {
												if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts) == true) {
													//System.out.println(s.get(looper));
													posError.add(s.get(looper));
													end = 1;
													ifContinue = false;
												}else {
													end = 2;
												}
												looper++;
											}
										} else {
											if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
												noErrors = true;									
											}
										}
									}
										if (noErrors == true) {
											commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
											end = 1;
										} else {
											end = 2;
										}
										
								}else if (end == 1 ){
									posError.add("+" + repo.get(i).getName() + "=" + repo.get(i).getVersion());
								}
							}else {
								end =2;
							}
					} else if (type == "<") {
						if (  Float.parseFloat(thePackage.getVersion()) < Float.parseFloat(v))  {
							
								if (end == 0) {
									List<List<String>> dependencies = thePackage.getDepends();
									conflicts.addAll(thePackage.getConflicts());
									boolean noErrors = true;
									for (List<String> s : dependencies) {
										if (s.size() > 1) {
											
											
											int looper = 0;
											boolean ifContinue = true;
											while (looper < s.size() && ifContinue == true) {
												if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts) == true) {
													//System.out.println(s.get(looper));
													posError.add(s.get(looper));
													end = 1;
													ifContinue = false;
												}else {
													end = 2;
												}
												looper++;
											}
										} else {
											if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
												noErrors = true;									
											}
										}
									}
										if (noErrors == true) {
											commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
											end = 1;
										} else {
											end = 2;
										}
										
								}else if (end == 1 ){
									posError.add("+" + repo.get(i).getName() + "=" + repo.get(i).getVersion());
								}
							}else {
								end =2;
						}
						
					} else if (type == "any") {
			
							if (end == 0) {
								List<List<String>> dependencies = thePackage.getDepends();
								conflicts.addAll(thePackage.getConflicts());
								boolean noErrors = true;
								for (List<String> s : dependencies) {
									//System.out.println(s.size());
									if (s.size() > 1) {
										
										
										int looper = 0;
										boolean ifContinue = true;
										while (looper < s.size() && ifContinue == true) {
											if (install(s.get(looper), commands, posError, repo, initial, blacklist, conflicts) == true) {
												//System.out.println("passed " + s.get(looper));
												posError.add(s.get(looper));
												end = 1;
												ifContinue = false;
												noErrors = true;
											}else {
												//System.out.println("failed " + s.get(looper));
												end = 2;
												noErrors = false;
											}
											looper++;
										}
									} else {
										if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
											noErrors = true;									
										}
									}
								}
									if (noErrors == true) {
										commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
										end = 1;
									} else {
										end = 2;
									}
									
							} else if (end == 1 ){
								posError.add("+" + repo.get(i-1).getName() + "=" + repo.get(i).getVersion());
							}
						
						} else {
							end =2;
						}
					 
						
				
				}
			} else if ( end == 1) {
					
				end = 3;
				
			} else if (end ==2) {
				return false;
			}
			i++;
			
		}
		
		if (end == 3) {
			return true;
		} else {
			blacklist = new ArrayList<String>();
			return false;
		}
	 
  }
  
  public static boolean doesConflict(String p, String type, String v, ArrayList<String> blacklist, ArrayList<String> conflicts) {
	 for (String s : blacklist) {
		 String v2 = "";
		  String type2 = "";
		  if (s.contains(">=")) {
			   v2 = s.substring(p.indexOf(">=")+2);
			   type2 = ">=";
			   s = s.substring(0, p.indexOf(">=") );
		  } else if (s.contains("<=")) {
			   v2 = s.substring(p.indexOf("<=")+2);
			   type2 = "<=";
			   s = s.substring(0, p.indexOf("<=") );
		  } else if (p.contains("=")) {
			   v2 = s.substring(p.indexOf("=")+1);
			   type2 = "=";
			   s = s.substring(0, p.indexOf("=") );
		  } else {
			type2 = "any";
		  }
		  if (p == s) {
			  if (type == "=") {
				  if (v== v2 && s == p) {
					  return true;
				  }
		  	  } else if (type == "<=") {
		  		  if (Float.parseFloat(v2) <= Float.parseFloat(v)) {
		  			  return true;
		  		  }
		  	  } else if (type == "<") {
		  		 if (Float.parseFloat(v2) < Float.parseFloat(v)) {
		  			  return true;
		  		  }
		  	  } else if (type == ">=") {
			  		 if (Float.parseFloat(v2) >= Float.parseFloat(v)) {
			  			  return true;
			  		  }
			  }  else if (type == ">") {
			  		 if (Float.parseFloat(v2) < Float.parseFloat(v)) {
			  			  return true;
			  		  }
			  }
		  }
	 }
	 for (String s : conflicts) {
		 String v2 = "";
		  String type2 = "";
		  if (s.contains(">=")) {
			   v2 = s.substring(p.indexOf(">=")+2);
			   type2 = ">=";
			   s = s.substring(0, p.indexOf(">=") );
		  } else if (s.contains("<=")) {
			   v2 = s.substring(p.indexOf("<=")+2);
			   type2 = "<=";
			   s = s.substring(0, p.indexOf("<=") );
		  } else if (p.contains("=")) {
			   v2 = s.substring(p.indexOf("=")+1);
			   type2 = "=";
			   s = s.substring(0, p.indexOf("=") );
		  } else {
			type2 = "any";
		  }
		  if (p == s) {
			  if (type == "=") {
				  if (v== v2 && s == p) {
					  return true;
				  }
		  	  } else if (type == "<=") {
		  		  if (Float.parseFloat(v2) <= Float.parseFloat(v)) {
		  			  return true;
		  		  }
		  	  } else if (type == "<") {
		  		 if (Float.parseFloat(v2) < Float.parseFloat(v)) {
		  			  return true;
		  		  }
		  	  } else if (type == ">=") {
			  		 if (Float.parseFloat(v2) >= Float.parseFloat(v)) {
			  			  return true;
			  		  }
			  }  else if (type == ">") {
			  		 if (Float.parseFloat(v2) < Float.parseFloat(v)) {
			  			  return true;
			  		  }
			  }
		  }
	 }
	 
	 
	return false;	 
  }
  
  static String readFile(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    StringBuilder sb = new StringBuilder();
    br.lines().forEach(line -> sb.append(line));
    return sb.toString();
  }
}
