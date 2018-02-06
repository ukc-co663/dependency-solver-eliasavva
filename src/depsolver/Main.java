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

    //doesnt iterate through different versions of one package

    
   Main main = new Main(repo, initial, constraints);
    
  }
  
  public Main(List<Package> repo, List<String> initial,List<String> constraints) {
	  ArrayList<String> commands = new ArrayList<String>();
	    ArrayList<String> posError= new ArrayList<String>();
	    ArrayList<String> blacklist= new ArrayList<String>();
	    ArrayList<String> conflicts= new ArrayList<String>();
	    int j = 0;
	    while ( j < constraints.size()) {
	    	
	    	String p = constraints.get(j);
	    	
	    	//need uninstall, checking the initial
	    	if (p.substring(0,1).equals("+")) {
	    		
	    	
	    	boolean result = install(p.substring(1), commands, posError, repo, initial, blacklist,conflicts);
	    	if (result == false) {
	    		blacklist.add(posError.get(posError.size()-1));
//System.out.println(posError.get(posError.size()-1));
	    		posError = new ArrayList<String>();
				commands = new ArrayList<String>();
				conflicts = new ArrayList<String>();
	    		j = 0;
	    	} else {	
	    		j++;
	    	}
	    } else {
	    	commands.add(p);
	    	j++;
	    }
	    	
	    }
	    	System.out.println(commands.toString());
	    
	    
  }
  
  
  public boolean install(String p, ArrayList<String> commands, ArrayList<String> posError, List<Package> repo, List<String> initial, ArrayList<String> blacklist, ArrayList<String> conflicts) {
	  //System.out.println(p);
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
		 
	  //System.out.println(p);
	  
		int i = 0;
		int end = 0;
		while (i < repo.size() && end < 3) {
			Package thePackage = repo.get(i);
			if (thePackage.getName().equals(p)){
				if (doesConflict(p,type, v, blacklist, conflicts) ) {
//					System.out.println(p +type+ v);
					//not sure about what value
					end = -1;
				} else {
					if (type == "=" ){
						//System.out.println(p + thePackage.getVersion());
						if (compare(thePackage.getVersion(), type, v)) {
							//System.out.println(thePackage.getVersion());
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
											posError.add( s.get(looper));
											end = 1;
											ifContinue = false;
										}else {
											noErrors = false;
										}
										looper++;
									}
								} else {
									if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
										
										noErrors = true;									
									} else {

										noErrors = false;
									}
								}
							}
								if (noErrors == true) {
									if (doesConflict(thePackage.getName(), "=",thePackage.getVersion(),blacklist, conflicts)) {
										return false;
									}
									//System.out.println("passed" + p);
									commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
									return true;
								} else {

									//System.out.println("failed" + p);
									return false;
								}
							
						}
					
					} else if (type == "any") {
			
							if (end !=1) {
								//System.out.println(p + thePackage.getVersion());
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
												posError.add( s.get(looper));
												end = 1;
												ifContinue = false;
												noErrors = true;
											}else {
												//System.out.println("failed " + s.get(looper));
												noErrors = false;
											}
											looper++;
										}
									} else {
										if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
											//System.out.println("passed " + s.get(0));
											noErrors = true;									
										} else {
											//System.out.println("failed " + s.get(0));
											noErrors = false;
										}
									}
								}
									if (noErrors == true) {
										if (doesConflict(thePackage.getName(), "=",thePackage.getVersion(),blacklist, conflicts)) {
											return false;
										}
										commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
										//System.out.println("passed " + s.get(0));
										end = 1;
									} else {
										//conflicts.removeAll(thePackage.getConflicts());
										end = 2;
									}
									
							} else {
								posError.add("+" + repo.get(i-1).getName() + "=" + repo.get(i-1).getVersion());
							}
						
						} else {
							if (compare(thePackage.getVersion(), type, v)) {
								//System.out.println(end);
								if (end != 1) {
									//System.out.println(p + thePackage.getVersion());
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
													posError.add( s.get(looper));
													end = 1;
													ifContinue = false;
												}else {
													noErrors = false;
												}
												looper++;
											}
										} else {
											if (install(s.get(0), commands, posError, repo, initial, blacklist, conflicts) == true) {
												noErrors = true;									
											} else {

												noErrors = false;
											}
										}
									}
										if (noErrors == true) {
											if (doesConflict(thePackage.getName(), "=",thePackage.getVersion(),blacklist, conflicts)) {
												return false;
											}
											commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
											end = 1;
										} else {
											end = 2;
										}
										
								}else{
									posError.add("+" + repo.get(i-1).getName() + "=" + repo.get(i-1).getVersion());
								}
							}else {
								end =4;
							}
						}
					 
						
				
				}
			} else if ( end == 1 ) {
					
				end = 3;
				
			
			} else if (end ==-1 || end == 2) {
				end = 4;
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
  
  public boolean doesConflict(String p, String type, String v, ArrayList<String> blacklist, ArrayList<String> conflicts) {
	 //System.out.println(conflicts);
	 for (String s : blacklist) {
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
		  } else if (p.contains("=")) {
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
		  } else if (p.contains("=")) {
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
	 
		  if (type.equals("=")) {
			  if (v1.equals(v2) ) {
				  return true;
			  }
	  	  } else if (type.equals("<=")) {
	  		  if (v1.equals(v2) ) {
				  return true;
			  }
	  		 String[] ints1 = v1.split(".");
	  		 String[] ints2 = v2.split(".");
	  			 for (int i =0; i< ints1.length;i++) {
	  				 if (Integer.parseInt(ints1[i]) < Integer.parseInt(ints2[i])) {
	  					 return true;
	  				 }
	  		  }
	  	  } else if (type.equals("<")) {
	  		 String[] ints1 = v1.split(".");
	  		 String[] ints2 = v2.split(".");
	  			 for (int i =0; i< ints1.length;i++) {
	  				 if (Integer.parseInt(ints1[i]) < Integer.parseInt(ints2[i])) {
	  					 return true;
	  				 }
	  		  }
	  	  } else if (type.equals(">=")) {
	  		 if (v1.equals(v2) ) {
				  return true;
			  }
	  		 String[] ints1 = v1.split(".");
	  		 String[] ints2 = v2.split(".");
	  			 for (int i =0; i< ints1.length;i++) {
	  				 if (Integer.parseInt(ints1[i]) > Integer.parseInt(ints2[i])) {
	  					 return true;
	  				 }
	  		  }
		  }  else if (type.equals(">")) {
	  		 String[] ints1 = v1.split(".");
	  		 String[] ints2 = v2.split(".");
	  			 for (int i =0; i< ints1.length;i++) {
	  				 if (Integer.parseInt(ints1[i]) > Integer.parseInt(ints2[i])) {
	  					 return true;
	  				 }
	  		  }
		  } else {
			  return true;
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
